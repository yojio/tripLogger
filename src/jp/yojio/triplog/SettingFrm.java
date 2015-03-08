package jp.yojio.triplog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.HashMap;

import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.DBAccessObject;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Image.ImageManager;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.Common.Tweet.TokenInfo;
import jp.yojio.triplog.Common.Tweet.TwitterOAuth;
import jp.yojio.triplog.DBAccess.ControlRecord;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.DBAccess.TagMasterRecord;
import jp.yojio.triplog.DBAccess.TranRecord;
import jp.yojio.triplog.DBAccess.TranTempRecord;
import jp.yojio.triplog.misc.TripLogMisc;
import twitter4j.TwitterException;
import twitter4j.http.OAuthAuthorization;
import twitter4j.http.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingFrm extends Activity {

  private static final String DIR_DATA = "data";
  private static final String DIR_DATA_IMAGE = "data_image";
  private static final String DIR_DATA_IMAGE_FILES = "images";
  private static final String BOOKMARK_FILENAME = "bookmarkdata.data";
  private static final String TAG_FILENAME = "tagdata.data";
  private static final String TRN_FILENAME = "trandata.data";
  private static final String RETURN_CODE = "\\return\\";

  private LinearLayout _twituse;
  private LinearLayout _twitarea;
  private LinearLayout _oauth;
  private LinearLayout _uploadcap;
  private LinearLayout _upload_pic;
  private LinearLayout _upload_location;
  private LinearLayout _location_type;
  private LinearLayout _location_accuracy;
  private LinearLayout _auto_location_type;
  private LinearLayout _filearea;
  private LinearLayout _import;
  private LinearLayout _export;
  private LinearLayout _picsizearea;
  private LinearLayout _pictype;
  private LinearLayout _picsize;
  private LinearLayout _idarea;
  private LinearLayout _twitter_g;

  private ImageView _twituse_c;
  private TextView _about;
  private TextView _oauth_data;
  private TextView _uploadcap_data;
  private TextView _upload_pic_data;
  private TextView _upload_location_data;
  private TextView _location_type_data;
  private TextView _location_type_memo;
  private TextView _location_accuracy_data;
  private TextView _auto_location_type_data;
  private TextView _picsize_data;
  private TextView _pictype_data;
  private TextView _filebtn;
  private TextView _id_data;
  private TextView _import_memo;
  private TextView _export_memo;
  private TextView _twitter;

  private boolean _usetwit = true;
  private DBAccessObject _dao;
  protected ObjectContainer _obj;
  private RecordBase _contr;
  private RecordBase _trn;
  private RecordBase _tag;
  private RecordBase _bookmark;
  private RecordBase _trn_tmp;
  private RecordBase _tag_tmp;
  private RecordBase _bookmark_tmp;
  private AlertDialog _AlertDialog;
  private OAuthAuthorization _oath = null;
  private RequestToken _req = null;
  private String _sdcardpath = "";
  private String _sdcardpath_dtl = "";
  private ProgressDialog _dialog;
  private String _messagevalue = "";
  protected static boolean _isDebug;

  // UI更新ハンドラ ※別スレッドから通知を受けてUI部品を更新する
  private final Handler _handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      boolean ret;
      String msgstr;
      Object obj = msg.obj;
      _dialog.dismiss();
      switch (msg.what) {
      case 0:
        ret = ((Boolean)obj).booleanValue();
        if (ret){
          msgstr = MessageFormat.format(getString(R.string.msg_export_normalend), new Object[] {_sdcardpath_dtl});
        }else{
          if (_messagevalue.equals("")){
            msgstr = getString(R.string.msg_export_abnormalend);
          }else{
            msgstr = _messagevalue;
          }
        }
        Toast.makeText(SettingFrm.this, msgstr, Toast.LENGTH_SHORT).show();
        break;
      case 1:
        ret = ((Boolean)obj).booleanValue();
        if (ret){
          msgstr = MessageFormat.format(getString(R.string.msg_import_normalend), new Object[] {_sdcardpath_dtl});
        }else{
          if (_messagevalue.equals("")){
            msgstr = getString(R.string.msg_import_abnormalend);
          }else{
            msgstr = _messagevalue;
          }
        }
        Toast.makeText(SettingFrm.this, msgstr, Toast.LENGTH_SHORT).show();
        break;
      default:
        break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.frm_setting);

    _isDebug = Misc.isDebug(this);

    _obj = ObjectContainer.getInstance(getApplication());
    _dao = _obj.getDao();
    _contr = DBCommon.GetTable(_dao, DBCommon.CONTR,_isDebug);
    _trn = DBCommon.GetTable(_dao, DBCommon.TRAN,_isDebug);
    _tag = DBCommon.GetTable(_dao, DBCommon.TAG,_isDebug);
    _bookmark = DBCommon.GetTable(_dao, DBCommon.BOOKMARK,_isDebug);
    _trn_tmp = DBCommon.GetTable(_dao, DBCommon.TRAN_TEMP,_isDebug);
    _tag_tmp = DBCommon.GetTable(_dao, DBCommon.TAG_TEMP,_isDebug);
    _bookmark_tmp = DBCommon.GetTable(_dao, DBCommon.BOOKMARK,_isDebug);

    _location_type = (LinearLayout)findViewById(R.id.set_location_type);
    _location_type_data = (TextView)findViewById(R.id.set_location_type_data);
    _location_type_memo = (TextView)findViewById(R.id.set_location_type_memo);
    _location_accuracy = (LinearLayout)findViewById(R.id.set_location_accuracy);
    _location_accuracy_data = (TextView)findViewById(R.id.set_location_accuracy_data);
    _auto_location_type = (LinearLayout)findViewById(R.id.set_auto_location_type);
    _auto_location_type_data = (TextView)findViewById(R.id.set_auto_location_type_data);
    _twitter = (TextView)findViewById(R.id.set_twitter);
    _twitter_g = (LinearLayout)findViewById(R.id.set_twitter_group);
    _twituse = (LinearLayout)findViewById(R.id.set_twitter_use);
    _twitarea = (LinearLayout)findViewById(R.id.set_twitter_area);
    _twituse_c = (ImageView)findViewById(R.id.set_twitter_use_c);
    _oauth = (LinearLayout)findViewById(R.id.set_twitter_check_oauth);
    _oauth_data = (TextView)findViewById(R.id.set_twitter_check_oauth_data);
    _uploadcap = (LinearLayout)findViewById(R.id.set_twitter_upload_cap);
    _uploadcap_data = (TextView)findViewById(R.id.set_twitter_upload_cap_data);
    _upload_pic = (LinearLayout)findViewById(R.id.set_twitter_upload_pic);
    _upload_pic_data = (TextView)findViewById(R.id.set_twitter_upload_pic_data);
    _picsizearea = (LinearLayout)findViewById(R.id.set_twitter_upload_pic_size_area);
    _picsize = (LinearLayout)findViewById(R.id.set_twitter_upload_pic_size);
    _picsize_data = (TextView)findViewById(R.id.set_twitter_upload_pic_size_data);
    _pictype = (LinearLayout)findViewById(R.id.set_twitter_upload_pic_type);
    _pictype_data = (TextView)findViewById(R.id.set_twitter_upload_pic_type_data);
    _idarea = (LinearLayout)findViewById(R.id.set_twitter_twitter_id_area);
    _id_data = (TextView)findViewById(R.id.set_twitter_twitter_id_data);

    _upload_location = (LinearLayout)findViewById(R.id.set_twitter_upload_location);
    _upload_location_data = (TextView)findViewById(R.id.set_twitter_upload_location_data);
    _filebtn = (TextView)findViewById(R.id.set_file);
    _filearea = (LinearLayout)findViewById(R.id.set_filearea);
    _import = (LinearLayout)findViewById(R.id.set_import);
    _import_memo = (TextView)findViewById(R.id.set_import_memo);
    _export = (LinearLayout)findViewById(R.id.set_export);
    _export_memo = (TextView)findViewById(R.id.set_export_memo);
    _about = (TextView)findViewById(R.id.set_about);

    _location_type.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (_contr.GetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_HIGH) == Const.SET_LOCATION_HIGH){
          _contr.SetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_LOW);
        }else if (_contr.GetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_HIGH) == Const.SET_LOCATION_LOW){
          _contr.SetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_LOW_HIGH);
