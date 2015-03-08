package jp.yojio.triplog.Common.Tweet;

import java.util.ArrayList;

import jp.yojio.triplog.misc.TripLogMisc;
import android.os.Handler;
import android.os.Message;

public class UrlShowtThread extends Thread {

  private Handler _handler;
  private ArrayList<String> _longurllist;
  private String _content;

  public UrlShowtThread(Handler handler, String content, ArrayList<String> longurllist) {
    this._handler = handler;
    this._longurllist = longurllist;
    this._content = content;
  }

  @Override
  public void run() {

    try {
      _content = TripLogMisc.ChangeUrl(_content, _longurllist);
    } finally {
      Message msg = new Message();
      msg.what = 4;
      msg.obj = _content;
      _handler.sendMessage(msg);
    }

  }

}
