package jp.yojio.triplog.Common.DataApi.Account;

import android.app.Activity;

public interface AuthActivity {

  public abstract Activity getInstanse();
  public abstract AccountChooser getAccountChooser();

}
