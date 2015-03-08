package jp.yojio.triplog.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import jp.yojio.triplog.R;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Image.ThumbnailUtil;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.Common.Tweet.TwitterOAuth;
import jp.yojio.triplog.DBAccess.ControlRecord;
import jp.yojio.triplog.DBAccess.TagMasterRecord;
import jp.yojio.triplog.Record.Control;
import jp.yojio.triplog.Record.ImageMetaInfo;
import jp.yojio.triplog.Record.LocationDataStruc;
import twitter4j.GeoLocation;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

public final class TripLogMisc {

  public static final String DETAIL_PATH_MEDIA = "media";

  private static final String APPDIR_PATH = "hageo/lifelog/TripLog/";
  private static final String NODATA_FILE = "images/nodata.jpg";
  private static final char SEPARATER = 30;
  private static final String HASH_KEY = "hashkey";
  private static final String HASH_TAG = "hashtag";

//  private static Twitter _twit = null;

  public static String mkRootDir(String rootpath, String detailpath) {

    File f = new File(rootpath, APPDIR_PATH + detailpath);

    if (f.exists()) {
      return f.getPath();
    } else {
      if (f.mkdirs()) {
        return f.getPath();
      } else {
        return "";
      }
    }

  }

  public static String GetPrevFunction(Intent it) {

    if (it.getBundleExtra(Const.INTENT_HOME) != null) {
      return Const.INTENT_HOME;
    } else if (it.getBundleExtra(Const.INTENT_LIST) != null) {
      return Const.INTENT_LIST;
    } else if (it.getBundleExtra(Const.INTENT_MAP) != null) {
      return Const.INTENT_MAP;
    } else {
      return "";
    }
  }

  public static void GoViewFunction(Context context, String name) {
    GoViewFunction(context, name, null);
  }

  public static void GoViewFunction(Context context, String name, HashMap<String, String[]> arg) {

    Intent intent = new Intent();
    intent.setClassName(context.getPackageName(), context.getClass().getPackage().getName() + name);
    Bundle bundle = new Bundle();
    String baseclass = context.getClass().getSimpleName();
    String wk = "";
    if (baseclass.equals("MainForm")) {
      wk = Const.INTENT_HOME;
    } else if (baseclass.equals("LogListView")) {
      wk = Const.INTENT_LIST;
    } else if (baseclass.equals("LogMapView")) {
      wk = Const.INTENT_MAP;
    } else {
      return;
    }

    if (arg != null) {
      Set<Entry<String, String[]>> stKey = arg.entrySet();
      Iterator<Entry<String, String[]>> i = stKey.iterator();

      while (i.hasNext()) {
        Entry<String, String[]> ent = (Entry<String, String[]>) i.next();
        String key = (String) ent.getKey();
        String[] val = (String[]) ent.getValue();
        bundle.putStringArray(key, val);
      }

    }

    intent.putExtra(wk, bundle);
    context.startActivity(intent);

  }

  public static ImageButton CreateButton(Context context, LinearLayout parent, int imgres, boolean canpress) {

    ImageButton ret = new ImageButton(context);
    if (canpress) {
      ret.setBackgroundResource(R.drawable.button_bk);
    } else {
      ret.setBackgroundResource(R.drawable.nobutton_bk);
    }
    ;
    ret.setImageResource(imgres);
    ret.setMaxHeight(50);
    ret.setMaxWidth(50);
    ret.setMinimumHeight(50);
    ret.setMinimumWidth(50);
    ret.setPadding(7, 7, 7, 7);
    parent.addView(ret, new LinearLayout.LayoutParams(Const.WC, Const.WC));
    return ret;

  }

