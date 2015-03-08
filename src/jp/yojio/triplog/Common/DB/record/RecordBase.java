package jp.yojio.triplog.Common.DB.record;

import java.io.Serializable;
import java.util.Vector;

import android.util.Log;

/**
 * テーブル規定クラス
 * @author yojio
 *
 */
public class RecordBase  implements Serializable{

  private static final long serialVersionUID = 1L;

  public boolean _isDebug;
  private String _tablename = "";
  private String _pkcolumnname = "";
  private String[][] _columns;
  private Vector<String[]> _data = new Vector<String[]>();
  private Vector<String> _rowstatus = new Vector<String>();
  private Vector<Long> _rowid = new Vector<Long>();
  private boolean _autono = true;
  private StringBuffer _retstr = new StringBuffer();

  public static final String DT_TEXT = "text";
  public static final String DT_INTEGER = "integer";
  public static final String DT_REAL = "real";

  public static final String NT_NULL = "";
  public static final String NT_NOTNULL = "not null";

  public static final String DS_NORMAL = "";
  public static final String DS_ADDED = "add";
  public static final String DS_MODIFY = "mod";

  public static final String DATA_SEPARATOR = ",";

  public RecordBase(String TableName,String[][] Columns,String PK){
    init(TableName,Columns,PK,true);
  }

  public RecordBase(String TableName,String[][] Columns,String PK,boolean AutoNo){
    init(TableName,Columns,PK,AutoNo);
  }

  public void init(String TableName,String[][] Columns,String PK,boolean AutoNo){
    _isDebug = false;
    _tablename = TableName;
    _columns = Columns;
    _pkcolumnname = PK;
    _autono = AutoNo;
  }

  public void SetDebug(boolean value){
    _isDebug = value;
  }

  public String TableName(){
    return _tablename == null?"":_tablename;
  }

  public int ColumnCount(){
    return _columns.length;
  }

  private boolean CheckColBounds(int i){
    return ((i>=0) && (i<ColumnCount()));
  }

  private boolean CheckRowBounds(int i){
    return ((i>=0) && (i<RecordCount()));
  }

  public String PKColName(){
    return _pkcolumnname == null?"":_pkcolumnname;
  }

  public int PKColIndex(){
    return GetColumnIndex(PKColName());
  }

  public String ColumnName(int i){
    if (!CheckColBounds(i)) return "";
    return _columns[i][0];
  }

  public int ColumnIndex(String name){
    for (int i = 0;i<_columns.length;i++){
      if (_columns[i][0].equals(name)){
        return i;
      }
    }
    return -1;
  }

  public String ColumnType(int i){
    if (!CheckColBounds(i)) return "";
    return _columns[i][1];
  }

  public String ColumnType(String name){
    return ColumnType(ColumnIndex(name));
  }

  public String ColumnNullType(int i){
    if (!CheckColBounds(i)) return "";
    return _columns[i][2];
  }

  public int AddRow(){
    _data.add(new String[ColumnCount()]);
    _rowstatus.add(DS_ADDED);
    _rowid.add(null);
    return _data.size() - 1;
  }

  public String DataStatus(int row){
    if (!CheckRowBounds(row)) return "";
    return _rowstatus.get(row);
  }

  public Long RowId(int row){
    if (!CheckRowBounds(row)) return null;
    return _rowid.get(row);
  }

  public void SetRowId(int row,Long rowid){
    if (!CheckRowBounds(row)) return;
    _rowid.set(row,rowid);
  }

  public void ClearModifyFlg(){
    for (int i=0;i<_rowstatus.size();i++){
      _rowstatus.set(i, DS_NORMAL);
    }
  }

  public boolean IsAutoNo(){
    return _autono;
  }

  public int RecordCount(){
    return _data.size();
  }

  public void ClearRecord(){
    _data.clear();
    _rowstatus.clear();
    if (_isDebug) Log.i(this.getClass().getName(),"ClearRecord");
  }

  private String[] GetData(int row){
    if (!CheckRowBounds(row)) return null;
    return _data.get(row);
  }

  private boolean CheckColumn(int col,String datatype){
    if (!CheckColBounds(col)) return false;
    if (!_columns[col][1].equals(datatype)) return false;
    return true;
  }

