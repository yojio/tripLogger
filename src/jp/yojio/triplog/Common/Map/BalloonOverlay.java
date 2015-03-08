package jp.yojio.triplog.Common.Map;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import jp.yojio.triplog.R;
import jp.yojio.triplog.Common.Misc.ImageInfo;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.Record.LocationDataStruc;
import jp.yojio.triplog.misc.TripLogMisc;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class BalloonOverlay extends Overlay implements GestureDetector.OnGestureListener {

  static int SPC = 40;
  static int CONTENT_WIDTH = 164;
  static int DEF_COLOR = Color.argb(230, 255, 255, 255);
  static int SELECT_COLOR = Color.argb(230, 255, 199, 50);

  private GeoPoint _geopoint;
  private LocationDataStruc _data;
  private Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private Paint _txtpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private PointF _pt1 = new PointF();
  private PointF _pt2 = new PointF();
  private PointF _pt3 = new PointF();
  private SimpleDateFormat _df = null;
  private Context _context;
  private int _offset = 5;
  private RectF _rc;
  private RectF _ContentRect = new RectF(0,0,0,0);
  private Path _path_c = new Path();
  private Path _path_s = new Path();
  private Path _path_wk = new Path();
  private ArrayList<String> _slst;
  private ArrayList<String> _slst2;
  private ArrayList<String> _slst3;
  private ArrayList<Bitmap> _slst_bm;
  private OnItemLongTapListener onItemLongTapListener = null;
  private GestureDetector _gesture;
  private int _bkcolor = DEF_COLOR;
  private int _bkcolor_sel = SELECT_COLOR;
  private float _scaledDensity;
  private Bitmap _bm;

  public BalloonOverlay(Context context,GeoPoint point,LocationDataStruc data,float scaledDensity) {

    _gesture = new GestureDetector(this);
    _scaledDensity = scaledDensity;

    SPC = (int)(40 * _scaledDensity);
    CONTENT_WIDTH = (int)(164 * _scaledDensity);
    _offset = (int)(5 * _scaledDensity);

    this._geopoint = point;
    this._data = data;
    this._context = context;
    _df = new SimpleDateFormat(_context.getString(R.string.format_calender_ymdhm2));
    _paint.setAntiAlias(true);
    _paint.setDither(true);
    _txtpaint.setAntiAlias(true);
    _txtpaint.setDither(true);
    _rc = getContentRect(); // コンテンツサイズ
    CreateObject();
    SetBitmap();

  }

  private void SetBitmap(){

    _slst_bm = new ArrayList<Bitmap>();
    ImageInfo im;
    Bitmap bms;
    for (int i=0;i<_data.FileCount();i++){
      try{
        im = Misc.GetImageInfo(_data.GetFile(i));
        bms = TripLogMisc.getThumbnailBitmap(_context, im.idx, im.uri);
        bms = Bitmap.createScaledBitmap(bms, (int)(50 * _scaledDensity), (int)(50 * _scaledDensity), true);
        _slst_bm.add(bms);
      } catch (Exception e){
//        _slst_bm.add(null);
      }
    };

    _bm = BitmapFactory.decodeResource(_context.getResources(), R.drawable.icon_twit_12);

  }

  private RectF getContentRect(){

    int i;
    float h = 25 * _scaledDensity;
    FontMetrics fontMetrics = _txtpaint.getFontMetrics();

    _txtpaint.setTextSize(12 * _scaledDensity);
    _txtpaint.setTypeface(Typeface.DEFAULT_BOLD);
    _slst = DivideString(_data.getCaption(),CONTENT_WIDTH,_offset,_txtpaint,3);

    if (!_data.getComment().equals("")){
      _txtpaint.setTextSize(10 * _scaledDensity);
      _txtpaint.setTypeface(Typeface.DEFAULT);
      _slst2 = DivideString(_data.getComment(),CONTENT_WIDTH,_offset,_txtpaint,6 - _slst.size());
    }else{
      _slst2 = null;
    }

    _txtpaint.setTextSize(10 * _scaledDensity);
    _txtpaint.setTypeface(Typeface.DEFAULT);
    _slst3 = DivideString(_data.GetTagNames(),CONTENT_WIDTH,_offset,_txtpaint,1);


    _txtpaint.setTextSize(12 * _scaledDensity);
    _txtpaint.setTypeface(Typeface.DEFAULT_BOLD);
    float rh = fontMetrics.bottom - fontMetrics.top - 1;
    for (i = 0;i<_slst.size();i++){
      h = h + rh;
    }
    h = h + (12 * _scaledDensity);

    if (!_data.getComment().equals("")){
      h = h + 8 * _scaledDensity;
      _txtpaint.setTextSize(10 * _scaledDensity);
      _txtpaint.setTypeface(Typeface.DEFAULT);
      rh = fontMetrics.bottom - fontMetrics.top - 1;
      for (i = 0;i<_slst2.size();i++){
        h = h + rh;
      }
    }

    if (!_data.GetTagNames().trim().equals("")){
      h = h + 18 * _scaledDensity;
    }

    if (_data.FileCount() != 0){
      h = h + 58 * _scaledDensity;
    }

    if (_data.FileCount() > 3){
      h = h + 10 * _scaledDensity;
    }
    return new RectF(0,0,CONTENT_WIDTH,h);
  }

  private void CreateObject(){

    _path_c.reset();
    _path_c.addRoundRect(_rc, 5 * _scaledDensity, 5 * _scaledDensity, Path.Direction.CW);

    // 吹き出し作成
    _pt1.set(_rc.width() / 2, _rc.height() + SPC / 2);
    float unit = _rc.width() / 5;
    _pt2.set(_rc.left + (unit * 3), _rc.bottom);
    _pt3.set(_rc.left + (unit * 4), _rc.bottom);

    // Path path2 = new Path();
    _path_c.moveTo(_pt1.x, _pt1.y);
    _path_c.lineTo(_pt2.x, _pt2.y - 2);
    _path_c.lineTo(_pt3.x, _pt3.y - 2);
    _path_c.close();
    // path.addPath(path2);

    _path_s.set(_path_c);
    Matrix matrix = new Matrix();
    // matrix.setRotate(10);
    matrix.setScale(1.1F, 0.7F, _rc.width() / 2, _rc.height() + SPC / 2);
    _path_s.transform(matrix);
    matrix.setSkew(-1.0F, 0.0F);
    // matrix.setSinCos(-0.12F, 0.0F);
    _path_s.transform(matrix);

  }

  @Override
  public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    super.draw(canvas, mapView, shadow);

    Projection projection = mapView.getProjection();
    Point pt = projection.toPixels(_geopoint, null);

    float x = pt.x - (_rc.width() / 2);
    float y = pt.y - _rc.height() - SPC;
    _paint.setStrokeWidth(3);
    if (!shadow){
      _path_wk.reset();
      _path_c.offset(x,y,_path_wk);
      _paint.setStyle(Paint.Style.FILL_AND_STROKE);
      _paint.setColor(Color.DKGRAY);
      _paint.setAlpha(150);

      canvas.drawPath(_path_wk, _paint);
      _paint.setColor(_bkcolor);
      _paint.setStyle(Paint.Style.FILL);
      canvas.drawPath(_path_wk, _paint);

      DrawContent(canvas,_rc,new PointF(x,y));
      _ContentRect.set(_rc);
    }else{
      _path_wk.reset();
//      PointF ptc = new PointF(pt.x - (rc.width() / 2) + rc.width() + 20, pt.y - rc.height() - SPC);
      _paint.setStyle(Paint.Style.FILL_AND_STROKE);
      _paint.setColor(Color.DKGRAY);
      _paint.setAlpha(70);
      _path_s.offset(x + (_rc.height() / 2) + (_rc.width() / 2) + (25 * _scaledDensity),y,_path_wk);
      canvas.drawPath(_path_wk, _paint);
    };

//    _paint.setColor(Color.argb(255, 102, 102, 102));
//    _paint.setStyle(Paint.Style.STROKE);
//    canvas.drawPath(path, _paint);

  }

  private void DrawContent(Canvas canvas, RectF rc,PointF pt){

     rc.offsetTo(pt.x, pt.y);

    RectF drc = new RectF();

    // 時間の表示
    _txtpaint.setTextAlign(Paint.Align.LEFT);
    _txtpaint.setTextSize(12 * _scaledDensity);
    _txtpaint.setTypeface(Typeface.DEFAULT_BOLD);
    _txtpaint.setColor(Color.BLUE);
    FontMetrics fontMetrics = _txtpaint.getFontMetrics();
    float h = fontMetrics.bottom - fontMetrics.top;
    drc.set(rc.left,rc.top,rc.right,rc.top + h);
    String wk = _df.format(_data.getDate());
    PointF base = Misc.GetTextPos(Misc.TEXT_LEFT, wk, drc, _txtpaint);
    canvas.drawText(wk, base.x, base.y, _txtpaint);
    if (_data.isTweeted()){
      canvas.drawBitmap(_bm, drc.left + _txtpaint.measureText(wk) + (10 * _scaledDensity), drc.top + ((drc.height() - _bm.getHeight()) / 2), _paint);
    }


    int rowcnt = 0;
    // キャプションの描画
    _txtpaint.setColor(Color.DKGRAY);
    wk = _data.getCaption();
    int i;
    float ah = 0;
    drc.offset(0, h);
    h =fontMetrics.bottom - fontMetrics.top - 3;
    drc.set(drc.left,drc.top,drc.right,drc.top + h);

    for (i=0;i<_slst.size();i++){
      ah = ah + h;
      wk = _slst.get(i);
      base = Misc.GetTextPos(Misc.TEXT_LEFT, wk, drc, _txtpaint,_offset);
      canvas.drawText(wk, base.x, base.y, _txtpaint);
      drc.offset(0, h);
      rowcnt++;
    };

    if (_data.FileCount() > 0){
      // 分割線の描画
      drc.offset(0, drawline(canvas,drc) + 2);

      _paint.setAlpha(255);
      float l = 5;
      h = 0;
      Bitmap bms;
      for (i=0;i<_slst_bm.size();i++){
        bms = _slst_bm.get(i);
        if (bms == null) continue;
        canvas.drawBitmap(bms, drc.left + l, drc.top, _paint);
        h = bms.getHeight();
        l = l + bms.getWidth() + 2;
        if (i == 2) break;
      };

      drc.offset(0, h);
      if (_slst_bm.size() > 3){
        _txtpaint.setColor(Color.GRAY);
        _txtpaint.setTextSize(10 * _scaledDensity);
        _txtpaint.setTypeface(Typeface.DEFAULT);
        wk = _context.getString(R.string.msg_filecount_over);
        base = Misc.GetTextPos(Misc.TEXT_LEFT, wk, drc, _txtpaint,5);
        canvas.drawText(wk, base.x, base.y, _txtpaint);
        drc.offset(0, 10);
      }
    }

    // コメントの描画
    if ((_slst2 != null) && (_slst2.size() > 0)){
      // 分割線の描画
      drc.offset(0, drawline(canvas,drc));

      _txtpaint.setColor(Color.DKGRAY);
      _txtpaint.setTextSize(10 * _scaledDensity);
      _txtpaint.setTypeface(Typeface.DEFAULT);
      h =fontMetrics.bottom - fontMetrics.top - 3;
      drc.set(drc.left,drc.top,drc.right,drc.top + h);
      for (i=0;i<_slst2.size();i++){
        ah = ah + h;
        wk = _slst2.get(i);
        base = Misc.GetTextPos(Misc.TEXT_LEFT, wk, drc, _txtpaint,_offset);
        canvas.drawText(wk, base.x, base.y, _txtpaint);
        drc.offset(0, h);
      };
      drc.offset(0, 3);
    }

    if (_slst3.size() > 0) {
      _txtpaint.setColor(Color.BLUE);
      _txtpaint.setTextSize(10 * _scaledDensity);
      _txtpaint.setTypeface(Typeface.DEFAULT);
      wk = _slst3.get(0);
      h = fontMetrics.bottom - fontMetrics.top;
      drc.set(rc.left, rc.bottom - h - 5, rc.right, rc.bottom);
      drawline(canvas, drc, 0.6F);
      drc.offset(0, 2 * _scaledDensity);
      base = Misc.GetTextPos(Misc.TEXT_LEFT, wk, drc, _txtpaint, (int)(5 * _scaledDensity));
      canvas.drawText(wk, base.x, base.y, _txtpaint);
    }

  }

  private float drawline(Canvas canvas,RectF drc){
    return drawline(canvas,drc,2);
  };

  private float drawline(Canvas canvas,RectF drc,float StrokeWidth){
    _paint.setAntiAlias(true);
    _paint.setColor(Color.DKGRAY);
    _paint.setAlpha(100);
    _paint.setStrokeWidth(StrokeWidth);
    _paint.setStyle(Paint.Style.STROKE);
    float wk = 5  * _scaledDensity;
    canvas.drawLine(drc.left + wk, drc.top + wk, drc.right - wk, drc.top + wk, _paint);
    return 8;
  }

  private ArrayList<String> DivideString(String value,float width,int offset,Paint paint,int maxlength){

    float w = width - (offset * 2);
    String wk = "";
    String wk2 = "";
    ArrayList<String> ret = new ArrayList<String>();
    int cnt = 0;

    for (int i=0;i<value.length();i++){
      wk = wk + value.charAt(i);
      if ((paint.measureText(wk) > w) || (String.valueOf(value.charAt(i)).equals("\n"))){
        if (cnt >= maxlength - 1){
          if (wk2.length() > 2){
            wk2 = wk2.substring(0,wk2.length() - 2) + "...";
          }else{
            wk2 = wk2 + "...";
          }
          ret.add(wk2.replace("\n", ""));
          return ret;
        }else{
          ret.add(wk2.replace("\n", ""));
          wk = "" + value.charAt(i);
          wk = wk.replace("\n", "");
          cnt++;
        };
      };
      wk2 = wk;
    }
    wk = wk.replace("\n", "");
    if (!wk.trim().equals("")) ret.add(wk);

    return ret;

  }

  private boolean CheckInContent(PointF pt){

    if ((_ContentRect.left <= pt.x) &&
        (_ContentRect.right >= pt.x) &&
        (_ContentRect.top <= pt.y) &&
        (_ContentRect.bottom >= pt.y)){
      return true;
    }else{
      return false;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent e, MapView mapView) {
    _gesture.onTouchEvent(e);
    return false;
  }

  public boolean onDown(MotionEvent e) {

    if (CheckInContent(new PointF(e.getX(),e.getY()))) _bkcolor = _bkcolor_sel;
    return false;

  }

  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  public void onLongPress(MotionEvent e) {
    if ((onItemLongTapListener != null) && (CheckInContent(new PointF(e.getX(),e.getY())))){
      onItemLongTapListener.OnItemLongTap();
    }
    _bkcolor = DEF_COLOR;
  }

  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    _bkcolor = DEF_COLOR;
    return false;
  }

  public void onShowPress(MotionEvent e) {
  }

  public boolean onSingleTapUp(MotionEvent e) {
    _bkcolor = DEF_COLOR;
    return false;
  }

  // Allows the user to set an Listener and react to the event
  public void setOnItemLongTapListener(OnItemLongTapListener listener) {
    onItemLongTapListener = listener;
  }

  //Define our custom Listener interface
  public interface OnItemLongTapListener {
    public abstract void OnItemLongTap();
  }

}














