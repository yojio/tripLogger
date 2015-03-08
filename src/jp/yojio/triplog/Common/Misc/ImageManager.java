package jp.yojio.triplog.Common.Misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.DateFormat;

public class ImageManager {

//  private static final String TAG = "ImageManager";
  private static final String APPLICATION_NAME = "TripLog";
  private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
  private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/" + APPLICATION_NAME;

  public static Uri addImageAsCamera(ContentResolver cr, Bitmap bitmap) {
    long dateTaken = System.currentTimeMillis();
    String name = createName(dateTaken) + ".jpg";
    String uriStr = MediaStore.Images.Media.insertImage(cr, bitmap, name,
            null);
    return Uri.parse(uriStr);
}

private static String createName(long dateTaken) {
    return DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString();
}

public static Uri addImageAsApplication(ContentResolver cr, Bitmap bitmap) {
      long dateTaken = System.currentTimeMillis();
      String name = createName(dateTaken) + ".jpg";
      return addImageAsApplication(cr, name, dateTaken, PATH, name, bitmap, null);
  }

  public static Uri addImageAsApplication(ContentResolver cr, String name,
          long dateTaken, String directory,
          String filename, Bitmap source, byte[] jpegData) {

      OutputStream outputStream = null;
      String filePath = directory + "/" + filename;
      try {
          File dir = new File(directory);
          if (!dir.exists()) {
              dir.mkdirs();
//              Log.d(TAG, dir.toString() + " create");
          }
          File file = new File(directory, filename);
          if (file.createNewFile()) {
              outputStream = new FileOutputStream(file);
              if (source != null) {
                  source.compress(CompressFormat.JPEG, 75, outputStream);
              } else {
                  outputStream.write(jpegData);
              }
          }

      } catch (FileNotFoundException ex) {
//          Log.w(TAG, ex);
          return null;
      } catch (IOException ex) {
//          Log.w(TAG, ex);
          return null;
      } finally {
          if (outputStream != null) {
              try {
                  outputStream.close();
              } catch (Throwable t) {
              }
          }
      }

      ContentValues values = new ContentValues(7);
      values.put(Images.Media.TITLE, name);
      values.put(Images.Media.DISPLAY_NAME, filename);
      values.put(Images.Media.DATE_TAKEN, new Long(dateTaken));
      values.put(Images.Media.MIME_TYPE, "image/jpeg");
      values.put(Images.Media.DATA, filePath);
      return cr.insert(IMAGE_URI, values);
  }
}
