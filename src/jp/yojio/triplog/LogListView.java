package jp.yojio.triplog;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.DBAccessObject;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.DataApi.*;
import jp.yojio.triplog.Common.DataApi.Account.*;
import jp.yojio.triplog.Common.Misc.*;
import jp.yojio.triplog.DBAccess.*;
import jp.yojio.triplog.Record.LocationDataStruc;
import jp.yojio.triplog.misc.TripLogMisc;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.ExpandableListView.*;

import com.google.android.accounts.Account;

public class LogListView extends Activity implements AuthActivity {

  private final static int MODE_DAY = 0;
  private final static int MODE_TAG = 1;
  private final static int MODE_SEARCH = 2;

  private DBAccessObject _dao;
  protected ObjectContainer _obj;
  private RecordBase _tran;
  private RecordBase _tagmst;
  private int _datatype_idx = MODE_DAY;
  private boolean _multiselectmode = false;
  private LinearLayout _datearea;
  private ImageButton _prevbtn;
  private ImageButton _nextbtn;
  private TextView _basedatetxt;
  private Date _basedate = new Date();
  private GregorianCalendar _gc = new GregorianCalendar();
  private ExpandableListView _list;
  private ArrayList<GroupViewParentData> _parentList = new ArrayList<GroupViewParentData>();
  private GroupViewAdapter _adapter;
  private TextView _datatype;
  private HashMap<String, Object> _tags = new HashMap<String, Object>();
  private ArrayList<String> _taglist = new ArrayList<String>();
  private AlertDialog _AlertDialog;
  private boolean _longclick = false;
  private ImageButton _modebtn;
  private boolean _isDebug;
  private LinearLayout _searcharea;
  private EditText _searchtxt;
  private ImageButton _searchbtn;
  protected ProgressDialog _dialog;
  private String _searchval = "";
  // 認証関連
  private final AccountChooser _accountChooser = new AccountChooser();
  private static Activity _instanse;
  private AuthManager _auth;
  private final HashMap<String, AuthManager> _authMap = new HashMap<String, AuthManager>();
  private GDataAccessThread _gdatathr;
  private ArrayList<HashMap<String, String>> _tablelist;
  private int _tableindex = -1;
  private String _tableuploadmsg = "";
  private boolean _initflg = false;

  private boolean _reset;
  private HashMap<String, Object> _exparent;
  private int _gpos;
  private int _cpos;
  private boolean _imageupload;
  private String _tableid;
  private String _tablename;