  public static BtnList CreateButtonArea(int btn_type, final Context context, LinearLayout parent) {

    ImageButton btn_home;
    ImageButton btn_list;
    ImageButton btn_map;

    if (btn_type == Const.BUTTON_MAIN) {
      btn_home = TripLogMisc.CreateButton(context, parent, R.drawable.icon_home_press_48, false);
      btn_list = TripLogMisc.CreateButton(context, parent, R.drawable.icon_note_w_48, true);
      btn_map = TripLogMisc.CreateButton(context, parent, R.drawable.icon_map_w_48, true);
    } else if (btn_type == Const.BUTTON_LIST) {
      btn_home = TripLogMisc.CreateButton(context, parent, R.drawable.icon_home_w_48, true);
      btn_list = TripLogMisc.CreateButton(context, parent, R.drawable.icon_note_press_48, false);
      btn_map = TripLogMisc.CreateButton(context, parent, R.drawable.icon_map_w_48, true);
    } else if (btn_type == Const.BUTTON_MAP) {
      btn_home = TripLogMisc.CreateButton(context, parent, R.drawable.icon_home_w_48, true);
      btn_list = TripLogMisc.CreateButton(context, parent, R.drawable.icon_note_w_48, true);
      btn_map = TripLogMisc.CreateButton(context, parent, R.drawable.icon_map_press_48, false);
    } else {
      return null;
    }

    if (btn_type != Const.BUTTON_MAIN) {
      btn_home.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          TripLogMisc.GoViewFunction(context, ".MainForm");
        }
      });
    }
    ;

    if (btn_type != Const.BUTTON_LIST) {
      btn_list.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          TripLogMisc.GoViewFunction(context, ".LogListView");
        }
      });
    }
    ;

    if (btn_type != Const.BUTTON_MAP) {
      btn_map.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          TripLogMisc.GoViewFunction(context, ".LogMapView");
        }
      });
    }
    ;

    return new BtnList(btn_home,btn_list,btn_map);
  }

  public static String GetTagNames(String tag,RecordBase tagmst,HashMap<String, Object> tags) {

    if (tag.equals(""))
      return "";

    StringBuffer val = new StringBuffer();

    tags.clear();

    String[] wk = tag.split(",");
    for (int i = 0; i < wk.length; i++) {
      tags.put(wk[i], null);
    }

    String s = "";
    for (int i = 0; i < tagmst.RecordCount(); i++) {
      if (tags.containsKey(String.valueOf(tagmst.GetInt(TagMasterRecord.ID, i, -1)))) {
        s = tagmst.GetString(TagMasterRecord.TAG_NAME, i, "");
        if (s.equals(""))
          continue;
        if (val.length() != 0)
          val.append(",");
        val.append(s);
      }
    }

    return val.toString();

  }

  public static boolean SaveObject(Context context, String name,Object object){
    try {
      FileOutputStream outFile = context.openFileOutput(name, Context.MODE_PRIVATE);
      ObjectOutputStream out = new ObjectOutputStream(outFile);
      out.writeObject(object);
      out.close();
      outFile.close();
    } catch(Exception ex) {
      return false;
    }

    return true;

  }

  public static Object LoadObject(Context context, String name,boolean DeleteFile){
    Object obj = null;
    try {
      FileInputStream inFile = context.openFileInput(name);
      ObjectInputStream in = new ObjectInputStream(inFile);
      obj = in.readObject();
      in.close();
      inFile.close();
      if (DeleteFile) DeleteObject(context,name);
    } catch(Exception ex) {
      return null;
    }
    return obj;
  }

  public static boolean DeleteObject(Context context, String name){

    return context.deleteFile(name);

  }

