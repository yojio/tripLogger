package jp.yojio.triplog;

import jp.yojio.triplog.Common.DB.DBAccessObject;
import jp.yojio.triplog.Common.Map.MyGeocoder;
import jp.yojio.triplog.DBAccess.DBCommon;
import android.content.Context;
import android.location.LocationManager;

public class ObjectContainer {
  private static ObjectContainer _instance;

  private MyGeocoder _geo = new MyGeocoder();
  private LocationManager _LocationManager;
  private DBAccessObject _dao;

  private ObjectContainer(Context context) {
    _LocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    _dao = DBCommon.GetDBAccessObject(context);
  }

  public static synchronized ObjectContainer getInstance(Context context) {
    if (_instance == null) {
      _instance = new ObjectContainer(context);
    }
    return _instance;
  }

  public MyGeocoder GetGeo(){
    return _geo;
  }

  public LocationManager GetLocationManager(){
    return _LocationManager;
  }

  public DBAccessObject getDao(){
    return _dao;
  }

}
