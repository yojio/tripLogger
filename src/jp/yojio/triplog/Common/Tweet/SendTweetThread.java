package jp.yojio.triplog.Common.Tweet;

import jp.yojio.triplog.Record.Control;
import jp.yojio.triplog.misc.TripLogMisc;
import twitter4j.Twitter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SendTweetThread extends Thread {

  private String _text;
  private String _hash;
  private String _imageuri;
  private double _lat;
  private double _lng;
  private Handler _handler;
  private Context _context;
  private Control _control;
  private boolean _tweetlocation;
  private Twitter _twitter;
  private int _imagesize;
  private boolean _isDebug;

  public SendTweetThread(Context context,Twitter twitter,Handler hnd,String text,String hash,String imageuri,double lat,double lng,Control control,boolean tweetlocation,int Imagesize,boolean IsDebug) {
    _handler = hnd;
    _text = text;
    if (hash.trim().equals("")){
      _hash = "";
    }else{
      _hash = "#" + hash;
    }
    _imageuri = imageuri;
    _lat = lat;
    _lng = lng;
    _control = control;
    _context = context;
    _tweetlocation = tweetlocation;
    _twitter = twitter;
    _imagesize = Imagesize;
    _isDebug = IsDebug;
  }

  @Override
  public void run() {

    boolean ret = false;
    try{
      if (TripLogMisc.Tweet(_context, _text, _hash, _imageuri, _lat, _lng,_twitter, _control,_tweetlocation,_imagesize)){
        ret = true;
      }
    } catch (Exception e) {
      if (_isDebug) Log.e(this.getName(),e.getMessage());
    }finally{
      // 終了を通知
      // 現在位置の住所取得
      Message msg = new Message();
      msg.what = 2;
      msg.obj = new Boolean(ret);
      _handler.sendMessage(msg);
     
    }

  }
}
