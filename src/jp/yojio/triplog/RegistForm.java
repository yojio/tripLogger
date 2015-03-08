package jp.yojio.triplog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import jp.yojio.triplog.Common.Activity.LocationBaseForm;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Image.ImageManager;
import jp.yojio.triplog.Common.Map.LocationInfo;
import jp.yojio.triplog.Common.Misc.CheckableAdapter;
import jp.yojio.triplog.Common.Misc.CheckableData;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.Common.Tweet.SendTweetThread;
import jp.yojio.triplog.Common.Tweet.TokenInfo;
import jp.yojio.triplog.Common.Tweet.TwitterOAuth;
import jp.yojio.triplog.Common.Tweet.UrlShortlenThread;
import jp.yojio.triplog.DBAccess.ControlRecord;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.DBAccess.TagMasterRecord;
import jp.yojio.triplog.DBAccess.TranRecord;
import jp.yojio.triplog.Record.LocationDataStruc;
import jp.yojio.triplog.misc.TripLogMisc;
import twitter4j.Twitter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class RegistForm extends LocationBaseForm {

  private static final String LOGTAG_CLASS = RegistForm.class.getSimpleName();

  private ImageButton _attatch;
  private ImageButton _locationbtn;
  private ImageButton _filemanagebtn;
  private ImageButton _confirmbtn;
  private ImageButton _twitbtn;
  private Button _gps;
  private Button _map;
  private TextView _locationcap;
  private EditText _locationtxt;
  private EditText _comment;
  private Button _insertphoto;
  private Button _takephoto;
  private TextView _atattchtxt;
  private LocationDataStruc _data = null;
  private RecordBase _tran;
  private RecordBase _tagmst;
  private File _TempFile;
  private TextView _registtime;
  private TextView _tagtext;
  private ArrayList<String> _taglist = new ArrayList<String>();
  private Bitmap _bm;
  private int _tweetimage;
  private SendTweetThread _tweetthr = null;
  private UrlShortlenThread _urlthr = null;
  private boolean _tweetlocation;
  private Twitter _twit = null;
//private int _imagesize = 0;
  private Rect _imagerect = null;
  private int _imageper = 0;
  private boolean _getlocation = false;
  private boolean _locationsearch = false;
  private String _tweetcomment = "";

  // UI更新ハンドラ ※別スレッドから通知を受けてUI部品を更新する
  private final Handler _handler2 = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      Object obj = msg.obj;
      switch (msg.what) {
      case 2:
        _dialog.dismiss();
        try {
          if (obj == null) {
            Toast.makeText(RegistForm.this, getString(R.string.msg_tweet_abnormalend), Toast.LENGTH_SHORT).show();
            return;
          }
          if (((Boolean) obj).booleanValue()) {
            _data.setTweeted(true);
            String msgtxt;
            if (SaveData()) {
              LocationDataStruc.DeleteTemp(getApplication());
              TripLogMisc.RemoveTakePhotoFlg(getApplication());
            } else {
              if (_data.isNew()) {
                msgtxt = getResources().getText(R.string.registdata_ng).toString();
              } else {
                msgtxt = getResources().getText(R.string.modifydata_ng).toString();
              }
              Toast.makeText(RegistForm.this, msgtxt, Toast.LENGTH_SHORT).show();
              return;
            }
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
            Toast.makeText(RegistForm.this, getString(R.string.msg_tweet_normalend), Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(RegistForm.this, getString(R.string.msg_tweet_abnormalend), Toast.LENGTH_SHORT).show();
          }
        } finally {
          _twit = null;
          _tweetthr = null;
        }
        break;
      case 3:
        StartGetLocationStatus();
        break;
      case 4:
        _dialog.dismiss();
        _tweetcomment = (String)obj;
        try{
          ShowTweetDlg();
        }finally{
          _twitbtn.setEnabled(true);
        }
      default:
        break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.frm_regist);

    _registtime = (TextView) findViewById(R.id.registtime);
    _registtime.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _registtime.setEnabled(false);
        try{
          ChangeRegDate();
        }finally{
          _registtime.setEnabled(true);
        }
      }
    });

    // 場所テキスト
    _locationcap = (TextView) findViewById(R.id.mapcaption);
    _locationtxt = (EditText) findViewById(R.id.maptext);
    _locationtxt.addTextChangedListener(new TextWatcher() {
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        _data.SetCaptionChanged((s != null) && (s.length() > 0));
        _confirmbtn.setEnabled(s.length() != 0);
        _twitbtn.setEnabled((s.length() != 0) && (_control.UseTwit()));
        SetEnabledImage();
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void afterTextChanged(Editable s) {
      }
    });

    // 場所検索ファイルボタン
    _locationbtn = (ImageButton) findViewById(R.id.searchlocationbtn);
    _locationbtn.setOnClickListener(this);

    // タグボタン
    _tagtext = (TextView) findViewById(R.id.tagtext);
    _tagtext.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        _tagtext.setEnabled(false);
        try{
          SelectTag();
        }finally{
          _tagtext.setEnabled(true);
        }
      }
    });

    // 添付ファイルボタン
    _attatch = (ImageButton) findViewById(R.id.attatchbtn);
    _attatch.setOnClickListener(this);
    _filemanagebtn = (ImageButton) findViewById(R.id.filemanagebtn);
    _filemanagebtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _filemanagebtn.setEnabled(false);
        try{
          SaveTempData();
          Intent intent = new Intent();
          intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".ImagePreview");

          Bundle bundle = new Bundle();
          String[] path = new String[_data.FileCount()];
          for (int i = 0; i < path.length; i++) {
            path[i] = _data.GetFile(i);
          }
          bundle.putStringArray(Const.FILE_LIST, path);
          intent.putExtra(Const.IMAGE_PARAM, bundle);
          // アクティビティの呼び出し
          startActivityForResult(intent, Const.REQUEST_IMAGEPREVIEW);
        }finally{
          _filemanagebtn.setEnabled(true);
        }
      }
    });
    _atattchtxt = (TextView) findViewById(R.id.atattchtext);

    // コメント
    _comment = (EditText) findViewById(R.id.comment);
    // _comment.setFocusable(true);
    // _comment.setFocusableInTouchMode(true);
    // _comment.requestFocus();

    _confirmbtn = (ImageButton) findViewById(R.id.confirmbtn);
    _confirmbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _confirmbtn.setEnabled(false);
        try{
          String msg;
          if (SaveData()) {
            if (_data.isNew()) {
              msg = getResources().getText(R.string.registdata_ok).toString();
            } else {
              msg = getResources().getText(R.string.modifydata_ok).toString();
            }
            LocationDataStruc.DeleteTemp(getApplication());
            TripLogMisc.RemoveTakePhotoFlg(getApplication());
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
          } else {
            if (_data.isNew()) {
              msg = getResources().getText(R.string.registdata_ng).toString();
            } else {
              msg = getResources().getText(R.string.modifydata_ng).toString();
            }
          }
          Toast.makeText(RegistForm.this, msg, Toast.LENGTH_SHORT).show();
        }finally{
          _confirmbtn.setEnabled(true);
        }
      }
    });

    _twitbtn = (ImageButton)findViewById(R.id.twitbtn);
    _twitbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _twitbtn.setEnabled(false);
        ExecTweet();
      };
    });

    _data = new LocationDataStruc();
    _tran = DBCommon.GetTable(_dao, DBCommon.TRAN,_isDebug);
    _tagmst = DBCommon.GetTable(_dao, DBCommon.TAG,_isDebug);
    _dao.list(_tagmst, new String[] { TagMasterRecord.TAG_NAME }, null, null);

    // Intent起動チェック
    boolean initflg = false;
    Intent it = getIntent();
    String action = it.getAction();
    Bundle extras;
    String url = "";
    if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_EDIT.equals(action)) {
      extras = it.getExtras();
      if (extras != null) {
        if (it.getType().toLowerCase().equals("image/jpeg")) {
          Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
          SetAttachFile(uri);
        } else if (it.getType().toLowerCase().equals("text/plain")) {
          url = extras.getCharSequence(Intent.EXTRA_TEXT).toString();
        }
      }
      initflg = true;
    } else {
      extras = it.getBundleExtra(Const.INTENT_INIT);
      initflg = (extras != null);

      long id = extras.getLong(Const.INTENT_KEY_TRNID, -1);
      LoadData(id);
    }

    _data.setinit(initflg && _data.isNew());