//  public static Twitter GetTwitterObj(String token,String tokensecret){
//    if (_twit == null){
//      return TwitterOAuth.getInstance(new TokenInfo(token,token));
//    }else{
//      return _twit;
//    }
//  };

  public static boolean Tweet(Context context,String txt,String hashtag,String image_uri,double lat,double lng, Twitter twit,Control control,boolean tweetlocation,int imagesize){

    String msg = "";
    String sub_msg = "";
    try{
      txt = txt.replace("\n", "");
      String filepath = "";
      if ((control.isUploadPic()) && (!image_uri.trim().equals(""))){
        filepath = TwitterOAuth.UploadImage(context, txt, image_uri, control.getToken(), control.getTokenSecret(),control.getUserId(),control.getPicType(),imagesize);
        if (filepath.equals("")) return false;
      }

      if (!filepath.trim().equals("")) sub_msg = sub_msg + " " + filepath;
      if (!hashtag.trim().equals("")) sub_msg = sub_msg + " " + hashtag;
      while (true){
        msg = txt + sub_msg;
        if (msg.length() < 140) break;
        txt = txt.substring(0, txt.length() - 1);
      };
    } catch (Exception e){
      return false;
    }

    try {
      if ((control.isUploadLocation()) && (tweetlocation)){
        twit.updateStatus(msg,new GeoLocation(lat, lng));
      }else{
        twit.updateStatus(msg);
      }
    } catch (TwitterException e) {
      return false;
    }
    return true;
  }

  public static ImageMetaInfo GetImageInfo(Context context, Uri uri){

    ImageMetaInfo info = new ImageMetaInfo();
    try{
      Cursor cursor = context.getContentResolver().query(uri,
          new String[]{
          MediaStore.Images.Media.ORIENTATION,
          MediaStore.Images.Media.LATITUDE,
          MediaStore.Images.Media.LONGITUDE,
          MediaStore.Images.Media.SIZE,
      },
      null,
      null,
      null);
      while( cursor.moveToNext() ){
        info.ORIENTATION = cursor.getInt( cursor.getColumnIndex( MediaStore.Images.Media.ORIENTATION ) );
        info.LATITUDE = cursor.getLong( cursor.getColumnIndex( MediaStore.Images.Media.LATITUDE ) );
        info.LONGITUDE = cursor.getLong( cursor.getColumnIndex( MediaStore.Images.Media.LONGITUDE ) );
        info.SIZE = cursor.getInt( cursor.getColumnIndex( MediaStore.Images.Media.SIZE ) );
      }
    }catch(Exception e){

    }
    return info;
  }

  public static String GetImagesizeString(Context context, int size,int per){

    double resize = ((double)(size * (per / 100.0)) / 1024.0);
    String msg = "";
    String unit = "";

    if (resize > 1024) {
      resize = resize / 1024.0;
      unit = "mb ";
    }else{
      unit = "kb ";
    }
    try {
      if (size != 0){
        DecimalFormat df=new DecimalFormat();
        df.applyPattern("0");
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        Double objnum=new Double(resize);
        msg=df.format(objnum) + unit;
      }
    }catch(Exception e){
      msg = "";
    }

    msg = msg + MessageFormat.format(context.getString(R.string.capdialog_twitter_imagesize), new Object[] {  String.valueOf(per) });

    return msg;
  }

  public static String GetImagesizeString(Context context, Rect size,int per){

    int w = (int) (size.width() * (per / 100.0));
    int h = (int) (size.height() * (per / 100.0));

    String msg = String.valueOf(w) + " X " + String.valueOf(h) + " ";
    msg = MessageFormat.format(context.getString(R.string.capdialog_twitter_imagesize), new Object[] {  String.valueOf(per),msg });

    return msg;
  }

  public static int GetImageSize(Context context, LocationDataStruc data,int ImageIndex){

    ImageMetaInfo info = new ImageMetaInfo();
    try{
      Cursor cursor = context.getContentResolver().query(Uri.parse(data.GetFile(ImageIndex)),
          new String[]{
          MediaStore.Images.Media.ORIENTATION,
          MediaStore.Images.Media.LATITUDE,
          MediaStore.Images.Media.LONGITUDE,
          MediaStore.Images.Media.SIZE,
          },
          null,
          null,
          null);
      while( cursor.moveToNext() ){
        info.ORIENTATION = cursor.getInt( cursor.getColumnIndex( MediaStore.Images.Media.ORIENTATION ) );
        info.LATITUDE = cursor.getLong( cursor.getColumnIndex( MediaStore.Images.Media.LATITUDE ) );
        info.LONGITUDE = cursor.getLong( cursor.getColumnIndex( MediaStore.Images.Media.LONGITUDE ) );
        info.SIZE = cursor.getInt( cursor.getColumnIndex( MediaStore.Images.Media.SIZE ) );
      }
    }catch(Exception e){

    }
    return info.SIZE;
  }

  public static Rect GetImageRect(Context context, LocationDataStruc data,int ImageIndex){

    InputStream is;
    try {
      try{
        is = context.getContentResolver().openInputStream(Uri.parse(data.GetFile(ImageIndex)));
      }catch (IOException e){
        is = context.getAssets().open(NODATA_FILE);
      }
      BitmapFactory.Options mOptions = new BitmapFactory.Options();
      mOptions.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(is, null, mOptions);
      is.close();
      return new Rect(0,0,mOptions.outWidth,mOptions.outHeight);
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  public static int GetBestImagePer(Context context, LocationDataStruc data,int ImageIndex,int ImagePer){

    if (ImagePer != 900){
      return ImagePer;
    }

    int wkper = 10;
    InputStream is;
    try {
      try{
        is = context.getContentResolver().openInputStream(Uri.parse(data.GetFile(ImageIndex)));
      }catch (IOException e){
        is = context.getAssets().open(NODATA_FILE);
      }
      BitmapFactory.Options mOptions = new BitmapFactory.Options();
      mOptions.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(is, null, mOptions);
      is.close();
      int mw = mOptions.outWidth;
      int mh = mOptions.outHeight;
      int w = mw;
      int h = mh;
      for (int i=100;i>0;i-=10){
        w = (int) (mw * ((i / 100.0)));
        h = (int) (mh * ((i / 100.0)));
        if ((w < 1024) && (h < 1024)){
          wkper = i;
          break;
        }
      }
      if (wkper < 10) wkper = 10;
      return wkper;
    } catch (FileNotFoundException e) {
      return 50;
    } catch (IOException e) {
      return 50;
    }
  }

  public static Bitmap GetImage(Context context, Uri uri,View view) throws IOException{

    InputStream is;
    ImageMetaInfo info;
    boolean nodata = false;
    try{
      is = context.getContentResolver().openInputStream(uri);
      info = GetImageInfo(context, uri);
    }catch (Exception e){
      nodata = true;
      info = new ImageMetaInfo();
      is = context.getAssets().open(NODATA_FILE);
    }
    if (is == null) return null;

    Bitmap resizeBitmap = null;
    try {
      BitmapFactory.Options mOptions = new BitmapFactory.Options();
      mOptions.inJustDecodeBounds = true;
      resizeBitmap = BitmapFactory.decodeStream(is, null, mOptions);
      is.close();
      if (nodata) {
        is = context.getAssets().open(NODATA_FILE);
      } else {
        is = context.getContentResolver().openInputStream(uri);
      }
      int scaleW = mOptions.outWidth / 250 + 1;
      ;
      int scaleH = mOptions.outHeight / 100 + 1;
      if (view.getWidth() != 0) {
        scaleW = mOptions.outWidth / view.getWidth();
        scaleH = mOptions.outHeight / view.getHeight();
      }

      int scale = Math.max(scaleW, scaleH);
      mOptions.inSampleSize = scale;
      mOptions.inJustDecodeBounds = false;
      resizeBitmap = BitmapFactory.decodeStream(is, null, mOptions);
      resizeBitmap = ThumbnailUtil.rotate(resizeBitmap, info.ORIENTATION);
      is.close();
    } catch (Exception e) {
      return null;
    }
    return resizeBitmap;
  }

  public static Bitmap GetImage(Context context, Uri uri,int size,int orientation) throws IOException{

    InputStream is;
    boolean nodata = false;
    try{
      is = context.getContentResolver().openInputStream(uri);
    }catch (IOException e){
      nodata = true;
      is = context.getAssets().open(NODATA_FILE);
    }

    Bitmap resizeBitmap = null;
    BitmapFactory.Options mOptions = new BitmapFactory.Options();
    mOptions.inJustDecodeBounds = true;
    resizeBitmap = BitmapFactory.decodeStream(is, null, mOptions);
    is.close();
    if (nodata){
      is = context.getAssets().open(NODATA_FILE);
    }else{
      is = context.getContentResolver().openInputStream(uri);
    }

    int scaleW = (int)(mOptions.outWidth * (size / 100.0));

    mOptions.inSampleSize = (int)100 / size;
    mOptions.inJustDecodeBounds = false;
    resizeBitmap = BitmapFactory.decodeStream(is, null, mOptions);

    float rate = ((float)scaleW / (float)resizeBitmap.getWidth());
    Matrix matrix = new Matrix();
    matrix.postScale(rate, rate);
    resizeBitmap = Bitmap.createBitmap(resizeBitmap, 0, 0, resizeBitmap.getWidth(), resizeBitmap.getHeight(), matrix, true);
    resizeBitmap = ThumbnailUtil.rotate(resizeBitmap, orientation);

    is.close();

    return resizeBitmap;

    //    InputStream is;
//    try{
//      is = context.getContentResolver().openInputStream(uri);
//    }catch (IOException e){
//      is = context.getAssets().open(NODATA_FILE);
//    }
//    Bitmap resizeBitmap = null;
//    resizeBitmap = BitmapFactory.decodeStream(is);
//    float rate = (size / 100.0f);
//    Matrix matrix = new Matrix();
//    matrix.postScale(rate, rate);
//    resizeBitmap = Bitmap.createBitmap(resizeBitmap, 0, 0, resizeBitmap.getWidth(), resizeBitmap.getHeight(), matrix, true);
//    resizeBitmap = ThumbnailUtil.rotate(resizeBitmap, orientation);
//    is.close();
//
//    return resizeBitmap;
  }

  public static boolean ExistUri(Context context, Uri uri) {

    if (uri == null) return false;

    try {
      Cursor c = context.getContentResolver().query(uri, null, null, null, null);
      c.moveToFirst();
      if (c.getCount() > 0) return true;
      else                  return false;
    } catch (Exception e) {
      return false;
    }
  }

  public static Bitmap getThumbnailBitmap(Context context, long origId,Uri baseUri) {

    Uri uri = Uri.withAppendedPath(baseUri, String.valueOf(origId));

    if (ExistUri(context,uri)){
      return ThumbnailUtil.getThumbnailBitmap(context.getContentResolver(), origId, Images.Thumbnails.MICRO_KIND, null, baseUri);
    }else{
      Bitmap bmp = null;
      try {
        InputStream is = context.getAssets().open(NODATA_FILE);
        bmp = BitmapFactory.decodeStream(is);
        is.close();
      } catch (IOException e) {
      }
      return bmp;
    }
  }



  public static int ShowImage(Context context,Bitmap bm,ImageView img,LocationDataStruc data,int idx,boolean isDebug){

    if (data.FileCount() == 0){
      return -1;
    }
    if (idx < 0) idx = data.FileCount() - 1;
    if (idx >= data.FileCount()) idx = 0;

    String image_uri = data.GetFile(idx);
    System.gc(); // 念のため
    try {
      bm = GetImage(context,Uri.parse(image_uri),img);
      img.setImageDrawable(new BitmapDrawable(bm));
    } catch (IOException e) {
      if (isDebug) Log.e("TripLogMisc", "ShowImage");
    }
    return idx;

  };

  public static Control SetControlData(RecordBase contr){
    return new Control(
        contr.GetInt(ControlRecord.LOCATION_SEARCH_TYPE, 0, Const.SET_LOCATION_HIGH),
        contr.GetInt(ControlRecord.AUTO_LOCATION_TYPE, 0, Const.SET_AUTO_LOCATION_MANUAL),
        (contr.GetInt(ControlRecord.TWITTER_USE, 0,  Const.SET_NG) != 0),
        contr.GetString(ControlRecord.TWITTER_ACCESS_TOKEN, 0, ""),
        contr.GetString(ControlRecord.TWITTER_ACCESS_SECRET, 0, ""),
        contr.GetInt(ControlRecord.TWITTER_UPLOAD_CAP, 0, Const.SET_CAP_COMMENT),
        (contr.GetInt(ControlRecord.TWITTER_UPLOAD_PIC, 0, Const.SET_NG) != 0),
        contr.GetInt(ControlRecord.TWITTER_IMAGE_SIZE, 0, 900),
        (contr.GetInt(ControlRecord.TWITTER_UPLOAD_LOCATION, 0, 0) != 0),
        contr.GetString(ControlRecord.TWITTER_HASH_TAG, 0, ""),
        contr.GetString(ControlRecord.TWITTER_USER_ID, 0, ""),
        contr.GetInt(ControlRecord.TWITTER_PIC_TYPE, 0, 0),
        contr.GetInt(ControlRecord.ACCURACY, 0, 0)
        );
  }

  public static String GetFileName(Activity activity,String imagePath){
    if (imagePath.trim().equals("")) return "";

    Cursor c = activity.managedQuery(Uri.parse(imagePath),new String[]{MediaStore.Images.Media.DATA},null,null,null);
    c.moveToFirst();
    if (c.getCount() <= 0) return "";
    String ret = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
    c = null;
    return ret;
  }

  public static void copyFile(File in, File out) throws Exception {

    FileChannel sourceChannel = new FileInputStream(in).getChannel();
    FileChannel destinationChannel = new FileOutputStream(out).getChannel();
    sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
    sourceChannel.close();
    destinationChannel.close();

  }

  public static void delete(File f) {
    if (!f.exists()) {
      return;
    }

    if (f.isFile()) {
      f.delete();
    }

    if (f.isDirectory()) {
      File[] files = f.listFiles();
      for (int i = 0; i < files.length; i++) {
        delete(files[i]);
      }
      f.delete();
    }
  }

  public static boolean isNetworkConnected(final Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

    return (networkInfo != null && networkInfo.isConnected());
  }

  public static String GetDistanceStr(double value){
    DecimalFormat df=new DecimalFormat();
    df.applyPattern("0");
    df.setMaximumFractionDigits(3);
    df.setMinimumFractionDigits(0);
    Double objnum=new Double(value);
    return df.format(objnum);
  }

  public static double GetDistance(GeoPoint StartPos,GeoPoint EndPos){

    // 2地点の緯度経度より、距離を計算
    double cosidowk;
    double slat = StartPos.getLatitudeE6() / 1E6;
    double slng = StartPos.getLongitudeE6() / 1E6;
    double elat = EndPos.getLatitudeE6() / 1E6;
    double elng = EndPos.getLongitudeE6() / 1E6;

    if (((slat - elat) == 0) && ((slng - elng) == 0)) return 0;

    if (slat < elat){
      cosidowk = slat;
    }else{
      cosidowk = elat;
    }

    double idowk = Math.abs(slat - elat) * 111;
    double keidowk = Math.abs(slng - elng) * Math.cos(Math.PI / 180 * cosidowk) * 111;
    double wk = (Math.pow(idowk, 2) + Math.pow(keidowk, 2));
    return Math.pow(wk,0.5);

  }

  public static void SetTakePhotoFlg(Context ctx){
    Misc.setEnvValue(ctx, "TakePhotoFlg", 1);
  };

  public static void RemoveTakePhotoFlg(Context ctx){
    Misc.DeleteEnvValue(ctx, "TakePhotoFlg");
  }

  public static boolean CheckTakePhoto(Context ctx){
    return (Misc.getEnvValueInt(ctx, "TakePhotoFlg") == 1);
  }

  public static boolean SelectHashTag(final Context ctx,final TextView deftxt,Control contr) {

    final AlertDialog dlg;

    LayoutInflater factory = LayoutInflater.from(ctx);
    View entryView = factory.inflate(R.layout.dlg_tagselect, null);

    final EditText txt = (EditText) entryView.findViewById(R.id.selecttagtext);
    final ListView lstview = (ListView) entryView.findViewById(R.id.selecttaglist);
    lstview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    // データの設定
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1);
    final ArrayList<String> lst = LoadHashTagList(ctx, contr);
    for (int i=0;i<lst.size();i++){
      adapter.add(lst.get(i));
    }
    lstview.setAdapter(adapter);

    lstview.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        String ret = "";
        if (arg2!=0){
          ret = lst.get(arg2);
        };
        txt.setText(ret);
      }
    });

  lstview.setOnItemLongClickListener(new OnItemLongClickListener() {
      public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
        if (paramInt < 2) return true;
        final int rowidx = paramInt;
        new AlertDialog.Builder(ctx)
        .setTitle(R.string.capdialog_deletehashtag)
        .setMessage(R.string.msg_deletehashtag)
        .setPositiveButton(R.string.capdialog_yesbutton, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            adapter.remove(lst.get(rowidx));
            lst.remove(rowidx);
            SaveHashTagList(ctx, lst, "");
          }
        })
        .setNegativeButton(R.string.capdialog_nobutton,null)
        .show();
        return true;
      }
    });

    // AlertDialog作成
    txt.setText(deftxt.getText());
    dlg = new AlertDialog.Builder(ctx)
    .setTitle(R.string.capdialog_selecthashtag)
    .setPositiveButton(R.string.capdialog_okbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
        SaveHashTagList(ctx, lst, txt.getText().toString());
        deftxt.setText(txt.getText());
        SaveDefHashTag(ctx, txt.getText().toString());
      }
    }).setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface paramDialogInterface, int paramInt) {
      }
    }).setView(entryView).create();

    dlg.show();

    return true;
  }

  private static ArrayList<String> LoadHashTagList(Context ctx,Control contr){
    ArrayList<String> ret = new ArrayList<String>();
    ret.add(ctx.getString(R.string.capdialog_nothing));
    ret.add(contr.getHashTag());
    SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    String s = pref.getString(HASH_KEY, "");
    if (!s.trim().equals("")){
      String[] wk = s.split(String.valueOf(SEPARATER));
      for (int i = 0;i < wk.length;i++){
        ret.add(wk[i]);
      }
    }
    return ret;
  }

  private static void SaveHashTagList(Context ctx,ArrayList<String> lst,String txt){
    boolean b = false;
    txt = txt.trim();
    String wk;
    StringBuffer sb = new StringBuffer();
    for (int i=0;i<lst.size();i++){
      wk = lst.get(i);
      if (wk.equals(txt)) b = true;
      if (i >= 2){
        if (i != 2) sb.append(SEPARATER);
        sb.append(wk);
      }
    }
    if ((!b) && (!txt.trim().equals(""))){
      if (sb.length() != 0) sb.append(SEPARATER);
      sb.append(txt);
    }
    SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    pref.edit().putString(HASH_KEY, sb.toString()).commit();
  }

  public static String LoadDefHashTag(Context ctx,Control contr){
    SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    return pref.getString(HASH_TAG, contr.getHashTag());
  }

  public static void SaveDefHashTag(Context ctx,String txt){
    SharedPreferences pref = ctx.getSharedPreferences(ctx.getPackageName(), Context.MODE_PRIVATE);
    pref.edit().putString(HASH_TAG, txt).commit();
  }

  public static ArrayList<String> GetUrlList(String txt) {

    ArrayList<String> ret = new ArrayList<String>();

    if ((txt.toLowerCase().indexOf("http://") == -1) && (txt.toLowerCase().indexOf("https://") == -1)) {
      return ret;
    }

    int i;
    int ii;
    int idx;
    int idx1;
    int idx2;
    int cnt = 0;
    String wk;
    boolean b;
    StringBuffer url = new StringBuffer();
//    while (cnt <= 1000) {
    while (true) {
      idx1 = -1;
      idx2 = -1;
      for (i = 0; i <= 1; i++) {
        if (i == 0) {
          idx1 = txt.toLowerCase().indexOf("http://");
        } else {
          idx2 = txt.toLowerCase().indexOf("https://");
        }

        if ((i==1) && (idx1==-1) && (idx2==-1)) return ret;
        if (i==0){
          idx = idx1;
        }else{
          idx = idx2;
        };

        if (idx == -1) continue;
        b = false;
        url.setLength(0);
        txt = txt.substring(idx);
        for (ii = 0; ii < txt.length(); ii++) {
          wk = txt.substring(ii, ii + 1);
          if ((wk.equals(" ")) || (wk.equals("　")) || (wk.equals("\n"))) {
            ret.add(url.toString());
            txt = txt.substring(ii);
            url.setLength(0);
            b = true;
            break;
          } else {
            url.append(wk);
          }
        }
        if (!b) {
          ret.add(url.toString());
          break;
        }
      }
      cnt++;
    }
//    return ret;
  }

  public static String ChangeUrl(String txt,ArrayList<String> lst){
    String ret = txt;
    try{
      String longurl;
      String shorturl;
      for (int i=0;i<lst.size();i++){
        longurl = ((String)lst.get(i)).trim();
//        shorturl = TwitterOAuth.getTinyUrl(longurl).trim();
        shorturl = TwitterOAuth.getGoogleShortUrl(longurl).trim();
//        shorturl = TwitterOAuth.getBit_lyShortUrl(longurl).trim();
        if (!shorturl.trim().equals("")){
          ret = ret.replace(longurl, shorturl);
        }
      }
      return ret;
    }catch(Exception e){
      return ret;
    }
  }

  public static String getVersion(Context context) {
    try {
      PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),PackageManager.GET_META_DATA);
      return pi.versionName;
    } catch (NameNotFoundException e)  {
      return "";
    }
  }
}
