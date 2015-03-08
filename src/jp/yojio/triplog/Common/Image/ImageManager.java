package jp.yojio.triplog.Common.Image;

import jp.yojio.triplog.Common.Misc.Misc;

import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;

public class ImageManager {
  public static Uri addImageAsCamera(ContentResolver cr, Bitmap bitmap) {
    long dateTaken = System.currentTimeMillis();
    String name = createName(dateTaken);
    String uriStr = MediaStore.Images.Media.insertImage(cr, bitmap, name, null);
    return Uri.parse(uriStr);
  }

  public static Uri addImageAsCamera(ContentResolver cr, String imagePath) throws FileNotFoundException {
    String uriStr = MediaStore.Images.Media.insertImage(cr, imagePath, Misc.getFileName(imagePath), null);
    return Uri.parse(uriStr);
  }


  public static String createName(long dateTaken) {
    return DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString() + ".jpg";
  }

  public static String createName() {
    long dateTaken = System.currentTimeMillis();
    return DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString() + ".jpg";
  }
}