//    if (!initflg) LoadTempData();
    LoadTempData();
    if ((!url.trim().equals("")) && (_data.getComment().equals(""))) {
      _data.setComment(url + " ");
    }
    DispData();
    _getlocation = ((_data.isInit()) && (_control.getAutoLocationType() == Const.SET_AUTO_LOCATION_AUTO));
    _data.setinit(false);

    if (_control.UseTwit()){
      _twitbtn.setVisibility(View.VISIBLE);
//      _twitbtn.setEnabled(false);
    }else{
      _twitbtn.setVisibility(View.GONE);
    }

  }

  @Override
  protected void onResume(){
    super.onResume();
    _twit = null;
  }

  @Override
  protected void onStart() {
    super.onStart();

    if (TripLogMisc.CheckTakePhoto(getApplication())){
//      RemoveTakePhotoFlg();
      _data.RestoreTemp(getApplication());
      return;
    }

    _locationsearch = false;

    if (_control.UseTwit()){
      _twitbtn.setVisibility(View.VISIBLE);
//      _twitbtn.setEnabled(false);
    }else{
      _twitbtn.setVisibility(View.GONE);
    }

    if (_getlocation) {
      _getlocation = false;
      _data.setinit(false);
      // 直前のデータが３分以内の場合はそれの場所をコピーする

      // 最大値（直近）のデータ取得
      _tran.ClearRecord();
      _dao.list(_tran, null, TranRecord.REGIST_TIME + " = (select max(" + TranRecord.REGIST_TIME + ") from " + TranRecord.TABLE_NAME + ")", null);

      if (_tran.RecordCount() > 0) {
        Date pdt = new Date();
        Date cdt = new Date();
        pdt.setTime(_tran.GetDouble(TranRecord.REGIST_TIME, 0, new Double(0)).longValue());
        if ((cdt.getTime() - pdt.getTime()) < Const.LOCATIONUPDATE_REG_MINTIME) {
          _data.setLatitude(_tran.GetDouble(TranRecord.LATITUDE, 0, new Double(Const.INIT_POS_LATITUDE)));
          _data.setLongitude(_tran.GetDouble(TranRecord.LONGITUDE, 0, new Double(Const.INIT_POS_LONGITUDE)));
          updateLocationCaption(_tran.GetString(TranRecord.CAPTION, 0, ""), true);
          return;
        }
      }

      GetLocationWaitThread thr = new GetLocationWaitThread(_handler2);
      thr.start();
    }

  };

  private void SetEnabledImage(){
    if (_confirmbtn.isEnabled()){
      _confirmbtn.setImageResource(R.drawable.icon_confirm_32);
//      _confirmbtn.setBackgroundResource(android.R.drawable.btn_default);
      _confirmbtn.setBackgroundResource(android.R.drawable.list_selector_background);
    }else{
      _confirmbtn.setImageResource(R.drawable.icon_confirm_no_use_32);
      _confirmbtn.setBackgroundColor(Color.TRANSPARENT);
    }
    SetTwitbuttonImage();
//    if (_twitbtn.isEnabled()){
//      _twitbtn.setImageResource(R.drawable.icon_twit_32);
////    _twitbtn.setBackgroundResource(android.R.drawable.btn_default);
//      _twitbtn.setBackgroundResource(android.R.drawable.list_selector_background);
//    }else{
//      _twitbtn.setImageResource(R.drawable.icon_twit_nouse_32);
//      _twitbtn.setBackgroundColor(Color.TRANSPARENT);
//    }
  }

  public void LoadData(long id) {
    _tran.ClearRecord();
    _dao.list(_tran, null, TranRecord.ID + " = ? ", new String[] { String.valueOf(id) });
    if (_tran.RecordCount() == 0)
      return;

    int i;
    String wk;

    try {
      _data.SetId(_tran.GetInt(TranRecord.ID, 0, 0));
      _data.setDate(new Date(_tran.GetDouble(TranRecord.REGIST_TIME, 0, new Double(0)).longValue()));
      _data.setNew(false);
      _data.setLocationSet(true);
      _data.setCaption(_tran.GetString(TranRecord.CAPTION, 0, ""));
      _data.SetCaptionChanged(true);
      _data.setLatitude(_tran.GetDouble(TranRecord.LATITUDE, 0, new Double(0)));
      _data.setLongitude(_tran.GetDouble(TranRecord.LONGITUDE, 0, new Double(0)));
      _data.SetTags(_tran.GetString(TranRecord.TAGS, 0, ""));
      SetTagNames();
      _data.setComment(_tran.GetString(TranRecord.COMMENT, 0, ""));
      _data.setTweeted(_tran.GetInt(TranRecord.TWEET, 0, 0) == 1);
      _data.setUploaded(_tran.GetInt(TranRecord.G_UPLOAD, 0, 0) == 1);
      _data.setLinkCode(_tran.GetString(TranRecord.LINKCODE, 0, ""));
      _data.setFileName("");
      wk = _tran.GetString(TranRecord.FILES, 0, "");
      String[] files = wk.split(",");
      _data.ClearFiles();
      for (i = 0; i < files.length; i++) {
        if (files[i].trim().equals(""))
          continue;
        _data.AddFile(files[i]);
      }
    } catch (Exception e) {
      return;
    }
    _tran.ClearRecord();
  }

  public boolean LoadTempData() {
//    LocationDataStruc work = (LocationDataStruc)TripLogMisc.LoadObject(getApplication(), Const.DATACLASS_NAME,true);
//    if (work == null) return false;
//    _data = work;
    if (_data == null) _data = new LocationDataStruc();
    _data.RestoreTemp(getApplication());
    return true;
  }

  public void SaveTempData() {
    _data.setCaption(_locationtxt.getText().toString());
    _data.setComment(_comment.getText().toString());
    _data.SaveTemp(getApplication());
//    TripLogMisc.SaveObject(getApplication(), Const.DATACLASS_NAME, _data);
  }

  public void DispRegistDate() {
    _registtime.setVisibility(TextView.VISIBLE);
    String regtime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(_data.getDate());
    _registtime.setText(getText(R.string.capRegistTime) + regtime);
  }

  public void DispData() {

    if (_data == null) return;

    DispRegistDate();

    boolean txtchange = _data.IsCaptionChanged();
    DispLATandLNG();
    _locationtxt.setText(_data.getCaption());
    DispAtattchFiles();
    _comment.setText(_data.getComment());
    String s = _data.GetTagNames();
    if (s.trim().equals("")) s = getString(R.string.txtcapDeftagmsg);
    _tagtext.setText(s);
    _data.SetCaptionChanged(txtchange);
    _confirmbtn.setEnabled(_data.getCaption().length() != 0);
    _twitbtn.setEnabled((_data.getCaption().length() != 0) && (_control.UseTwit()));
    SetEnabledImage();

  }

  private void SetTwitbuttonImage(){

    if (_twitbtn.isEnabled()){
      if (_data.isTweeted()){
        _twitbtn.setImageResource(R.drawable.icon_twit_checked_32);
      }else{
        _twitbtn.setImageResource(R.drawable.icon_twit_32);
      }
      _twitbtn.setBackgroundResource(android.R.drawable.list_selector_background);
    }else{
      if (_data.isTweeted()){
        _twitbtn.setImageResource(R.drawable.icon_twit_nouse_checked_32);
      }else{
        _twitbtn.setImageResource(R.drawable.icon_twit_nouse_32);
      }
      _twitbtn.setBackgroundColor(Color.TRANSPARENT);
    }

  }

  public void SetTagNames() {

    StringBuffer val = new StringBuffer();
    if (!_data.GetTagNames().trim().equals("")) return;
    if (_data.GetTags().trim().equals("")) return;

    HashMap<String, Object> tags = new HashMap<String, Object>();
    String[] wk = _data.GetTags().split(",");
    for (int i = 0; i < wk.length; i++) {
      tags.put(wk[i], null);
    }

    String s = "";
    _data.SetTagNames("");
    for (int i = 0; i < _tagmst.RecordCount(); i++) {
      if (tags.containsKey(String.valueOf(_tagmst.GetInt(TagMasterRecord.ID, i, -1)))) {
        s = _tagmst.GetString(TagMasterRecord.TAG_NAME, i, "");
        if (s.equals("")) continue;
        if (val.length() != 0) val.append(",");
        val.append(s);
      }
    }

    _data.SetTagNames(val.toString());

  }

  public void DispLATandLNG() {
    _locationtxt.setEnabled(_data.isLocationSet());
    _locationtxt.setFocusable(_data.isLocationSet());
    _locationtxt.setFocusableInTouchMode(_data.isLocationSet());
    _confirmbtn.setEnabled((_data.isLocationSet()) && (_locationtxt.getText().length() != 0));
    _twitbtn.setEnabled((_data.getCaption().length() != 0) && (_control.UseTwit()));
    SetEnabledImage();

    if (!_data.isLocationSet()) {
      _locationcap.setText(getString(R.string.txtcapDefLocationmsg));
    }else{
      _locationcap.setText(Misc.GetLATString(_data.getLatitude()) + " / " + (Misc.GetLNGString(_data.getLongitude())));
    }
  }

  public void DispAtattchFiles() {
    int cnt = _data.FileCount();
    String msg = " × " + String.valueOf(cnt);
    if (cnt == 0) msg = msg + "      " + getString(R.string.capNoPhoto);
    _atattchtxt.setText(msg);
    _filemanagebtn.setEnabled(cnt > 0);
  }

  public void onClick(View arg0) {
    arg0.setEnabled(false);
    try{
      if (arg0 == _locationbtn) {
        SearchLocationClicked();
      } else if (arg0 == _gps) {
        _AlertDialog.dismiss();
        GetLocationButtonClicked();
      } else if (arg0 == _map) {
        _AlertDialog.dismiss();
        SearchMapButtpnclicked();
      } else if (arg0 == _attatch) {
        AttatchOnClick();
      } else if (arg0 == _insertphoto) {
        _AlertDialog.dismiss();
        SelectPhoto();
      } else if (arg0 == _takephoto) {
        _AlertDialog.dismiss();
        TakePhoto();
      }
    }finally{
      arg0.setEnabled(true);
    }
  };

  // 場所検索
  public void SearchLocationClicked() {

    if (_locationsearch){
      CancelLocationSearch();
      return;
    }

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_locationconf, null);
    // 写真挿入・撮影ボタン
    _gps = (Button) entryView.findViewById(R.id.seachgps);
    _map = (Button) entryView.findViewById(R.id.searchmap);
    _gps.setOnClickListener(this);
    _map.setOnClickListener(this);

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setTitle(R.string.capLocationSearch).setView(entryView).create();

    _AlertDialog.show();

  }

  // GPS検索
  public void GetLocationButtonClicked() {
    _networkposget = false;
    StartGetLocationStatus();
  }

  // 地図検索
  public void SearchMapButtpnclicked() {

    SaveTempData();

    Intent intent = new Intent();
    intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".SearchMapForm");

    if ((_data.getLatitude().doubleValue()!=0.0) || (_data.getLongitude().doubleValue()!=0.0)){
      Bundle bundle = new Bundle();
      bundle.putDouble(Const.INIT_LATITUDE, _data.getLatitude().doubleValue());
      bundle.putDouble(Const.INIT_LONGITUDE, _data.getLongitude().doubleValue());
      intent.putExtra(Const.INIT_POS, bundle);
    }

    // アクティビティの呼び出し
    startActivityForResult(intent, Const.REQUEST_GET_POS);
  }

  // 添付ボタンクリック
  public void AttatchOnClick() {

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_photoconf, null);
    // 写真挿入・撮影ボタン
    _insertphoto = (Button) entryView.findViewById(R.id.insertpic);
    _takephoto = (Button) entryView.findViewById(R.id.takephoto);
    _insertphoto.setOnClickListener(this);
    _takephoto.setOnClickListener(this);

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon_attach_w_32).setTitle(R.string.capAttatch).setView(entryView).create();

    _AlertDialog.show();
  }

  public void TakePhoto() {

    Intent intent = new Intent();
    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

    _TempFile = new File(Environment.getExternalStorageDirectory(), ImageManager.createName());
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(_TempFile));
    _data.setFileName(_TempFile.getPath());
    TripLogMisc.SetTakePhotoFlg(getApplication());
    SaveTempData();
    startActivityForResult(intent, Const.REQUEST_CAMERA);

  }

  public void SelectPhoto() {

    SaveTempData();
    Intent intent = new Intent();
    intent.setType("image/*");
//    intent.setAction(Intent.ACTION_GET_CONTENT);
    intent.setAction(Intent.ACTION_PICK);
    startActivityForResult(intent, Const.REQUEST_GALLERY);
  }

  @Override
  protected boolean StartGetLocationStatus() {
    _locationsearch = true;
    if (!((_contr.GetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_HIGH) == Const.SET_LOCATION_LOW_HIGH) && (_networkposget))) {
      _locationcap.setTextColor(Color.rgb(85, 85, 255));
      _locationcap.setText(getString(R.string.msg_locationinfo));
    }
    _locationbtn.setImageResource(R.drawable.icon_cancel_24);
    _locationtxt.setEnabled(false);
    _locationtxt.setFocusable(false);
    _locationtxt.setFocusableInTouchMode(false);
    _attatch.setEnabled(false);
    _attatch.setImageResource(R.drawable.icon_attach_nouse_32);
    _filemanagebtn.setEnabled(false);
    if (!StartGetLocationStatus(false)){
      CancelLocationSearch();
      return false;
    }else{
      return true;
    }
  }

  private void RestoreLocationCapStatus(){
    _locationsearch = false;
    _locationcap.setTextColor(Color.rgb(51, 51, 51));
    DispLATandLNG();
    _locationbtn.setImageResource(R.drawable.icon_map_24);
    _attatch.setEnabled(true);
    _attatch.setImageResource(R.drawable.icon_attach_32);
    _filemanagebtn.setEnabled(_data.FileCount() > 0);
    DispLATandLNG();
  }

  @Override
  protected void CancelLocationSearch() {
    RestoreLocationCapStatus();
    super.CancelLocationSearch();
  }

  @Override
  public void GetLocationEv(LocationInfo info) {
    RestoreLocationCapStatus();
    if (info.isReaded()) {
      final String location = info.getLocation();
      _data.setLatitude(new Double(info.getLatitude()));
      _data.setLongitude(new Double(info.getLongitude()));
      setLocationTxt(location);
    } else {
      Toast.makeText(this, getResources().getText(R.string.msg_locationerror), Toast.LENGTH_SHORT).show();
    }
    ;
    if (ReLocationSearch()){
      super.CancelLocationSearch();
      StartGetLocationStatus();
    };
  }

  private void setLocationTxt(final String location) {

    if (_data.IsCaptionChanged()) {
      new AlertDialog.Builder(this).setTitle(getString(R.string.capdialog_locationname_change)).setMessage(getString(R.string.msgdialog_locationname_change) + "\n" + location).setPositiveButton(
          getString(R.string.capdialog_yesbutton), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              updateLocationCaption(location, true);
            }
          }).setNegativeButton(getString(R.string.capdialog_nobutton), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          updateLocationCaption(location, false);
        }
      }).show();
    } else {
      updateLocationCaption(location, true);
    }
  }

  private void updateLocationCaption(String location, boolean updtxt) {
    if (updtxt) {
      _data.setCaption(location);
      _locationtxt.setText(location);
      _data.SetCaptionChanged(false);
    }
    _data.setLocationSet(true);
    DispLATandLNG();
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode != RESULT_OK) {
      LoadTempData();
      DispData();
      return;
    }

    Uri sUri = null;
    Bundle extras;

    if (requestCode == Const.REQUEST_CAMERA) {
      LoadTempData();
      DispData();

      try {
        sUri = ImageManager.addImageAsCamera(getContentResolver(), _data.getFileName());
        File f = new File(_data.getFileName());
        f.delete();
      } catch (FileNotFoundException e) {
//        Toast.makeText(this, getResources().getText(R.string.msg_TakePhotoError), Toast.LENGTH_LONG).show();
        if (_isDebug) Log.e(LOGTAG_CLASS, "TakePhotoError");
      }

      if (sUri == null){
        sUri = data.getData();
      }

      if (sUri != null)_data.AddFile(sUri.toString());
      DispAtattchFiles();
    } else if (requestCode == Const.REQUEST_GALLERY) {
      sUri = data.getData();
      SetAttachFile(sUri);
    } else if (requestCode == Const.REQUEST_GET_POS) {
      extras = data.getBundleExtra(Const.RESULT_POS);
      if (extras != null) {
        _data.setLatitude(new Double(extras.getDouble(Const.RESULT_LATITUDE)));
        _data.setLongitude(new Double(extras.getDouble(Const.RESULT_LONGITUDE)));
        String location;
        String wk = extras.getString(Const.RESULT_BOOKMARKFLG);
        try {
          boolean b = wk.equals(Const.RESULT_YES);
          if (b) {
            location = extras.getString(Const.RESULT_BOOKMARKCAP);
          } else {
            location = _geo.point2address(_data.getLatitude().doubleValue(), _data.getLongitude().doubleValue(), getApplication());
          }
          setLocationTxt(location);
          if (b)
            _data.SetCaptionChanged(true);
        } catch (IOException e) {
          Toast.makeText(this, getResources().getText(R.string.msg_locationerror), Toast.LENGTH_SHORT).show();
          if (_isDebug) Log.e(LOGTAG_CLASS, "point2adress-error");
        }
      }
    } else if (requestCode == Const.REQUEST_IMAGEPREVIEW) {
      extras = data.getBundleExtra(Const.IMAGE_RESULT);
      if (extras != null) {
        int idx = extras.getInt(Const.DELETE_FILE_IDX);
        if (idx == -1)
          return;
        _data.DeleteFile(idx);
        DispAtattchFiles();
      }
    }
    _data.SaveTemp(getApplication());
  }

  private void SetAttachFile(Uri uri) {
    LoadTempData();
    // データのロード処理
    DispData();

    if (uri == null)
      return;
    _data.AddFile(uri.toString());
    DispAtattchFiles();
  }

  public boolean ChangeRegDate() {
    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_datetimepicker, null);

    final DatePicker dp = (DatePicker) entryView.findViewById(R.id.dialog_datepicker);
    final TimePicker tp = (TimePicker) entryView.findViewById(R.id.dialog_timepicker);
    tp.setIs24HourView(new Boolean(true));

    final GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(_data.getDate());

    dp.init(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc.get(Calendar.DATE), null);
    tp.setCurrentHour(new Integer(gc.get(Calendar.HOUR_OF_DAY)));
    tp.setCurrentMinute(new Integer(gc.get(Calendar.MINUTE)));

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon_clock_w_32).setTitle(R.string.capRegistTimeChange).setPositiveButton(R.string.capdialog_changebutton,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            gc.set(Calendar.YEAR, dp.getYear());
            gc.set(Calendar.MONTH, dp.getMonth());
            gc.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
            gc.set(Calendar.HOUR_OF_DAY, tp.getCurrentHour().intValue());
            gc.set(Calendar.MINUTE, tp.getCurrentMinute().intValue());
            _data.setDate(gc.getTime());
            DispRegistDate();
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

    String[] ret = new String[_taglist.size()];

    for (i = 0; i < _taglist.size(); i++) {
      ret[i] = _taglist.get(i);
    }

    return ret;

  }

  public boolean[] GetTagChecked() {
    int i;
    boolean[] ret = new boolean[_taglist.size()];

    String[] wk = _data.GetTagNames().split(",");
    HashMap<String, Object> data = new HashMap<String, Object>();

    for (i = 0; i < wk.length; i++) {
      data.put(wk[i], null);
    }

    for (i = 0; i < _taglist.size(); i++) {
      ret[i] = data.containsKey(_taglist.get(i));
    }

    return ret;

  }

  public boolean SelectTag() {
    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_tagselect, null);

    final EditText txt = (EditText) entryView.findViewById(R.id.selecttagtext);
    final ListView lstview = (ListView) entryView.findViewById(R.id.selecttaglist);
    lstview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    // データの設定
    final String[] listItems = GetTagList();
    final boolean[] listchecked = GetTagChecked();

    final CheckableAdapter<CheckableData> adapter = new CheckableAdapter<CheckableData>(this, R.layout.item_checkablerow, new ArrayList<CheckableData>());
    lstview.setAdapter(adapter);

    for (int i = 0; i < listItems.length; i++) {
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
    _AlertDialog = new AlertDialog.Builder(this).setTitle(R.string.capdialog_selecttag).setPositiveButton(R.string.capdialog_registbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        for (int i = 0; i < adapter.getCount(); i++) {
          listchecked[i] = adapter.getItem(i).isChecked();
        }
        SetTags(listItems, listchecked, txt.getText().toString());
        String s = _data.GetTagNames();
        if (s.trim().equals("")) s = getString(R.string.txtcapDeftagmsg);
        _tagtext.setText(s);
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

  public void SetTags(String[] value, boolean[] chk, String txt) {

    HashMap<String, Object> wk = new HashMap<String, Object>();
    StringBuffer ret = new StringBuffer();
    for (int i = 0; i < chk.length; i++) {
      if (chk[i]) {
        if (ret.length() != 0)
          ret.append(",");
        ret.append(value[i]);
        wk.put(value[i], null);
      }
    }

    txt = txt.trim();
    if (!txt.equals("")) {
      SaveTagName(txt);
      if (!wk.containsKey(txt)) {
        if (ret.length() != 0)
          ret.append(",");
        ret.append(txt);
      }
    }
    ;

    _data.SetTagNames(ret.toString());
  };

  public void SaveTags() {

    int i;
    StringBuffer val = new StringBuffer();

    HashMap<String, Object> tags = new HashMap<String, Object>();
    String[] wk = _data.GetTagNames().split(",");
    for (i = 0; i < wk.length; i++) {
      tags.put(wk[i], null);
    }

    String s = "";
    String ss;
    _data.SetTags("");

    _tagmst.ClearRecord();
    _dao.list(_tagmst, new String[] { TagMasterRecord.TAG_NAME }, null, null);

    for (i = 0; i < _tagmst.RecordCount(); i++) {
      ss = _tagmst.GetString(TagMasterRecord.TAG_NAME, i, "");
      if (tags.containsKey(ss)) {
        s = String.valueOf(_tagmst.GetInt(TagMasterRecord.ID, i, -1));
        if (s.equals(""))
          continue;
        if (val.length() != 0)
          val.append(",");
        val.append(s);
      }
    }

    if (val.length() == 0) {
      _data.SetTags("");
    } else {
      _data.SetTags("," + val.toString() + ",");
    }
  }

  public boolean SaveTagName(String value) {

    for (int i = 0; i < _tagmst.RecordCount(); i++) {
      if (value.equals(_tagmst.GetString(TagMasterRecord.TAG_NAME, i, "")))
        return false;
    }

    int row = _tagmst.AddRow();

    _tagmst.SetRowId(row, null);
    _tagmst.SetString(TagMasterRecord.TAG_NAME, row, value);
    _dao.save(_tagmst, row);
    _dao.list(_tagmst, new String[] { TagMasterRecord.TAG_NAME }, null, null);
    _taglist.clear();
    return true;

  };

  public boolean SaveData() {

    int row;
    int i;

    _tran.ClearRecord();
    if (_data.isNew()) {
      row = _tran.AddRow();
      _tran.SetRowId(row, null);
    } else {
      _dao.list(_tran, null, TranRecord.ID + " = ? ", new String[] { String.valueOf(_data.GetId()) });
      if (_tran.RecordCount() != 1)
        return false;
      row = 0;
      _tran.SetRowId(row, _tran.RowId(row));
    }
    ;

    _tran.SetDouble(TranRecord.REGIST_TIME, row, new Double(_data.getDate().getTime()));
    _tran.SetDouble(TranRecord.LATITUDE, row, _data.getLatitude());
    _tran.SetDouble(TranRecord.LONGITUDE, row, _data.getLongitude());
    _tran.SetString(TranRecord.CAPTION, row, _locationtxt.getText().toString());
    _tran.SetString(TranRecord.COMMENT, row, _comment.getText().toString());
    SaveTags();
    _tran.SetString(TranRecord.TAGS, row, _data.GetTags());
    if (_data.isTweeted()){
      _tran.SetInt(TranRecord.TWEET, row,1);
    }else{
      _tran.SetInt(TranRecord.TWEET, row,0);
    }

    StringBuffer files = new StringBuffer();
    for (i = 0; i < _data.FileCount(); i++) {
      if (i != 0)
        files.append(",");
      files.append(_data.GetFile(i));
    }
    _tran.SetString(TranRecord.FILES, row, files.toString());

    for (i = 0; i < _tran.RecordCount(); i++) {
      _dao.save(_tran, i);
    }

    return true;
  }

  public void ExecTweet(){

    final LocationDataStruc data = _data;
    data.setCaption(_locationtxt.getText().toString());
    data.setComment(_comment.getText().toString());

    _contr.ClearRecord();
    _dao.list(_contr, null, null, null);
    _control = TripLogMisc.SetControlData(_contr);

    boolean flg = false;
    ArrayList<String>urllist = null;
    _tweetcomment = data.getComment();
    if (_control.getUploadCap() != 0){
      urllist = TripLogMisc.GetUrlList(_tweetcomment);
      if (urllist.size() > 0) flg = true;
    };

    if (flg){
      _dialog = ProgressDialog.show(this, "",getString(R.string.msg_url_shortlen), true);
      _dialog.setCancelable(true);
      _dialog.show();

      _urlthr = new UrlShortlenThread(_handler2,_tweetcomment,urllist);
      _urlthr.start();
    }else{
      try{
        ShowTweetDlg();
      }finally{
        _twitbtn.setEnabled(true);
      }
    }

  }

  public void ShowTweetDlg(){

    final LocationDataStruc data = _data;

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_tweet, null);
    RefreshControl();
    _twit = TwitterOAuth.getInstance(new TokenInfo(_control.getToken(),_control.getTokenSecret()));

    final TextView location = (TextView)entryView.findViewById(R.id.tweet_location);
    final EditText contents = (EditText)entryView.findViewById(R.id.tweet_contents);
    final TextView hashtag = (TextView)entryView.findViewById(R.id.tweet_tag);
    final ImageView img = (ImageView)entryView.findViewById(R.id.tweet_image);
    final LinearLayout imgarea = (LinearLayout)entryView.findViewById(R.id.tweet_image_area);
    final TextView imgsize = (TextView)entryView.findViewById(R.id.tweet_image_size);
    final Button imgbtn = (Button)entryView.findViewById(R.id.tweet_imagebtn);
    final String locationtxt = Misc.GetLATString(data.getLatitude()) + " / " + (Misc.GetLNGString(data.getLongitude()));

    if (_control.isUploadLocation()){
      location.setVisibility(View.VISIBLE);
    }else{
      location.setVisibility(View.GONE);
    }
    location.setText(locationtxt);
    _tweetlocation = true;
    location.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        _tweetlocation = !_tweetlocation;
        if (_tweetlocation){
          location.setText(locationtxt);
        }else{
          location.setText(RegistForm.this.getString(R.string.capdialog_twitter_location_off));
        }
      }
    });

    contents.setOnKeyListener(new OnKeyListener() {
      public boolean onKey(View paramView, int paramInt, KeyEvent paramKeyEvent) {
        if (paramInt == KeyEvent.KEYCODE_ENTER){
          return true;
        }
         return false;
      }
    });

    if ((_control.isUploadPic()) && (data.FileCount() > 0)){
      imgbtn.setVisibility(View.VISIBLE);
      imgarea.setVisibility(View.VISIBLE);
      _tweetimage = TripLogMisc.ShowImage(this,_bm,img, data, 0,_isDebug);
      _imageper = TripLogMisc.GetBestImagePer(this, data, _tweetimage, _control.getPicSize());
      _imagerect = TripLogMisc.GetImageRect(this, data, _tweetimage);
      if (_imagerect == null) return;
      imgsize.setText(TripLogMisc.GetImagesizeString(RegistForm.this,_imagerect,_imageper));
     }else{
      imgbtn.setVisibility(View.GONE);
      imgarea.setVisibility(View.GONE);
      _tweetimage = -1;
    }


    imgbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (imgarea.getVisibility() == View.INVISIBLE){
          imgbtn.setText(getText(R.string.capdialog_twitter_image_on).toString());
          imgarea.setVisibility(View.VISIBLE);
          _tweetimage = TripLogMisc.ShowImage(RegistForm.this,_bm,img, data, 0,_isDebug);
          _imagerect = TripLogMisc.GetImageRect(RegistForm.this, data, _tweetimage);
          if (_imagerect == null) return;
          imgsize.setText(TripLogMisc.GetImagesizeString(RegistForm.this,_imagerect,_imageper));
        }else{
          imgbtn.setText(getText(R.string.capdialog_twitter_image_off).toString());
          imgarea.setVisibility(View.INVISIBLE);
          _tweetimage = -1;
        }
      }
    });

    img.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _tweetimage = TripLogMisc.ShowImage(RegistForm.this,_bm,img, data, _tweetimage + 1,_isDebug);
        if (_control.getPicSize() == 900) _imageper = TripLogMisc.GetBestImagePer(RegistForm.this, data, _tweetimage, _control.getPicSize());
