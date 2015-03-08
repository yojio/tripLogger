package jp.yojio.triplog.Common.DataApi.Account;

import android.content.Intent;

public interface AuthManager {

  public abstract void doLogin(Runnable whenFinished, Object o);

  public abstract boolean authResult(int resultCode, Intent results);

  public abstract String getAuthToken();

  public abstract void invalidateAndRefresh(Runnable whenFinished);

}
