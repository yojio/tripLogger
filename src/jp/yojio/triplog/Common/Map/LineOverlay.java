package jp.yojio.triplog.Common.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class LineOverlay extends Overlay {

  private final int LINE_STEP_START_UP = 60;
  private final int LINE_STEP_START_DOWN = 60;
  private final int LINE_STEP = 170;
  private final int THETA = 20;
  private final int LINE_LEN = 20;

  private GeoPoint _geoStart;
  private GeoPoint _geoEnd;
  private Paint _paint;
  private float _scaledDensity;

//  private MaskFilter  mEmboss;

  public LineOverlay(GeoPoint geoStart, GeoPoint geoEnd,float scaledDensity) {
    this._geoStart = geoStart;
    this._geoEnd = geoEnd;

    _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    _paint.setColor(Color.RED);
    _paint.setStyle(Paint.Style.STROKE);
    _paint.setStrokeWidth(3);
    _paint.setAntiAlias(true);
    _paint.setDither(true);
    _paint.setAlpha(150);
    _scaledDensity = scaledDensity;
//    mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 3 }, 0.8f, 8, 3f);
  }


  @Override
  public void draw(Canvas canvas,MapView mapView, boolean shadow) {
    super.draw(canvas, mapView, shadow);

    if (!shadow) {
      if ((_geoStart.getLatitudeE6() == _geoEnd.getLatitudeE6()) &&
          (_geoStart.getLongitudeE6() == _geoEnd.getLongitudeE6())) return;

      Projection projection = mapView.getProjection();
      Point pxStart = projection.toPixels(_geoStart, null);
      pxStart.y = pxStart.y - 6;
      Point pxEnd = projection.toPixels(_geoEnd, null);
      pxEnd.y = pxEnd.y - 6;
      canvas.drawLine(pxStart.x, pxStart.y, pxEnd.x, pxEnd.y, _paint);
      drawArrows(canvas,pxStart,pxEnd);
    }
  }

  private void drawArrows(Canvas canvas,Point pxStart,Point pxEnd){

    PointF s = new PointF(pxStart.x,pxStart.y);
    PointF e = new PointF(pxEnd.x,pxEnd.y);

    if ((Math.abs(e.x - s.x) < 50) && (Math.abs(e.y - s.y) < 50)) return;

    PointF wk = new PointF(0,0);
    int flg = s.x < e.x?1:-1;
    int step = LINE_STEP_START_UP;
    if (s.y > e.y) step = LINE_STEP_START_DOWN;

    float len = LINE_LEN * _scaledDensity;
    Path path;
    int cnt = 0;

    _paint.setStyle(Paint.Style.FILL);
    _paint.setAntiAlias(true);
    _paint.setDither(false);
    _paint.setAlpha(200);
    while (cnt < 200){
      if ((Math.abs(e.x - wk.x) < 20) && (Math.abs(e.y - wk.y) < 20)) break;
      wk.x = (float) (s.x + ((step /  Math.sqrt(1 + Math.pow((s.y - e.y) / (s.x - e.x), 2))) * flg));
      wk.y = (float) (s.y + ((((step /  Math.sqrt(1 + Math.pow((s.y - e.y) / (s.x - e.x), 2))) * flg)) * ((s.y - e.y) / (s.x - e.x))));

      if (((flg == 1) && (wk.x > e.x)) || ((flg == -1) && (wk.x < e.x))) {
        break;
//        wk.set(e);
      }

      if ((wk.x >= 0) &&
          (wk.y >= 0) &&
          (wk.x <= canvas.getWidth() + len) &&
          (wk.y <= canvas.getHeight() + len)){
        path = CreateArrow(s, wk,len);
        canvas.drawPath(path, _paint);
      }

      if ((wk.x == e.x) && (wk.y == e.y)){
        break;
      }else{
        step = (int) (step + (LINE_STEP * _scaledDensity));
      }

      cnt++;
    }
    _paint.setStyle(Paint.Style.STROKE);
    _paint.setStrokeWidth(3);
    _paint.setAntiAlias(true);
    _paint.setDither(true);
    _paint.setAlpha(150);
    path = null;

  }

  private Path CreateArrow(PointF pxStart,PointF pxPoint,float len){

    Path path = new Path();

    double wkThete = THETA * Math.PI / 180;
    double Pai = Math.atan(Math.abs((pxStart.y - pxPoint.y) / (pxStart.x - pxPoint.x)));
    double Psi = Pai - wkThete;
    double Rho = Pai + wkThete;

    PointF[] pts = new PointF[]{new PointF(0,0),new PointF(0,0),new PointF(0,0)};

    if (pxPoint.x >= pxStart.x) {
      pts[0].x = Math.round(pxPoint.x - len * Math.cos(Psi));
      pts[2].x = Math.round(pxPoint.x - len * Math.sin(Math.PI / 2 - Rho));
    }else{
      pts[0].x = Math.round(pxPoint.x + len * Math.cos(Psi));
      pts[2].x = Math.round(pxPoint.x + len * Math.sin(Math.PI / 2 - Rho));
    }

    if (pxPoint.y >= pxStart.y) {
      pts[0].y = Math.round(pxPoint.y - len * Math.sin(Psi));
      pts[2].y = Math.round(pxPoint.y - len * Math.cos(Math.PI / 2 - Rho));
    }else{
      pts[0].y = Math.round(pxPoint.y + len * Math.sin(Psi));
      pts[2].y = Math.round(pxPoint.y + len * Math.cos(Math.PI / 2 - Rho));
    }
    pts[1].x = pxPoint.x;
    pts[1].y = pxPoint.y;

    path.moveTo(Math.round(pts[0].x), Math.round(pts[0].y));
    path.lineTo(pts[1].x, pts[1].y);
    path.lineTo(pts[2].x, pts[2].y);
    path.close();

    return path;
  }

}












