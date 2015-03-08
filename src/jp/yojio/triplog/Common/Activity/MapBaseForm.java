package jp.yojio.triplog.Common.Activity;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import jp.yojio.triplog.ObjectContainer;
import jp.yojio.triplog.R;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.DBAccessObject;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Layout.AutoInvisibleLinearLayout;
import jp.yojio.triplog.Common.Map.GetLocationInfoThread;
import jp.yojio.triplog.Common.Map.LocationHelper;
import jp.yojio.triplog.Common.Map.LocationInfo;
import jp.yojio.triplog.Common.Map.MapCenterOverlay;
import jp.yojio.triplog.Common.Map.MyGeocoder;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.DBAccess.BookmarkRecord;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.Record.Control;
import jp.yojio.triplog.misc.TripLogMisc;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public abstract class MapBaseForm extends MapActivity implements Runnable, LocationListener {

  private static final String LOGTAG_CLASS = MapBaseForm.class.getSimpleName();

  // ロケーション関連
  protected MyGeocoder _geo;
  protected LocationManager _LocationManager;
  protected ObjectContainer _obj;
  protected MapView _mapView;
  protected MapController _mapController;
  protected GetLocationInfoThread _gpsthr = null;
  protected ProgressDialog _dialog;
  protected String currLocationProvider = "";
  protected AlertDialog _AlertDialog;
  private List<Address> _addrlst;
  protected AutoInvisibleLinearLayout _ButtonLayout;
  private ImageButton _currentlocationbtn;
  private ImageButton _satellitebtn;
  private boolean _satelliteflg = false;
  private DisplayMetrics _metrics = new DisplayMetrics();
  protected float _scaledDensity;

  // タイマー関係
  private android.os.Handler _timerHandler = new android.os.Handler();
  private Timer _Timer = new Timer();
  // レイアウト関連
  protected LinearLayout _headerlayout;
  protected LinearLayout _headerlayout2;
  protected LinearLayout _footerlayout;
  protected LinearLayout _footerlayout2;
  private String _location_japan;
  private RecordBase _bookmark;
  protected DBAccessObject _dao;
  protected RecordBase _contr;
  protected Control _control;

  protected GeoPoint _bookmarkpos = LocationHelper.getGeoPointLatLong(0.0, 0.0);
  protected String _bookmarkcap = "";
  protected static boolean _isDebug;

  // UI更新ハンドラ ※別スレッドから通知を受けてUI部品を更新する
  private final Handler _handler = new Handler() {
    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message msg) {

      // プログレスダイアログを閉じる
      if (_dialog != null && _dialog.isShowing()) {
        _dialog.dismiss();
        _dialog = null;
      }

      switch (msg.what) {
      case Const.MESSAGE_WHAT_LASTKNOWNPOINT_END:
        // 現在地取得: 正常終了ならマップを更新
        SetMapCenter((GeoPoint) msg.obj);
        break;
      case Const.MESSAGE_WHAT_LASTKNOWNPOINT2_END:
        // 現在地取得: 正常終了ならマップを更新
        SetMapCenter((GeoPoint) msg.obj, false);
        break;
      case Const.MESSAGE_WHAT_SCROLL_END:
        // 現在地取得: 正常終了ならマップを更新
        DoScrollMap((Point) msg.obj);
        break;
      case Const.MESSAGE_WHAT_SEARCHADDRESS_END:
        // 住所検索: 正常終了ならマップを更新
        _addrlst = (List<Address>) msg.obj;
        // if (_cancelflg) return;
        switch (_addrlst.size()) {
        case 0:
          Toast.makeText(MapBaseForm.this, getResources().getText(R.string.errorDialog_searchResultText), Toast.LENGTH_SHORT).show();
          break;
        case 1:
          Address address = (Address) _addrlst.get(0);
          SetMapCenter(LocationHelper.getGeoPointLatLong(address.getLatitude(), address.getLongitude()));
          break;
        default:
          ShowSelectAddressDialog();
        }
        break;
      case Const.MESSAGE_WHAT_SELECTADDRESS_END:
        Integer idx = (Integer) msg.obj;
        Address address = (Address) _addrlst.get(idx.intValue());
        SetMapCenter(LocationHelper.getGeoPointLatLong(address.getLatitude(), address.getLongitude()));
        if ((_AlertDialog != null) && (_AlertDialog.isShowing())) _AlertDialog.dismiss();
        break;
      case Const.MESSAGE_WHAT_CURRENTPOS_END:
        SetMapCenter((GeoPoint) msg.obj);
        if ((_AlertDialog != null) && (_AlertDialog.isShowing())) _AlertDialog.dismiss();
        break;
      case Const.MESSAGE_WHAT_LASTKNOWNPOINT_ERROR:
      case Const.MESSAGE_WHAT_LASTKNOWNPOINT_TOADDRESS_ERROR:
        // 現在地取得: エラーならトースト表示 ※又はエラーダイアログ
        String locationError = (String) msg.obj;
        Toast.makeText(MapBaseForm.this, locationError, Toast.LENGTH_SHORT).show();
        break;
      case Const.MESSAGE_WHAT_SEARCHADDRESS_ERROR:
        // 住所検索: エラーならアラートダイアログ表示
        String searchError = (String) msg.obj;
        Toast.makeText(MapBaseForm.this, searchError, Toast.LENGTH_SHORT).show();
        break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.getWindowManager().getDefaultDisplay().getMetrics(_metrics);

    _isDebug = Misc.isDebug(this);
    _scaledDensity = _metrics.scaledDensity;

    setContentView(R.layout.frm_mapbase);

    _location_japan = getResources().getString(R.string.location_japan);

    // 動的にレイアウトを生成
    ViewGroup layout = createLayout();
    setContentView(layout);

    // ジオコーダ生成
    _obj = ObjectContainer.getInstance(getApplication());
    _LocationManager = _obj.GetLocationManager();
    _geo = _obj.GetGeo();

    _dao = _obj.getDao();
    _bookmark = DBCommon.GetTable(_dao, DBCommon.BOOKMARK, _isDebug);
    _contr = DBCommon.GetTable(_dao, DBCommon.CONTR, _isDebug);
    RefreshControl();

  }

  private ViewGroup createLayout() {
    // 大元のレイアウト
    FrameLayout ll = (FrameLayout) this.getLayoutInflater().inflate(R.layout.frm_mapbase, null);

    // 地図用レイアウト
    ViewGroup v = createMapLayout();
    ll.addView(v, new ViewGroup.LayoutParams(Const.FP, Const.FP));

    // ボタン用レイアウト
    v = createButtonLayout();
    ll.addView(v, new ViewGroup.LayoutParams(Const.FP, Const.FP));

    return ll;
  }

  private ViewGroup createMapLayout() {

    // 地図用レイアウトのレイアウト枠
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);

    // 子要素のデフォルトのパラメータ
    LinearLayout.LayoutParams defparam = new LinearLayout.LayoutParams(Const.FP, Const.WC, Const.WIDGET_WEIGHT);

    // 境界線, 境界線をレイアウトに追加時にweightパラメータを設定しない
    View v = new View(this) {
      @Override
      public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.GRAY);
      }
    };
    v.setLayoutParams(new ViewGroup.LayoutParams(Const.FP, Const.BODER_WIDTH));
    layout.addView(v);

    _headerlayout = new LinearLayout(this);
    layout.addView(_headerlayout);

    // CustomizedMapView
    _mapView = new MapView(this, Const.MAP_KEY(_isDebug));
    _mapView.setBuiltInZoomControls(true);
    _mapView.getController().setZoom(Const.INIT_ZOOMLEVEL);
    _mapView.setClickable(true);
    _mapView.setEnabled(true);
    _mapView.getOverlays().add(new MyOverlay(_mapView));
    _mapView.getOverlays().add(new MapCenterOverlay(_scaledDensity));

    layout.addView(_mapView, defparam);

    _footerlayout = new LinearLayout(this);
    layout.addView(_footerlayout);

    return layout;
  }

  private ViewGroup createButtonLayout() {

    // XMLファイルからViewを生成する
    _ButtonLayout = (AutoInvisibleLinearLayout) this.getLayoutInflater().inflate(R.layout.frm_mapbuttons, null);

    // 現在地へ移動
    _currentlocationbtn = (ImageButton) _ButtonLayout.findViewById(R.id.currentbtn);
    _currentlocationbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _ButtonLayout.setVisibility(LinearLayout.GONE);
        StartGetLocationStatus();
      }
    });
    _currentlocationbtn.setOnLongClickListener(new View.OnLongClickListener() {
      public boolean onLongClick(View view) {
        _ButtonLayout.setVisibility(LinearLayout.GONE);
        SelectProvider();
        return false;
      }
    });

    // 地図・サテライト切り替え
    _satellitebtn = (ImageButton) _ButtonLayout.findViewById(R.id.viewchangebutton);
    _satelliteflg = _mapView.isSatellite();
    if (_satelliteflg) {
      _satellitebtn.setImageResource(R.drawable.icon_map_green_16);
    } else {
      _satellitebtn.setImageResource(R.drawable.icon_globe_blue_16);
    }
    ;
    _satellitebtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _ButtonLayout.setVisibility(LinearLayout.GONE);
        _satelliteflg = !_satelliteflg;
        _mapView.setSatellite(_satelliteflg);
        if (_satelliteflg) {
          _satellitebtn.setImageResource(R.drawable.icon_map_green_16);
        } else {
          _satellitebtn.setImageResource(R.drawable.icon_globe_blue_16);
        }
      }
    });

    _headerlayout2 = new LinearLayout(this);
    _ButtonLayout.addView(_headerlayout2, 0);

    _ButtonLayout.setHandlerAndTimer(_timerHandler, _Timer);

    Animation amin = AnimationUtils.loadAnimation(this, R.xml.fadein);
    _ButtonLayout.setFadeinAnimation(amin);
    amin = AnimationUtils.loadAnimation(this, R.xml.fadeout);
    _ButtonLayout.setFadeoutAnimation(amin);

    _ButtonLayout.setVisibility(View.GONE);

    _footerlayout2 = new LinearLayout(this);
    _ButtonLayout.addView(_footerlayout2);

    return _ButtonLayout;
  }

  @Override
  protected void onStart() {
    super.onStart();

    RefreshControl();

    // マップコントローラ取得
    _mapController = _mapView.getController();
    _mapController.setZoom(LocationHelper.ZOOM_INIT);

    // 連携情報がある場合
    Intent it = getIntent();
    Bundle bundle = it.getBundleExtra(Const.INIT_POS);
    if (bundle != null) {
      double la = bundle.getDouble(Const.INIT_LATITUDE);
      double lo = bundle.getDouble(Const.INIT_LONGITUDE);
      GeoPoint geo = LocationHelper.getGeoPointLatLong(la, lo);
      // 現在地取得
      SetLocation(geo);

      // // サンプル
      // Drawable pin = getResources().getDrawable(R.drawable.icon_pin_ss);
      // PinItemizedOverlay pinOverlay = new PinItemizedOverlay(pin);
      // _mapView.getOverlays().add(pinOverlay);
      //
      // GeoPoint tokyo = new GeoPoint(35681396, 139766049);
      // pinOverlay.addPoint(tokyo);
      // pinOverlay.addPoint(geo);
      //
      // LineOverlay lineOverlay = new LineOverlay(tokyo, geo);
      // _mapView.getOverlays().add(lineOverlay);
    } else {
      SetInitLocation();
    }
  }

  private void SelectProvider() {

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_providerconf, null);
    // 写真挿入・撮影ボタン
    Button _gps = (Button) entryView.findViewById(R.id.provide_gps);
    Button _newwork = (Button) entryView.findViewById(R.id.provide_network);
    _gps.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        _AlertDialog.dismiss();
        StartGetLocationStatus(Const.SET_LOCATION_HIGH,true);
      }
    });
    _newwork.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        _AlertDialog.dismiss();
        StartGetLocationStatus(Const.SET_LOCATION_LOW,true);
      }
    });

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setTitle(R.string.btncapGpsLocation).setView(entryView).create();

    _AlertDialog.show();

  };

  protected void RefreshControl() {
    _contr.ClearRecord();
    _dao.list(_contr, null, null, null);
    _control = TripLogMisc.SetControlData(_contr);
  }

  protected abstract void SetInitLocation();

  @Override
  protected void onResume() {
    super.onResume();

    if (_dialog != null && _dialog.isShowing()) {
      _dialog.dismiss();
      _dialog = null;
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (_dialog != null && _dialog.isShowing()) {
      _dialog.dismiss();
      _dialog = null;
    }
  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    return super.onMenuItemSelected(featureId, item);
  }

  public void SetLocation(GeoPoint point) {
    // 現在位置の住所取得
    Message msg = new Message();
    msg.what = Const.MESSAGE_WHAT_LASTKNOWNPOINT_END;
    msg.obj = point;
    _handler.sendMessage(msg);
  }

  public void SetLocation2(GeoPoint point) {
    // 現在位置の住所取得
    Message msg = new Message();
    msg.what = Const.MESSAGE_WHAT_LASTKNOWNPOINT2_END;
    msg.obj = point;
    _handler.sendMessage(msg);
  }

  public void ScrollMap(Point po) {
    // 現在位置の住所取得
    Message msg = new Message();
    msg.what = Const.MESSAGE_WHAT_SCROLL_END;
    msg.obj = po;
    _handler.sendMessage(msg);
  }

  // 住所検索
  @Override
  public void onNewIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query = intent.getStringExtra(SearchManager.QUERY);
      searchAddressMap(query);
    }
  }

  private void searchAddressMap(final String query) {
    // 住所検索プログレスダイアログ表示

    // _dialog = new ProgressDialog(this);
    // _dialog.setIndeterminate(true);
    // _dialog.setTitle(getString(R.string.progressDialog_addressSearchTitle));
    // _dialog.setMessage(getString(R.string.progressDialog_addressSearchmsg));
    // _dialog.show();

    // 別スレッドで実行
    new Thread() {
      @Override
      public void run() {
        Message msg = null;
        try {
          msg = new Message();
          msg.what = Const.MESSAGE_WHAT_SEARCHADDRESS_END;
          msg.obj = _geo.adress2point(query.toString(), getApplication());
          _handler.sendMessage(msg);
        } catch (IOException e) {
          if (_isDebug) Log.e(LOGTAG_CLASS, e.getMessage());
          // エラーメッセージ
          msg = new Message();
          msg.what = Const.MESSAGE_WHAT_SEARCHADDRESS_ERROR;
          msg.obj = e.getMessage();
          ;
          _handler.sendMessage(msg);
        }
      }
    }.start();
  }

  // //////////////////////////////////// 位置情報の取得
  // ///////////////////////////////////////////////////
  protected void CancelLocationSearch() {
    CancelLocationSearchWithoutThread();
    _gpsthr = null;
  }

  protected void CancelLocationSearchWithoutThread() {
    if ((_gpsthr != null) && (_LocationManager != null)) {
      _LocationManager.removeUpdates(this);
      if (_dialog != null) _dialog.dismiss();
    }
    _dialog = null;
  }

  protected boolean CheckNetworkProvider(String Provider) {

    if (Provider == null) return false;

    LocationProvider locationProvider;
    locationProvider = _LocationManager.getProvider(Provider);

    if (locationProvider == null) {
      return false;
    } else {
      return true;
    }
  }

  protected void StartGetLocationStatus() {
    RefreshControl();
    StartGetLocationStatus(_control.getLocationType(), false);
  }

  protected void StartGetLocationStatus(int locationtype, boolean refreshcontrol) {

    if (!TripLogMisc.isNetworkConnected(this)) {
      Toast.makeText(this, getResources().getText(R.string.msg_network_connecterror), Toast.LENGTH_SHORT).show();
      return;
    }

    // 位置情報サービスの要求条件をピックアップする
    // 速度、電力消費などから適切な位置情報サ_control.getLocationType()ービスを選択する
    Criteria criteria = new Criteria();
    // 使える中で最も条件にヒットする位置情報サービスを取得する
    if (refreshcontrol) RefreshControl();
    if (locationtype == Const.SET_LOCATION_HIGH) {
      criteria.setAccuracy(Criteria.ACCURACY_FINE); // 精度を優先
      criteria.setPowerRequirement(Criteria.POWER_HIGH); // 許容電力消費
    } else {
      criteria.setAccuracy(Criteria.ACCURACY_COARSE);
      criteria.setPowerRequirement(Criteria.POWER_LOW); // 許容電力消費
    }
    criteria.setSpeedRequired(false); // 速度不要
    criteria.setAltitudeRequired(false); // 高度不要
    criteria.setBearingRequired(false); // 方位不要
    criteria.setCostAllowed(false); // 費用の発生不可？

    currLocationProvider = _LocationManager.getBestProvider(criteria, true);

    if (!CheckNetworkProvider(currLocationProvider)) {
      Toast.makeText(this, getResources().getText(R.string.msg_locationerror), Toast.LENGTH_SHORT).show();
      return;
    }
    StartGetLocationStatus(currLocationProvider, this);
  }

  protected void StartGetLocationStatus(String Provider, Context context) {

    // 位置情報の取得
    boolean b = true;
    Location loc = _LocationManager.getLastKnownLocation(Provider);
    if (loc != null) {
      Date dt = new Date();
      if ((dt.getTime() - loc.getTime()) < Const.LOCATIONUPDATE_MINTIME){
//      if (((dt.getTime() - loc.getTime()) < Const.LOCATIONUPDATE_MINTIME) &&
//          ((_control.getAccuracy() == 0) || (loc.getAccuracy() == -1) || (loc.getAccuracy() <= _control.getAccuracy()))){
        // 現在位置の住所取得
        Message message = new Message();
        message.what = Const.MESSAGE_WHAT_LASTKNOWNPOINT_END;
        message.obj = LocationHelper.getGeoPointLatLong(loc.getLatitude(), loc.getLongitude());
        _handler.sendMessage(message);
        b = false;
      }
    }

    if (!b) return;

    // プログレスダイアログを表示
    _dialog = new ProgressDialog(context);
    _dialog.setIndeterminate(true);
    _dialog.setOnCancelListener(new OnCancelListener() {
      public void onCancel(DialogInterface dialog) {
        CancelLocationSearch();
      }
    });

    String msg = (String) getResources().getText(R.string.msg_locationinfo);
    if (Provider.equals(LocationManager.GPS_PROVIDER)) {
      msg = msg + "\n" + getResources().getText(R.string.msg_location_gps);
    } else {
      msg = msg + "\n" + getResources().getText(R.string.msg_location_network);
    }
    _dialog.setMessage(msg);
    _dialog.show();

    _gpsthr = new GetLocationInfoThread(_handler, this, _control,(Provider.equals(LocationManager.GPS_PROVIDER)));
    _gpsthr.start();
    _LocationManager.requestLocationUpdates(Provider, Const.LOCATIONUPDATE_MINTIME, Const.LOCATIONUPDATE_MINDISTANCE, this);
  }

  public void run() {

    CancelLocationSearchWithoutThread();

    // 位置情報の取得
    if (_gpsthr != null) {
      LocationInfo info = _gpsthr.getInfo();
      if (info.isReaded()) {
        // 現在位置の住所取得
        Message msg = new Message();
        msg.what = Const.MESSAGE_WHAT_CURRENTPOS_END;
        GeoPoint searchPoint = LocationHelper.getGeoPointLatLong(info.getLatitude(), info.getLongitude());
        msg.obj = searchPoint;
        _handler.sendMessage(msg);
      } else {
        Toast.makeText(this, getResources().getText(R.string.msg_locationerror), Toast.LENGTH_SHORT).show();
      }
      _gpsthr = null;
    }
  }

  public void onLocationChanged(Location arg0) {
    try {
      if (_gpsthr == null) return;
      LocationInfo info = _gpsthr.getInfo();
      info.setProvider(arg0.getProvider());
      info.setLatitude(arg0.getLatitude());
      info.setLongitude(arg0.getLongitude());
      if (arg0.hasAccuracy()) info.setAccuracy(arg0.getAccuracy());
      if (arg0.hasAltitude()) info.setAltitude(arg0.getAltitude());
      info.setTime(arg0.getTime());
      if (arg0.hasSpeed()) info.setSpeed(arg0.getSpeed());
      if (arg0.hasBearing()) info.setBearing(arg0.getBearing());
      if (Misc.isConnected(getApplication())) {
        info.setLocation(_geo.point2address(arg0.getLatitude(), arg0.getLongitude(), getApplication()));
      } else {
        info.setLocation("");
      }
      ;
      info.setReaded(true);
      _LocationManager.removeUpdates(this);
    } catch (Exception e) {
      if (_isDebug) Log.e("onLocationChanged", "error!");
    }
  }

  public void onProviderDisabled(String paramString) {
    // TODO 自動生成されたメソッド・スタブ

  }

  public void onProviderEnabled(String paramString) {
    // TODO 自動生成されたメソッド・スタブ

  }

  public void onStatusChanged(String paramString, int status, Bundle paramBundle) {
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

  public void ShowSelectAddressDialog() {

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_addrselect, null);
    ListView listView = (ListView) entryView.findViewById(R.id.addrlist);
    // リスナーの設定
    listView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        // 現在位置の住所取得
        Message msg = new Message();
        msg.what = Const.MESSAGE_WHAT_SELECTADDRESS_END;
        msg.obj = new Integer(position);
        _handler.sendMessage(msg);
      }
    });

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_simple_line_custom);
    // アイテムを追加します
    int cnt = 0;
    String wk = "";
    for (int i = 0; i < _addrlst.size(); i++) {
      wk = CreateAddressSentense(_addrlst.get(i));
      if (wk.equals("")) continue;
      adapter.add(wk);
      cnt++;
    }
    listView.setAdapter(adapter);

    if (cnt == 0) {
      Toast.makeText(this, getResources().getText(R.string.errorDialog_searchResultText), Toast.LENGTH_SHORT).show();
      return;
    }

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon_map_w_32).setTitle(R.string.capLocationSelect).setView(entryView).create();

    _AlertDialog.show();
  }

  private String CreateAddressSentense(Address addr) {

    // StringBuffer ret = new StringBuffer();
    // if (addr.getAdminArea() != null)
    // ret.append(addr.getAdminArea()).append(" ");
    // if (addr.getLocality() != null)
    // ret.append(addr.getLocality()).append(" ");
    // if (addr.getSubAdminArea() != null)
    // ret.append(addr.getSubAdminArea()).append(" ");
    // if (addr.getFeatureName() != null)
    // ret.append(addr.getFeatureName()).append(" ");
    // if (addr.getThoroughfare() != null)
    // ret.append(addr.getThoroughfare()).append(" ");
    //
    // return ret.toString();
    StringBuffer strbuf = new StringBuffer();

    // adressをStringへ
    String buf;
    int cnt = 0;
    for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
      buf = addr.getAddressLine(i);
      if (buf == null) continue;
      if (i == 0) {
        if (!buf.equals(_location_japan)) {
          return "";
        } else {
          continue;
        }
      }
      if (cnt != 0) strbuf.append("\n");
      strbuf.append(buf);
      cnt++;
    }
    return strbuf.toString();
  }

  protected void SetMapCenter(GeoPoint searchPoint) {
    SetMapCenter(searchPoint, true);
  };

  protected void SetMapCenter(GeoPoint searchPoint, boolean doinvalidate) {
    _mapController.setCenter(searchPoint);
    _mapController.animateTo(searchPoint);
    if (doinvalidate) _mapView.invalidate();
  }

  protected void DoScrollMap(Point po) {
    _mapController.scrollBy(po.x, po.y);
    _mapView.invalidate();
  }

  // ///////////////////// メニュー関連 //////////////////////////
  // メニュー構築時に呼び出される
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    super.onCreateOptionsMenu(menu);

    // メニューアイテムの作成
    menu.add(0, Const.MENUICON_MAP_SEARCH, 0, R.string.menucap_searchmap).setIcon(android.R.drawable.ic_search_category_default);
    menu.add(0, Const.MENUICON_BOOKMARK, 0, R.string.menucap_bookmark).setIcon(R.drawable.icon_list_32);
    menu.add(0, Const.MENUICON_BOOKMARK_ADD, 0, R.string.menucap_bookmark_add).setIcon(R.drawable.icon_bookmark_32);

    return true;

  }

  // メニュー表示直前に呼び出される
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {

    return super.onPrepareOptionsMenu(menu);

  }

  // メニューの項目が呼び出された時に呼び出される
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == Const.MENUICON_MAP_SEARCH) {
      return onSearchRequested();
    } else if (item.getItemId() == Const.MENUICON_BOOKMARK_ADD) {
      return addBookmark();
    } else if (item.getItemId() == Const.MENUICON_BOOKMARK) {
      return ShowBookmark();
    }
    ;

    return false;

  }

  public boolean ShowBookmark() {

    if (_bookmark.RecordCount() == 0) _dao.list(_bookmark, new String[] {
      BookmarkRecord.CAPTION
    }, null, null);

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_addrselect, null);
    ListView listView = (ListView) entryView.findViewById(R.id.addrlist);
    // リスナーの設定
    listView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        _bookmarkcap = _bookmark.GetString(BookmarkRecord.CAPTION, position, "");
        _bookmarkpos = LocationHelper.getGeoPointLatLong(_bookmark.GetDouble(BookmarkRecord.LATITUDE, position, new Double(0)).doubleValue(), _bookmark.GetDouble(BookmarkRecord.LONGITUDE, position, new Double(0)).doubleValue());
        SetLocation(_bookmarkpos);
        _AlertDialog.dismiss();
      }
    });
    listView.setOnItemLongClickListener(new OnItemLongClickListener() {
      public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
        final int idx = position;
        TextView txt = new TextView(MapBaseForm.this.getApplication());
        txt.setTextSize(16);
        txt.setPadding(10, 5, 5, 5);
        txt.setTextColor(Color.WHITE);
        txt.setText(_bookmark.GetString(BookmarkRecord.CAPTION, position, ""));
        _AlertDialog.dismiss();
        AlertDialog dlg = new AlertDialog.Builder(MapBaseForm.this).setIcon(R.drawable.icon_trash_w_32).setTitle(R.string.menucap_bookmark_del).setView(txt).setPositiveButton(getString(R.string.capdialog_deletebutton), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            _dao.delete(_bookmark, idx);
            _bookmark.ClearRecord();
            Toast.makeText(MapBaseForm.this, getResources().getText(R.string.registbookmarkdel_ok), Toast.LENGTH_SHORT).show();
          }
        }).setNegativeButton(getString(R.string.capdialog_cancelbutton), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        }).create();

        dlg.show();
        return false;
      }
    });

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_simple_line_custom);
    // アイテムを追加します
    int cnt = 0;
    String wk = "";
    for (int i = 0; i < _bookmark.RecordCount(); i++) {
      wk = _bookmark.GetString(BookmarkRecord.CAPTION, i, "");
      if (wk.equals("")) continue;
      adapter.add(wk);
      cnt++;
    }
    listView.setAdapter(adapter);

    if (cnt == 0) {
      Toast.makeText(this, getResources().getText(R.string.errorDialog_bookmarklistResult), Toast.LENGTH_SHORT).show();
      return true;
    }

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon_map_w_32).setTitle(R.string.menucap_bookmark).setView(entryView).create();

    _AlertDialog.show();

    return true;
  }

  public boolean addBookmark() {
    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_bookmark_add, null);

    final EditText txt = (EditText) entryView.findViewById(R.id.addbookmarktext);
    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this).setIcon(R.drawable.icon_bookmark_w_32).setTitle(R.string.menucap_bookmark_add).setPositiveButton(R.string.capdialog_registbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        String wk = txt.getText().toString();
        if (wk.trim().equals("")) return;
        if (RegistBookmark(wk)) {
          Toast.makeText(MapBaseForm.this, getResources().getText(R.string.registbookmark_ok), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(MapBaseForm.this, getResources().getText(R.string.registbookmark_ng), Toast.LENGTH_SHORT).show();
        }
        ;
      }
    }).setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        _AlertDialog.dismiss();
      }
    }).setView(entryView).create();
    _AlertDialog.show();

    return true;
  }

  public boolean RegistBookmark(String txt) {
    GeoPoint pos = GetCurrentPos();
    int row = _bookmark.AddRow();
    _bookmark.SetRowId(row, null);
    _bookmark.SetString(BookmarkRecord.CAPTION, row, txt);
    _bookmark.SetDouble(BookmarkRecord.LATITUDE, row, new Double(pos.getLatitudeE6() / 1E6));
    _bookmark.SetDouble(BookmarkRecord.LONGITUDE, row, new Double(pos.getLongitudeE6() / 1E6));

    for (int i = 0; i < _bookmark.RecordCount(); i++) {
      _dao.save(_bookmark, i);
    }
    _bookmark.ClearRecord();
    _bookmarkcap = txt;
    _bookmarkpos = LocationHelper.getGeoPointLatLong(pos.getLatitudeE6() / 1E6, pos.getLongitudeE6() / 1E6);

    return true;
  }

  protected abstract GeoPoint GetCurrentPos();

  protected abstract boolean onTouchEvent(MotionEvent e, MapView mapView);

  protected abstract boolean onTrackballEvent(MotionEvent paramMotionEvent, MapView paramMapView);

  protected abstract boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent, MapView paramMapView);

  protected abstract boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent, MapView paramMapView);

  protected abstract boolean onTap(GeoPoint paramGeoPoint, MapView paramMapView);

  protected abstract void draw(Canvas paramCanvas, MapView paramMapView, boolean paramBoolean);

  protected abstract boolean draw(Canvas paramCanvas, MapView paramMapView, boolean paramBoolean, long paramLong);

  protected abstract boolean onDoubleTap(MotionEvent paramMotionEvent);

  protected abstract boolean onDoubleTapEvent(MotionEvent paramMotionEvent);

  protected abstract boolean onSingleTapConfirmed(MotionEvent paramMotionEvent);

  protected abstract boolean onDown(MotionEvent paramMotionEvent);

  protected abstract boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2);

  protected abstract void onLongPress(MotionEvent paramMotionEvent);

  protected abstract boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2);

  protected abstract void onShowPress(MotionEvent paramMotionEvent);

  protected abstract boolean onSingleTapUp(MotionEvent paramMotionEvent);

  /**
   * Overlayクラス追加
   */
  class MyOverlay extends Overlay implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {

    private GestureDetector gesture;

    // private MapView parent;

    MyOverlay(MapView mapView) {
      // parent = mapView;
      gesture = new GestureDetector(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView) {
      _ButtonLayout.setVisibility(LinearLayout.VISIBLE);
      // TODO 自動生成されたメソッド・スタブ
      gesture.onTouchEvent(e);
      if (MapBaseForm.this.onTouchEvent(e, mapView))
        return false;
      else
        return super.onTouchEvent(e, mapView);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent paramMotionEvent, MapView paramMapView) {
      if (MapBaseForm.this.onTrackballEvent(paramMotionEvent, paramMapView))
        return false;
      else
        return super.onTrackballEvent(paramMotionEvent, paramMapView);
    }

    @Override
    public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent, MapView paramMapView) {
      if (MapBaseForm.this.onKeyDown(paramInt, paramKeyEvent, paramMapView))
        return false;
      else
        return super.onKeyDown(paramInt, paramKeyEvent, paramMapView);
    }

    @Override
    public boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent, MapView paramMapView) {
      if (MapBaseForm.this.onKeyUp(paramInt, paramKeyEvent, paramMapView))
        return false;
      else
        return super.onKeyUp(paramInt, paramKeyEvent, paramMapView);
    }

    @Override
    public boolean onTap(GeoPoint paramGeoPoint, MapView paramMapView) {
      if (MapBaseForm.this.onTap(paramGeoPoint, paramMapView))
        return false;
      else
        return super.onTap(paramGeoPoint, paramMapView);
    }

    @Override
    public void draw(Canvas paramCanvas, MapView paramMapView, boolean paramBoolean) {
      MapBaseForm.this.draw(paramCanvas, paramMapView, paramBoolean);
      super.draw(paramCanvas, paramMapView, paramBoolean);
    }

    @Override
    public boolean draw(Canvas paramCanvas, MapView paramMapView, boolean paramBoolean, long paramLong) {
      if (MapBaseForm.this.draw(paramCanvas, paramMapView, paramBoolean, paramLong))
        return false;
      else
        return super.draw(paramCanvas, paramMapView, paramBoolean, paramLong);
    }

    public boolean onDoubleTap(MotionEvent paramMotionEvent) {
      return MapBaseForm.this.onTouchEvent(paramMotionEvent);
    }

    public boolean onDoubleTapEvent(MotionEvent paramMotionEvent) {
      boolean ret = MapBaseForm.this.onDoubleTapEvent(paramMotionEvent);
      if (!ret) {
        _mapView.getController().zoomIn();
      }
      return ret;
    }

    public boolean onSingleTapConfirmed(MotionEvent paramMotionEvent) {
      return MapBaseForm.this.onSingleTapConfirmed(paramMotionEvent);
    }

    public boolean onDown(MotionEvent paramMotionEvent) {
      return MapBaseForm.this.onDown(paramMotionEvent);
    }

    public boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
      return MapBaseForm.this.onFling(paramMotionEvent1, paramMotionEvent2, paramFloat1, paramFloat2);
    }

    public void onLongPress(MotionEvent paramMotionEvent) {
      MapBaseForm.this.onLongPress(paramMotionEvent);
    }

    public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
      return MapBaseForm.this.onScroll(paramMotionEvent1, paramMotionEvent2, paramFloat1, paramFloat2);
    }

    public void onShowPress(MotionEvent paramMotionEvent) {
      MapBaseForm.this.onShowPress(paramMotionEvent);
    }

    public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
      return MapBaseForm.this.onSingleTapUp(paramMotionEvent);
    }

  }

}