//          _contr.SetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_HIGH);
        }else{
          _contr.SetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_HIGH);
        }
        SaveContr();
      }
    });

    _location_type.setOnLongClickListener(new View.OnLongClickListener() {
      public boolean onLongClick(View v) {
        Intent intent=new Intent("android.settings.SETTINGS");
        intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        return false;
      }
    });

    _location_accuracy.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        int val = _contr.GetInt(ControlRecord.ACCURACY, 0, 0);
        if (val == 3000){
          val = 2000;
        }else if (val == 2000){
            val = 1000;
        }else if (val == 1000){
          val = 750;
        }else if (val == 750){
          val = 500;
        }else if (val == 500){
          val = 250;
        }else if (val == 250){
          val = 100;
        }else if (val == 100){
          val = 0;
        }else{
          val = 3000;
        }
        _contr.SetInt(ControlRecord.ACCURACY, 0, val);
        SaveContr();
      }
    });

    _auto_location_type.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (_contr.GetInt(ControlRecord.AUTO_LOCATION_TYPE, 0, Const.SET_AUTO_LOCATION_MANUAL) == Const.SET_AUTO_LOCATION_MANUAL){
          _contr.SetInt(ControlRecord.AUTO_LOCATION_TYPE, 0, Const.SET_AUTO_LOCATION_AUTO);
        }else{
          _contr.SetInt(ControlRecord.AUTO_LOCATION_TYPE, 0, Const.SET_AUTO_LOCATION_MANUAL);
        }
        SaveContr();
      }
    });

    _twitter_g.setVisibility(View.GONE);
    _twitter.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (_twitter_g.getVisibility() == View.GONE){
          _twitter_g.setVisibility(View.VISIBLE);
        }else{
          _twitter_g.setVisibility(View.GONE);
        }
      }
    });

    _twituse.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (_contr.GetInt(ControlRecord.TWITTER_USE, 0, Const.SET_NG) == Const.SET_OK){
          _contr.SetInt(ControlRecord.TWITTER_USE, 0, Const.SET_NG);
        }else{
          _contr.SetInt(ControlRecord.TWITTER_USE, 0, Const.SET_OK);
        }
        SaveContr();
      }
    });

    TripLogMisc.DeleteObject(getApplication(), "set_oath");
    TripLogMisc.DeleteObject(getApplication(), "set_req");
    _oauth.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (!TripLogMisc.isNetworkConnected(SettingFrm.this)){
          Toast.makeText(SettingFrm.this, getResources().getText(R.string.msg_network_connecterror), Toast.LENGTH_SHORT).show();
          return;
        }
        _AlertDialog = new AlertDialog.Builder(SettingFrm.this)
        .setTitle(R.string.capdialog_twitter_oauth)
        .setMessage(R.string.capdialog_twitter_oauth_msg)
        .setIcon(R.drawable.icon_twit_32_w)
        .setPositiveButton(getString(R.string.capdialog_yesbutton),new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // 認証情報初期化
            _contr.SetInt(ControlRecord.TWITTER_CHECK_OAUTH, 0, Const.SET_NG);
            _contr.SetString(ControlRecord.TWITTER_ACCESS_TOKEN, 0, "");
            _contr.SetString(ControlRecord.TWITTER_ACCESS_SECRET, 0, "");
            SaveContr();
            DispContr();
            Misc.setEnvValue(getApplication(), Const.OAUTH_TAG, 1);
            _oath = TwitterOAuth.getOath();
            try {
              _req = TwitterOAuth.GetRequestToken(_oath);
            } catch (TwitterException e) {
              return;
            }
            SaveObject();
            _AlertDialog.dismiss();
            TwitterOAuth.doOauth(SettingFrm.this,_req);
          }
        })
        .setNegativeButton(getString(R.string.capdialog_nobutton), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            _AlertDialog.dismiss();
          }
        })
        .create();
        _AlertDialog.show();
      }
    });
    _uploadcap.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        int idx = _contr.GetInt(ControlRecord.TWITTER_UPLOAD_CAP, 0, Const.SET_CAP_COMMENT);
        if (idx == Const.SET_CAP_LOCATION){
          _contr.SetInt(ControlRecord.TWITTER_UPLOAD_CAP, 0, Const.SET_CAP_COMMENT);
        }else if (idx == Const.SET_CAP_COMMENT){
            _contr.SetInt(ControlRecord.TWITTER_UPLOAD_CAP, 0, Const.SET_CAP_FULL);
        }else{
          _contr.SetInt(ControlRecord.TWITTER_UPLOAD_CAP, 0, Const.SET_CAP_LOCATION);
        }
        SaveContr();
      }
    });
    _upload_pic.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (_contr.GetInt(ControlRecord.TWITTER_UPLOAD_PIC, 0, Const.SET_NG) == Const.SET_OK){
          _contr.SetInt(ControlRecord.TWITTER_UPLOAD_PIC, 0, Const.SET_NG);
          _picsizearea.setVisibility(View.VISIBLE);
        }else{
          _contr.SetInt(ControlRecord.TWITTER_UPLOAD_PIC, 0, Const.SET_OK);
          _picsizearea.setVisibility(View.VISIBLE);
        }
        SaveContr();
      }
    });

    _pictype.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        int val = _contr.GetInt(ControlRecord.TWITTER_PIC_TYPE, 0, 0);
        _idarea.setVisibility(View.GONE);
        if (val == 0){
          val = 1;
          _idarea.setVisibility(View.VISIBLE);
          _id_data.setText(_contr.GetString(ControlRecord.TWITTER_USER_ID, 0, ""));
        }else if (val == 1){
          val = 2;
        }else if (val == 2){
          val = 3;
        }else if (val == 3){
          val = 4;
        }else if (val == 4){
          val = 5;
        }else{
          val = 0;
        }
        _contr.SetInt(ControlRecord.TWITTER_PIC_TYPE, 0, val);
        SaveContr();
      }
    });

    _idarea.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        InputUserId();
      }
    });

    _picsize.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        int val = _contr.GetInt(ControlRecord.TWITTER_IMAGE_SIZE, 0, 100);

        if (val == 900){
          val = 100;
          _picsize_data.setText(String.valueOf(val) + "%");
        }else{
          val = val - 10;
          if (val < 10) val = 900;
          _picsize_data.setText(getString(R.string.setcap_upload_pic_size_data));
        }
        _contr.SetInt(ControlRecord.TWITTER_IMAGE_SIZE, 0, val);
        SaveContr();
      }
    });

    _upload_location.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (_contr.GetInt(ControlRecord.TWITTER_UPLOAD_LOCATION, 0, Const.SET_NG) == Const.SET_OK){
          _contr.SetInt(ControlRecord.TWITTER_UPLOAD_LOCATION, 0, Const.SET_NG);
        }else{
          _contr.SetInt(ControlRecord.TWITTER_UPLOAD_LOCATION, 0, Const.SET_OK);
        }
        SaveContr();
      }
    });

    _filebtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (_filearea.getVisibility() == View.VISIBLE){
          _filearea.setVisibility(View.GONE);
        }else{
          _filearea.setVisibility(View.VISIBLE);
        }
      }
    });

    _import.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        importExportData(false);
      }
    });

    _export.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        importExportData(true);
      }
    });

    LoadObject();

    _sdcardpath = Environment.getExternalStorageDirectory().getPath() + File.separator + getPackageName();
    String msg = MessageFormat.format(getString(R.string.setcap_dlg_import_path), new Object[] {_sdcardpath});
    _import_memo.setText(msg);
    msg = MessageFormat.format(getString(R.string.setcap_dlg_export_path), new Object[] {_sdcardpath});
    _export_memo.setText(msg);

    _about.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".AboutFrm");
        startActivity(intent);
      }
    });

    DispContr();

  };

  private void SaveContr(){

    _dao.save(_contr, 0);
    DispContr();
  };

  private void DispTwitArea(){
    if (_usetwit){
      _twituse_c.setVisibility(View.VISIBLE);
      _twitarea.setVisibility(View.VISIBLE);
    }else{
      _twituse_c.setVisibility(View.INVISIBLE);
      _twitarea.setVisibility(View.GONE);
    }
  }

  private void DispContr(){

    int idx = 0;
    int val = 0;

    _contr.ClearRecord();
    _dao.list(_contr, null, null, null);
    if (_contr.RecordCount() == 0){
      _contr.AddRow();
      _contr.SetInt(ControlRecord.AUTO_LOCATION_TYPE, 0, Const.SET_AUTO_LOCATION_MANUAL);
      _contr.SetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_HIGH);
      _contr.SetInt(ControlRecord.TWITTER_USE, 0, Const.SET_NG);
      _contr.SetInt(ControlRecord.TWITTER_CHECK_OAUTH, 0, Const.SET_NG);
      _contr.SetString(ControlRecord.TWITTER_ACCESS_TOKEN, 0, "");
      _contr.SetString(ControlRecord.TWITTER_ACCESS_SECRET, 0, "");
      _contr.SetInt(ControlRecord.TWITTER_UPLOAD_CAP, 0, Const.SET_CAP_COMMENT);
      _contr.SetInt(ControlRecord.TWITTER_UPLOAD_PIC, 0, Const.SET_NG);
      _contr.SetInt(ControlRecord.TWITTER_IMAGE_SIZE, 0, 900);
      _contr.SetInt(ControlRecord.TWITTER_UPLOAD_LOCATION, 0, Const.SET_NG);
      _contr.SetString(ControlRecord.TWITTER_USER_ID, 0, "");
      _contr.SetInt(ControlRecord.TWITTER_PIC_TYPE, 0, 0);
      _contr.SetInt(ControlRecord.ACCURACY, 0, 0);
    }
    _contr.SetString(ControlRecord.TWITTER_HASH_TAG, 0, getString(R.string.setcap_hash_tag));

    idx = _contr.GetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_HIGH);
    if (idx == Const.SET_LOCATION_HIGH){
      _location_type_data.setText(getString(R.string.setcap_location_data_fine));
      _location_type_memo.setText(getString(R.string.setcap_location_type_memo_fine));
    }else if (idx == Const.SET_LOCATION_LOW){
      _location_type_data.setText(getString(R.string.setcap_location_data_low));
      _location_type_memo.setText(getString(R.string.setcap_location_type_memo_low));
    }else{
      _location_type_data.setText(getString(R.string.setcap_location_data_low_fine));
      _location_type_memo.setText(getString(R.string.setcap_location_type_memo_low_fine));
    }

    idx = _contr.GetInt(ControlRecord.ACCURACY, 0, 0);
    String msg;
    if (idx == 0){
      _location_accuracy_data.setText(getString(R.string.setcap_location_accuracy_unlimited));
    }else if (idx >= 1000){
      msg = MessageFormat.format(getString(R.string.setcap_location_accuracy_unit), new Object[] {String.valueOf(idx / 1000) + "km"});
      _location_accuracy_data.setText(msg);
    }else{
      msg = MessageFormat.format(getString(R.string.setcap_location_accuracy_unit), new Object[] {String.valueOf(idx) + "m"});
      _location_accuracy_data.setText(msg);
    }

    idx = _contr.GetInt(ControlRecord.AUTO_LOCATION_TYPE, 0, Const.SET_AUTO_LOCATION_MANUAL);
    if (idx == Const.SET_AUTO_LOCATION_MANUAL){
      _auto_location_type_data.setText(getString(R.string.setcap_auto_location_data_manual));
    }else{
      _auto_location_type_data.setText(getString(R.string.setcap_auto_location_data_auto));
    }

    idx = _contr.GetInt(ControlRecord.TWITTER_USE, 0, Const.SET_NG);
    _usetwit = (idx == Const.SET_OK);
    DispTwitArea();

    idx = _contr.GetInt(ControlRecord.TWITTER_CHECK_OAUTH, 0, Const.SET_NG);
    if (idx == Const.SET_OK){
      _oauth_data.setText(getString(R.string.setcap_oauth_data_ok));
    }else{
      _oauth_data.setText(getString(R.string.setcap_oauth_data_ng));
    }

    idx = _contr.GetInt(ControlRecord.TWITTER_UPLOAD_CAP, 0, Const.SET_CAP_COMMENT);
    if (idx == Const.SET_CAP_LOCATION){
      _uploadcap_data.setText(getString(R.string.setcap_contents_data_location));
    }else if (idx == Const.SET_CAP_COMMENT){
      _uploadcap_data.setText(getString(R.string.setcap_contents_data_comment));
    }else{
      _uploadcap_data.setText(getString(R.string.setcap_contents_data_full));
    }

    val = _contr.GetInt(ControlRecord.TWITTER_IMAGE_SIZE, 0, 900);
    idx = _contr.GetInt(ControlRecord.TWITTER_UPLOAD_PIC, 0, Const.SET_NG);
    if (idx == Const.SET_OK){
      _upload_pic_data.setText(getString(R.string.setcap_upload_pic_data_ok));
      _picsizearea.setVisibility(View.VISIBLE);
      if (val == 900){
        _picsize_data.setText(getString(R.string.setcap_upload_pic_size_data));
      }else{
        _picsize_data.setText(String.valueOf(val) + "%");
      }
      val = _contr.GetInt(ControlRecord.TWITTER_PIC_TYPE, 0, 0);
      _idarea.setVisibility(View.GONE);
      if (val == 0){
        _pictype_data.setText("Twitpic");
      }else if (val == 2){
        _pictype_data.setText("Img.ly");
      }else if (val == 3){
        _pictype_data.setText("Plixi");
      }else if (val == 4){
        _pictype_data.setText("twipple");
      }else if (val == 5){
        _pictype_data.setText("Twitgoo");
      }else{
        _pictype_data.setText("Yfrog");
        _idarea.setVisibility(View.VISIBLE);
      }
    }else{
      _upload_pic_data.setText(getString(R.string.setcap_upload_pic_data_ng));
      _picsizearea.setVisibility(View.GONE);
    }
    _id_data.setText(_contr.GetString(ControlRecord.TWITTER_USER_ID, 0, ""));

    idx = _contr.GetInt(ControlRecord.TWITTER_UPLOAD_LOCATION, 0, Const.SET_NG);
    if (idx == Const.SET_OK){
      _upload_location_data.setText(getString(R.string.setcap_upload_location_data_ok));
    }else{
      _upload_location_data.setText(getString(R.string.setcap_upload_location_data_ng));
    }

  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    TokenInfo info = TwitterOAuth.GetTokenInfo(intent,_oath,_req);
    if (info != null){
      _contr.SetInt(ControlRecord.TWITTER_CHECK_OAUTH, 0, Const.SET_OK);
      _contr.SetString(ControlRecord.TWITTER_ACCESS_TOKEN, 0, info.token);
      _contr.SetString(ControlRecord.TWITTER_ACCESS_SECRET, 0, info.tokenSecret);
      SaveContr();
      TripLogMisc.DeleteObject(getApplication(), "open_twitter");
    }
  }

  public void SaveObject(){
    TripLogMisc.SaveObject(getApplication(), "set_oath", _oath);
    TripLogMisc.SaveObject(getApplication(), "set_req", _req);
    TripLogMisc.SaveObject(getApplication(), "open_twitter", new Integer(_twitter_g.getVisibility()));
  }

  public void LoadObject() {
    _oath = (OAuthAuthorization) TripLogMisc.LoadObject(getApplication(), "set_oath",true);
    if (_isDebug) Log.e("LoadObject","_oath");
    boolean b = (Misc.getEnvValueInt(getApplication(), Const.OAUTH_TAG) != 0);
    Misc.DeleteEnvValue(getApplication(), Const.OAUTH_TAG);
    if ((_oath == null) && (b)) {
      if (_isDebug) Log.e("LoadObject","_oath=null");
      _oath = TwitterOAuth.getOath();
    }

    _req = (RequestToken) TripLogMisc.LoadObject(getApplication(), "set_req",true);
    if (_isDebug) Log.e("LoadObject","_req");
    if ((_req == null) && (b)) {
      if (_isDebug) Log.e("LoadObject","_req=null");
      try {
        _req = TwitterOAuth.GetRequestToken(_oath);
      } catch (TwitterException e) {
      }
    }

    Integer twtterg = (Integer) TripLogMisc.LoadObject(getApplication(), "open_twitter",true);
    if (_isDebug) Log.e("LoadObject","twtterg");
    if (twtterg == null) {
      if (_isDebug) Log.e("LoadObject","twtterg=null");
      twtterg = Integer.valueOf(View.GONE);
    }
    _twitter_g.setVisibility(twtterg.intValue());
  }

  public boolean InputUserId() {
    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_inputuserid, null);

    final EditText txt = (EditText) entryView.findViewById(R.id.dlg_inputuserid);

    // データの設定
    txt.setText(_contr.GetString(ControlRecord.TWITTER_USER_ID, 0, ""));

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this)
      .setTitle(R.string.capdialog_twitter_userid)
      .setPositiveButton(R.string.capdialog_registbutton, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
          _contr.SetString(ControlRecord.TWITTER_USER_ID, 0, txt.getText().toString());
          SaveContr();
        }
      })
      .setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
          _AlertDialog.dismiss();
        }
      })
      .setView(entryView)
      .create();
    _AlertDialog.show();

    return true;
  }

  public void importExportData(final boolean IsExport){

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_importexportconf, null);
    Button execute = (Button)entryView.findViewById(R.id.set_ie_execute);
    execute.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _AlertDialog.dismiss();
        importExportData(false,IsExport);
      }
    });

    Button executeimage = (Button)entryView.findViewById(R.id.set_ie_execute_with_image);
    executeimage.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _AlertDialog.dismiss();
        importExportData(true,IsExport);
      }
    });

    // AlertDialog成
    int iconid = R.drawable.icon_export_32_w;
    int titleid = R.string.capdialog_export_data;
    if (!IsExport){
      iconid = R.drawable.icon_import_32_w;
      titleid = R.string.capdialog_import_data;
    }
    _AlertDialog = new AlertDialog.Builder(this)
      .setIcon(iconid)
      .setTitle(titleid)
      .setView(entryView)
      .create();
    _AlertDialog.show();


  }

  public void importExportData(boolean withimage,boolean IsExport) {

    _messagevalue = "";

    String msg;
    if (IsExport){
      msg = getString(R.string.msg_export_processing);
    }else{
      msg = getString(R.string.msg_import_processing);
    }
    _dialog = new ProgressDialog(this);
    _dialog.setTitle("");
    _dialog.setMessage(msg);
    _dialog.setIndeterminate(false);
    if (IsExport){
      _dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }else{
      _dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
    _dialog.incrementProgressBy(0);
    _dialog.incrementSecondaryProgressBy(0);
    _dialog.setCancelable(true);
    _dialog.setMax(0);
    _dialog.show();

    ImportExportThread exthr = new ImportExportThread(_handler, withimage,IsExport);
    exthr.start();
  }

  class ImportExportThread extends Thread {

    private Handler _handler;
    private boolean _withimage;
    private boolean _IsExport;

    public ImportExportThread(Handler hnd,boolean withimage,boolean isExport) {
      this._handler = hnd;
      this._withimage = withimage;
      this._IsExport = isExport;
    }

    @Override
    public void run() {

      boolean ret = true;
      int exectype = 0;
      if (_withimage){
        _sdcardpath_dtl = _sdcardpath + File.separator + DIR_DATA_IMAGE;
      }else{
        _sdcardpath_dtl = _sdcardpath + File.separator + DIR_DATA;
      }

      try {
        if (_IsExport){
          exectype = 0;
          ret = ExportData();
        }else{
          exectype = 1;
          ret = ImportData();
        }
      } catch (Exception e) {
        ret = false;
      }

      // 終了を通知
      Message msg = new Message();
      msg.what = exectype;
      msg.obj = new Boolean(ret);
      _handler.sendMessage(msg);
    }

    private boolean ExportData(){

      _messagevalue = "";
      int i;
      int ii;
      int cnt = 0;
      HashMap<String, String> taghash = new HashMap<String, String>();


      try{
        _dao.list(_bookmark);
        _dao.list(_tag);
        _dao.list(_trn);
        _dialog.setMax(_bookmark.RecordCount() + _tag.RecordCount() + _trn.RecordCount());
        _dialog.setProgress(cnt);

        if (_trn.RecordCount() == 0){
          _messagevalue = getString(R.string.msg_export_abnormalend_nothing);
          return false;
        }

        // フォルダの作成
        File dir = new File(_sdcardpath_dtl);
        TripLogMisc.delete(dir);
        dir.mkdirs();

        if ((!dir.isDirectory()) && (!dir.canWrite())){
          return false;
        }

        File dirimg;
        if (_withimage){
          dirimg = new File(dir,DIR_DATA_IMAGE_FILES);
          if (!dirimg.exists()){
            dirimg.mkdirs();
          }
          if ((!dirimg.isDirectory()) && (!dirimg.canWrite())){
            return false;
          }
        }

        // ブックマークの保存
        File data = new File(dir,BOOKMARK_FILENAME);
        if (data.exists()) data.delete();

        BufferedWriter bw = null;
        FileOutputStream file = new FileOutputStream(data, true);
        bw = new BufferedWriter(new OutputStreamWriter(file, "UTF-8"));
        bw.append(_bookmark.GetColumnString()).append("\n");
        for (i = 0;i<_bookmark.RecordCount();i++){
          bw.append(_bookmark.GetDataString(i)).append("\n");
          _dialog.setProgress(cnt++);
        }
        bw.close();

        // タグマスタの保存
        data = new File(dir,TAG_FILENAME);
        if (data.exists()) data.delete();

        bw = null;
        file = new FileOutputStream(data, true);
        bw = new BufferedWriter(new OutputStreamWriter(file, "UTF-8"));
        bw.append(_tag.GetColumnString()).append("\n");
        for (i = 0;i<_tag.RecordCount();i++){
          bw.append(_tag.GetDataString(i)).append("\n");
          taghash.put(String.valueOf(_tag.GetInt(TagMasterRecord.ID, i, -1)), _tag.GetString(TagMasterRecord.TAG_NAME, i, ""));
          _dialog.setProgress(cnt++);
        }
        bw.close();

        // トランザクションの保存
        File src;
        File dst;
        data = new File(dir,TRN_FILENAME);
        if (data.exists()) data.delete();

        String wk = "";
        String tagval = "";
        StringBuffer tagret = new StringBuffer();
        String[] taglst;
        String wktag = "";
        StringBuffer wkfiles = new StringBuffer();
        String imagefile;
        String imagefiledest;
        String[] imagefiles;
        file = new FileOutputStream(data, true);
        bw = new BufferedWriter(new OutputStreamWriter(file, "UTF-8"));
        bw.append(_trn.GetColumnString()).append("\n");
        for (i = 0;i<_trn.RecordCount();i++){
          // タグ変換
          tagret.setLength(0);
          tagval = _trn.GetString(TranRecord.TAGS, i, "");
          if (!tagval.trim().equals("")){
            taglst = tagval.split(",");
            for (ii = 0;ii<taglst.length;ii++){
              if (taglst[ii].trim().equals("")) continue;
              wktag = taghash.get(taglst[ii]);
              if (wktag == null) continue;
              tagret.append(",'").append(wktag).append("'");
            }
            if (tagret.length() != 0) tagret.append(",");
          }
          _trn.SetString(TranRecord.TAGS, i, tagret.toString());

          if (_withimage){
            wkfiles.setLength(0);
            wk = _trn.GetString(TranRecord.FILES, i, "");
            if (wk.trim().equals("")) continue;
            imagefiles = wk.split(",");
            for (ii = 0;ii < imagefiles.length;ii++){
              imagefile = TripLogMisc.GetFileName(SettingFrm.this,imagefiles[ii]);
              if (imagefile.trim().equals("")) continue;
              src = new File(imagefile);
              imagefiledest = _sdcardpath_dtl + File.separator  + DIR_DATA_IMAGE_FILES + File.separator + src.getName();
              dst = new File(imagefiledest);
              TripLogMisc.copyFile(src,dst);
              if (wkfiles.length() != 0) wkfiles.append(",");
              wkfiles.append(imagefiledest);
            };
            _trn.SetString(TranRecord.FILES, i,wkfiles.toString());
          };
          _trn.SetString(TranRecord.CAPTION, i,_trn.GetString(TranRecord.CAPTION, i, "").replace("\n", RETURN_CODE));
          _trn.SetString(TranRecord.COMMENT, i,_trn.GetString(TranRecord.COMMENT, i, "").replace("\n", RETURN_CODE));
          bw.append(_trn.GetDataString(i)).append("\n");
          _dialog.setProgress(cnt++);
        }
        bw.close();

        dir = null;
      }catch (Exception e){
        if (_isDebug) Log.e("ExportData",e.getMessage());
        return false;
      }

      return true;
    }

    private boolean SetData(RecordBase rec, String[] collist,String data){

      if (data.length() > 2) data = data.substring(1, data.length() - 1); // 前後の"を除去
      return SetData(rec,collist,data.split("\"" + RecordBase.DATA_SEPARATOR + "\""));

    }

    private boolean SetData(RecordBase rec, String[] collist,String[] datalist){

      int idx = rec.AddRow();
      String colname;
      String data;
      int cnt = collist.length;
      int datalen = datalist.length;
      for (int i = 0;i<cnt;i++){
        colname = collist[i];
        if (datalen <= i) break;
        data = datalist[i];
        if (rec.ColumnType(colname) == RecordBase.DT_INTEGER){
          rec.SetInt(colname, idx, new Integer(data).intValue());
        } else if (rec.ColumnType(colname) == RecordBase.DT_REAL){
          rec.SetDouble(colname, idx, new Double(data));
        } else if (rec.ColumnType(colname) == RecordBase.DT_TEXT){
          rec.SetString(colname, idx, data.replace(RETURN_CODE, "\n"));
        }
      }

      return true;

    }

    private boolean ImportData(){

      int i;
      int ii;
      int cnt = 0;
      String[] files;
      String wk;
      StringBuffer filestr = new StringBuffer();
      HashMap<String, String> taghash = new HashMap<String, String>();

      try{
        _dao.delete_with_cond(_bookmark_tmp,null,null);
        _dao.delete_with_cond(_tag_tmp,null,null);
        _dao.delete_with_cond(_trn_tmp,null,null);

        // フォルダの存在チェック
        File dir = new File(_sdcardpath_dtl);
        if ((!dir.exists()) || (!dir.isDirectory())){
          _messagevalue = MessageFormat.format(getString(R.string.msg_import_abnormalend_dir), new Object[] {_sdcardpath_dtl});
          return false;
        }

        // フォルダ存在チェック
        File dirimg;
        if (_withimage){
          dirimg = new File(dir,DIR_DATA_IMAGE_FILES);
          if ((!dirimg.exists()) || (!dirimg.isDirectory())){
            _messagevalue = MessageFormat.format(getString(R.string.msg_import_abnormalend_dir), new Object[] {dirimg.getPath()});
            return false;
          }
        }

        // ファイル存在チェック
        File bookmark = new File(dir,BOOKMARK_FILENAME);
        if ((!bookmark.exists()) || (!bookmark.isFile())){
          _messagevalue = MessageFormat.format(getString(R.string.msg_import_abnormalend_file), new Object[] {bookmark.getPath()});
          return false;
        }

        // ファイル存在チェック
        File tag = new File(dir,TAG_FILENAME);
        if ((!tag.exists()) || (!tag.isFile())){
          _messagevalue = MessageFormat.format(getString(R.string.msg_import_abnormalend_file), new Object[] {tag.getPath()});
          return false;
        }

        // ファイル存在チェック
        File trn = new File(dir,TRN_FILENAME);
        if ((!trn.exists()) || (!trn.isFile())){
          _messagevalue = MessageFormat.format(getString(R.string.msg_import_abnormalend_file), new Object[] {trn.getPath()});
          return false;
        }

        // データ読込み実行
        cnt = 0;
        String[] collst = null;

        // ブックマーク
        _bookmark_tmp.ClearRecord();

        try{
          BufferedReader br = new BufferedReader(new FileReader(bookmark));

          String str;
          while((str = br.readLine()) != null){
            if (cnt == 0){
              collst = str.split(RecordBase.DATA_SEPARATOR);
            }else{
              SetData(_bookmark_tmp,collst,str);
            }
            cnt++;
          }
          br.close();

          _dao.save_all(_bookmark_tmp);


        }catch(FileNotFoundException e){
          if (_isDebug) Log.e("bookmark_FileNotFoundException",e.getMessage());
          return false;
        }catch(IOException e){
          if (_isDebug) Log.e("bookmark_IOException",e.getMessage());
          return false;
        }


        // タグ
        cnt = 0;
        _tag_tmp.ClearRecord();

        try{
          BufferedReader br = new BufferedReader(new FileReader(tag));

          String str;
          while((str = br.readLine()) != null){
            if (cnt == 0){
              collst = str.split(RecordBase.DATA_SEPARATOR);
            }else{
              SetData(_tag_tmp,collst,str);
            }
            cnt++;
          }
          br.close();

          _dao.save_all(_tag_tmp);

        }catch(FileNotFoundException e){
          if (_isDebug) Log.e("tag_FileNotFoundException",e.getMessage());
          return false;
        }catch(IOException e){
          if (_isDebug) Log.e("tag_IOException",e.getMessage());
          return false;
        }


        // トランザクション
        cnt = 0;
        _trn_tmp.ClearRecord();

        try{
          BufferedReader br = new BufferedReader(new FileReader(trn));

          String str;
          while((str = br.readLine()) != null){
            if (cnt == 0){
              collst = str.split(RecordBase.DATA_SEPARATOR);
            }else{
              SetData(_trn_tmp,collst,str);
            }
            cnt++;
          }
          br.close();

          if (_trn_tmp.RecordCount() == 0){
            _messagevalue = getString(R.string.msg_import_abnormalend_nothing);
            return false;
          }

          File imagefile;
          Uri uri;

          if (_withimage){
            for (i = 0;i<_trn_tmp.RecordCount();i++){
              wk = _trn_tmp.GetString(TranTempRecord.FILES, i, "");
              if (wk.trim().equals("")) continue;
              files = wk.split(",");
              filestr.setLength(0);
              cnt = 0;
              for (ii = 0;ii<files.length;ii++){
                imagefile = new File(files[ii]);
                if ((!imagefile.exists()) || (!imagefile.isFile())) continue;
                uri = ImageManager.addImageAsCamera(getContentResolver(), imagefile.getPath());
                if (cnt != 0) filestr.append(",");
                filestr.append(uri.toString());
                imagefile = null;
                cnt++;
              }
              _trn_tmp.SetString(TranTempRecord.FILES, i, filestr.toString());
            }
          }

          _dao.save_all(_trn_tmp);

        }catch(FileNotFoundException e){
          if (_isDebug) Log.e("trn_FileNotFoundException",e.getMessage());
          return false;
        }catch(IOException e){
          if (_isDebug) Log.e("trn_IOException",e.getMessage());
          return false;
        }

        // データの入れ替え
        try{
          _dao.delete_with_cond(_bookmark, null, null);
          _dao.delete_with_cond(_tag, null, null);
          _dao.delete_with_cond(_trn, null, null);

          _dao.updateSQL("INSERT INTO " + _bookmark.TableName() + " SELECT * FROM " + _bookmark_tmp.TableName() + ";");
          _dao.updateSQL("INSERT INTO " + _tag.TableName() + " SELECT * FROM " + _tag_tmp.TableName() + ";");
          _dao.updateSQL("INSERT INTO " + _trn.TableName() + " SELECT * FROM " + _trn_tmp.TableName() + ";");

          // タグの翻訳
          _tag.ClearRecord();
          _dao.list(_tag);
          for (i=0;i<_tag.RecordCount();i++){
            taghash.put(_tag.GetString(TagMasterRecord.TAG_NAME, i, ""),String.valueOf(_tag.GetInt(TagMasterRecord.ID, i, -1)));
          }

          _trn.ClearRecord();
          _dao.list(_trn);

          // タグ変換
          String tagval = "";
          StringBuffer tagret = new StringBuffer();
          String[] taglst;
          String wktag = "";

          for (i=0;i<_trn.RecordCount();i++){
            tagret.setLength(0);
            tagval = _trn.GetString(TranTempRecord.TAGS,i, "");
            if (!tagval.trim().equals("")){
              tagval = "'" + tagval + "'";
              taglst = tagval.split("','");

              for (ii = 0;ii<taglst.length;ii++){
                if (taglst[ii].trim().equals("")) continue;
                wktag = taghash.get(taglst[ii]);
                if (wktag == null) continue;
                tagret.append(",").append(wktag);
              }
              if (tagret.length() != 0) tagret.append(",");
              _trn.SetString(TranRecord.TAGS, i, tagret.toString());
            }
         }
          _dao.save_all(_trn);

        }catch(Exception e){

        }


      }catch (Exception e){
        if (_isDebug) Log.e("ImportData",e.getMessage());
        return false;
      }

      return true;
    }
  }


}
