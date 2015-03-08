package jp.yojio.triplog.DBAccess;

import jp.yojio.triplog.Common.DB.record.RecordBase;

import java.io.Serializable;

public class TagMasterRecord extends RecordBase implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -1789073034738234854L;

  public static final String TABLE_NAME = "TAGMST";
  public static final String ID = "ID";
  public static final String TAG_NAME = "TAG_NAME";

  public static final String[][] COLUMNLIST = {
                                 {ID,RecordBase.DT_INTEGER,NT_NULL},
                                 {TAG_NAME,RecordBase.DT_TEXT,NT_NULL}
                                 };

  public TagMasterRecord() {
    super(TABLE_NAME, COLUMNLIST,ID);
  }

}
