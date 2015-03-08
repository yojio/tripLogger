package jp.yojio.triplog.Common.DataApi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import jp.yojio.triplog.Common.DataApi.Account.AuthManager;
import jp.yojio.triplog.Common.DataApi.gdata.GDataWrapper;
import jp.yojio.triplog.misc.TripLogMisc;
import android.content.Context;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.Strings;

public class FusionTableInterface {

  private static final String GDATA_VERSION = "2";
  private static final String FUSIONTABLES_BASE_FEED_URL = "http://www.google.com/fusiontables/api/query";
  private static HttpTransport _transport;
  private static ArrayList<HashMap<String, String>> _ret = null;

  private static HttpRequest InitGoogleAccess(Context context, AuthManager auth) {
    _transport = GoogleTransport.create();
    GoogleHeaders headers = (GoogleHeaders) _transport.defaultHeaders;
    headers.setApplicationName("yoji-triplogger-" + TripLogMisc.getVersion(context));
    headers.gdataVersion = GDATA_VERSION;
    ((GoogleHeaders) _transport.defaultHeaders).setGoogleLogin(auth.getAuthToken());
    return _transport.buildGetRequest();
  }

  private static void FinGoogleAccess() {
    _transport = null;
  }

  private static ArrayList<HashMap<String, String>> ExtractData(String response) {

    HashMap<String, String> data;
    ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
    ArrayList<String> collist = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(response, "\n");
    StringTokenizer strow;
    int row = 0;
    int colidx = 0;
    while (st.hasMoreTokens()) {
      String wk = st.nextToken();
      if (wk.trim().equals("")) break;
      strow = new StringTokenizer(wk, ",");
      if (row == 0) {
        while (strow.hasMoreTokens()) {
          collist.add(strow.nextToken());
        }
      } else {
        colidx = 0;
        data = new HashMap<String, String>();
        while (strow.hasMoreTokens()) {
          if (colidx >= collist.size()) break;
          data.put(collist.get(colidx), strow.nextToken());
          colidx++;
        }
        ret.add(data);
      }
      row++;
    }

    return ret;

  }

  private static boolean runQuery(final String query, AuthManager auth) {
    GDataWrapper<HttpTransport> wrapper = new GDataWrapper<HttpTransport>();
    wrapper.setAuthManager(auth);
    wrapper.setRetryOnAuthFailure(true);
    wrapper.setClient(_transport);
    wrapper.runQuery(new GDataWrapper.QueryFunction<HttpTransport>() {
      public void query(HttpTransport client) throws IOException, GDataWrapper.ParseException, GDataWrapper.HttpException, GDataWrapper.AuthenticationException {
        HttpRequest request = _transport.buildPostRequest();
        request.headers.contentType = "application/x-www-form-urlencoded";
        GenericUrl url = new GenericUrl(FUSIONTABLES_BASE_FEED_URL);
        request.url = url;
        InputStreamContent isc = new InputStreamContent();
        String sql = "sql=" + URLEncoder.encode(query, "UTF-8");
        isc.inputStream = new ByteArrayInputStream(Strings.toBytesUtf8(sql));
        request.content = isc;
        try {
          HttpResponse response = request.execute();
          boolean success = response.isSuccessStatusCode;
          if (success) {
            byte[] result = new byte[1024];
            response.getContent().read(result);
            String s = Strings.fromBytesUtf8(result);
            _ret = ExtractData(s);
          } else {
            throw new GDataWrapper.HttpException(response.statusCode, response.statusMessage);
          }
        } catch (HttpResponseException e) {
          if (e.response.statusCode == 401) {
            throw new GDataWrapper.AuthenticationException(e);
          }
        }

      }
    });
    return wrapper.getErrorType() == GDataWrapper.ERROR_NO_ERROR;
  }

  // テーブルリスト取得
  public static ArrayList<HashMap<String, String>> GetTableList(Context context, AuthManager auth) {
    return ExecSQL(context, "SHOW TABLES", auth);
  }

  // テーブルリスト取得
  public static ArrayList<HashMap<String, String>> GetTableColumns(Context context, String TableId, AuthManager auth) {
    return ExecSQL(context, "DESCRIBE " + TableId, auth);
  }

  // SQL発行
  private static ArrayList<HashMap<String, String>> ExecSQL(Context context, String SQL, AuthManager auth) {
    InitGoogleAccess(context, auth);
    try {
      _ret = null;
      if (runQuery(SQL, auth)) {
        return _ret;
      } else {
        return null;
      }
    } finally {
      FinGoogleAccess();
    }
  }

  // 新規テーブル作成
  public static boolean CreateTable(String TableName){
    return true;
  }

  // テーブル更新
  public static boolean InsertData(Context context,
      String TableId,
      long id,
      Date date,
      Double latitude,
      Double longitude,
      String caption,
      String tags,
      ArrayList<String> pic,
      AuthManager auth) {
   return true;
  }


}
