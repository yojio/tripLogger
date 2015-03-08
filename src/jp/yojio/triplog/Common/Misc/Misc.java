package jp.yojio.triplog.Common.Misc;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public final class Misc {

  public static final int TEXT_LEFT = 0;
  public static final int TEXT_CENTER = 1;
  public static final int TEXT_RIGHT = 2;

  public static boolean isConnected(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo ni = cm.getActiveNetworkInfo();
    if (ni != null) {
      return cm.getActiveNetworkInfo().isConnected();
    }
    return false;
  }

  public static String DoubleToString(Double val, int Digits) {
    try {
      DecimalFormat df = new DecimalFormat();
      df.applyPattern("0");
      df.setMaximumFractionDigits(Digits);
      df.setMinimumFractionDigits(Digits);
      Double objnum = new Double(val.doubleValue());
      return df.format(objnum);
    } catch (Exception e) {
      // エラーをキャッチする必要があります。
      return "error!";
    }
  }

  public static String GetLATString(Double val) {
    return "LAT." + DoubleToString(val, 6);
  }

  public static String GetLNGString(Double val) {
    return "LNG." + DoubleToString(val, 6);
  }

  public static String getFileName(String path) {
    File f = new File(path);
    return f.getName();
  }

  public static String getParentPath(String path) {
    File f = new File(path);
    return f.getParent();
  }

  public static PointF GetTextPos(int pattern, String Caption, RectF rc, Paint txtpaint) {
    return GetTextPos(pattern, Caption, rc, txtpaint, 5);
  }

  public static PointF GetTextPos(int pattern, String Caption, RectF rc, Paint txtpaint, int offset) {

    FontMetrics fontMetrics = txtpaint.getFontMetrics();

    // 文字列の幅を取得
    float textWidth = txtpaint.measureText(Caption);

    float baseX = rc.centerX() - textWidth / 2;
    float baseY = rc.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2;

    if (pattern == TEXT_LEFT) {
      baseX = rc.left + offset;
    } else if (pattern == TEXT_RIGHT) {
      baseX = rc.right - textWidth - offset;
    }
    ;

    return new PointF(baseX, baseY);

  }

  public static float GetTextHeight(String Caption, FontMetrics fontMetrics) {
    return fontMetrics.ascent + fontMetrics.descent;
  }

  public static ImageInfo GetImageInfo(String arg) {
    int idx;
    ImageInfo im;
    idx = arg.lastIndexOf("/");
    if (idx == -1)
      return null;
    im = new ImageInfo();
    try{
      im.uri = Uri.parse(arg.substring(0, idx));
      im.idx = Long.parseLong(Uri.parse(arg).getLastPathSegment());
    }catch(Exception e){
      im.uri = Uri.parse("");
      im.idx = -1;
    }
    return im;
  }

  public static ArrayList<ImageInfo> GetImageInfo(String[] arg) {
    ArrayList<ImageInfo> ret = new ArrayList<ImageInfo>();

    int idx;
    ImageInfo im;
    for (int i = 0; i < arg.length; i++) {
      idx = arg[i].lastIndexOf("/");
      if (idx == -1)
        continue;
      im = new ImageInfo();
      try{
        im.uri = Uri.parse(arg[i].substring(0, idx));
        im.idx = Long.parseLong(Uri.parse(arg[i]).getLastPathSegment());
      }catch(Exception e){
        im.uri = Uri.parse("");
        im.idx = -1;
      }
      ret.add(im);
    }
    return ret;
  }

  // バージョン情報の取得関連
  public static int getVersionCode(Context context) {
    int ver;
    try {
      ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 1).versionCode;
    } catch (NameNotFoundException e) {
      ver = -1;
    }
    return ver;
  }

  public static String getVersionName(Context context) {
    String ver;
    try {
      ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 1).versionName;
    } catch (NameNotFoundException e) {
      ver = "";
    }
    return ver;
  }

  public static boolean isDebug(Context context) {
    PackageManager pm = context.getPackageManager();
    ApplicationInfo ai = new ApplicationInfo();
    try {
      ai = pm.getApplicationInfo(context.getPackageName(), 0);
    } catch (NameNotFoundException e) {
      ai = null;
      return false;
    }
    if ((ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
      return true;
    }
    return false;
  }

  public static void setEnvValue(Context ctx, String key, String value) {
    SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(key, value);
    editor.commit();
  }

  public static void setEnvValue(Context ctx, String key, int value) {
    Misc.setEnvValue(ctx, key, String.valueOf(value));
  }

  public static void DeleteEnvValue(Context ctx, String key) {
    SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = pref.edit();
    editor.remove(key);
    editor.commit();
  }

  public static String getEnvValueStr(Context ctx, String key) {
    SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    return pref.getString(key, "");
  }

  public static int getEnvValueInt(Context ctx, String key) {
    SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    try{
      return Integer.parseInt(pref.getString(key, "0"));
    }catch (Exception e){
      return 0;
    }
  }

}
