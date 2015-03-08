package jp.yojio.triplog;

import jp.yojio.triplog.Common.Activity.MapBaseForm;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Map.LocationHelper;
import jp.yojio.triplog.Common.Map.PinItemizedOverlay;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.DBAccess.TranRecord;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class SearchMapForm extends MapBaseForm {

  private PinItemizedOverlay _pin = null;
  private GeoPoint _pos = LocationHelper.getGeoPointLatLong(Const.INIT_POS_LATITUDE, Const.INIT_POS_LONGITUDE);
  private RecordBase _tran;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    _tran = DBCommon.GetTable(_dao, DBCommon.TRAN,_isDebug);

    _pin = new PinItemizedOverlay(getResources().getDrawable( R.drawable.icon_current_16));
    _mapView.getOverlays().add(_pin);
    // レイアウトの設定
    _headerlayout.setBackgroundColor(Color.WHITE);
    TextView txt = new TextView(this);
    txt.setTextSize(12/* * _scaledDensity*/);
    txt.setTextColor(Color.DKGRAY);
    txt.setBackgroundColor(Color.TRANSPARENT);
    txt.setLayoutParams(new ViewGroup.LayoutParams(Const.FP,Const.FP));
    txt.setGravity(Gravity.CENTER);
    txt.setText(R.string.msg_setlocation_info);
    txt.setMaxHeight((int)(25 * _scaledDensity));
    _headerlayout.addView(txt);
    _headerlayout.setMinimumHeight((int)(25 * _scaledDensity));
    _headerlayout2.setMinimumHeight((int)(25 * _scaledDensity));

    _footerlayout.setBackgroundColor(Color.WHITE);
    _footerlayout.setGravity(Gravity.CENTER);
    _footerlayout.setMinimumHeight((int)(25 * _scaledDensity));
    _footerlayout.setBackgroundResource(R.drawable.border);
    _footerlayout2.setMinimumHeight((int)(40 * _scaledDensity));
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    int width = metrics.widthPixels;  // 横幅サイズを取得
    ImageButton imgbtn = new ImageButton(this);
//    imgbtn.setMinimumWidth((int)(150 * _scaledDensity));
    imgbtn.setMinimumWidth(width);
    imgbtn.setMinimumHeight((int)(40 * _scaledDensity));
    imgbtn.setImageResource(R.drawable.icon_confirm_32);
    imgbtn.setBackgroundResource(android.R.drawable.list_selector_background);
    imgbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        // 確定通知
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putDouble(Const.RESULT_LATITUDE, _pos.getLatitudeE6() / 1E6);
        bundle.putDouble(Const.RESULT_LONGITUDE, _pos.getLongitudeE6() / 1E6);
        if ((_pos.getLatitudeE6() == _bookmarkpos.getLatitudeE6()) &&
            (_pos.getLongitudeE6() == _bookmarkpos.getLongitudeE6())){
          bundle.putString(Const.RESULT_BOOKMARKFLG, Const.RESULT_YES);
        }else{
          bundle.putString(Const.RESULT_BOOKMARKFLG, Const.RESULT_NO);
        }
        bundle.putString(Const.RESULT_BOOKMARKCAP,_bookmarkcap);

        intent.putExtra(Const.RESULT_POS, bundle);
        setResult(Activity.RESULT_OK,intent);
        finish();
      }
    });

    _footerlayout.addView(imgbtn);

  }

  @Override
  protected void SetInitLocation() {

    double la = Const.INIT_POS_LATITUDE;
    double lo = Const.INIT_POS_LONGITUDE;
    GeoPoint geo = LocationHelper.getGeoPointLatLong(la, lo);

    // 最大値（直近）のデータ取得
    _tran.ClearRecord();
    _dao.list(_tran, null, TranRecord.ID + " = (select max(" + TranRecord.ID + ") from " + TranRecord.TABLE_NAME + ")", null);

    if (_tran.RecordCount() > 0) {
      la = _tran.GetDouble(TranRecord.LATITUDE, 0,new Double(0)).doubleValue();
      lo = _tran.GetDouble(TranRecord.LONGITUDE, 0, new Double(0)).doubleValue();
      geo = LocationHelper.getGeoPointLatLong(la, lo);
    }
    SetLocation(geo);

  }

  private void SetMapPin(GeoPoint searchPoint){
    if (_pin != null){
      _pin.clearPoint();
      _pin.addPoint(searchPoint);
    };
    _pos = searchPoint;
  }

  @Override
  protected void SetMapCenter(GeoPoint searchPoint){

    super.SetMapCenter(searchPoint);
    SetMapPin(searchPoint);
  }

  @Override
  protected boolean onTouchEvent(MotionEvent e, MapView mapView) {
    return true;
  }

  @Override
  protected void onLongPress(MotionEvent paramMotionEvent) {
    GeoPoint temp = _mapView.getProjection().fromPixels((int)paramMotionEvent.getX(), (int)paramMotionEvent.getY());
    SetMapPin(temp);
  }

  @Override
  protected boolean onTap(GeoPoint paramGeoPoint, MapView paramMapView) {
    return true;
  }

  @Override
  protected void draw(Canvas paramCanvas, MapView paramMapView, boolean paramBoolean) {
    // TODO 自動生成されたメソッド・スタブ

  }

  @Override
  protected boolean draw(Canvas paramCanvas, MapView paramMapView, boolean paramBoolean, long paramLong) {
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
  protected boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent, MapView paramMapView) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent, MapView paramMapView) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
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
  protected boolean onTrackballEvent(MotionEvent paramMotionEvent, MapView paramMapView) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  @Override
  protected GeoPoint GetCurrentPos() {
    return _pos;
  }

}
