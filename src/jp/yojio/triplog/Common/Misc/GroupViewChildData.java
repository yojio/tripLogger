package jp.yojio.triplog.Common.Misc;

import java.util.Date;

public class GroupViewChildData {

  private long _id = 0;
  private String _header = "";
  private String _caption = "";
  private String _tag = "";
  private Date _dt;
  private boolean _photoflg = false;
  private boolean _commentflg = false;
  private boolean _checked = false;
  private boolean _tweeted = false;
  private boolean _uploaded = false;
  private String _linkcode = "";
  private Double _latitude = new Double(0);
  private Double _longitude = new Double(0);

  public GroupViewChildData(long id,Date date,Double latitude,Double longitude,String Header,String Caption,String Tag,boolean HasPhoto,boolean HasComment,boolean tweeted,boolean uploaded,String linkcode){

    _id = id;
    _dt = date;
    _header = Header;
    _caption = Caption;
    _tag = Tag;
    _photoflg = HasPhoto;
    _commentflg = HasComment;
    _tweeted = tweeted;
    _uploaded = uploaded;
    _linkcode = linkcode;
    _latitude = latitude;
    _longitude = longitude;

  }

  public long getId() {
    return _id;
  }
  public void setId(long id) {
    _id = id;
  }
  public Date getDate(){
    return _dt;
  }
  public void setDate(Date date){
    _dt = date;
  }
  public String getHeader() {
    return _header;
  }
  public void setHeader(String header) {
    _header = header;
  }
  public String getCaption() {
    return _caption;
  }
  public void setCaption(String caption) {
    _caption = caption;
  }
  public String getTag() {
    return _tag;
  }
  public void setTag(String tag) {
    _tag = tag;
  }
  public boolean isPhotoflg() {
    return _photoflg;
  }
  public void setPhotoflg(boolean photoflg) {
    _photoflg = photoflg;
  }
  public boolean isCommentflg() {
    return _commentflg;
  }
  public void setCommentflg(boolean commentflg) {
    _commentflg = commentflg;
  }
  public boolean isChecked(){
    return _checked;
  }
  public void setChecked(boolean val){
    _checked = val;
  }
  public boolean isTweeted(){
    return _tweeted;
  }
  public void setTweeted(boolean val){
    _tweeted = val;
  }
  public boolean isUpdated(){
    return _uploaded;
  }
  public void setUpdated(boolean val){
    _uploaded = val;
  }
  public String getLinkCode() {
    return _linkcode;
  }
  public void setLinkCode(String linkcode) {
    _linkcode = linkcode;
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
}
