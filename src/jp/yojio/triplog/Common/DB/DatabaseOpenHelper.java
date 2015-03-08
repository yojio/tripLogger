package jp.yojio.triplog.Common.DB;

import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.DBAccess.BookmarkTempRecord;
import jp.yojio.triplog.DBAccess.TagMasterTempRecord;
import jp.yojio.triplog.DBAccess.TranTempRecord;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

  private RecordBase[] _tables = null;
  StringBuilder _createSql = new StringBuilder();
  StringBuffer _wk = new StringBuffer();

  /**
   * コンストラクタ
   */
  public DatabaseOpenHelper(Context context, String DB_NAME, RecordBase[] Tables,int version_no) {

    // 指定したデータベース名が存在しない場合は、新たに作成されonCreate()が呼ばれる
    // バージョンを変更するとonUpgrade()が呼ばれる
    super(context, DB_NAME, null, version_no);
    _tables = Tables;
  }

  /**
   * データベースの生成に呼び出されるので、 スキーマの生成を行う
   */
  @Override
  public void onCreate(SQLiteDatabase db) {

    db.beginTransaction();
    try {
      // テーブルの生成
      for (int i = 0; i < _tables.length; i++) {
        CreateTable(db,_tables[i]);
      }

      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  private void CreateTable(SQLiteDatabase db,RecordBase tbl){

    _createSql.setLength(0);
    _createSql.append("create table " + tbl.TableName() + " (");

    for (int ii = 0; ii < tbl.ColumnCount(); ii++) {
      _wk.setLength(0);
      if (ii != 0)
        _wk.append(",");
      _wk.append(tbl.ColumnName(ii)).append(" ").append(
          tbl.ColumnType(ii)).append(" ");
      if (tbl.PKColIndex() == ii) {
        _wk.append("primary key ");
        if (tbl.IsAutoNo()) _wk.append(" autoincrement ");
      }
      _wk.append(tbl.ColumnNullType(ii));
      _createSql.append(_wk.toString());
    }
    _createSql.append(")");

    db.execSQL(_createSql.toString());
  }

  /**
   * データベースの更新
   *
   * 親クラスのコンストラクタに渡すversionを変更したときに呼び出される
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    RecordBase tbl;

    db.beginTransaction();
    try {
      if (oldVersion <= 1){
        // Yfrog用 Twitter－IDフィールドの追加（コントロール）
        db.execSQL("ALTER TABLE CONTR ADD COLUMN TWITTER_USER_ID TEXT;");
        db.execSQL("ALTER TABLE CONTR ADD COLUMN TWITTER_PIC_TYPE INTEGER;");
      }

      if (oldVersion <= 2){
        // テーブルの生成（インポート用テンポラリテーブルの追加）
        for (int i = 0; i < _tables.length; i++) {
          tbl = _tables[i];
          if (tbl.TableName().equals(TagMasterTempRecord.TABLE_NAME) ||
              tbl.TableName().equals(TranTempRecord.TABLE_NAME)){
            CreateTable(db,tbl);
          }
        }
      }

      if (oldVersion <= 3){
        // 新規登録時の位置情報取得設定
        db.execSQL("ALTER TABLE CONTR ADD COLUMN AUTO_LOCATION_TYPE INTEGER;");
      }

      if (oldVersion <= 4){
        // テーブルの生成（インポート用テンポラリテーブルの追加）
        for (int i = 0; i < _tables.length; i++) {
          tbl = _tables[i];
          if (tbl.TableName().equals(BookmarkTempRecord.TABLE_NAME)){
            CreateTable(db,tbl);
          }
        }
      }

      if (oldVersion <= 21){
        // 新規登録時の位置情報取得設定
        db.execSQL("ALTER TABLE CONTR ADD COLUMN ACCURACY INTEGER;");
      }

      if (oldVersion <= 30){
        // アップロード済みフラグ
        db.execSQL("ALTER TABLE TRAN ADD G_UPLOAD AUTO_LOCATION_TYPE INTEGER;");
        db.execSQL("ALTER TABLE TRAN ADD LINKCODE AUTO_LOCATION_TYPE INTEGER;");
        db.execSQL("ALTER TABLE TRAN_TEMP ADD G_UPLOAD AUTO_LOCATION_TYPE TEXT;");
        db.execSQL("ALTER TABLE TRAN_TEMP ADD LINKCODE AUTO_LOCATION_TYPE TEXT;");
      }

      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }
}