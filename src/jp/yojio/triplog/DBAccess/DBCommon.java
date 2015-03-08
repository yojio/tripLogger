package jp.yojio.triplog.DBAccess;

import android.content.Context;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.DBAccessObject;
import jp.yojio.triplog.Common.DB.record.RecordBase;

public class DBCommon {

  public static String BOOKMARK = "BOOKMARK"; // 地図画面用ブックマーク
  public static String BOOKMARK_TEMP = "BOOKMARK_TEMP"; // 地図画面用ブックマーク(テンポラリ)
  public static String TRAN = "TRAN"; // トランザクションデータ
  public static String TRAN_TEMP = "TRAN_TEMP"; // トランザクションデータ(テンポラリ)
  public static String TAG = "TAG"; // タグリスト
  public static String TAG_TEMP = "TAG_TEMP"; // タグリスト(テンポラリ)
  public static String CONTR = "CONTR"; // コントロール

  private static BookmarkRecord _bookmark = new BookmarkRecord();
  private static BookmarkTempRecord _bookmark_temp = new BookmarkTempRecord();
  private static TranRecord _tran = new TranRecord();
  private static TranTempRecord _trantmp = new TranTempRecord();
  private static TagMasterRecord _tag = new TagMasterRecord();
  private static TagMasterTempRecord _tagtmp = new TagMasterTempRecord();
  private static ControlRecord _contr = new ControlRecord();

  public static DBAccessObject GetDBAccessObject(Context context){

    return new DBAccessObject(context, Const.DBNAME, new RecordBase[]{_tran,_bookmark,_tag,_contr,_trantmp,_tagtmp,_bookmark_temp});

  };

  public static RecordBase GetTable(DBAccessObject dao,String ptn,boolean isDebug){

    RecordBase ret = null;
    if (ptn.equals(BOOKMARK)){
      ret = _bookmark;
    } else if (ptn.equals(BOOKMARK_TEMP)){
      ret = _bookmark_temp;
    } else if (ptn.equals(TRAN)){
      ret = _tran;
    } else if (ptn.equals(TRAN_TEMP)){
      ret = _trantmp;
    } else if (ptn.equals(TAG)){
      ret = _tag;
    } else if (ptn.equals(TAG_TEMP)){
      ret = _tagtmp;
    } else if (ptn.equals(CONTR)){
      ret = _contr;
    }else{
      return null;
    }

    if (ret != null) {
      ret.SetDebug(isDebug);
    }
    return ret;

  }

}
