package jp.yojio.triplog.Common.DataApi.Account;

import android.os.Build;
import android.os.Bundle;

public class AuthManagerFactory {

  private AuthManagerFactory() {
  }

  public static boolean useModernAuthManager() {
    return Integer.parseInt(Build.VERSION.SDK) >= 7;
  }

  public static AuthManager getAuthManager(AuthActivity activity, int code, Bundle extras, boolean requireGoogle, String service) {
    if (useModernAuthManager()) {
      return new ModernAuthManager(activity, code, extras, requireGoogle, service);
    } else {
      return new AuthManagerOld(activity.getInstanse(), code, extras, requireGoogle, service);
    }
  }

}