  private boolean SetData(int col,int row,String val,String[] rowdata){

    String wk = rowdata[col];

    if ((wk != null) && (wk.equals(val))) return true;

    rowdata[col] = val;
    _data.set(row, rowdata);

    String wksts = _rowstatus.get(row);
    if (!wksts.equals(DS_ADDED)) _rowstatus.set(row,DS_MODIFY);

    return true;
  }

  public boolean SetInt(String ColumnName,int row,int val){
    return SetInt(GetColumnIndex(ColumnName),row,val);
  }

  public boolean SetInt(int col,int row,int val){
    String[] wk = GetData(row);
    if (wk == null) return false;
    if (!CheckColumn(col, DT_INTEGER)) return false;

    return SetData(col,row,String.valueOf(val),wk);

  }

  public boolean SetDouble(String ColumnName,int row,Double val){
    return SetDouble(GetColumnIndex(ColumnName),row,val);
  }

  public boolean SetDouble(int col,int row,Double val){
    String[] wk = GetData(row);
    if (wk == null) return false;
    if (!CheckColumn(col, DT_REAL)) return false;

    return SetData(col,row,String.valueOf(val),wk);

  }

  public boolean SetString(String ColumnName,int row,String val){
    return SetString(GetColumnIndex(ColumnName),row,val);
  }

  public boolean SetString(int col,int row,String val){
    String[] wk = GetData(row);
    if (wk == null) return false;
    if (!CheckColumn(col, DT_TEXT)) return false;

    return SetData(col,row,val,wk);

  }

  public int GetInt(String ColumnName,int row,int defval){
    return GetInt(GetColumnIndex(ColumnName),row,defval);
  }

  public int GetInt(int col,int row,int defval){
    String[] wk = GetData(row);
    if (wk == null) return defval;
    if (!CheckColumn(col, DT_INTEGER)) return defval;
    if (wk[col] == null) return defval;
    return Integer.parseInt(wk[col]);
  }

  public Double GetDouble(String ColumnName,int row,Double defval){
    return GetDouble(GetColumnIndex(ColumnName),row,defval);
  }

  public Double GetDouble(int col,int row,Double defval){
    String[] wk = GetData(row);
    if (wk == null) return defval;
    if (!CheckColumn(col,DT_REAL)) return defval;
    if (wk[col] == null) return defval;
    return Double.valueOf(wk[col]);
  }

  public String GetString(String ColumnName,int row,String defval){
    return GetString(GetColumnIndex(ColumnName),row,defval);
  }

  public String GetString(int col,int row,String defval){
    String[] wk = GetData(row);
    if (wk == null) return defval;
    if (!CheckColumn(col,DT_TEXT)) return defval;
    if (wk[col] == null) return defval;
    return wk[col];
  }

  public String GetValue(String ColumnName,int row,String defval){
    return GetValue(GetColumnIndex(ColumnName),row,defval);
  }

  public String GetValue(int col,int row,String defval){
    String[] wk = GetData(row);
    if (wk == null) return defval;
    if (wk[col] == null) return defval;
    return wk[col];
  }

  private int GetColumnIndex(String ColumnName){

    for (int i=0;i<_columns.length;i++){
      if (_columns[i][0].equals(ColumnName)){
        return i;
      }
    }

    return -1;
  }

  public String GetColumnString() throws Exception {

    _retstr.setLength(0);
    try{
      for (int i = 0;i<_columns.length;i++){
        if (i != 0) _retstr.append(DATA_SEPARATOR);
        _retstr.append(_columns[i][0]);
      }
    }catch (Exception e){
      if (_isDebug) Log.e(_tablename + " GetColumnString",e.getMessage());
      throw e;
    }

    return _retstr.toString();

  }

  public String GetDataString(int row) throws Exception {

    _retstr.setLength(0);
    String[] wk;
    try{
      wk = GetData(row);
      if (wk == null) return "";
      for (int i = 0;i<wk.length;i++){
        if (i != 0) _retstr.append(DATA_SEPARATOR);
        _retstr.append("\"").append(wk[i]).append("\"");
      }
    }catch (Exception e){
      if (_isDebug) Log.e(_tablename + " GetDataString",e.getMessage());
      throw e;
    }

    return _retstr.toString();

  }
}
















