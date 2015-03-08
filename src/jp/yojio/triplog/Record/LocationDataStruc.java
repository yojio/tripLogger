package jp.yojio.triplog.Record;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LocationDataStruc implements Serializable {

  private static final long serialVersionUID = -7608351100029698713L;
  private static final String TEMPNAME = "LocationDataStruc";
  private static final String DATEFORMAT = "yyyy/MM/dd HH:mm";
  private static final String SEP = ",";
  private static final String FLG = "flg";

  private boolean _locationset = false;
  private Date _date = new Date();
  private String _caption = "";
  private boolean _new = true;
  private boolean _captionchanged = false;
  private Double _latitude = new Double(0);
  private Double _longitude = new Double(0);
  private String _comment = "";
  private String _filename = "";
  private String _tags = "";
  private String _tagnames = "";
  private boolean _tweeted = false;
  private boolean _uploaded = false;
  private String _linkcode = "";
  private boolean _init = false;
  private ArrayList<String> _files = new ArrayList<String>();
  private int _id = 0;

  public LocationDataStruc() {

  }

  public int GetId(){
    return _id;
  }

  public void SetId(int value){
    _id = value;
  }

  public boolean isNew(){
    return _new;
  }

  public void setNew(boolean value){
    _new = value;
  }

  public boolean isLocationSet() {
    return _locationset;
  }

  public void setLocationSet(boolean readed) {
    _locationset = readed;
  }

  public String getCaption() {
    return _caption == null ? "" : _caption;
  }

  public void setCaption(String caption) {
    _caption = caption;
  }

  public Double getLatitude() {
    return _latitude;
  }

  public void setLatitude(Double latitude) {
    _latitude = latitude;
  }

  public Double getLongitude() {
    return _longitude;
  }

  public void setLongitude(Double longitude) {
    _longitude = longitude;
  }

  public String getComment() {
    return _comment == null ? "" : _comment;
  }

  public void setComment(String comment) {
    _comment = comment;
  }

  public String getFileName() {
    return _filename == null ? "" : _filename;
  }

  public void setFileName(String filename) {
    _filename = filename;
  }

  public Date getDate() {
    return _date;
  }

  public void setDate(Date date) {
    _date = date;
  }

  // 添付ファイル
  public int AddFile(String path) {

    if (CheckDuplicate(path)) return -1;

    _files.add(path);
    return _files.size() - 1;
  }

  public int FileCount() {
    return _files.size();
  }

  public String GetFile(int idx) {
    if ((idx < 0) || (idx >= _files.size())) return "";
    return (String) _files.get(idx);
  }

  public void ClearFiles() {
    _files.clear();
  }

  public boolean DeleteFile(int idx) {
    if ((idx < 0) || (idx >= _files.size())) return false;
    _files.remove(idx);
    return true;
  }

  public boolean CheckDuplicate(String path) {
    String wk;
    for (int i = 0; i < _files.size(); i++) {
      wk = (String) _files.get(i);
      if (wk.equals(path)) {
        return true;
      }
    }
    return false;
  }

  public boolean IsCaptionChanged() {
    return _captionchanged;
  }

  public void SetCaptionChanged(boolean value) {
    _captionchanged = value;
  }

  public String GetTags(){
    return _tags == null ? "" : _tags;
  }

  public void SetTags(String value){
    _tags = value;
  }

  public String GetTagNames(){
    return _tagnames == null ? "" : _tagnames;
  }

  public void SetTagNames(String value){
    _tagnames = value;
  }

  public boolean isTweeted(){
    return _tweeted;
  }

  public void setTweeted(boolean value){
    _tweeted = value;
  }

  public boolean ipUploaded(){
    return _uploaded;
  }

  public void setUploaded(boolean value){
    _uploaded = value;
  }

  public String getLinkCode(){
    return _linkcode == null ? "" : _linkcode;
  }

  public void setLinkCode(String value){
    _linkcode = value;
  }

  public boolean isInit(){
    return _init;
  }

  public void setinit(boolean value){
    _init = value;
  }

  public boolean SaveTemp(Context context){

    SharedPreferences common = context.getSharedPreferences(TEMPNAME,Context.MODE_PRIVATE);
    Editor editor = common.edit();
    editor.putInt("_id", _id);
    editor.putBoolean("_locationset", _locationset);
    editor.putString("_date", new SimpleDateFormat(DATEFORMAT).format(_date));
    editor.putString("_caption", _caption);
    editor.putBoolean("_new", _new);
    editor.putBoolean("_captionchanged", _captionchanged);
    editor.putString("_latitude",String.valueOf(_latitude));
    editor.putString("_longitude",String.valueOf(_longitude));
    editor.putString("_comment", _comment);
    editor.putString("_filename", _filename);
    editor.putString("_tags", _tags);
    editor.putString("_tagnames", _tagnames);
    editor.putBoolean("_tweeted", _tweeted);
    editor.putBoolean("_init", _init);
    StringBuffer sb = new StringBuffer();
    for (int i=0;i<_files.size();i++){
      if (i != 0) sb.append(SEP);
      sb.append(_files.get(i));
    }
    editor.putString("_files", sb.toString());
    editor.putBoolean(FLG, true);
    editor.commit();
    return true;
  }

  public boolean RestoreTemp(Context context){

    SharedPreferences common = context.getSharedPreferences(TEMPNAME,Context.MODE_PRIVATE);
    try{
      if (!common.getBoolean(FLG, false)) return true;
    }catch (Exception e){
      return true;
    }

    _id = common.getInt("_id", 0);
    _locationset = common.getBoolean("_locationset", false);
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
    try {
      _date = sdf.parse(common.getString("_date", sdf.format(new Date())));
    } catch (ParseException e) {
      _date = new Date();
    }
    _caption = common.getString("_caption", "");
    _new = common.getBoolean("_new", true);
    _captionchanged = common.getBoolean("_captionchanged", false);
    _latitude = new Double(common.getString("_latitude", String.valueOf(0.0)));
    _longitude = new Double(common.getString("_longitude", String.valueOf(0.0)));
    _comment = common.getString("_comment", "");
    _filename = common.getString("_filename", "");
    _tagnames = common.getString("_tagnames", "");
    _tweeted = common.getBoolean("_tweeted", false);
    _init = common.getBoolean("_init", false);
    String s = common.getString("_files", "");
    String[] wk = s.split(SEP);
    _files.clear();
    for (int i=0;i<wk.length;i++){
      if (wk[i].trim().equals("")) continue;
      _files.add(wk[i]);
    }
    return true;
  }

  public static boolean DeleteTemp(Context context){
    SharedPreferences common = context.getSharedPreferences(TEMPNAME,Context.MODE_PRIVATE);
    common.edit().clear().commit(); // クリア
    return true;
  }
}
