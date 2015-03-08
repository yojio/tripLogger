package jp.yojio.triplog.DBAccess;

import jp.yojio.triplog.Common.DB.record.RecordBase;

import java.io.Serializable;

public class ControlRecord extends RecordBase implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 4012216005302822880L;

  public static final String TABLE_NAME = "CONTR";
  public static final String ID = "ID";
  public static final String LOCATION_SEARCH_TYPE = "LOCATION_SEARCH_TYPE";
  public static final String TWITTER_USE = "TWITTER_USE";
  public static final String TWITTER_CHECK_OAUTH = "TWITTER_CHECK_OAUTH";
  public static final String TWITTER_ACCESS_TOKEN = "TWITTER_ACCESS_TOKEN";
  public static final String TWITTER_ACCESS_SECRET = "TWITTER_ACCESS_SECRET";
  public static final String TWITTER_UPLOAD_CAP = "TWITTER_UPLOAD_CAP";
  public static final String TWITTER_UPLOAD_PIC = "TWITTER_UPLOAD_PIC";
  public static final String TWITTER_IMAGE_SIZE = "TWITTER_IMAGE_SIZE";
  public static final String TWITTER_UPLOAD_LOCATION = "TWITTER_UPLOAD_LOCATION";
  public static final String TWITTER_HASH_TAG = "TWITTER_HASH_TAG";
  public static final String TWITTER_USER_ID = "TWITTER_USER_ID";
  public static final String TWITTER_PIC_TYPE = "TWITTER_PIC_TYPE";
  public static final String AUTO_LOCATION_TYPE = "AUTO_LOCATION_TYPE";
  public static final String ACCURACY = "ACCURACY";

  public static final String[][] COLUMNLIST = {
                                 {ID,RecordBase.DT_INTEGER,NT_NULL},
                                 {LOCATION_SEARCH_TYPE,RecordBase.DT_INTEGER,NT_NULL},
                                 {TWITTER_USE,RecordBase.DT_INTEGER,NT_NULL},
                                 {TWITTER_CHECK_OAUTH,RecordBase.DT_INTEGER,NT_NULL},
                                 {TWITTER_ACCESS_TOKEN,RecordBase.DT_TEXT,NT_NULL},
                                 {TWITTER_ACCESS_SECRET,RecordBase.DT_TEXT,NT_NULL},
                                 {TWITTER_UPLOAD_CAP,RecordBase.DT_INTEGER,NT_NULL},
                                 {TWITTER_UPLOAD_PIC,RecordBase.DT_INTEGER,NT_NULL},
                                 {TWITTER_IMAGE_SIZE,RecordBase.DT_INTEGER,NT_NULL},
                                 {TWITTER_UPLOAD_LOCATION,RecordBase.DT_INTEGER,NT_NULL},
                                 {TWITTER_HASH_TAG,RecordBase.DT_TEXT,NT_NULL},
                                 {TWITTER_USER_ID,RecordBase.DT_TEXT,NT_NULL},
                                 {TWITTER_PIC_TYPE,RecordBase.DT_INTEGER,NT_NULL},
                                 {AUTO_LOCATION_TYPE,RecordBase.DT_INTEGER,NT_NULL},
                                 {ACCURACY,RecordBase.DT_INTEGER,NT_NULL},
                                 };

  public ControlRecord() {
    super(TABLE_NAME, COLUMNLIST,ID);
  }

}