  private final Handler _handler2 = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      GetTableList();
    }
  };

  // UI更新ハンドラ ※別スレッドから通知を受けてUI部品を更新する
  private final Handler _handler3 = new Handler() {
    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case Const.GDATA_RETURN_GET_TABLE_LIST_NORMAL:
        _tablelist = (ArrayList<HashMap<String, String>>) msg.obj;
        _tableindex = _tablelist.size() - 1;
        CheckTableColumn(_tablelist, _tableindex);
        break;
      case Const.GDATA_RETURN_GET_TABLE_LIST_ERROR:
        if (_dialog != null) _dialog.dismiss();
        break;
      case Const.GDATA_RETURN_CHECK_TABLE_COLUMNS_NORMAL:
        if ((msg.obj != null) && (!((Boolean)msg.obj).booleanValue())){
          _tablelist.remove(_tableindex);
        }
        if (_tableindex == 0){
          if (_dialog != null) _dialog.dismiss();
          SelectTable(_tablelist);
        }else{
          _tableindex--;
          CheckTableColumn(_tablelist, _tableindex);
        }
        break;
      case Const.GDATA_RETURN_CHECK_TABLE_COLUMNS_ERROR:
        if (_dialog != null) _dialog.dismiss();
        break;
      case Const.GDATA_RETURN_UPLOAD_DATA_NORMAL:
      case Const.GDATA_RETURN_UPLOAD_DATA_ALLERROR:
      case Const.GDATA_RETURN_UPLOAD_DATA_ERROR:
      case Const.GDATA_RETURN_UPLOAD_DATA_CREATETABLE_ERROR:
      case Const.GDATA_RETURN_UPLOAD_DATA_OTHER_ERROR:
         if (_dialog != null) _dialog.dismiss();
        UploadCheck(_tableid,_tablename,msg.what,msg.obj);
        break;
      default:
        break;
      };
    };
  };

  public Activity getInstanse() {
    return _instanse;
  }

  public AccountChooser getAccountChooser() {
    return _accountChooser;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_PROGRESS);
    setContentView(R.layout.frm_list);
    _isDebug = Misc.isDebug(this);
    _instanse = this;
    _initflg = true;

    _obj = ObjectContainer.getInstance(getApplication());
    _dao = _obj.getDao();
    _tran = DBCommon.GetTable(_dao, DBCommon.TRAN, _isDebug);
    _tagmst = DBCommon.GetTable(_dao, DBCommon.TAG, _isDebug);

    _datearea = (LinearLayout) findViewById(R.id.month_area);
    _datearea.setVisibility(LinearLayout.VISIBLE);
    _basedatetxt = (TextView) findViewById(R.id.monthtxt);
    _basedatetxt.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View v) {
        _basedate = new Date();
        DispMonth();
        return false;
      }
    });

    _prevbtn = (ImageButton) findViewById(R.id.monthprev);
    _prevbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        DecMonth();
      }
    });

    _nextbtn = (ImageButton) findViewById(R.id.monthnext);
    _nextbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        IncMonth();
      }
    });

    _datatype = (TextView) findViewById(R.id.datatype);
    _datatype.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        _initflg = true;
        try{
          _datatype_idx++;
          if (_datatype_idx > MODE_SEARCH) _datatype_idx = MODE_DAY;
          _parentList.clear();
          ChangeMode(_datatype_idx, false);
        }finally{
          _initflg = false;
        }
      }
    });

    _searcharea = (LinearLayout) findViewById(R.id.search_area);
    _searchtxt = (EditText) findViewById(R.id.search_txt);
    _searchbtn = (ImageButton) findViewById(R.id.search_exec);
    _searchbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        btnSearchClick();
      }
    });

    // リストの追加
    _list = (ExpandableListView) findViewById(R.id.itemlist);
    SetListEventlistener();
    DispMonth();
    if (_adapter.GetParentCount() > 0) {
      _list.expandGroup(0);
    }

    // メニューボタン
    TripLogMisc.CreateButtonArea(Const.BUTTON_LIST, this, (LinearLayout) findViewById(R.id.buttonarea));
    _modebtn = (ImageButton) findViewById(R.id.list_mode_button);
    _modebtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (_multiselectmode) {
          ChangeMultiSelectMode(false, false);
        } else {
          ChangeMultiSelectMode(true, false);
        }
      }
    });

    ChangeMultiSelectMode(false, false);
    _initflg = false;
  }

  private void ChangeMultiSelectMode(boolean value, boolean refresh) {
    _multiselectmode = value;

    TextView mode = (TextView) findViewById(R.id.list_mode);
    if (_multiselectmode) {
      mode.setVisibility(View.VISIBLE);
      _modebtn.setImageResource(R.drawable.icon_undo_32);
    } else {
      mode.setVisibility(View.GONE);
      _modebtn.setImageResource(R.drawable.icon_checklist_32);
    }

    _adapter.SetMultiSelectMode(value);
    if (refresh) ChangeMode(true);

  }

  private GroupViewParentData GetGroupViewAtPos(int position) {
    GroupViewParentData p;

    int cnt = 0;
    for (int i = 0; i < _adapter.GetParentCount(); i++) {
      p = _adapter.GetParent(i);
      if (p == null) return null;
      if (cnt == position) return p;
      cnt++;
      if (!_list.isGroupExpanded(i)) continue;
      for (int ii = 0; ii < _adapter.getChildrenCount(i); ii++) {
        cnt++;
      }
    }
    return null;
  }

  private void SetListEventlistener() {

    _list.setOnGroupClickListener(new OnGroupClickListener() {
      public boolean onGroupClick(ExpandableListView paramExpandableListView, View paramView, int paramInt, long paramLong) {
        if (_longclick) {
          _longclick = false;
          return true;
        }
        return false;
      }
    });

    _list.setOnItemLongClickListener(new OnItemLongClickListener() {
      public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
        return ShowGroupDialog(GetGroupViewAtPos(paramInt));
      }
    });

    _list.setOnChildClickListener(new OnChildClickListener() {
      public boolean onChildClick(ExpandableListView paramExpandableListView, View paramView, final int paramInt1, final int paramInt2, long paramLong) {

        final GroupViewParentData p = (GroupViewParentData) _adapter.GetParent(paramInt1);
        final GroupViewChildData c = (GroupViewChildData) _adapter.getChild(paramInt1, paramInt2);
        final long id = c.getId();

        if (_multiselectmode) {
          c.setChecked(!c.isChecked());
          // _list.collapseGroup(paramInt1);
          // _list.expandGroup(paramInt1);
          _adapter.notifyDataSetChanged();
          _list.invalidateViews();
          return false;
        }

        LayoutInflater factory = LayoutInflater.from(LogListView.this);
        View entryView = factory.inflate(R.layout.dlg_listitemconf, null);

        LinearLayout showmaparea = (LinearLayout) entryView.findViewById(R.id.list_showmaparea);
        LinearLayout viewdataarea = (LinearLayout) entryView.findViewById(R.id.list_viewdataarea);
        LinearLayout changedataarea = (LinearLayout) entryView.findViewById(R.id.list_changearea);
        LinearLayout deletedataarea = (LinearLayout) entryView.findViewById(R.id.list_deletearea);
        LinearLayout selectdataarea = (LinearLayout) entryView.findViewById(R.id.list_selectarea);
        Button showmap = (Button) entryView.findViewById(R.id.list_showmap);
        Button viewdata = (Button) entryView.findViewById(R.id.list_viewdata);
        Button changedata = (Button) entryView.findViewById(R.id.list_changedata);

        showmap.setOnClickListener(new OnClickListener() {
          public void onClick(View paramView) {
            _AlertDialog.dismiss();
            int idx = GetParentIndex(p);
            long[] ids = new long[_adapter.getChildrenCount(idx)];
            GroupViewChildData c;
            for (int i = 0; i < ids.length; i++) {
              c = (GroupViewChildData) _adapter.getChild(idx, i);
              ids[i] = c.getId();
            }
            ShowMap(p, ids.length - paramInt2 - 1);
          }
        });

        viewdata.setOnClickListener(new OnClickListener() {
          public void onClick(View paramView) {
            _AlertDialog.dismiss();
            int idx = GetParentIndex(p);
            long[] ids = new long[_adapter.getChildrenCount(idx)];
            GroupViewChildData c;
            for (int i = 0; i < ids.length; i++) {
              c = (GroupViewChildData) _adapter.getChild(idx, i);
              ids[i] = c.getId();
            }
            Intent intent = new Intent();
            intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".ViewForm");
            Bundle bundle = new Bundle();
            bundle.putInt(Const.INTENT_KEY_CURRENTIDX, ids.length - paramInt2 - 1);
            bundle.putLongArray(Const.INTENT_KEY_TRNID_ARR, ids);
            bundle.putString(Const.INTENT_KEY_TITLE, p.GetCaption() + " " + getString(R.string.capdialog_listitemheader));
            intent.putExtra(Const.INTENT_INIT, bundle);
            startActivityForResult(intent, Const.REQUEST_VIEWDATA);

          }
        });

        changedata.setOnClickListener(new OnClickListener() {
          public void onClick(View paramView) {
            _AlertDialog.dismiss();
            LocationDataStruc.DeleteTemp(getApplication());
            TripLogMisc.RemoveTakePhotoFlg(getApplication());
            Intent intent = new Intent();
            intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".RegistForm");
            Bundle bundle = new Bundle();
            bundle.putLong(Const.INTENT_KEY_TRNID, id);
            intent.putExtra(Const.INTENT_INIT, bundle);
            startActivityForResult(intent, Const.REQUEST_CHANGEDATA);
          }
        });

        showmaparea.setVisibility(View.VISIBLE);
        viewdataarea.setVisibility(View.VISIBLE);
        changedataarea.setVisibility(View.VISIBLE);
        deletedataarea.setVisibility(View.GONE);
        selectdataarea.setVisibility(View.GONE);
        _AlertDialog = new AlertDialog.Builder(LogListView.this).setTitle(c.getHeader().replace("\n", " ") + getString(R.string.capdialog_listitemheader)).setView(entryView).create();
        _AlertDialog.show();
        return false;
      }
    });

  }

  private void GetItemList(int DataType) {

    String cond = "";
    String val = "";
    ArrayList<String> condp = new ArrayList<String>();

    // 条件設定
    if (DataType == MODE_DAY) {
      _gc.setTime(_basedate);
      InitgcDay();
      Date sdt = _gc.getTime();
      _gc.add(Calendar.MONTH, 1);
      _gc.add(Calendar.MILLISECOND, -1);
      Date edt = _gc.getTime();

      cond = TranRecord.REGIST_TIME + " > ? AND " + TranRecord.REGIST_TIME + " < ? ";
      condp.add(String.valueOf(sdt.getTime()));
      condp.add(String.valueOf(edt.getTime()));
    } else if (DataType == MODE_SEARCH) {
      val = _searchval;
      if (!val.equals("")) {
        val = "%" + val + "%";
        cond = "(" + TranRecord.CAPTION + " LIKE ?) OR (" + TranRecord.COMMENT + " LIKE ?)";
        condp.add(val);
        condp.add(val);
      }
    }

    String[] param = null;
    if (condp.size() != 0) {
      param = new String[condp.size()];
      for (int i = 0; i < condp.size(); i++) {
        param[i] = condp.get(i);
      }
    }

    GetTran(cond, param);

  }

  private void IncMonth() {
    DispMonth(1);
  }

  private void DecMonth() {

    DispMonth(-1);
  }

  private void DispMonth() {
    DispMonth(0);
  }

  private void DispMonth(int diff) {

    if (diff != 0) {
      _gc.setTime(_basedate);
      _gc.add(Calendar.MONTH, diff);
      _basedate = _gc.getTime();
    }

    _basedatetxt.setText(new SimpleDateFormat(getString(R.string.format_calender)).format(_basedate));
    ChangeMode(MODE_DAY, false);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == Const.REQUEST_CHANGEDATA) {
      if (resultCode != RESULT_OK) return;
      _tagmst.ClearRecord();
      ChangeMode(true);
    } else if (requestCode == Const.REQUEST_VIEWDATA) {
      if (resultCode != RESULT_OK) return;
      Bundle extras = data.getBundleExtra(Const.INTENT_VIEW);
      if (extras != null) {
        if (extras.getBoolean(Const.INTENT_KEY_CHANGEDATA, false)) {
          _tagmst.ClearRecord();
          ChangeMode(true);
        }
      }
    } else if (requestCode == Const.REQUEST_GET_LOGIN) {
      if (resultCode == RESULT_OK && _auth != null) {
        if (!_auth.authResult(resultCode, data)) {
//          _dialogManager.dismissDialogSafely(DialogManager.DIALOG_PROGRESS);
        }
      } else {
//        _dialogManager.dismissDialogSafely(DialogManager.DIALOG_PROGRESS);
      }
    } else if (requestCode == Const.REQUEST_GET_TABLES) {
      if (resultCode != RESULT_OK) return;
      Message message = new Message();
      _handler2.sendMessage(message);
    }

  }

  private boolean ShowGroupDialog(final GroupViewParentData p) {

    if (p == null) return false;

    LayoutInflater factory = LayoutInflater.from(LogListView.this);
    View entryView = factory.inflate(R.layout.dlg_listitemconf, null);

    LinearLayout showmaparea = (LinearLayout) entryView.findViewById(R.id.list_showmaparea);
    LinearLayout viewdataarea = (LinearLayout) entryView.findViewById(R.id.list_viewdataarea);
    LinearLayout changedataarea = (LinearLayout) entryView.findViewById(R.id.list_changearea);
    LinearLayout deletedataarea = (LinearLayout) entryView.findViewById(R.id.list_deletearea);
    LinearLayout selectdataarea = (LinearLayout) entryView.findViewById(R.id.list_selectarea);
    Button showmap = (Button) entryView.findViewById(R.id.list_showmap);
    Button viewdata = (Button) entryView.findViewById(R.id.list_viewdata);
    Button deletedata = (Button) entryView.findViewById(R.id.list_deletedata);
    Button selectdata = (Button) entryView.findViewById(R.id.list_selectdata);

    showmap.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _AlertDialog.dismiss();
        ShowMap(p, 0);
      }
    });

    deletedata.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _AlertDialog.dismiss();
        DeleteTagConf(p);
      }
    });

    selectdata.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        SelectChild(p);
        _AlertDialog.dismiss();
      }
    });

    viewdata.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _AlertDialog.dismiss();
        int idx = GetParentIndex(p);
        long[] ids = new long[_adapter.getChildrenCount(idx)];
        if (ids.length == 0) {
          Toast.makeText(LogListView.this, getString(R.string.msg_showmaperror_nodata), Toast.LENGTH_SHORT).show();
          return;
        }
        GroupViewChildData c;
        for (int i = 0; i < ids.length; i++) {
          c = (GroupViewChildData) _adapter.getChild(idx, i);
          ids[i] = c.getId();
        }
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".ViewForm");
        Bundle bundle = new Bundle();
        bundle.putInt(Const.INTENT_KEY_CURRENTIDX, 0);
        bundle.putLongArray(Const.INTENT_KEY_TRNID_ARR, ids);
        // bundle.putString(Const.INTENT_KEY_TITLE, getString(R.string.app_name)
        // + " - " + p.GetCaption() + " " +
        // getString(R.string.capdialog_listitemheader));
        bundle.putString(Const.INTENT_KEY_TITLE, p.GetCaption() + " " + getString(R.string.capdialog_listitemheader));
        intent.putExtra(Const.INTENT_INIT, bundle);
        startActivityForResult(intent, Const.REQUEST_VIEWDATA);
      }
    });

    if (_multiselectmode) {
      showmaparea.setVisibility(View.GONE);
      viewdataarea.setVisibility(View.GONE);
    } else {
      showmaparea.setVisibility(View.VISIBLE);
      viewdataarea.setVisibility(View.VISIBLE);
    }
    changedataarea.setVisibility(View.GONE);
    if ((!_multiselectmode) || (_datatype_idx != MODE_TAG)) {
      deletedataarea.setVisibility(View.GONE);
    } else {
      deletedataarea.setVisibility(View.VISIBLE);
      deletedata.setText(getString(R.string.capdialog_deletetagbutton));
    }
    if (_multiselectmode) {
      selectdataarea.setVisibility(View.VISIBLE);
      int idx = _adapter.GetParentIndex(p);
      ImageView img = (ImageView) entryView.findViewById(R.id.list_selectdataimage);
      selectdata.setText(getString(R.string.capdialog_selectdatabutton));
      img.setImageResource(R.drawable.icon_check_w_32);
      if (_adapter.getChildrenCount(idx) > 0) {
        GroupViewChildData c = (GroupViewChildData) _adapter.getChild(idx, 0);
        if (c.isChecked()) {
          selectdata.setText(getString(R.string.capdialog_selectdatabutton2));
          img.setImageResource(R.drawable.icon_uncheck_w_32);
        }
      }
    } else {
      selectdataarea.setVisibility(View.GONE);
    }
    ;

    // if ((_multiselectmode) && (_datatype_cal)){
    // return false;
    // };
    _AlertDialog = new AlertDialog.Builder(LogListView.this).setTitle(p.GetCaption() + getString(R.string.capdialog_listitemheader)).setView(entryView).create();
    _AlertDialog.show();
    _longclick = true;
    return true;

  }

  private void ShowMap(GroupViewParentData p, int CurrentIndex) {

    int i;
    int idx = -1;
    for (i = 0; i < _adapter.GetParentCount(); i++) {
      if (p == _adapter.GetParent(i)) {
        idx = i;
        break;
      }
    }
    ;
    if (idx == -1) return;

    int cnt = _adapter.getChildrenCount(idx);
    if (cnt == 0) {
      SetChildList(idx);
      cnt = _adapter.getChildrenCount(idx);
    }
    ;
    if (cnt == 0) {
      Toast.makeText(this, getString(R.string.msg_showmaperror_nodata), Toast.LENGTH_SHORT).show();
      return;
    }

    String[] data = new String[cnt];
    GroupViewChildData c;
    for (i = 0; i < cnt; i++) {
      c = (GroupViewChildData) _adapter.getChild(idx, i);
      data[i] = String.valueOf(c.getId());
    }

    HashMap<String, String[]> hm = new HashMap<String, String[]>();
    hm.put(Const.INTENT_CURRENTIDX, new String[] {
      String.valueOf(CurrentIndex)
    });
    hm.put(Const.INTENT_MAPDATA, data);
    hm.put(Const.INTENT_GROUPCAP, new String[] {
      p.GetCaption()
    });
    TripLogMisc.GoViewFunction(LogListView.this, ".LogMapView", hm);

  }

  // private void ShowMap(GroupViewParentData p,GroupViewChildData c,int
  // CurrentIndex) {
  // ShowMap(p,CurrentIndex);
  // // String[] data = new String[1];
  // // data[0] = String.valueOf(c.getId());
  // //
  // // HashMap<String, String[]> hm = new HashMap<String, String[]>();
  // // hm.put(Const.INTENT_MAPDATA, data);
  // // TripLogMisc.GoViewFunction(LogListView.this, ".LogMapView", hm);
  // }

  private int GetParentIndex(GroupViewParentData p) {
    for (int i = 0; i < _adapter.GetParentCount(); i++) {
      if (p == _adapter.GetParent(i)) {
        return i;
      }
    }
    return -1;
  }

  private void SelectChild(GroupViewParentData p) {

    int i;
    int idx = GetParentIndex(p);
    if (idx == -1) return;

    GroupViewChildData c;
    boolean b = true;
    for (i = 0; i < _adapter.getChildrenCount(idx); i++) {
      c = (GroupViewChildData) _adapter.getChild(idx, i);
      if (i == 0) b = !c.isChecked();
      c.setChecked(b);
    }

    // _list.collapseGroup(idx);
    // _list.expandGroup(idx);
    _adapter.notifyDataSetChanged();
    _list.invalidateViews();
  }

  private void ChangeMode(boolean Reset) {
    ChangeMode(_datatype_idx, Reset);
  };

  private void ChangeMode(int DispType, boolean Reset) {

    int i;
    // int ii;
    _gpos = -1;
    _cpos = -1;
    _reset = Reset;

    GroupViewParentData p;
    // GroupViewChildData c;
    _exparent = new HashMap<String, Object>();
    // HashMap<String,Object>chkchild = new HashMap<String,Object>();
    if (_adapter != null) {
      _gpos = _adapter.GetCurrentGroupIndex();
      _cpos = _adapter.GetCurrentChildIndex();
      for (i = 0; i < _adapter.getGroupCount(); i++) {
        p = (GroupViewParentData) _adapter.getGroup(i);
        if (_list.isGroupExpanded(i)) _exparent.put(p.GetCaption(), null);
        // for (ii=0;ii<_adapter.getChildrenCount(i);ii++){
        // c = (GroupViewChildData)_adapter.getChild(i, ii);
        // if (c.isChecked()) chkchild.put(String.valueOf(c.getId()),null);
        // }
      }
    }

    if (_tagmst.RecordCount() == 0) {
      _dao.list(_tagmst, new String[] {
        TagMasterRecord.TAG_NAME
      }, null, null);
    }

    if (DispType == MODE_DAY) {
      _datatype.setText(getString(R.string.cap_tab_calender));
      _datearea.setVisibility(LinearLayout.VISIBLE);
      _searcharea.setVisibility(View.GONE);
      if (_initflg){
        GetItemList(MODE_DAY);
        SetDateParent();
        ChangeModeSetList(_reset,_exparent,_gpos,_cpos);
      }else{
        setProgressBarVisibility(true);
        setProgressBarIndeterminate(true);
//        setProgress(5000);
//        _dialog = new ProgressDialog(this);
//        _dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        _dialog.setMessage(getString(R.string.progress_title));
//        _dialog.setCancelable(false);
//        _dialog.show();
        (new Thread(_runnable2)).start();
      }
    } else if (DispType == MODE_TAG) {
      _datatype.setText(getString(R.string.cap_tab_group));
      _datearea.setVisibility(LinearLayout.GONE);
      _searcharea.setVisibility(View.GONE);
      SetTagParent();
      ChangeModeSetList(_reset,_exparent,_gpos,_cpos);
    } else {
      _datatype.setText(getString(R.string.cap_tab_search));
      _datearea.setVisibility(LinearLayout.GONE);
      _searcharea.setVisibility(View.VISIBLE);
      if (_parentList.size() > 0) SearchExec();
      _parentList.clear();
      ChangeModeSetList(_reset,_exparent,_gpos,_cpos);
    }
  }

  private void ChangeModeSetList(boolean Reset,HashMap<String, Object> exparent,int gpos,int cpos){

    GroupViewParentData p;
    SetList();

    if (Reset) {
      for (int i = 0; i < _adapter.getGroupCount(); i++) {
        // for (ii=0;ii<_adapter.getChildrenCount(i);ii++){
        // c = (GroupViewChildData)_adapter.getChild(i, ii);
        // if (chkchild.containsKey(String.valueOf(c.getId())))
        // c.setChecked(true);
        // }
        p = (GroupViewParentData) _adapter.getGroup(i);
        if (exparent.containsKey(p.GetCaption())) {
          SetChildList(i);
          _list.expandGroup(i);
        }
      }
      _adapter.notifyDataSetChanged();
      _list.invalidateViews();
      SetVerticalScroll(gpos, cpos);
    }
  }
