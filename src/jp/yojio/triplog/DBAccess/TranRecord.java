package jp.yojio.triplog.DBAccess;

import jp.yojio.triplog.Common.DB.record.RecordBase;

import java.io.Serializable;

public class TranRecord extends RecordBase implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 3304937822937213429L;

  public static final String TABLE_NAME = "TRAN";

  public static final String ID = "ID";
  public static final String REGIST_TIME = "REGIST_TIME";
  public static final String LATITUDE = "LATITUDE";
  public static final String LONGITUDE = "LONGITUDE";
  public static final String CAPTION = "CAPTION";
  public static final String COMMENT = "COMMENT";
  public static final String TAGS = "TAGS";
  public static final String TWEET = "TWEET";
  public static final String FILES = "FILES";
  public static final String G_UPLOAD = "G_UPLOAD";
  public static final String LINKCODE = "LINKCODE";

  public static final String[][] COLUMNLIST = {
                                 {ID,RecordBase.DT_INTEGER,NT_NULL},
                                 {REGIST_TIME,RecordBase.DT_REAL,NT_NOTNULL},
                                 {LATITUDE,RecordBase.DT_REAL,NT_NOTNULL},
                                 {LONGITUDE,RecordBase.DT_REAL,NT_NOTNULL},
                                 {CAPTION,RecordBase.DT_TEXT,NT_NULL},
                                 {COMMENT,RecordBase.DT_TEXT,NT_NULL},
                                 {TAGS,RecordBase.DT_TEXT,NT_NULL},
                                 {TWEET,RecordBase.DT_INTEGER,NT_NULL},
                                 {FILES,RecordBase.DT_TEXT,NT_NULL},
                                 {G_UPLOAD,RecordBase.DT_INTEGER,NT_NULL},
                                 {LINKCODE,RecordBase.DT_TEXT,NT_NULL}
                                 };

  public TranRecord() {
    super(TABLE_NAME, COLUMNLIST,ID);
  }

}
