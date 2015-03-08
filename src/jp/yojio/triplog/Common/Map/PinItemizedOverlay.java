package jp.yojio.triplog.Common.Map;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;

public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem> {

  private List<GeoPoint> points = new ArrayList<GeoPoint>();
  OnItemTapListener onItemTapListener = null;

  public PinItemizedOverlay(Drawable defaultMarker) {
    super(boundCenterBottom(defaultMarker));
  }

  @Override
  protected PinOverlayItem createItem(int i) {

    if (points.size() <= i) return null;

    GeoPoint point = points.get(i);
    return new PinOverlayItem(point);
  }

  @Override
  public int size() {
    return points.size();
  }

  public void addPoint(GeoPoint point) {

    this.points.add(point);
    populate();
  }

  public void clearPoint() {
    this.points.clear();
    populate();
  }

  @Override
  public boolean onTap(int index) {
//    PinOverlayItem x = getItem(index);

    if (onItemTapListener != null){
      if (this.size() > index) onItemTapListener.OnItemTap(index);
    }

  return true;
  }

  // Allows the user to set an Listener and react to the event
  public void setOnItemTapListener(OnItemTapListener listener) {
    onItemTapListener = listener;
  }

  //Define our custom Listener interface
  public interface OnItemTapListener {
    public abstract void OnItemTap(int index);
  }
}