/*
 *   private void SearchExec(String SearchValue) {
    _parentList.clear();

    _dialog = new ProgressDialog(this);
    _dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    _dialog.setMessage(getString(R.string.progressDialog_addressSearchmsg));
    _dialog.setCancelable(false);
    _dialog.show();
    _searchval = SearchValue;
    (new Thread(_runnable)).start();

  }

  private Runnable _runnable = new Runnable() {
    public void run() {
      GetItemList(MODE_SEARCH);
      Message message = new Message();
      _handler.sendMessage(message);
      _dialog.dismiss();
    }
  };

  private final Handler _handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      String cap = MessageFormat.format(getString(R.string.cap_search_result), new Object[] {
        new Integer(_tran.RecordCount())
      });
      GroupViewParentData p = new GroupViewParentData(cap, 0, GroupViewParentData.RESULTCOLOR);
      _parentList.add(p);
      SetList();
      _list.requestFocus();
    }
  };


 */

  private void SetVerticalScroll(int GroupIndex, int ChildIndex) {

    if (GroupIndex == -1) return;
    if (ChildIndex == -1) {
      _list.setSelectedGroup(GroupIndex);
    } else {
      _list.setSelectedChild(GroupIndex, ChildIndex, false);
    }
  }

  private void SetTagParent() {
    _parentList.clear();
    for (int i = 0; i < _tagmst.RecordCount(); i++) {
      GroupViewParentData p = new GroupViewParentData(_tagmst.GetString(TagMasterRecord.TAG_NAME, i, ""), _tagmst.GetInt(TagMasterRecord.ID, i, 0), GroupViewParentData.DEFCOLOR);
      _parentList.add(p);
    }
  }

  private Runnable _runnable2 = new Runnable() {
    public void run() {
      GetItemList(MODE_DAY);
      Message message = new Message();
      _handler4.sendMessage(message);
//      _dialog.dismiss();
    }
  };

  private final Handler _handler4 = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      SetDateParent();
      ChangeModeSetList(_reset,_exparent,_gpos,_cpos);
      setProgressBarVisibility(false);
    }
  };

  private void SetDateParent() {

    _parentList.clear();

    Date dtbk = new Date(0);
    Date sdt = new Date();
    Date edt = new Date();
    String wk = "";
    GroupViewParentData p;
    SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.format_calender_md));

    int rowcolor;
    for (int i = 0; i < _tran.RecordCount(); i++) {
      sdt.setTime(_tran.GetDouble(TranRecord.REGIST_TIME, i, new Double(0)).longValue());
      _gc.setTime(sdt);
      InitgcHour();
      sdt = _gc.getTime();
      _gc.add(Calendar.DAY_OF_MONTH, 1);
      _gc.add(Calendar.MILLISECOND, -1);
      edt = _gc.getTime();
      if (dtbk.getTime() != sdt.getTime()) {
        wk = sdf.format(sdt);
        rowcolor = GroupViewParentData.DEFCOLOR;
        switch (_gc.get(Calendar.DAY_OF_WEEK)) {
        case Calendar.SUNDAY:
          rowcolor = Color.RED;
          break;
        case Calendar.SATURDAY:
          rowcolor = Color.BLUE;
          break;
        }
        p = new GroupViewParentData(wk, sdt.getTime(), edt.getTime(), _parentList.size(), rowcolor);
        _parentList.add(p);
        dtbk.setTime(sdt.getTime());
      }
    }
  }

  private void btnSearchClick() {
    SearchExec(_searchtxt.getText().toString());
  }

  private void SearchExec() {
    SearchExec(_searchval);
  }

  private void SearchExec(String SearchValue) {
    _parentList.clear();

    setProgressBarVisibility(true);
    setProgressBarIndeterminate(true);
//    _dialog = new ProgressDialog(this);
//    _dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//    _dialog.setMessage(getString(R.string.progressDialog_addressSearchmsg));
//    _dialog.setCancelable(false);
//    _dialog.show();
    _searchval = SearchValue;
    (new Thread(_runnable)).start();

  }

  private Runnable _runnable = new Runnable() {
    public void run() {
      GetItemList(MODE_SEARCH);
      Message message = new Message();
      _handler.sendMessage(message);
//      _dialog.dismiss();
    }
  };

  private final Handler _handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      String cap = MessageFormat.format(getString(R.string.cap_search_result), new Object[] {
        new Integer(_tran.RecordCount())
      });
      GroupViewParentData p = new GroupViewParentData(cap, 0, GroupViewParentData.RESULTCOLOR);
      _parentList.add(p);
      SetList();
      _list.requestFocus();
      setProgressBarVisibility(false);
    }
  };

  private void SetList() {

    // アダプタを作る
    _adapter = new GroupViewAdapter(getApplication(), _parentList, _multiselectmode);

    // アダプタを設定
    _list.setAdapter(_adapter);

    _list.setOnGroupClickListener(new OnGroupClickListener() {

      public boolean onGroupClick(ExpandableListView paramExpandableListView, View paramView, int paramInt, long paramLong) {
        SetChildList(paramInt);
        return false;
      }
    });

    if ((_datatype_idx == MODE_DAY) || (_datatype_idx == MODE_SEARCH)) {
      GroupViewParentData p;
      for (int i = 0; i < _adapter.GetParentCount(); i++) {
        p = _adapter.GetParent(i);
        SetChild(i, p, (_datatype_idx == MODE_DAY));
      }
      if ((_datatype_idx == MODE_SEARCH) && (_adapter.GetParentCount() > 0)) {
        _list.expandGroup(0);
      }
    }

  };

  private void SetChildList(int parentidx) {
    GroupViewParentData p = (GroupViewParentData) _adapter.getGroup(parentidx);

    if (_adapter.getChildrenCount(parentidx) == 0) {
      if (_datatype_idx == MODE_TAG) {
        SetTagChild(parentidx, p);
      }
    }
  }

  private void SetChild(int parentidx, GroupViewParentData p, boolean isDate) {

    ArrayList<GroupViewChildData> lst = new ArrayList<GroupViewChildData>();
    GroupViewChildData c;

    long sdt = p.GetSTime();
    long edt = p.GetETime();
    long wkdt;
    Date dt = new Date();
    SimpleDateFormat sdf;
    if (isDate) {
      sdf = new SimpleDateFormat(getString(R.string.format_calender_hhmm));
    } else {
      sdf = new SimpleDateFormat(getString(R.string.format_calender_ymdhm));
    }

    for (int i = 0; i < _tran.RecordCount(); i++) {
      wkdt = _tran.GetDouble(TranRecord.REGIST_TIME, i, new Double(0)).longValue();

      if ((isDate) && ((wkdt < sdt) || (wkdt > edt))) continue;
      dt.setTime(wkdt);
      c = new GroupViewChildData(
          _tran.GetInt(TranRecord.ID, i, -1),
          dt,
          _tran.GetDouble(TranRecord.LATITUDE, i, new Double(0)),
          _tran.GetDouble(TranRecord.LONGITUDE, i, new Double(0)),
          sdf.format(dt),
          _tran.GetString(TranRecord.CAPTION, i, ""),
          TripLogMisc.GetTagNames(_tran.GetString(TranRecord.TAGS, i, ""), _tagmst, _tags),
          !_tran.GetString(TranRecord.FILES, i, "").trim().equals(""),
          !_tran.GetString(TranRecord.COMMENT, i, "").trim().equals(""),
          (_tran.GetInt(TranRecord.TWEET, i, 0) == 1),
          (_tran.GetInt(TranRecord.G_UPLOAD, i, 0) == 1),
          _tran.GetString(TranRecord.LINKCODE, i, "")
          );
      lst.add(c);
    }

    _adapter.SetChildList(p, lst);

  }

  private void SetTagChild(int parentidx, GroupViewParentData p) {

    ArrayList<GroupViewChildData> lst = new ArrayList<GroupViewChildData>();
    GroupViewChildData c;
    Date dt = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.format_calender_ymdhm));

    String tagidx = "%," + String.valueOf(p.GetIndex()) + ",%";
    String cond = TranRecord.TAGS + " LIKE ? ";
    String[] param = new String[] {
      tagidx
    };

    GetTran(cond, param);

    for (int i = 0; i < _tran.RecordCount(); i++) {
      dt.setTime(_tran.GetDouble(TranRecord.REGIST_TIME, i, new Double(0)).longValue());
      c = new GroupViewChildData(
          _tran.GetInt(TranRecord.ID, i, -1),
          dt,
          _tran.GetDouble(TranRecord.LATITUDE, i, new Double(0)),
          _tran.GetDouble(TranRecord.LONGITUDE, i, new Double(0)),
          sdf.format(dt),
          _tran.GetString(TranRecord.CAPTION, i, ""),
          TripLogMisc.GetTagNames(_tran.GetString(TranRecord.TAGS, i, ""), _tagmst, _tags),
          !_tran.GetString(TranRecord.FILES, i, "").trim().equals(""),
          !_tran.GetString(TranRecord.COMMENT, i, "").trim().equals(""),
          (_tran.GetInt(TranRecord.TWEET, i, 0) == 1),
          (_tran.GetInt(TranRecord.G_UPLOAD, i, 0) == 1),
          _tran.GetString(TranRecord.LINKCODE, i, "")
          );
      lst.add(c);
    }

    _adapter.SetChildList(p, lst);

  }

  private void GetTran(String cond, String[] param) {
    _tran.ClearRecord();
    _dao.list(_tran, new String[] {
      TranRecord.REGIST_TIME + " DESC"
    }, cond, param);
  }

  private void InitgcDay() {

    _gc.set(Calendar.DATE, 1);
    InitgcHour();
  }

  private void InitgcHour() {
    _gc.set(Calendar.HOUR_OF_DAY, 0);
    _gc.set(Calendar.MINUTE, 0);
    _gc.set(Calendar.SECOND, 0);
    _gc.set(Calendar.MILLISECOND, 0);
  }

  // ///////////////////// メニュー関連 //////////////////////////
  // メニュー構築時に呼び出される
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    super.onCreateOptionsMenu(menu);
    // メニューアイテムの作成
    menu.add(0, Const.MENUICON_NORMALMODE, 0, R.string.menucap_normalmode).setIcon(R.drawable.icon_undo_32);
    menu.add(1, Const.MENUICON_MULTISELECTMODE, 1, R.string.menucap_multiselectmode).setIcon(R.drawable.icon_checklist_32);
    // V21以降のみアップロード対応
    if (AuthManagerFactory.useModernAuthManager()) {
      menu.add(0, Const.MENUICON_UPLOAD_DATA, 2, R.string.menucap_uploaddata).setIcon(R.drawable.icon_db_32);
    }
    ;
    menu.add(0, Const.MENUICON_ADDTAG, 3, R.string.menucap_tag).setIcon(R.drawable.icon_tag_32);
    menu.add(0, Const.MENUICON_SELECTDELETE, 4, R.string.menucap_selectedtrash).setIcon(R.drawable.icon_trash_32);
    return true;

  }

  // メニュー表示直前に呼び出される
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {

    menu.setGroupVisible(1, !_multiselectmode);
    menu.setGroupVisible(0, _multiselectmode);

    return super.onPrepareOptionsMenu(menu);

  }

  // メニューの項目が呼び出された時に呼び出される
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == Const.MENUICON_NORMALMODE) {
      ChangeMultiSelectMode(false, false);
      return true;
    } else if (item.getItemId() == Const.MENUICON_MULTISELECTMODE) {
      ChangeMultiSelectMode(true, false);
      return true;
    } else if (item.getItemId() == Const.MENUICON_ADDTAG) {
      SelectTag();
      return true;
    } else if (item.getItemId() == Const.MENUICON_SELECTDELETE) {
      DeleteSelectedItemConf();
      return true;
    } else if (item.getItemId() == Const.MENUICON_UPLOAD_DATA) {
      int ret = CheckExecuteUpload();

      if (ret == 2){
        Toast.makeText(this, getString(R.string.msg_noselectitem), Toast.LENGTH_SHORT).show();
        return true;
      }
      _tableuploadmsg = ((ret == 1)?getString(R.string.cap_tableselect_descript2):"");
      authenticate(new Intent(), Const.REQUEST_GET_TABLES, Const.SERVICE_ID_FT);

      return true;
    }

    return false;

  }

  public void DeleteSelectedItemConf() {

    _AlertDialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon_trash_w_32).setTitle(R.string.menucap_selectedtrash).setPositiveButton(R.string.capdialog_deletebutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        if (DeleteSelectedItem()) {
          ChangeMode(true);
        }
        _AlertDialog.dismiss();
      }
    }).setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        _AlertDialog.dismiss();
      }
    }).create();
    _AlertDialog.show();
  }

  public boolean DeleteSelectedItem() {

    GroupViewChildData c;
    String cond = TranRecord.ID + " = ? ";
    String[] param = new String[1];
    int cnt = 0;

    for (int i = 0; i < _adapter.GetParentCount(); i++) {
      for (int ii = 0; ii < _adapter.getChildrenCount(i); ii++) {
        c = (GroupViewChildData) _adapter.getChild(i, ii);
        if (!c.isChecked()) continue;
        _tran.ClearRecord();
        param[0] = String.valueOf(c.getId());
        GetTran(cond, param);
        _dao.delete_all(_tran);
        cnt++;
      }
    }
    if (cnt == 0) {
      Toast.makeText(this, getString(R.string.msg_noselectitem), Toast.LENGTH_SHORT).show();
      return false;
    } else {
      Toast.makeText(this, getString(R.string.msg_deleteselectitem), Toast.LENGTH_SHORT).show();
      return true;
    }
  }

  public void DeleteTagConf(final GroupViewParentData p) {

    _AlertDialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon_trash_w_32).setTitle(MessageFormat.format(getString(R.string.menucap_tagtrash), new Object[] {
      p.GetCaption()
    })).setPositiveButton(R.string.capdialog_deletebutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        _AlertDialog.dismiss();
        DeleteTag(p);
      }
    }).setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        _AlertDialog.dismiss();
      }
    }).create();
    _AlertDialog.show();
  }

  private boolean DeleteTag(GroupViewParentData p) {

    // トランザクションの修正
    String tagidx = "," + String.valueOf(p.GetIndex()) + ",";
    String cond = TranRecord.TAGS + " LIKE ? ";

    GetTran(cond, new String[] {
      "%" + tagidx + "%"
    });

    for (int i = 0; i < _tran.RecordCount(); i++) {
      _tran.SetString(TranRecord.TAGS, i, _tran.GetString(TranRecord.TAGS, i, "").replace(tagidx, ","));
      _dao.save(_tran, i);
    }

    // グループの削除
    cond = TagMasterRecord.TAG_NAME + " = ? ";

    _tagmst.ClearRecord();
    _dao.list(_tagmst, null, cond, new String[] {
      p.GetCaption()
    });
    _dao.delete_all(_tagmst);

    String msg = MessageFormat.format(getString(R.string.msg_deletetag), new Object[] {
      p.GetCaption()
    });
    _tagmst.ClearRecord();
    ChangeMode(true);

    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    return true;
  }

  public boolean SelectTag() {
    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_tagselect, null);

    final EditText txt = (EditText) entryView.findViewById(R.id.selecttagtext);
    final ListView lstview = (ListView) entryView.findViewById(R.id.selecttaglist);
    lstview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    // データの設定
    int i;
    final String[] listItems = GetTagList();
    final boolean[] listchecked = new boolean[listItems.length];
    for (i = 0; i < listchecked.length; i++) {
      listchecked[i] = false;
    }

    final CheckableAdapter<CheckableData> adapter = new CheckableAdapter<CheckableData>(this, R.layout.item_checkablerow, new ArrayList<CheckableData>());
    lstview.setAdapter(adapter);

    for (i = 0; i < listItems.length; i++) {
      adapter.add(new CheckableData(listItems[i], listchecked[i]));
    }

    lstview.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        LinearLayout layout = (LinearLayout) arg1;
        CheckBox item = (CheckBox) layout.findViewById(R.id.row_check);
        adapter.getItem(arg2).setChecked(!item.isChecked());
      }
    });

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setTitle(R.string.menucap_tag).setPositiveButton(R.string.capdialog_addbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        for (int i = 0; i < adapter.getCount(); i++) {
          listchecked[i] = adapter.getItem(i).isChecked();
        }
        SetTags(listItems, listchecked, txt.getText().toString());
        _AlertDialog.dismiss();
      }
    }).setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        _AlertDialog.dismiss();
      }
    }).setView(entryView).create();
    _AlertDialog.show();

    return true;
  }

  public String[] GetTagList() {

    int i;

    if (_taglist.size() == 0) {
      for (i = 0; i < _tagmst.RecordCount(); i++) {
        _taglist.add(_tagmst.GetString(TagMasterRecord.TAG_NAME, i, ""));
      }
    }
    ;

    String[] ret = new String[_taglist.size()];

    for (i = 0; i < _taglist.size(); i++) {
      ret[i] = _taglist.get(i);
    }

    return ret;

  }

  public void SetTags(String[] value, boolean[] listchecked, String txt) {

    HashMap<String, Object> hm = new HashMap<String, Object>();

    int i;
    int cnt = 0;
    for (i = 0; i < value.length; i++) {
      if (listchecked[i]) {
        hm.put(value[i], null);
        cnt++;
      }
    }

    txt = txt.trim();
    if (!txt.equals("")) {
      if (SaveTagName(txt)) {
        hm.put(txt, null);
        cnt++;
      }
      _tagmst.ClearRecord();
      _dao.list(_tagmst, new String[] {
        TagMasterRecord.TAG_NAME
      }, null, null);
    }
    ;

    String[] tagmst = new String[cnt];
    int idx = 0;
    for (i = 0; i < _tagmst.RecordCount(); i++) {
      if (hm.containsKey(_tagmst.GetString(TagMasterRecord.TAG_NAME, i, ""))) {
        tagmst[idx] = String.valueOf(_tagmst.GetInt(TagMasterRecord.ID, i, -1));
        idx++;
      }
    }

    GroupViewChildData c;
    String cond = TranRecord.ID + " = ? ";
    String[] param = new String[1];
    cnt = 0;

    for (i = 0; i < _adapter.GetParentCount(); i++) {
      for (int ii = 0; ii < _adapter.getChildrenCount(i); ii++) {
        c = (GroupViewChildData) _adapter.getChild(i, ii);
        if (!c.isChecked()) continue;
        _tran.ClearRecord();
        param[0] = String.valueOf(c.getId());
        GetTran(cond, param);
        _tran.SetString(TranRecord.TAGS, 0, settagInfo(tagmst, 0));
        _dao.save(_tran, 0);
        cnt++;
      }
    }
    if (cnt == 0) {
      Toast.makeText(this, getString(R.string.msg_noselectitem), Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, getString(R.string.msg_addtagselectitem), Toast.LENGTH_SHORT).show();
    }

    ChangeMode(true);

  }

  public boolean SaveTagName(String value) {

    for (int i = 0; i < _tagmst.RecordCount(); i++) {
      if (value.equals(_tagmst.GetString(TagMasterRecord.TAG_NAME, i, ""))) return false;
    }

    int row = _tagmst.AddRow();

    _tagmst.SetRowId(row, null);
    _tagmst.SetString(TagMasterRecord.TAG_NAME, row, value);
    _dao.save(_tagmst, row);
    _dao.list(_tagmst, new String[] {
      TagMasterRecord.TAG_NAME
    }, null, null);
    _taglist.clear();
    return true;

  };

  public String settagInfo(String[] taglst, int idx) {

    String tag = _tran.GetString(TranRecord.TAGS, idx, "");

    for (int i = 0; i < taglst.length; i++) {
      if (tag.indexOf("," + taglst[i] + ",") == -1) {
        if (tag.trim().equals("")) tag = ",";
        tag = tag + taglst[i] + ",";
      }
    }

    return tag;
  }

  //////////////////////// Upload関連 //////////////////////////
  private ArrayList<GroupViewChildData> GetSelectedDataList(){

    GroupViewChildData c;
    ArrayList<GroupViewChildData> ret = new ArrayList<GroupViewChildData>();

    // 対象データ抽出
    for (int i = 0; i < _adapter.GetParentCount(); i++) {
      for (int ii = 0; ii < _adapter.getChildrenCount(i); ii++) {
        c = (GroupViewChildData) _adapter.getChild(i, ii);
        if (!c.isChecked()) continue;
        ret.add(c);
      }
    }

    return ret;

  }

  // 0:選択 1:選択（Upload済みデータあり） 2:未選択
  private int CheckExecuteUpload(){

    GroupViewChildData c;
    ArrayList<GroupViewChildData> list = GetSelectedDataList();

    if (list.size()==0){
      return 2;
    } else {
      for (int i = 0;i<list.size();i++){
        c = list.get(i);
        if (c.isUpdated()){
          return 1;
        }
      }
      return 0;
    }
  }

  private void ShowProgressDialogForGData(int resid){
    _dialog = new ProgressDialog(this);
    _dialog.setIcon(android.R.drawable.ic_dialog_info);
    _dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    _dialog.setMessage(getString(resid));
    _dialog.setCancelable(true);
    _dialog.show();
  }

  private void GetTableList() {

    // 選択データ有無チェック

    // テーブルリスト読込みチェック
    if ((_tablelist != null) && (_tablelist.size() > 0)){
      SelectTable(_tablelist);
    }else{
      ShowProgressDialogForGData(R.string.tablelist_progress_title);
      _gdatathr = new GDataAccessThread(Const.GDATA_ACCESS_TYPE_GET_TABLE_LIST,LogListView.this, _handler3, _auth,_isDebug);
      _gdatathr.start();
    }
  }

  // テーブルのカラム状態チェック（お出かけログで使えるか？）
  private void CheckTableColumn(ArrayList<HashMap<String, String>> tablelist,int idx){
    _dialog.setMessage(getString(R.string.tablecheck_progress_title));
    _gdatathr = new GDataAccessThread(Const.GDATA_ACCESS_TYPE_CHECK_TABLE_COLUMNS,LogListView.this, _handler3, _auth,_isDebug);
    _gdatathr.SetTableId(tablelist.get(idx).get(Const.FT_COL_TABLELIST_ID));
    _gdatathr.start();
  }

  private String[] GetTableNameList(ArrayList<HashMap<String, String>> tablelist){
    String[] ret = new String[tablelist.size()];
    HashMap<String, String> data;
    for (int i = 0; i < tablelist.size(); i++) {
      data = tablelist.get(i);
      ret[i] = (String)data.get(Const.FT_COL_TABLELIST_NAME);
    }
    return ret;
  }

  public boolean SelectTable(final ArrayList<HashMap<String, String>> tablelist) {
    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_tableselect, null);

    final CheckBox chk = (CheckBox) entryView.findViewById(R.id.newtablechk);
    final EditText txt = (EditText) entryView.findViewById(R.id.selecttabletext);
    final ImageView lstimage = (ImageView) entryView.findViewById(R.id.selecttablelist_sep);
    final ListView lstview = (ListView) entryView.findViewById(R.id.selecttablelist);
    final TextView info = (TextView) entryView.findViewById(R.id.selecttablemsg);
    final TextView chkimg = (TextView) entryView.findViewById(R.id.uploadimagechk);
    lstview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    // データの設定
    final String[] listItems = GetTableNameList(tablelist);
    if (listItems.length == 0){
      chk.setChecked(true);
      txt.setVisibility(View.VISIBLE);
      lstimage.setVisibility(View.GONE);
      lstview.setVisibility(View.GONE);
    }

    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,listItems);
    lstview.setAdapter(adapter);

    txt.setVisibility(View.GONE);
    lstview.setVisibility(View.VISIBLE);
    chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton compoundbutton, boolean flag) {
        if (flag){
          txt.setVisibility(View.VISIBLE);
          lstview.setVisibility(View.GONE);
        }else{
          txt.setVisibility(View.GONE);
          lstview.setVisibility(View.VISIBLE);
        }
      }
    });

    if (_tableuploadmsg.trim().equals("")){
      info.setVisibility(View.GONE);
    }else{
      info.setVisibility(View.VISIBLE);
      info.setText(_tableuploadmsg);
    }

    lstview.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        lstview.setItemChecked(arg2,true);
      }
    });
    if (lstview.getCount() > 0){
      lstview.setItemChecked(0, true);
    }

    _imageupload = true;
    chkimg.setText(getString(R.string.cap_uploadimageon_descript));
    chkimg.setTextColor(Color.rgb(50, 255, 50));
    chkimg.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        _imageupload = !_imageupload;
        if (_imageupload){
          chkimg.setTextColor(Color.rgb(50, 255, 50));
          chkimg.setText(getString(R.string.cap_uploadimageon_descript));
        }else{
          chkimg.setTextColor(Color.rgb(90, 90, 90));
          chkimg.setText(getString(R.string.cap_uploadimageoff_descript));
        }
      }
    });

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setTitle(R.string.capdialog_selecttable).setPositiveButton(R.string.capdialog_okbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        _AlertDialog.dismiss();
        if (chk.isChecked()){
          String wk = txt.getText().toString();
          if (!wk.trim().equals("")){
            Upload(true, "", wk,_imageupload);
          }
        }else{
          int idx = lstview.getCheckedItemPosition();
          if ((idx >= 0) && (idx < adapter.getCount())){
            HashMap<String, String> data = tablelist.get(idx);
            Upload(true, data.get(Const.FT_COL_TABLELIST_ID), data.get(Const.FT_COL_TABLELIST_NAME),_imageupload);
          }
        }
      }
    }).setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        _AlertDialog.dismiss();
      }
    }).setView(entryView).create();
    _AlertDialog.show();

    return true;
  }

  public void Upload(boolean IsNewTable,String TableId,String TableName,boolean uploadimage){
    ShowProgressDialogForGData(R.string.uploadlocation_title);
    UploadData(TableId,TableName,IsNewTable, uploadimage);
  }

  // 画像・データのアップロード
  public void UploadData(String tableid,String tablename,boolean newtable, boolean uploadimage){
    _gdatathr = new GDataAccessThread(Const.GDATA_ACCESS_TYPE_UPLOAD_DATA,LogListView.this, _handler3, _auth,_isDebug);
    _gdatathr.SetNewTable(newtable);
    _gdatathr.SetTableId(tableid);
    _gdatathr.SetTableName(tablename);
    _gdatathr.SetUploadImage(uploadimage);
    _tableid = tableid;
    _tablename = tablename;
    // データ設定
    for (int i = 0; i < _adapter.getGroupCount(); i++) {
      for (int ii = 0; ii < _adapter.getChildrenCount(i); ii++) {
        _gdatathr.SetLocationData((GroupViewChildData) _adapter.getChild(i, ii));
      }
    }
    _gdatathr.start();
  }

  @SuppressWarnings("unchecked")
  // フラグ更新・メッセージ表示など
  public void UploadCheck(String TableId,String TableName, int ErrorCode,Object obj){
    ArrayList<String> idlist;
    String msg = "";
    if (obj == null) idlist = new ArrayList<String>();
    else             idlist = (ArrayList<String>)obj;
    でーたこうしん
    Toast.makeText(LogListView.this, msg, Toast.LENGTH_LONG);
  }

  private void authenticate(final Intent results, final int requestCode, final String service) {
    _auth = _authMap.get(service);
    if (_auth == null) {
      _auth = AuthManagerFactory.getAuthManager(this, Const.REQUEST_GET_LOGIN, null, true, service);
      _authMap.put(service, _auth);
    }
    if (AuthManagerFactory.useModernAuthManager()) {
      runOnUiThread(new Runnable() {
        public void run() {
          _accountChooser.chooseAccount(LogListView.this, new AccountChooser.AccountHandler() {
            public void handleAccountSelected(Account account) {
              if (account == null) {
                return;
              }
              doLogin(results, requestCode, service, account);
            }
          });
        }
      });
    } else {
      doLogin(results, requestCode, service, null);
    }
  }

  private void doLogin(final Intent results, final int requestCode, final String service, final Account account) {
    _auth.doLogin(new Runnable() {
      public void run() {
        onActivityResult(requestCode, RESULT_OK, results);
      }
    }, account);
  }

}
