package jp.yojio.triplog.Common.Misc;


public class CheckableData {

  private boolean _checked;
  private String _caption;

  public CheckableData(String Caption,boolean Checked) {

    this._checked = Checked;
    this._caption = Caption;

  }

  public String getCaption() {
    return _caption == null ? "" : _caption;
  }

  public boolean isChecked() {
    return _checked;
  }

  public void setChecked(boolean value) {
    _checked = value;
  }

}
