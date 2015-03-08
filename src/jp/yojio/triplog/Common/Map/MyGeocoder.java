package jp.yojio.triplog.Common.Map;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import jp.yojio.triplog.R;
import jp.yojio.triplog.Common.Common.Const;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

public class MyGeocoder {

  final String tag = "ReverseGeocode";

  public String point2address(double latitude, double longitude, Context context) throws IOException {
    Geocoder geocoder = new Geocoder(context, Locale.JAPAN);
    return point2address(geocoder,latitude,longitude,context);
  }

  // 座標を住所のStringへ変換
  public String point2address(Geocoder geocoder,double latitude, double longitude, Context context) throws IOException {

    String string = new String();

    // geocoedrの実体化
//    Log.d(tag, "Start point2adress");
    if (geocoder == null) geocoder = new Geocoder(context, Locale.JAPAN);
    List<Address> list_address = geocoder.getFromLocation(latitude, longitude, 10); // 引数末尾は返す検索結果数

    // ジオコーディングに成功したらStringへ
    if (!list_address.isEmpty()) {

      Address address = list_address.get(0);
//      string = CreateAddressSentense(address);
      StringBuffer strbuf = new StringBuffer();

      // adressをStringへ
      String buf;
      int cnt = 0;
      for (int i = 1;i <= address.getMaxAddressLineIndex();i++){
        buf = address.getAddressLine(i);
        if (buf == null) continue;
        if (cnt != 0) strbuf.append("\n");
        strbuf.append(buf);
//        Log.d(tag, "loop no." + i);
        cnt++;
      }
      if (strbuf.length() > 0) strbuf.append(context.getResources().getString(R.string.txtcaparound));

      string = strbuf.toString();

    }

    // 失敗（Listが空だったら）
    else {
//      Log.d(tag, "Fail Geocoding");
    }

//    Log.d(tag, string);
    return string;
  }

  public List<Address> adress2point(String adress,Context context) throws IOException{

    Geocoder geocoder = new Geocoder(context, Locale.JAPAN);
    return adress2point(geocoder,adress,context);
  }

  public List<Address> adress2point(Geocoder geocoder,String adress,Context context) throws IOException{

    if (geocoder == null) geocoder = new Geocoder(context, Locale.JAPAN);

    return geocoder.getFromLocationName(adress, Const.MAX_ADDRESS_SEARCH_RESULT);

  }

}
