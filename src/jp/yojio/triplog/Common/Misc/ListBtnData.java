package jp.yojio.triplog.Common.Misc;

import android.graphics.drawable.Drawable;

public class ListBtnData {

  private Drawable _icon;
  private String _caption;

  public ListBtnData(Drawable icon, String Caption) {

    this._icon = icon;
    this._caption = Caption;

  }

  public String getCaption() {
    return _caption == null ? "" : _caption;
  }

  public Drawable getIcon() {
    return _icon;
  }

}
