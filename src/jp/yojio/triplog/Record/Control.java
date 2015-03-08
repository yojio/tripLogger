package jp.yojio.triplog.Record;


public class Control {
  private String _token;
  private String _tokensecret;
  private boolean _usetwit;
  private int _upload_cap;
  private boolean _upload_pic;
  private boolean _upload_location;
  private String _hashtag;
  private int _uploadpicsize;
  private String _userid;
  private int _pictype;
  private int _location_type;
  private int _auto_location_type;
  private int _accuracy;

  public Control(int location_type,int auto_location_type,boolean usetwit,String token,String tokensecret,int uploadcap,boolean uploadpic,int picsize,boolean uploadlocation,String hashtag,String userid,int pictype,int accuracy){
    _location_type = location_type;
    _auto_location_type = auto_location_type;
    _usetwit = usetwit;
    _token = token;
    _tokensecret = tokensecret;
    _upload_cap = uploadcap;
    _upload_pic = uploadpic;
    _upload_location = uploadlocation;
    _hashtag = hashtag;
    _uploadpicsize = picsize;
    _userid = userid;
    _pictype = pictype;
    _accuracy = accuracy;
  }

  public int getPicSize() {
    return _uploadpicsize;
  }

  public void setPicSize(int uploadpicsize) {
    _uploadpicsize = uploadpicsize;
  }

  public String getToken() {
    return _token;
  }

  public void setToken(String token) {
    _token = token;
  }

  public String getTokenSecret() {
    return _tokensecret;
  }

  public void setTokenSecret(String tokensecret) {
    _tokensecret = tokensecret;
  }

  public boolean UseTwit() {
    return _usetwit;
  }

  public void setUseTwit(boolean usetwit) {
    _usetwit = usetwit;
  }

  public int getUploadCap() {
    return _upload_cap;
  }

  public void setUploadCap(int uploadCap) {
    _upload_cap = uploadCap;
  }

  public boolean isUploadPic() {
    return _upload_pic;
  }

  public void setUploadPic(boolean uploadPic) {
    _upload_pic = uploadPic;
  }

  public boolean isUploadLocation() {
    return _upload_location;
  }

  public void setUploadLocation(boolean uploadLocation) {
    _upload_location = uploadLocation;
  }

  public String getHashTag() {
    String s = _hashtag;
    if (s.startsWith("#")) s = s.substring(1);
    return s;
  }

  public void setHashTag(String hashtag) {
    if (hashtag == null){
      _hashtag = "";
    }else{
      _hashtag = hashtag;
    }
  }

  public String getUserId() {
    return _userid;
  }

  public void setUserId(String userid) {
    _userid = userid;
  }

  public int getPicType() {
    return _pictype;
  }

  public void setPicType(int pictype) {
    _pictype = pictype;
  }

  public int getLocationType() {
    return _location_type;
  }

  public void setLocationType(int locationType) {
    _location_type = locationType;
  }

  public int getAutoLocationType() {
    return _auto_location_type;
  }

  public void setAutoLocationType(int autoLocationType) {
    _auto_location_type = autoLocationType;
  }

  public int getAccuracy(){
    return _accuracy;
  }

  public void setAccuracy(int accuracy){
    _accuracy = accuracy;
  }
}
