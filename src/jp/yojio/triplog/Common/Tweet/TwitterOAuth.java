package jp.yojio.triplog.Common.Tweet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.misc.TripLogMisc;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.AccessToken;
import twitter4j.http.OAuthAuthorization;
import twitter4j.http.RequestToken;
import twitter4j.media.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.provider.MediaStore;


public class TwitterOAuth {

  public static OAuthAuthorization getOath(){
    Configuration conf = ConfigurationContext.getInstance();
    return new OAuthAuthorization(conf, Const.TWITTER_CUNSUMER_KEY,Const.TWITTER_CONSUMER_SECRET);
  }

  public static RequestToken GetRequestToken(OAuthAuthorization oath) throws TwitterException{
    return oath.getOAuthRequestToken(Const.TWITTER_CALLBACK);
  }

  // OAuth認証による通信（3）
  public static boolean doOauth(Activity context,RequestToken req) {
    try {
      String url = req.getAuthorizationURL();
      context.startActivityForResult(new Intent(Intent.ACTION_VIEW , Uri.parse(url)), 0);
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  // 認証完了時に呼ばれる
  public static TokenInfo GetTokenInfo(Intent intent,OAuthAuthorization oath,RequestToken req) {

    AccessToken token = null;
    Uri uri = intent.getData();
    if (uri != null && uri.toString().startsWith(Const.TWITTER_CALLBACK)) {
      String verifier = uri.getQueryParameter("oauth_verifier");
      if (verifier == null){
        return null;
      }

      try {
        token = oath.getOAuthAccessToken(req, verifier);
      } catch (TwitterException e) {
        return null;
      }
    }
    TokenInfo info = new TokenInfo();
    info.token = token.getToken();
    info.tokenSecret = token.getTokenSecret();

    return info;

  }

  public static Twitter getInstance(TokenInfo info) {
    Configuration conf = getConfiguration();

    TwitterFactory twitterfactory = new TwitterFactory(conf);
    Twitter twitter = twitterfactory.getInstance(new AccessToken(info.token, info.tokenSecret));

    try {
      twitter.getOAuthAccessToken();
    } catch (TwitterException e) {
      return null;
    }

    return twitter;
  }

  public static Configuration getConfiguration() {
    ConfigurationBuilder confbuilder = new ConfigurationBuilder();
    confbuilder.setOAuthConsumerKey(Const.TWITTER_CUNSUMER_KEY);
    confbuilder.setOAuthConsumerSecret(Const.TWITTER_CONSUMER_SECRET);
    return confbuilder.build();
  }

  public static Configuration getConfiguration(String access_token,String access_secret, String apikey,String userid) {
    ConfigurationBuilder confbuilder = new ConfigurationBuilder();
    confbuilder.setOAuthConsumerKey(Const.TWITTER_CUNSUMER_KEY);
    confbuilder.setOAuthConsumerSecret(Const.TWITTER_CONSUMER_SECRET);
    confbuilder.setMediaProviderAPIKey(apikey);
    confbuilder.setUser(userid);
    confbuilder.setOAuthAccessToken(access_token);
    confbuilder.setOAuthAccessTokenSecret(access_secret);
    return confbuilder.build();
  }

  public static String UploadImage(Context context,String Caption, String uri_string,String access_token,String access_secret,String user_id,int objtype,int imagesize){

    Cursor cursor = context.getContentResolver().query(Uri.parse(uri_string), new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION }, null, null, null);
    int orientation = 0;
    if (cursor != null){
      cursor.moveToFirst();
      if (cursor.getCount() > 0) {
        orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        cursor.moveToNext();
      }
//      String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
      cursor.close();
    }

    Configuration conf;
    MediaProvider mp;
    switch (objtype) {
    case 0:
      mp = MediaProvider.TWITPIC;
      conf = getConfiguration(access_token,access_secret,Const.TWITPIC_API_KEY,"");
      break;
    case 1:
      mp = MediaProvider.YFROG;
      conf = getConfiguration(access_token,access_secret,"",user_id);
      break;
    case 2:
      mp = MediaProvider.IMG_LY;
      conf = getConfiguration(access_token,access_secret,"","");
      break;
    case 3:
      mp = MediaProvider.PLIXI;
      conf = getConfiguration(access_token,access_secret,Const.PLIXI_API_KEY,"");
      break;
    case 4:
      mp = MediaProvider.TWIPPLE;
      conf = getConfiguration(access_token,access_secret,"","");
      break;
    case 5:
      mp = MediaProvider.TWITGOO;
      conf = getConfiguration(access_token,access_secret,"","");
      break;
    default:
      return "";
    }
    ImageUpload img;
    ImageUploadFactory imf = new ImageUploadFactory(conf);
    img = imf.getInstance(mp);

    try {
      Bitmap bm = TripLogMisc.GetImage(context, Uri.parse(uri_string),imagesize, orientation);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      System.gc();
      bm.compress(CompressFormat.JPEG, 75, outputStream);
      bm.recycle();
      InputStream fi = new ByteArrayInputStream(outputStream.toByteArray()); // ここでメモリがパンクする
      return img.upload(Caption,fi,Caption); // ファイル名,FileBody,メッセージ
    } catch (TwitterException e) {
      return "";
    } catch (IOException e) {
      return "";
    } catch (OutOfMemoryError e) { // OutIfMemoryのCatch
      return "";
    }

  }

  public static String getBit_lyShortUrl(String longUrl) {

    String login = Const.BIT_LY_USER_ID; // ここにbit.lyアカウントを記述する
    String apiKey = Const.BIT_LY_API_KEY; // ここにapiKeyアカウントを記述する

    // HTTP GET リクエスト文字列作成
    Uri.Builder uriBuilder = new Uri.Builder();
    uriBuilder.path("http://api.bit.ly/shorten");
    uriBuilder.appendQueryParameter("version", "2.0.1");
    uriBuilder.appendQueryParameter("longUrl", Uri.encode(longUrl));
    uriBuilder.appendQueryParameter("login", login);
    uriBuilder.appendQueryParameter("apiKey", apiKey);
    String uri = Uri.decode(uriBuilder.build().toString());

    try {
      // HTTP GET リクエスト
      HttpUriRequest httpGet = new HttpGet(uri);
      DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
      HttpResponse httpResponse = defaultHttpClient.execute(httpGet);
      if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        // HTTP レスポンス
        String entity = EntityUtils.toString(httpResponse.getEntity());
        // JSON デコード
        JSONObject jsonEntity = new JSONObject(entity);
        if (jsonEntity != null) {
          JSONObject jsonResults = jsonEntity.optJSONObject("results");
          if (jsonResults != null) {
            JSONObject jsonResult = jsonResults.optJSONObject(longUrl);
            if (jsonResult != null) {
              // 結果の代入
              return jsonResult.optString("shortUrl");
            }
          }
        }
      }
    } catch (IOException e) {
    } catch (JSONException e) {
    }
    return "";
  }

