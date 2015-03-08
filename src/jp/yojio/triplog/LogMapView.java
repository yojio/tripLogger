package jp.yojio.triplog;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import jp.yojio.triplog.Common.Activity.MapBaseForm;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Map.BalloonOverlay;
import jp.yojio.triplog.Common.Map.LineOverlay;
import jp.yojio.triplog.Common.Map.LocationHelper;
import jp.yojio.triplog.Common.Map.PinItemizedOverlay;
import jp.yojio.triplog.Common.Map.BalloonOverlay.OnItemLongTapListener;
import jp.yojio.triplog.Common.Map.PinItemizedOverlay.OnItemTapListener;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.DBAccess.TagMasterRecord;
import jp.yojio.triplog.DBAccess.TranRecord;
import jp.yojio.triplog.Record.LocationDataStruc;
import jp.yojio.triplog.misc.BtnList;
import jp.yojio.triplog.misc.TripLogMisc;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

public class LogMapView extends MapBaseForm {

  private RecordBase _tran;
  private RecordBase _tagmst;
  private HashMap<String, Object> _tags = new HashMap<String, Object>();
  private ArrayList<LocationDataStruc> _datalist = new ArrayList<LocationDataStruc>();
  private PinItemizedOverlay _pin = null;
  private ArrayList<LineOverlay> _linelist = new ArrayList<LineOverlay>();
  private ArrayList<Double> _distancelist = new ArrayList<Double>();
  private BalloonOverlay _balloon = null;
  private GregorianCalendar _gc = new GregorianCalendar();
  private int _CurrentIndex = -1;
  private ImageButton _prevbtn;
  private ImageButton _nextbtn;
  private TextView _currenttxt;
  private boolean _itemcaption = false;
  private String _GroupTitle = "";
  private boolean _fromhome = false;
  private BtnList _btnlist = null;
  private boolean _showline = false;
  private boolean _back = false;
  private DisplayMetrics _metrics = new DisplayMetrics();
  private TextView _topinfo;
  double _alldistance = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.getWindowManager().getDefaultDisplay().getMetrics(_metrics);
    setTitle(getString(R.string.app_name));

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.item_mapitemcontrol, null);
    entryView.setLayoutParams(new ViewGroup.LayoutParams(Const.FP,Const.FP));

    _headerlayout.addView(entryView);
    _prevbtn = (ImageButton)entryView.findViewById(R.id.mapitem_prev);
    _prevbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        MoveCurrentData(_CurrentIndex - 1,false);
      }
    });
    _nextbtn = (ImageButton)entryView.findViewById(R.id.mapitemnext);
    _nextbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        MoveCurrentData(_CurrentIndex + 1,false);
      }
    });
    _currenttxt = (TextView)entryView.findViewById(R.id.mapitemtxt);
    _currenttxt.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        _itemcaption = !_itemcaption;
        MoveCurrentData(_CurrentIndex,false,false,false);
      }
    });
    _currenttxt.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View v) {
        MoveCurrentData(_CurrentIndex,false);
        return true;
      }
    });

    _headerlayout2.setMinimumHeight((int)(60 * _scaledDensity));
    _footerlayout.setBackgroundResource(R.drawable.background_btn);
    _footerlayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
    _footerlayout.setOrientation(LinearLayout.HORIZONTAL);
    _footerlayout.setMinimumHeight((int)(50 * _scaledDensity));
    _footerlayout2.setMinimumHeight((int)(60 * _scaledDensity));

    ImageButton btn = (ImageButton)_ButtonLayout.findViewById(R.id.map_optionbutton1);
    btn.setImageResource(R.drawable.icon_arrow_red_12);
    btn.setVisibility(View.VISIBLE);
    btn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _showline = !_showline;
        SetPinData();
        _mapView.invalidate();
      }
    });

    _topinfo = (TextView)_ButtonLayout.findViewById(R.id.map_topinfo);

    _tran = DBCommon.GetTable(_dao, DBCommon.TRAN,_isDebug);
    _tagmst = DBCommon.GetTable(_dao, DBCommon.TAG,_isDebug);

    // メニューボタン
    _btnlist = TripLogMisc.CreateButtonArea(Const.BUTTON_MAP, this,_footerlayout);

    _CurrentIndex = -1;

    InitData();

  }

  @Override
  protected void onStart() {
    super.onStart();
  };

  protected void InitData() {

    _tagmst.ClearRecord();
    _dao.list(_tagmst, new String[] { TagMasterRecord.TAG_NAME }, null, null);

    // Intent起動チェック
    boolean isInit = true;
    int i;
    int idx = -1;
    StringBuffer cond = new StringBuffer();
    String[] val = null;
    Intent it = getIntent();
    Bundle extras;
    extras = it.getBundleExtra(Const.INTENT_LIST);
    if (extras != null){
      val = extras.getStringArray(Const.INTENT_MAPDATA);
      isInit = (val == null);
      String[] wkidx = extras.getStringArray(Const.INTENT_CURRENTIDX);
      if ((wkidx != null) &&(wkidx.length > 0)) {
        idx = Integer.valueOf(wkidx[0]).intValue();
      }
      String[] cap = extras.getStringArray(Const.INTENT_GROUPCAP);
      if ((cap != null) &&(cap.length > 0)) {
//        _GroupTitle = getString(R.string.app_name) + " - " + cap[0] + " " +  getString(R.string.capdialog_listitemheader);
        _GroupTitle = cap[0] + " " +  getString(R.string.capdialog_listitemheader);
      }
      _fromhome = false;
    }else{
      _fromhome = true;
    }

    if ((_fromhome) && (_btnlist != null)){
      _btnlist.ListButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          LogMapView.this.finish();
          TripLogMisc.GoViewFunction(LogMapView.this, ".LogListView");
        }
      });
    }

    if (isInit){
      Date sdt = new Date();
      Date edt = new Date();

      // 最大値（直近）のデータ取得
      _tran.ClearRecord();
      _dao.list(_tran, null, TranRecord.REGIST_TIME + " = (select max("+ TranRecord.REGIST_TIME + ") from " + TranRecord.TABLE_NAME + ")", null);

      if (_tran.RecordCount() > 0) {
        sdt.setTime(_tran.GetDouble(TranRecord.REGIST_TIME, 0, new Double(0)).longValue());
        _gc.setTime(sdt);
        InitgcHour();
        sdt = _gc.getTime();
        _gc.add(Calendar.DAY_OF_MONTH, 1);
        _gc.add(Calendar.MILLISECOND, -1);
        edt = _gc.getTime();
        cond.append(TranRecord.REGIST_TIME).append(" >= ? AND ").append(TranRecord.REGIST_TIME).append(" <= ?");
        val = new String[2];
        val[0] = String.valueOf(sdt.getTime());
        val[1] = String.valueOf(edt.getTime());

        _GroupTitle = getString(R.string.app_name) + " - " + new SimpleDateFormat(getString(R.string.format_calender_md)).format(sdt) + " " +  getString(R.string.capdialog_listitemheader);

      }else{
        SetLocation(LocationHelper.getGeoPointLatLong(Const.INIT_POS_LATITUDE, Const.INIT_POS_LONGITUDE));
      }
    }else{
      if (extras != null) {
        for (i = 0; i < val.length; i++) {
          if (i != 0) cond.append(" OR ");
          cond.append(TranRecord.ID).append(" = ?");
        }
      }
    }

    if ((val != null) && (val.length != 0)) {
      GetTranData(cond.toString(), val);
      if (!_back) {
        if (isInit) {
          _CurrentIndex = _datalist.size() - 1;
        } else {
          if (idx == -1){
            _CurrentIndex = 0;
          }else{
            _CurrentIndex = idx;
          }
        }
      }
    }

    SetPinData();

    MoveCurrentData(_CurrentIndex,true);

    _back = false;

  }

  protected void SetPinData(){

    if ((_datalist.size() > 1) && (_showline)){
      _topinfo.setVisibility(View.VISIBLE);
    }else{
      _topinfo.setVisibility(View.INVISIBLE);
    }

    LocationDataStruc data;
    GeoPoint geo = null;
    GeoPoint geobk = null;
    LineOverlay line;
    int i;

    if (_pin != null){
      _pin.clearPoint();
      _mapView.getOverlays().remove(_pin);
    }
    _pin = new PinItemizedOverlay(getResources().getDrawable( R.drawable.icon_current_16));
    _pin.setOnItemTapListener(new OnItemTapListener() {
      public void OnItemTap(int index) {
        if ((_balloon == null) || (index != _CurrentIndex)){
          MoveCurrentData(index,false,true,false);
        }else{
          ShowCurrentInfomation(index,false);
        }
      }
    });

    // 線のクリア
    for (i=0;i<_linelist.size();i++){
      line = _linelist.get(i);
      _mapView.getOverlays().remove(line);
    }
    _linelist.clear();
    _distancelist.clear();

    _alldistance = 0;
    for (i = 0; i < _datalist.size(); i++) {
      data = _datalist.get(i);
      geo = LocationHelper.getGeoPointLatLong(data.getLatitude().doubleValue(), data.getLongitude().doubleValue());
      _pin.addPoint(geo);

      if (_showline) {
        if (i != 0) {
//          if ((geobk.getLatitudeE6() == geo.getLatitudeE6()) && (geobk.getLongitudeE6() == geo.getLongitudeE6())) continue;
          line = new LineOverlay(geobk, geo,_metrics.scaledDensity);
          _mapView.getOverlays().add(line);
          _linelist.add(line);
          _distancelist.add(new Double(TripLogMisc.GetDistance(geobk,geo)));
          _alldistance = _alldistance + _distancelist.get(_distancelist.size() - 1).doubleValue();
        }
      }
      _mapView.getOverlays().add(_pin);

      geobk = geo;
    }

     ShowCurrentInfomation(_CurrentIndex,(_balloon != null));
     // 距離インフォメーションの設定
     if ((_datalist.size() > 1) && (_showline)){
       _topinfo.setText(GetDistanceInfomation());
     }

  }

  @Override
  protected void SetInitLocation() {
  }

  protected void SetLocation(int idx,boolean defaultset){

    double la = Const.INIT_POS_LATITUDE;
    double lo = Const.INIT_POS_LONGITUDE;
    GeoPoint geo = LocationHelper.getGeoPointLatLong(la, lo);

    // 最大値（直近）のデータ取得
    LocationDataStruc data;
    if ((idx >= 0) && (idx < _datalist.size())){
      data = _datalist.get(idx);

      la = data.getLatitude().doubleValue();
      lo = data.getLongitude().doubleValue();
      geo = LocationHelper.getGeoPointLatLong(la, lo);
    }else{
      if (!defaultset) return;
    }
    SetLocation2(geo);
    ScrollMap(new Point(0,(int)(-100 * _scaledDensity)));

 }

  private void MoveCurrentData(int idx,boolean defaultset){
    MoveCurrentData(idx,true,true, defaultset);
  }

  private void MoveCurrentData(int idx,boolean setlocation,boolean showinfo,boolean defaultset){

    if (!_GroupTitle.equals("")) setTitle(_GroupTitle);

    boolean b = (_datalist.size() != 0);
    _prevbtn.setEnabled(b);
    _nextbtn.setEnabled(b);
    _currenttxt.setEnabled(b);
    _currenttxt.setText("");

    if (!b){
      _currenttxt.setText(R.string.msg_showmaperror_nodata);
      return;
    }

    if (idx < 0) idx = _datalist.size() - 1;
    if (idx >= _datalist.size()) idx = 0;
//    if (_CurrentIndex == idx) return;

    LocationDataStruc data = _datalist.get(idx);

//    _currenttxt.setText(data.getCaption());
    if (_itemcaption){
      _currenttxt.setText(data.getCaption());
    }else{
      _currenttxt.setText(new SimpleDateFormat(getString(R.string.format_calender_ymdhm2)).format(data.getDate()));
   }

    if (showinfo){
      ShowCurrentInfomation(idx);
    };

    if (_datalist.size() <= 1){
      _prevbtn.setVisibility(View.GONE);
      _nextbtn.setVisibility(View.GONE);
    }else{
      _prevbtn.setVisibility(View.VISIBLE);
      _nextbtn.setVisibility(View.VISIBLE);
    }
    _CurrentIndex = idx;
    if (!_GroupTitle.equals("")) setTitle(_GroupTitle + "  (" + String.valueOf(_CurrentIndex + 1) + "/" + String.valueOf(_datalist.size()) + ")");

    // 距離インフォメーションの設定
    if ((_datalist.size() > 0) && (_showline)){
      _topinfo.setText(GetDistanceInfomation());
    }

    if (setlocation) {
      SetLocation(idx,defaultset);
    }

  };

  private String GetDistanceInfomation(){
    String wk = "";
    if (_datalist.size() > 1){
      wk = " " + MessageFormat.format(getString(R.string.capDistanceInfo), new Object[] { TripLogMisc.GetDistanceStr(_alldistance) }) + " ";
      if (_CurrentIndex > 0){
        wk = wk + "\n " + MessageFormat.format(getString(R.string.capDistanceInfo_p), new Object[] { TripLogMisc.GetDistanceStr(_distancelist.get(_CurrentIndex - 1).doubleValue()) }) + " ";
     }
      if (_CurrentIndex < _distancelist.size()){
        wk = wk + "\n " + MessageFormat.format(getString(R.string.capDistanceInfo_n), new Object[] { TripLogMisc.GetDistanceStr(_distancelist.get(_CurrentIndex).doubleValue()) }) + " ";
     }
    }
    return wk;
  }

  public void ShowCurrentInfomation(int idx){
    ShowCurrentInfomation(idx,true);
  };

  public void ShowCurrentInfomation(int idx,boolean doCreate){

    if ((idx < 0) || (idx >= _datalist.size())) return;

    LocationDataStruc data = _datalist.get(idx);
    GeoPoint geo = LocationHelper.getGeoPointLatLong(data.getLatitude().doubleValue(), data.getLongitude().doubleValue());

    if (_balloon != null){
      _mapView.getOverlays().remove(_balloon);
      _balloon = null;
    }

    if (doCreate){
      _balloon = new BalloonOverlay(this,geo, data,_metrics.scaledDensity);
      _balloon.setOnItemLongTapListener(new OnItemLongTapListener() {
        public void OnItemLongTap() {
          ShowInfomation(_CurrentIndex);
        }
      });
      _mapView.getOverlays().add(_balloon);
    };

  };

  private void ShowInfomation(int index){

    Intent intent = new Intent();
    intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".ViewForm");
    Bundle bundle = new Bundle();
    long[] val = new long[_datalist.size()];
    LocationDataStruc data;
    for (int i=0;i<_datalist.size();i++){
      data = _datalist.get(i);
      val[i] = data.GetId();
    }
    bundle.putInt(Const.INTENT_KEY_CURRENTIDX, index);
    bundle.putLongArray(Const.INTENT_KEY_TRNID_ARR, val);
    bundle.putString(Const.INTENT_KEY_TITLE, _GroupTitle);
    intent.putExtra(Const.INTENT_INIT, bundle);
    startActivityForResult(intent, Const.REQUEST_VIEWDATA);
  };

  private void GetTranData(String cond,String[] param){

    _tran.ClearRecord();
    _dao.list(_tran, new String[] { TranRecord.REGIST_TIME}, cond, param);

    _datalist.clear();
    LocationDataStruc data;
    String wk;
    for (int i=0;i<_tran.RecordCount();i++){
      data = new LocationDataStruc();
      try {
        data.SetId(_tran.GetInt(TranRecord.ID, i, 0));
        data.setDate(new Date(_tran.GetDouble(TranRecord.REGIST_TIME, i, new Double(0)).longValue()));
        data.setNew(false);
        data.setLocationSet(true);
        data.setCaption(_tran.GetString(TranRecord.CAPTION, i, ""));
        data.SetCaptionChanged(true);
        data.setLatitude(_tran.GetDouble(TranRecord.LATITUDE, i, new Double(0)));
        data.setLongitude(_tran.GetDouble(TranRecord.LONGITUDE, i, new Double(0)));
        data.SetTags(_tran.GetString(TranRecord.TAGS, i, ""));
        data.SetTagNames(TripLogMisc.GetTagNames(_tran.GetString(TranRecord.TAGS, i, ""),_tagmst,_tags));
        data.setComment(_tran.GetString(TranRecord.COMMENT, i, ""));
        data.setTweeted((_tran.GetInt(TranRecord.TWEET, i, 0) == 1));
        data.setUploaded((_tran.GetInt(TranRecord.G_UPLOAD, i, 0) == 1));
        data.setLinkCode((_tran.GetString(TranRecord.LINKCODE, i, "")));
        data.setFileName("");
        wk = _tran.GetString(TranRecord.FILES, i, "");
        String[] files = wk.split(",");
        data.ClearFiles();
        for (int ii = 0; ii < files.length; ii++) {
          if (files[ii].trim().equals(""))
            continue;
          data.AddFile(files[ii]);
        }
      } catch (Exception e) {
      }
      _datalist.add(data);
    }
  }

  private void InitgcHour() {
    _gc.set(Calendar.HOUR_OF_DAY, 0);
    _gc.set(Calendar.MINUTE, 0);
    _gc.set(Calendar.SECOND, 0);
    _gc.set(Calendar.MILLISECOND, 0);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode != RESULT_OK) {
      return;
    }

    if (requestCode == Const.REQUEST_VIEWDATA) {
      Bundle extras = data.getBundleExtra(Const.INTENT_VIEW);
      if (extras != null){
        _CurrentIndex = extras.getInt(Const.INTENT_KEY_CURRENTIDX,-1);
        if (extras.getBoolean(Const.INTENT_KEY_CHANGEDATA,false)){
          _back = true;
          InitData();
        }else{
          MoveCurrentData(_CurrentIndex, false);
        }
      }
    }

  }


  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    return super.dispatchKeyEvent(event);
  }

  @Override
  protected GeoPoint GetCurrentPos() {
    return _mapView.getMapCenter();
  }

  @Override
  protected void draw(Canvas paramCanvas, com.google.android.maps.MapView paramMapView, boolean paramBoolean) {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  protected boolean draw(Canvas paramCanvas, com.google.android.maps.MapView paramMapView, boolean paramBoolean, long paramLong) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onDoubleTap(MotionEvent paramMotionEvent) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onDoubleTapEvent(MotionEvent paramMotionEvent) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onDown(MotionEvent paramMotionEvent) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent, com.google.android.maps.MapView paramMapView) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent, com.google.android.maps.MapView paramMapView) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected void onLongPress(MotionEvent paramMotionEvent) {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  protected boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected void onShowPress(MotionEvent paramMotionEvent) {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  protected boolean onSingleTapConfirmed(MotionEvent paramMotionEvent) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onSingleTapUp(MotionEvent paramMotionEvent) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onTap(GeoPoint paramGeoPoint, com.google.android.maps.MapView paramMapView) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onTouchEvent(MotionEvent e, com.google.android.maps.MapView mapView) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onTrackballEvent(MotionEvent paramMotionEvent, com.google.android.maps.MapView paramMapView) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

}
