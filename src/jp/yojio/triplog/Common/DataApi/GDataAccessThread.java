package jp.yojio.triplog.Common.DataApi;

import java.util.ArrayList;
import java.util.HashMap;

import jp.yojio.triplog.ObjectContainer;
import jp.yojio.triplog.R;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.DBAccessObject;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.DataApi.Account.AuthManager;
import jp.yojio.triplog.Common.Misc.GroupViewChildData;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.DBAccess.TranRecord;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public class GDataAccessThread extends Thread {

  private Handler _handler;
  private Context _context;
  private AuthManager _auth;
  private int _pattern;
  private String _tableid = "";
  private boolean _new = false;
  private String _tablename = "";
  private boolean _uploadimage = false;
  private ArrayList<HashMap<String, String>> _tablelist;
  private HashMap<String, String> _columninfo = new HashMap<String, String>();
  private ArrayList<GroupViewChildData> _locationlist = new ArrayList<GroupViewChildData>();
  private DBAccessObject _dao;
  private RecordBase _tran;
  private ObjectContainer _obj;
  private boolean _isdebug;

  public GDataAccessThread(int pattern, Context context,Handler hnd,AuthManager auth,boolean _isDebug) {
    this._pattern = pattern;
    this._handler = hnd;
    this._context = context;
    this._auth = auth;
    this._isdebug = _isDebug;
    setColumnInfo();
  }

  private void setColumnInfo(int resid,String type){
    _columninfo.put(_context.getString(resid), type);
  }
  private void setColumnInfo(){

    _columninfo.clear();
    setColumnInfo(R.string.fcol_id,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_regdate,Const.FT_COLUMN_TYPE_DATETIME);
    setColumnInfo(R.string.fcol_location,Const.FT_COLUMN_TYPE_LOCATION);
    setColumnInfo(R.string.fcol_locationname,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_tags,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_pic1,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_pic2,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_pic3,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_pic4,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_pic5,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_updatedate,Const.FT_COLUMN_TYPE_DATETIME);
    setColumnInfo(R.string.fcol_updateuser,Const.FT_COLUMN_TYPE_TEXT);
    setColumnInfo(R.string.fcol_datelinkcode,Const.FT_COLUMN_TYPE_TEXT);

  }

  public void SetNewTable(boolean value){
    this._new = value;
  }

  public void SetUploadImage(boolean value){
    this._uploadimage = value;
  }

  public void SetTableName(String tablename){
    this._tablename = tablename;
  }

  public void SetTableId(String tableid){
    this._tableid = tableid;
  }

  public void SetLocationData(GroupViewChildData c){
    this._locationlist.add(c);
  }

  @Override
  public void run() {

    Message msg = new Message();
    switch (this._pattern) {
    case Const.GDATA_ACCESS_TYPE_GET_TABLE_LIST:
      msg = GetTableList(msg);
      break;
    case Const.GDATA_ACCESS_TYPE_CHECK_TABLE_COLUMNS:
      msg = CheckTableColumn(msg);
      break;
    case Const.GDATA_ACCESS_TYPE_UPLOAD_DATA:
      msg = UpdateLocationData(msg);
      break;
    default:
      break;
    }
    _handler.sendMessage(msg);
  }

  // テーブルリスト取得
  private Message GetTableList(Message msg){

    _tablelist = null;
    msg.what = Const.GDATA_RETURN_GET_TABLE_LIST_NORMAL;
    try {
      _tablelist = FusionTableInterface.GetTableList(_context, _auth);
    } catch (Exception e) {
      msg.what = Const.GDATA_RETURN_GET_TABLE_LIST_ERROR;
    }
    msg.obj = _tablelist;
    return msg;

  }

  // テーブルチェック
  private Message CheckTableColumn(Message msg){

    _tablelist = null;
    boolean ret = false;
    if (this._tableid.equals("")) {
      msg.what = Const.GDATA_RETURN_CHECK_TABLE_COLUMNS_ERROR;
    }else{
      msg.what = Const.GDATA_RETURN_CHECK_TABLE_COLUMNS_NORMAL;
      try {
        _tablelist = FusionTableInterface.GetTableColumns(_context, this._tableid, _auth);
        ret = CheckTranColumncheck(_tablelist);
      } catch (Exception e) {
        msg.what = Const.GDATA_RETURN_CHECK_TABLE_COLUMNS_ERROR;
      }
    }

    msg.obj = Boolean.valueOf(ret);
    return msg;

  }

  private boolean CheckTranColumncheck(ArrayList<HashMap<String, String>>  tableinfo ){

    String type;

    for (int i=0;i<tableinfo.size();i++){
      type = _columninfo.get(tableinfo.get(i).get(Const.FT_COL_TABLE_COLNAME));
      if ((type == null) || (!type.equals(tableinfo.get(i).get(Const.FT_COL_TABLE_COLTYPE)))){
        return false;
      }
    }
    return true;

  }

  // ユニークID作成
  private String getUniqueId(long id){
    return "";
  }
  // データアップロード
  private Message UpdateLocationData(Message msg){
    _obj = ObjectContainer.getInstance(this._context.getApplicationContext());
    _dao = _obj.getDao();
    _tran = DBCommon.GetTable(_dao, DBCommon.TRAN,_isdebug);

    msg.what = Const.GDATA_RETURN_UPLOAD_DATA_OTHER_ERROR;
    try {
      // 新規テーブル作成
      if (_new){
        if ((_tablename.trim().equals("")) || (!FusionTableInterface.CreateTable(_tablename))){
          msg.what = Const.GDATA_RETURN_UPLOAD_DATA_CREATETABLE_ERROR;
          return msg;
        }
      }

      HashMap<Integer, ArrayList<String>> pict = new HashMap<Integer, ArrayList<String>>();
      int i;
      int ii;
      GroupViewChildData c;
      String wk;
      String wkCaption;
      String[] files;
      ArrayList<String> wkarr;
      // 画像のアップロード
      if (_uploadimage) {
        for (i = 0; i < _locationlist.size(); i++) {
          c = (GroupViewChildData) _locationlist.get(i);
          if (c.isPhotoflg()) {
            _tran.ClearRecord();
            _dao.list(_tran, null, TranRecord.ID + " = ?", new String[] {
              String.valueOf(c.getId())
            });
            if (_tran.RecordCount() >= 0) continue;
            wk = _tran.GetString(TranRecord.FILES, 0, "");
            if (wk.trim().equals("")) continue;
            files = wk.split(",");
            if (files.length == 0) continue;
            wkarr = new ArrayList<String>();
            for (ii = 0; ii < files.length; ii++) {
              if (files[ii].trim().equals("")) continue;
              wkCaption = c.getCaption();
              if (files.length > 1) wkCaption = wkCaption + " " + String.valueOf(ii + 1) + "/" + String.valueOf(files.length);
              wkarr.add(PicasaInterface.UploadPict(Uri.parse(files[ii]), wkCaption));
            }
            if (wkarr.size() == 0) continue;
            pict.put(Integer.valueOf(i), wkarr);
          }
        }
      }

      // データのアップロード
      ArrayList<String> retlist = new ArrayList<String>();
      boolean ret = true;
      int errcnt = 0;
      String uniqueid;
      for (i=0;i<_locationlist.size();i++){
        c = (GroupViewChildData)_locationlist.get(i);
        uniqueid = getUniqueId(c.getId());
        if (pict.containsKey(Integer.valueOf(i))){
          wkarr = pict.get(Integer.valueOf(i));
        }else{
          wkarr = null;
        }
        if (FusionTableInterface.InsertData(
            _context,
            _tableid,
            c.getId(),
            c.getDate(),
            c.getLatitude(),
            c.getLongitude(),
            c.getCaption(),
            c.getTag(),
            wkarr,
            _auth)){
          retlist.add(uniqueid);
        }else{
          retlist.add("");
          ret = false;
          errcnt++;
        };
      };
      if (!ret){
        if (errcnt == _locationlist.size()){
          msg.what = Const.GDATA_RETURN_UPLOAD_DATA_ALLERROR;
        }else{
          msg.what = Const.GDATA_RETURN_UPLOAD_DATA_ERROR;
        }
        msg.what = Const.GDATA_RETURN_UPLOAD_DATA_NORMAL;
        msg.obj = retlist;
      }
     } catch (Exception e) {
      msg.what = Const.GDATA_RETURN_UPLOAD_DATA_ERROR;
    }
    return msg;
  }
}
