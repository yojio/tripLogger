package jp.yojio.triplog.Common.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapCenterOverlay extends Overlay {

  private Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private float _radious;

  public MapCenterOverlay(float scaledDensity) {
    _radious = 5 * scaledDensity;
    _paint.setAntiAlias(true);
    _paint.setDither(true);
    _paint.setStyle(Paint.Style.STROKE);
    _paint.setColor(Color.GRAY);
    _paint.setStrokeWidth(3 * scaledDensity);
    _paint.setAlpha(70);
  }

  @Override
  public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    super.draw(canvas, mapView, shadow);
    float x = ((mapView.getWidth() - (_radious * 2)) / 2);
    float y = ((mapView.getHeight() - (_radious * 2)) / 2);
    canvas.drawCircle(x, y, _radious, _paint);

  }

}