//        _imagesize = TripLogMisc.GetImageSize(RegistForm.this, data, _tweetimage);
        _imagerect = TripLogMisc.GetImageRect(RegistForm.this, data, _tweetimage);
        if (_imagerect == null) return;
        imgsize.setText(TripLogMisc.GetImagesizeString(RegistForm.this,_imagerect,_imageper));
      }
    });

    imgsize.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _imageper = _imageper - 10;
        if (_imageper < 10) _imageper = 100;
        imgsize.setText(TripLogMisc.GetImagesizeString(RegistForm.this,_imagerect,_imageper));
      }
    });

    hashtag.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        TripLogMisc.SelectHashTag(RegistForm.this,hashtag,_control);
      }
    });

    String wk = "";
    int idx = _control.getUploadCap();
    if (idx == 0){
      wk = data.getCaption();
    }else if (idx == 1){
      wk = _tweetcomment;
    }else {
      wk = data.getCaption() + " " + _tweetcomment;
    }
    contents.setText(wk.replace("\n", " "));
    contents.setSelection(contents.getText().toString().length());
    hashtag.setText(TripLogMisc.LoadDefHashTag(this, _control));

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this)
        .setIcon(R.drawable.icon_twit_32_w)
        .setTitle(R.string.capdialog_twitter_send)
        .setPositiveButton(R.string.capdialog_tweetbutton, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            _AlertDialog.dismiss();
            _dialog = ProgressDialog.show(RegistForm.this, "",getString(R.string.msg_tweet_sending), true);
//            _dialog.setCancelable(false);
            _dialog.show();

            LocationDataStruc.DeleteTemp(getApplication());
            TripLogMisc.RemoveTakePhotoFlg(getApplication());
            String image_url = "";
            if ((data.FileCount() > 0) && (_tweetimage != -1)) image_url = data.GetFile(_tweetimage);
            _tweetthr = new SendTweetThread(RegistForm.this,_twit,_handler2,contents.getText().toString(),hashtag.getText().toString(),image_url,data.getLatitude().doubleValue(), data.getLongitude().doubleValue(),_control,_tweetlocation,_imageper,_isDebug);
            _tweetthr.start();
//            String msg;
//            if (SaveData()) {
//              TripLogMisc.DeleteObject(getApplication(), DATACLASS_NAME);
//              String image_url = "";
//              if ((data.FileCount() > 0) && (_tweetimage != -1)) image_url = data.GetFile(_tweetimage);
//              _tweetthr = new SendTweetThread(RegistForm.this,_twit,_handler2,contents.getText().toString(),hashtag.getText().toString(),image_url,data.getLatitude(), data.getLongitude(),_control,_tweetlocation,_imageper);
//              _tweetthr.start();
//            } else {
//              if (_data.isNew()) {
//                msg = getResources().getText(R.string.registdata_ng).toString();
//              } else {
//                msg = getResources().getText(R.string.modifydata_ng).toString();
//              }
//              Toast.makeText(RegistForm.this, msg, Toast.LENGTH_LONG).show();
//            }
          }
        })
        .setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            _AlertDialog.dismiss();
        }
    })
    .setView(entryView).create();
    _AlertDialog.show();
  }

  class GetLocationWaitThread extends Thread {

    private Handler _handler;

    public GetLocationWaitThread(Handler hnd) {
      this._handler = hnd;
     }

    @Override
    public void run() {

      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        if (_isDebug) Log.e("run", "sleep exception");
      }
      // 終了を通知
      Message msg = new Message();
      msg.what = 3;
      msg.obj = null;
      _handler.sendMessage(msg);
    }
  };
}
