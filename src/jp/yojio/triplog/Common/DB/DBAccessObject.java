package jp.yojio.triplog.Common.DB;

import jp.yojio.triplog.Common.DB.record.RecordBase;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * BizCard用データアクセスクラス
 */
public class DBAccessObject {

  private DatabaseOpenHelper helper = null;

  public DBAccessObject(Context context, String DB_NAME, RecordBase[] tables) {

    int vno = 1;
    PackageManager pm = context.getPackageManager();
    try {
      PackageInfo info = null;
      info = pm.getPackageInfo(context.getPackageName(), 0);
      vno = info.versionCode;
    } catch (NameNotFoundException e) {
    }
    helper = new DatabaseOpenHelper(context, DB_NAME, tables,vno);

  }

  /**
   * データの保存 rowidがnullの場合はinsert、rowidが!nullの場合はupdate
   *
   * @param TestRecord
   *          保存対象のオブジェクト
   * @return 保存結果
   */
  public boolean save(RecordBase rec, int row) {

    if (rec.DataStatus(row).equals(RecordBase.DS_NORMAL))
      return true;

    SQLiteDatabase db = helper.getWritableDatabase();
    try {

      ContentValues values = new ContentValues();
      for (int i = 0; i < rec.ColumnCount(); i++) {
        if ((rec.PKColIndex() == i) && (rec.IsAutoNo())) continue;
        if (rec.ColumnType(i).equals(RecordBase.DT_INTEGER)) {
          values.put(rec.ColumnName(i), new Integer(rec.GetInt(i, row, 0)));
        } else if (rec.ColumnType(i).equals(RecordBase.DT_REAL)) {
          values.put(rec.ColumnName(i), rec.GetDouble(i, row, new Double(i)));
        } else if (rec.ColumnType(i).equals(RecordBase.DT_TEXT)) {
          values.put(rec.ColumnName(i), rec.GetString(i, row, ""));
        }
      }
      if (rec.DataStatus(row).equals(RecordBase.DS_ADDED)) {
        long rowid = db.insert(rec.TableName(), null, values);
        rec.SetRowId(row, new Long(rowid));
      } else {
        db.update(rec.TableName(), values, rec.PKColName() + "=?",
            new String[] {rec.GetValue(rec.PKColName(), row, "")});
      }
      db.execSQL("vacuum;");
    } finally {
      db.close();
    }
    return true;
  }

  public boolean save_all(RecordBase rec) {

    SQLiteDatabase db = helper.getWritableDatabase();
    try {

      ContentValues values = new ContentValues();
      for (int row = 0;row < rec.RecordCount();row++){
        if (rec.DataStatus(row).equals(RecordBase.DS_NORMAL)) continue;

        for (int i = 0; i < rec.ColumnCount(); i++) {
          if ((rec.PKColIndex() == i) && (rec.IsAutoNo())) continue;
          if (rec.ColumnType(i).equals(RecordBase.DT_INTEGER)) {
            values.put(rec.ColumnName(i), new Integer(rec.GetInt(i, row, 0)));
          } else if (rec.ColumnType(i).equals(RecordBase.DT_REAL)) {
            values.put(rec.ColumnName(i), rec.GetDouble(i, row, new Double(i)));
          } else if (rec.ColumnType(i).equals(RecordBase.DT_TEXT)) {
            values.put(rec.ColumnName(i), rec.GetString(i, row, ""));
          }
        }
        if (rec.DataStatus(row).equals(RecordBase.DS_ADDED)) {
          long rowid = db.insert(rec.TableName(), null, values);
          rec.SetRowId(row, new Long(rowid));
        } else {
          db.update(rec.TableName(), values, rec.PKColName() + "=?", new String[] {rec.GetValue(rec.PKColName(), row, "")});
        }
      }
      db.execSQL("vacuum;");
    } finally {
      db.close();
    }
    return true;
  }

  public void updateSQL(String SQL) {
    SQLiteDatabase db = helper.getWritableDatabase();
    try {
      db.execSQL(SQL);
    } finally {
      db.close();
    }
  }

  /**
   * レコードの削除
   *
   * 削除対象のオブジェクト
   */
  public void delete(RecordBase rec, int row) {
    SQLiteDatabase db = helper.getWritableDatabase();
    try {
      db.delete(rec.TableName(), rec.PKColName() + "=?", new String[] { rec.GetValue(rec.PKColName(), row, "") });
      db.execSQL("vacuum;");
    } finally {
      db.close();
    }
  }

  public void delete_all(RecordBase rec) {
    SQLiteDatabase db = helper.getWritableDatabase();
    try {
      for (int i = 0; i < rec.RecordCount(); i++) {
        db.delete(rec.TableName(), rec.PKColName() + "=?", new String[] { rec.GetValue(rec.PKColName(), i, "") });
      }
      db.execSQL("vacuum;");
    } finally {
      db.close();
    }
  }

  public void delete_with_cond(RecordBase rec, String condition, String[] param) {
    SQLiteDatabase db = helper.getWritableDatabase();
    try {
      db.delete(rec.TableName(), condition, param);
    } finally {
      db.close();
    }
  }

  /**
   * 一覧を取得する
   *
   * @return 検索結果
   */
  public void list(RecordBase rec) {
    list(rec,null,null,null);
  }

  public void list(RecordBase rec,String[] ordcollst,String cond,String[] condparam) {

    String ordcol = rec.PKColName();
    if (ordcollst != null){
      ordcol = "";
      for (int i=0;i<ordcollst.length;i++){
        if (i != 0) ordcol = ordcol + ",";
        ordcol = ordcol + ordcollst[i];
      }
    }

    SQLiteDatabase db = helper.getReadableDatabase();

    rec.ClearRecord();
    try {
      Cursor cursor = db.query(rec.TableName(), null, cond, condparam, null, null,ordcol);
      cursor.moveToFirst();
      int row = 0;
      while (!cursor.isAfterLast()) {
        getRecord(cursor, rec, row);
        cursor.moveToNext();
        row++;
      }
      rec.ClearModifyFlg();
      cursor.close();
    } finally {
      db.close();
    }
    return;
  }

  /**
   * カーソルからオブジェクトへの変換
   *
   * @param cursor
   *          カーソル
   * @return 変換結果
   */
  private void getRecord(Cursor cursor, RecordBase rec, int row) {

    long rowid = cursor.getLong(rec.PKColIndex());

    if (rec.RecordCount() <= row) {
      rec.AddRow();
    }
    rec.SetRowId(row, new Long(rowid));
    for (int i = 0; i < rec.ColumnCount(); i++) {
      if (rec.ColumnType(i).equals(RecordBase.DT_INTEGER)) {
        rec.SetInt(i, row, cursor.getInt(i));
      } else if (rec.ColumnType(i).equals(RecordBase.DT_REAL)) {
        rec.SetDouble(i, row, new Double(cursor.getDouble(i)));
      } else if (rec.ColumnType(i).equals(RecordBase.DT_TEXT)) {
        rec.SetString(i, row, cursor.getString(i));
      }
      ;
    }
   }
}