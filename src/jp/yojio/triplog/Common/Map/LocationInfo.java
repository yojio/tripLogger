package jp.yojio.triplog.Common.Map;

public class LocationInfo {

  boolean readed = false;
  String Provider = "";
  long Time = -1;
  double Latitude = -1;
  double Longitude = -1;
  double Altitude = -1;
  float Speed = -1;
  float Bearing = -1;
  float Accuracy = -1;
  String Location = "";

  public String getLocation() {
    return Location;
  }

  public void setLocation(String location) {
    Location = location;
  }

  public boolean isReaded() {
    return readed;
  }

  public void setReaded(boolean readed) {
    this.readed = readed;
  }

  public String getProvider() {
    return Provider;
  }

  public void setProvider(String provider) {
    Provider = provider;
  }

  public long getTime() {
    return Time;
  }

  public void setTime(long time) {
    Time = time;
  }

  public double getLatitude() {
    return Latitude;
  }

  public void setLatitude(double latitude) {
    Latitude = latitude;
  }

  public double getLongitude() {
    return Longitude;
  }

  public void setLongitude(double longitude) {
    Longitude = longitude;
  }

  public double getAltitude() {
    return Altitude;
  }

  public void setAltitude(double altitude) {
    Altitude = altitude;
  }

  public float getSpeed() {
    return Speed;
  }

  public void setSpeed(float speed) {
    Speed = speed;
  }

  public float getBearing() {
    return Bearing;
  }

  public void setBearing(float bearing) {
    Bearing = bearing;
  }

  public float getAccuracy() {
    return Accuracy;
  }

  public void setAccuracy(float accuracy) {
    Accuracy = accuracy;
  }

}
