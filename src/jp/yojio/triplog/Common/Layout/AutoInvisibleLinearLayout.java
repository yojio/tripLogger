package jp.yojio.triplog.Common.Layout;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

/**
 * 自動で表示されなくなるLinearLayout
 */
public class AutoInvisibleLinearLayout extends LinearLayout {

    private android.os.Handler _Handler;
    private Timer _Timer;
    private TimerTask _Task;
    private LinearLayout _LinearLayout;

    private long DELAY_TIME = 6000;    // 単位 ms

    private Animation _AnimFadein;
    private Animation _AnimFadeout;

    public AutoInvisibleLinearLayout(Context context) {
        super(context);
        init();
    }

    public AutoInvisibleLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        _Handler = null;
        _Timer = null;
        _Task = null;
        _AnimFadein = null;
        _AnimFadeout = null;
        _LinearLayout = this;
    }

    public void setHandlerAndTimer(android.os.Handler handler, Timer timer) {
        _Handler = handler;
        _Timer = timer;
    }

    public void setFadeinAnimation(Animation amin) {
        _AnimFadein = amin;
    }
    public void setFadeoutAnimation(Animation amin) {
        _AnimFadeout = amin;
    }

    @Override
    public void setVisibility(int visibility) {
        // アニメーションの実行
        //   指定がある場合のみ実行する
        //   連続して呼ばれてもちらつかないようにガードをかけておく
        if (visibility == View.VISIBLE) {
             if (_AnimFadein != null && (this.getVisibility() == View.GONE ||
                                                     this.getVisibility() == View.INVISIBLE)) {
                _LinearLayout.startAnimation(_AnimFadein);
            }
        } else {
            if (_AnimFadeout != null && (this.getVisibility() == View.VISIBLE)) {
                _LinearLayout.startAnimation(_AnimFadeout);
            }
        }
        super.setVisibility(visibility);

        if (visibility == View.VISIBLE) {
            this.autoInvisible();
        }
    }

    // 自動で見せなくするメソッド
    //   連続で呼ばれてもよいように既存のタスクのキャンセル処理も行う
    public void autoInvisible() {
        stopAutoInvisible();

        _Task = new TimerTask() {

            @Override
            public void run() {
                _Handler.post( new Runnable() {
                    public void run() {
                        _LinearLayout.setVisibility(View.GONE);
                    }
                });
            }
        };

        _Timer.schedule(_Task, DELAY_TIME);
    }

    // autoinvisibleを停止して、常に表示する
    public void stopAutoInvisible() {
        if (_Task != null) {
            _Task.cancel();
            _Timer.purge();
            _Task = null;
        }
    }

}
