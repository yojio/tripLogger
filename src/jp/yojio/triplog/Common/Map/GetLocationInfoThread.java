package jp.yojio.triplog.Common.Map;

import jp.yojio.triplog.Record.Control;
import android.os.Handler;

public class GetLocationInfoThread extends Thread {

  private Handler _handler;
  private final Runnable _listener;
  private LocationInfo _info;
  private Control _control;
  private float _minaccuracy;
  private int _loopcounter;

  public GetLocationInfoThread(Handler hnd, Runnable lis,Control control,boolean IsGps) {
    this._handler = hnd;
    this._listener = lis;
    this._control = control;
    this._minaccuracy = this._control.getAccuracy();
    this._info = new LocationInfo();
    if (IsGps){
      _loopcounter = 300;
    }else{
      _loopcounter = 150;
    }
  }

  public LocationInfo getInfo() {
    return _info;
  }

  public void setInfo(LocationInfo info) {
    _info = info;
  }

  @Override
  public void run() {

    // 位置情報の取得
    int count = 0;
    for (int i = 0; i < _loopcounter; i++) {
      if (_info.isReaded()){
        float accuracy = _info.getAccuracy();
        if ((_minaccuracy == 0) || (accuracy == -1) || (accuracy <= _minaccuracy)){
          break;
        }
      }
      try {
        if (count == 0){
          Thread.sleep(5);
        }else{
          Thread.sleep(200);
        }
        count++;
      } catch (InterruptedException e) {
//        Log.e("run", "sleep exception");
      }
    }
    // 終了を通知
    _handler.post(_listener);
  }

}
