package jp.yojio.triplog.Common.Activity;

import java.util.Date;

import jp.yojio.triplog.ObjectContainer;
import jp.yojio.triplog.R;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.DBAccessObject;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Map.GetLocationInfoThread;
import jp.yojio.triplog.Common.Map.LocationInfo;
import jp.yojio.triplog.Common.Map.MyGeocoder;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.Record.Control;
import jp.yojio.triplog.misc.TripLogMisc;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Toast;

public abstract class LocationBaseForm extends Activity implements Runnable, LocationListener, OnClickListener {

  protected static final String LOGTAG_CLASS = LocationBaseForm.class.getSimpleName();

  protected final Handler _handler = new Handler();
  protected ProgressDialog _dialog;
  protected GetLocationInfoThread _gpsthr = null;
  protected MyGeocoder _geo;
  protected LocationManager _LocationManager;
  protected String currLocationProvider = "";
  protected ObjectContainer _obj;
  protected AlertDialog _AlertDialog;
  protected RecordBase _contr;
  protected DBAccessObject _dao;
  protected String _searchmsg = "";
  protected static boolean _isDebug;
  protected boolean _networkposget = false;
  protected Control _control;

  // UI更新ハンドラ ※別スレッドから通知を受けてUI部品を更新する
  private final Handler _handler2 = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case 0:
        LocationInfo info = (LocationInfo) msg.obj;
        GetLocationEv(info);
        break;
      default:
        break;
      };
    };
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    _isDebug = Misc.isDebug(this);
    _obj = ObjectContainer.getInstance(getApplication());
    _LocationManager = _obj.GetLocationManager();
    _geo = _obj.GetGeo();
    _dao = _obj.getDao();
    _contr = DBCommon.GetTable(_dao, DBCommon.CONTR,_isDebug);
    RefreshControl();
  }

  @Override
  protected void onStart() {
    super.onStart();
    RefreshControl();
  }

  protected void RefreshControl(){
    _contr.ClearRecord();
    _dao.list(_contr, null, null, null);
    _control = TripLogMisc.SetControlData(_contr);
  }

  @Override
  protected void onResume() {
    super.onResume();
    CancelLocationSearch();
  }

  @Override
  protected void onPause() {
    CancelLocationSearch();
    super.onPause();
  }

  ////////////////////////////////////// 位置情報の取得 /////////////////////////////////////////////////////
  protected void CancelLocationSearch() {
    CancelLocationSearchWithoutThread();
    _gpsthr = null;
  }

  protected void CancelLocationSearchWithoutThread() {
    if ((_gpsthr != null) && (_LocationManager != null)) {
      _LocationManager.removeUpdates(this);
      if (_dialog != null)
        _dialog.dismiss();
    }
    _dialog = null;
  }

  protected boolean CheckNetworkProvider(String Provider) {

    if (Provider == null) return false;

    LocationProvider locationProvider;
    try{
      locationProvider = _LocationManager.getProvider(Provider);
    } catch (Exception e){
      return false;
    }

    if (locationProvider == null) {
      return false;
    } else {
      return true;
    }
  }

  protected boolean StartGetLocationStatus() {
    return StartGetLocationStatus(true);
  }

  protected boolean StartGetLocationStatus(boolean ShowDialog) {
    return StartGetLocationStatuslocal(ShowDialog);
  }

  protected boolean StartGetLocationStatuslocal(boolean ShowDialog) {

    if (!TripLogMisc.isNetworkConnected(this)){
      Toast.makeText(this, getResources().getText(R.string.msg_network_connecterror), Toast.LENGTH_SHORT).show();
      return false;
    }

    // 位置情報サービスの要求条件をピックアップする
    // 速度、電力消費などから適切な位置情報サービスを選択する
    Criteria criteria = new Criteria();
    // 使える中で最も条件にヒットする位置情報サービスを取得する
    RefreshControl();
    int lst = _control.getLocationType();
    if ((lst == Const.SET_LOCATION_HIGH) || ((_networkposget) && (lst == Const.SET_LOCATION_LOW_HIGH))){
      criteria.setAccuracy(Criteria.ACCURACY_FINE); // 精度を優先
      criteria.setPowerRequirement(Criteria.POWER_HIGH); // 許容電力消費
      _networkposget = false;
    }else{
      criteria.setAccuracy(Criteria.ACCURACY_COARSE);
      criteria.setPowerRequirement(Criteria.POWER_LOW); // 許容電力消費
      _networkposget = true;
    }
    criteria.setSpeedRequired(false);               // 速度不要
    criteria.setAltitudeRequired(false);            // 高度不要
    criteria.setBearingRequired(false);            // 方位不要
    criteria.setCostAllowed(false);               // 費用の発生不可？

    currLocationProvider = _LocationManager.getBestProvider(criteria, true);
    if (!CheckNetworkProvider(currLocationProvider)) {
      Toast.makeText(this, getResources().getText(R.string.msg_locationerror), Toast.LENGTH_SHORT).show();
      return false;
    }
    StartGetLocationStatus(currLocationProvider, this,ShowDialog);
    return true;
  }

  protected boolean StartGetLocationStatus(String Provider, Context context) {
    return StartGetLocationStatus(Provider,context,true);
  }
  protected boolean StartGetLocationStatus(String Provider, Context context,boolean ShowDialog) {

    if (_dialog != null) _dialog.dismiss();

    // 位置情報の取得
    boolean b = true;
    Location loc = _LocationManager.getLastKnownLocation(Provider);
    if (loc != null){
//      Log.e("aaa",new SimpleDateFormat(getString(R.string.format_calender_ymdhm2)).format(loc.getTime()));
      Date dt = new Date();
      if ((dt.getTime() - loc.getTime()) < Const.LOCATIONUPDATE_MINTIME){
//      if (((dt.getTime() - loc.getTime()) < Const.LOCATIONUPDATE_MINTIME) &&
//          ((_control.getAccuracy() == 0) || (loc.getAccuracy() == -1) || (loc.getAccuracy() <= _control.getAccuracy()))){
        GetLocationEv(GetLocationInfo(loc));
        b = false;
      }
    }

    if (!b) return true;

    // プログレスダイアログを表示
    if (ShowDialog){
      _dialog = new ProgressDialog(context);
      _dialog.setIndeterminate(true);
      _dialog.setOnCancelListener(new OnCancelListener() {
        public void onCancel(DialogInterface dialog) {
          CancelLocationSearch();
        }
      });
    }else{
      _dialog = null;
    }

    _searchmsg = (String) getResources().getText(R.string.msg_locationinfo);
    if (Provider.equals(LocationManager.GPS_PROVIDER)) {
      _searchmsg = _searchmsg + "\n" + getResources().getText(R.string.msg_location_gps);
    } else {
      _searchmsg = _searchmsg + "\n" + getResources().getText(R.string.msg_location_network);
    }

    if (ShowDialog){
      _dialog.setMessage(_searchmsg);
      _dialog.show();
    }

    _gpsthr = new GetLocationInfoThread(_handler, this,_control,(Provider.equals(LocationManager.GPS_PROVIDER)));
    _gpsthr.start();
    _LocationManager.requestLocationUpdates(Provider, Const.LOCATIONUPDATE_MINTIME, Const.LOCATIONUPDATE_MINDISTANCE, this);

    return true;
  }

  public void run() {

    CancelLocationSearchWithoutThread();

    // 位置情報の取得
    if (_gpsthr != null) {
      LocationInfo info = _gpsthr.getInfo();
      _gpsthr = null;

      // 終了を通知
      Message msg = new Message();
      msg.what = 0;
      msg.obj = info;
      _handler2.sendMessage(msg);

//      if (info.isReaded()) {
//        GetLocationEv(info);
//      } else {
//        Toast.makeText(this, getResources().getText(R.string.msg_locationerror), Toast.LENGTH_LONG).show();
//      }
    }
  }

  public void onLocationChanged(Location arg0) {

    if (_gpsthr == null) return;
    _gpsthr.setInfo(GetLocationInfo(arg0));

  }

  private LocationInfo GetLocationInfo(Location arg0){
    LocationInfo info = new LocationInfo();
    try {
      info.setReaded(true);
      info.setProvider(arg0.getProvider());
      info.setLatitude(arg0.getLatitude());
      info.setLongitude(arg0.getLongitude());
      if (arg0.hasAccuracy())
        info.setAccuracy(arg0.getAccuracy());
      if (arg0.hasAltitude())
        info.setAltitude(arg0.getAltitude());
      info.setTime(arg0.getTime());
      if (arg0.hasSpeed())
        info.setSpeed(arg0.getSpeed());
      if (arg0.hasBearing())
        info.setBearing(arg0.getBearing());
      if (Misc.isConnected(getApplication())) {
        info.setLocation(_geo.point2address(arg0.getLatitude(), arg0.getLongitude(), getApplication()));
      } else {
        info.setLocation("");
      }
      ;
    } catch (Exception e) {
      info.setLocation("");
    }
    return info;
  }

  public void onProviderDisabled(String provider) {
    // TODO 自動生成されたメソッド・スタブ

  }

  public void onProviderEnabled(String provider) {
    // TODO 自動生成されたメソッド・スタブ

  }

  public void onStatusChanged(String provider, int status, Bundle extras) {
    switch (status) {
    case LocationProvider.AVAILABLE:
      if (_isDebug) Log.v("Status", "AVAILABLE");
      break;
    case LocationProvider.OUT_OF_SERVICE:
      if (_isDebug) Log.v("Status", "OUT_OF_SERVICE");
      break;
    case LocationProvider.TEMPORARILY_UNAVAILABLE:
      if (_isDebug) Log.v("Status", "TEMPORARILY_UNAVAILABLE");
      break;
    }
  }

  public boolean ReLocationSearch(){
    int lst = _control.getLocationType();
    if ((_networkposget) && (lst == Const.SET_LOCATION_LOW_HIGH) && (!currLocationProvider.equals(LocationManager.GPS_PROVIDER))){
      return true;
    }else{
      return false;
    }
  }

  public abstract void GetLocationEv(LocationInfo info);


}