  public static String getGoogleShortUrl(String longUrl) {
    String apiUri = "https://www.googleapis.com/urlshortener/v1/url";
    // 以下の API Key を取得したものに置き換える（省略可）
    String apiKey = Const.GOOGLE_API_KEY;
    String postUrl = ""; // POST用URL文字列

    // パラメーターに日本語を含む場合は下記のようにエスケープしてください
    // Uri.Builder tmpUriBuilder = new Uri.Builder();
    // tmpUriBuilder.path("http://www.google.co.jp/search");
    // tmpUriBuilder.appendQueryParameter("q", Uri.encode("みっくみく"));
    // longUrl = Uri.decode(tmpUriBuilder.build().toString());

    // POST用URL文字列作成
    Uri.Builder uriBuilder = new Uri.Builder();
    uriBuilder.path(apiUri);
    uriBuilder.appendQueryParameter("key", apiKey); // APIキー推奨
    postUrl = Uri.decode(uriBuilder.build().toString());

    try {
      // リクエスト作成
      HttpPost httpPost = new HttpPost(postUrl);
      httpPost.setHeader("Content-type", "application/json");
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put("longUrl", longUrl);
      StringEntity stringEntity = new StringEntity(jsonRequest.toString());
      httpPost.setEntity(stringEntity);
      // リクエスト発行
      DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
      HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        // 結果の取得
        String entity = EntityUtils.toString(httpResponse.getEntity());
        JSONObject jsonEntity = new JSONObject(entity);
        if (jsonEntity != null) {
          // 短縮URL結果 （このサンプルの場合、「http://goo.gl/sGdK」）
          return jsonEntity.optString("id");
        }
      }
    } catch (IOException e) {
      return "";
    } catch (JSONException e) {
      return "";
    }
    return "";
  }

  public static String getTinyUrl(String fullUrl) {
    HttpClient client = new DefaultHttpClient();

    HttpResponse response = null;
    HttpGet method = new HttpGet(MessageFormat.format(Const.TINY_URL, new Object[] { fullUrl }));

    try {
      response = client.execute(method); // (B)
      int statuscode = response.getStatusLine().getStatusCode(); // 以下(C)

      //リクエストが成功 200 OK and 201 CREATED
      if (statuscode == HttpStatus.SC_OK | statuscode == HttpStatus.SC_CREATED){
        return EntityUtils.toString(response.getEntity());
      } else {
        return "";
      }
    }catch (RuntimeException e) {
      method.abort();
      return "";
    } catch (ClientProtocolException e) {
      method.abort();
      return "";
    } catch (IOException e) {
      method.abort();
      return "";
    }

  }

}
