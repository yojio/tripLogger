package jp.yojio.triplog.DBAccess;

import jp.yojio.triplog.Common.DB.record.RecordBase;

import java.io.Serializable;

public class BookmarkRecord extends RecordBase implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -1789073034738234854L;

  public static final String TABLE_NAME = "BOOKMARK";
  public static final String ID = "ID";
  public static final String CAPTION = "CAPTION";
  public static final String LATITUDE = "LATITUDE";
  public static final String LONGITUDE = "LONGITUDE";

  public static final String[][] COLUMNLIST = {
                                 {ID,RecordBase.DT_INTEGER,NT_NULL},
                                 {CAPTION,RecordBase.DT_TEXT,NT_NULL},
                                 {LATITUDE,RecordBase.DT_REAL,NT_NULL},
                                 {LONGITUDE,RecordBase.DT_REAL,NT_NULL}
                                 };

  public BookmarkRecord() {
    super(TABLE_NAME, COLUMNLIST,ID);
  }

}
