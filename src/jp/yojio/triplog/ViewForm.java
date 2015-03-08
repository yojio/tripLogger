package jp.yojio.triplog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.DBAccessObject;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Misc.ImageInfo;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.Common.Tweet.UrlShortlenThread;
import jp.yojio.triplog.Common.Tweet.SendTweetThread;
import jp.yojio.triplog.Common.Tweet.TokenInfo;
import jp.yojio.triplog.Common.Tweet.TwitterOAuth;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.DBAccess.TagMasterRecord;
import jp.yojio.triplog.DBAccess.TranRecord;
import jp.yojio.triplog.Record.Control;
import jp.yojio.triplog.Record.LocationDataStruc;
import jp.yojio.triplog.misc.TripLogMisc;
import twitter4j.Twitter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class ViewForm  extends Activity implements ViewSwitcher.ViewFactory, OnGestureListener  {

  private static final String LOGTAG_CLASS = ViewForm.class.getSimpleName();
  private LinearLayout _mapviewarea;
  private LinearLayout _tagarea;
  private LinearLayout _commentarea;
  private LinearLayout _attatcharea;
  private LinearLayout _buttonarea;
  private ImageButton _editbtn;
  private ImageButton _twitbtn;
  private TextView _locationcap;
  private TextView _locationtxt;
  private TextView _comment;
  private DBAccessObject _dao;
  private RecordBase _tran;
  private RecordBase _tagmst;
  private RecordBase _contr;
  private TextView _tagtext;
  private int _CurrentIndex;
  private boolean _changedata = false;
  protected ObjectContainer _obj;
  private ImageButton _prevbtn;
  private ImageButton _nextbtn;
  private TextView _datatext;
  private long[] _keylist;
  private ArrayList<LocationDataStruc> _datalist = new ArrayList<LocationDataStruc>();
  private String _GroupTitle = "";
  private TextView _attatchtxt;
  private ImageSwitcher _attatchview;
  private Bitmap _bm;
  private int _ImageIndex = -1;
  private ShowImageThread _thr = null;
  private SendTweetThread _tweetthr = null;
  private UrlShortlenThread _urlthr = null;
  private GestureDetector gesture;
//  private boolean _backedit = false;
  private AlertDialog _AlertDialog;
  private int _tweetimage;
  private ProgressDialog _dialog;
  private Control _control;
  private boolean _tweetlocation;
  private Twitter _twit = null;
  private int _imageper = 0;
//  private int _imagesize = 0;
  private Rect _imagerect = null;
  private boolean _isDebug;
  private String _tweetcomment = "";
//  private boolean _captiontype = false;
//  private LocationDataStruc _currentdata = null;

  TokenInfo _info = new TokenInfo();

  // UI更新ハンドラ ※別スレッドから通知を受けてUI部品を更新する
  private final Handler _handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      Object obj = msg.obj;
      switch (msg.what) {
      case 0:
        DispImage(((Integer)obj).intValue(),0);
        break;
      case 1:
//        _backedit = false;
        break;
      case 2:
        _dialog.dismiss();
//        _twit = null;
        if (((Boolean)obj).booleanValue()){
          _tran.SetInt(TranRecord.TWEET, _CurrentIndex, 1);
          _dao.save(_tran, _CurrentIndex);
          _twitbtn.setImageResource(R.drawable.icon_twit_checked_32);
          Toast.makeText(ViewForm.this, getString(R.string.msg_tweet_normalend), Toast.LENGTH_SHORT).show();
        }else{
          Toast.makeText(ViewForm.this, getString(R.string.msg_tweet_abnormalend), Toast.LENGTH_SHORT).show();
        }
//        _backedit = false;
        _tweetthr = null;
        break;
      case 4:
        _dialog.dismiss();
        _tweetcomment = (String)obj;
        ShowTweetDlg();
      default:
        break;
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.frm_view);
    _isDebug = Misc.isDebug(this);

    setTitle(getString(R.string.app_name));
//    _captiontype = false;
//    _currentdata = null;
    _obj = ObjectContainer.getInstance(getApplication());

    _prevbtn = (ImageButton)findViewById(R.id.viewdataprev);
    _prevbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        MoveCurrentData(_CurrentIndex - 1);
      }
    });
    _nextbtn = (ImageButton)findViewById(R.id.viewdatanext);
    _nextbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        MoveCurrentData(_CurrentIndex + 1);
      }
    });
    _datatext = (TextView)findViewById(R.id.viewdatatxt);
    _datatext.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
      }
    });

    _locationcap = (TextView) findViewById(R.id.view_mapcaption);
    _locationcap.setOnLongClickListener(new View.OnLongClickListener() {
      public boolean onLongClick(View view) {
        LocationDataStruc data = _datalist.get(_CurrentIndex);
        String loc = "geo:" + data.getLatitude().toString() + "," + data.getLongitude().toString() + "?z=18";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(loc));
        startActivity(intent);
        return false;
      }
    });

    _locationtxt = (TextView) findViewById(R.id.view_maptext);
    _tagtext = (TextView) findViewById(R.id.view_tagtext);

    _mapviewarea = (LinearLayout) findViewById(R.id.view_maparea);
    _mapviewarea.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
      }
    });
    _tagarea = (LinearLayout) findViewById(R.id.view_tagarea);
    _tagarea.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
      }
    });
    _buttonarea = (LinearLayout) findViewById(R.id.view_confirmbtnarea);
    _buttonarea.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
      }
    });

    // コメント
    _commentarea = (LinearLayout) findViewById(R.id.view_commentarea);
    _commentarea.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
      }
    });
    _comment = (TextView) findViewById(R.id.view_comment);

    _attatcharea = (LinearLayout) findViewById(R.id.view_atattcharea);
    _attatchtxt = (TextView) findViewById(R.id.view_atattchtxt);
    _attatchview = (ImageSwitcher) findViewById(R.id.view_atattchswitcher);
    _attatchview.setFactory(this);
//    _attatchview.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.fadein));
//    _attatchview.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.fadeout));
    _attatchview.setInAnimation(null);
    _attatchview.setOutAnimation(null);

    gesture = new GestureDetector(this);

    _editbtn = (ImageButton) findViewById(R.id.view_editbtn);
    _editbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _editbtn.setEnabled(false);
        try{
          SaveCurrentInfo();
//          _backedit = true;
          LocationDataStruc.DeleteTemp(getApplication());
          TripLogMisc.RemoveTakePhotoFlg(getApplication());
          Intent intent = new Intent();
          intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".RegistForm");
          Bundle bundle = new Bundle();
          bundle.putLong(Const.INTENT_KEY_TRNID, _datalist.get(_CurrentIndex).GetId());
          intent.putExtra(Const.INTENT_INIT, bundle);
          startActivityForResult(intent, Const.REQUEST_CHANGEDATA);
        }finally{
          _editbtn.setEnabled(true);
        }
      }
    });

    _twitbtn = (ImageButton) findViewById(R.id.view_twitbtn);
    _twitbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _twitbtn.setEnabled(false);
        try{
          ExecTweet();
        }finally{
          _twitbtn.setEnabled(true);
        }
      };
    });

    _dao = _obj.getDao();
    _tran = DBCommon.GetTable(_dao, DBCommon.TRAN,_isDebug);
    _tagmst = DBCommon.GetTable(_dao, DBCommon.TAG,_isDebug);
    _contr = DBCommon.GetTable(_dao, DBCommon.CONTR,_isDebug);
    _contr.ClearRecord();
    _dao.list(_contr, null, null, null);
    _control = TripLogMisc.SetControlData(_contr);

    // Intent起動チェック
    Intent it = getIntent();
    Bundle extras;
    extras = it.getBundleExtra(Const.INTENT_INIT);
    if (extras != null){
      _keylist = extras.getLongArray(Const.INTENT_KEY_TRNID_ARR);
      _CurrentIndex = extras.getInt(Const.INTENT_KEY_CURRENTIDX);
      _GroupTitle = extras.getString(Const.INTENT_KEY_TITLE);
      LoadData(_keylist);
    }

    MoveCurrentData(_CurrentIndex,false);
    _thr = new ShowImageThread(_handler);
    _thr.start();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState){
    super.onPostCreate(savedInstanceState);
  };

  @Override
  protected void onResume(){
    super.onResume();
//    _twit = null;
  }

  @Override
  protected void onStart() {
    super.onStart();
    _contr.ClearRecord();
    _dao.list(_contr, null, null, null);
    _control = TripLogMisc.SetControlData(_contr);

    if (_control.UseTwit()){
      _twitbtn.setVisibility(View.VISIBLE);
    }else{
      _twitbtn.setVisibility(View.GONE);
    }

  };

  public void LoadData(long[] ids) {

    _dao.list(_tagmst, new String[] { TagMasterRecord.TAG_NAME }, null, null);

    _tran.ClearRecord();

    int i;
    int ii;
    String wk;

    StringBuffer cond = new StringBuffer();
    String[] param = new String[ids.length];
    LocationDataStruc data;

    for (i=0;i<ids.length;i++){
      if (i != 0) cond.append(" OR ");
      cond.append("(").append(TranRecord.ID).append(" = ?)");
      param[i] = String.valueOf(ids[i]);
    }

    _dao.list(_tran, new String[] { TranRecord.REGIST_TIME},cond.toString(),param);

    if (_tran.RecordCount() <= 1) {
      _prevbtn.setVisibility(View.GONE);
      _nextbtn.setVisibility(View.GONE);
    }else{
      _prevbtn.setVisibility(View.VISIBLE);
      _nextbtn.setVisibility(View.VISIBLE);
    }

    if (_tran.RecordCount() == 0) return;

    try {
      _datalist.clear();
      for (i=0;i<_tran.RecordCount();i++){
        data = new LocationDataStruc();
        data.SetId(_tran.GetInt(TranRecord.ID, i, 0));
        data.setDate(new Date(_tran.GetDouble(TranRecord.REGIST_TIME, i, new Double(0)).longValue()));
        data.setNew(false);
        data.setLocationSet(true);
        data.setCaption(_tran.GetString(TranRecord.CAPTION, i, ""));
        data.SetCaptionChanged(true);
        data.setLatitude(_tran.GetDouble(TranRecord.LATITUDE, i, new Double(0)));
        data.setLongitude(_tran.GetDouble(TranRecord.LONGITUDE, i, new Double(0)));
        data.SetTags(_tran.GetString(TranRecord.TAGS, i, ""));
        data.SetTagNames(GetTagNames(data));
        data.setComment(_tran.GetString(TranRecord.COMMENT, i, ""));
        data.setTweeted(_tran.GetInt(TranRecord.TWEET, i, 0) == 1);
        data.setUploaded(_tran.GetInt(TranRecord.G_UPLOAD, i, 0) == 1);
        data.setLinkCode(_tran.GetString(TranRecord.LINKCODE, i, ""));
        data.setFileName("");
        wk = _tran.GetString(TranRecord.FILES, i, "");
        String[] files = wk.split(",");
        data.ClearFiles();
        for (ii = 0; ii < files.length; ii++) {
          if (files[ii].trim().equals(""))
            continue;
          data.AddFile(files[ii]);
        }
        _datalist.add(data);
      }
    } catch (Exception e) {
      return;
    }
//    _tran.ClearRecord();
  }

  public String GetTagNames(LocationDataStruc data) {

    StringBuffer val = new StringBuffer();
    if (!data.GetTagNames().trim().equals("")) return data.GetTagNames();
    if (data.GetTags().trim().equals("")) return "";

    HashMap<String, Object> tags = new HashMap<String, Object>();
    String[] wk = data.GetTags().split(",");
    for (int i = 0; i < wk.length; i++) {
      tags.put(wk[i], null);
    }

    String s = "";
    data.SetTagNames("");
    for (int i = 0; i < _tagmst.RecordCount(); i++) {
      if (tags.containsKey(String.valueOf(_tagmst.GetInt(TagMasterRecord.ID, i, -1)))) {
        s = _tagmst.GetString(TagMasterRecord.TAG_NAME, i, "");
        if (s.equals(""))
          continue;
        if (val.length() != 0) val.append(",");
        val.append(s);
      }
    }

    return val.toString();

  }

  private Uri GetUri(int index,int imageindex){
    if ((index < 0) || (index >= _datalist.size())) return null;
    LocationDataStruc data = _datalist.get(index);
    if ((imageindex < 0) || (imageindex >= data.FileCount())) return null;
    return Uri.parse(data.GetFile(imageindex));
  }

  private void MoveCurrentData(int idx){
    MoveCurrentData(idx,true);
  }

  private void MoveCurrentData(int idx,boolean loadimage){

    if (idx < 0) idx = _datalist.size() - 1;
    if (idx >= _datalist.size()) idx = 0;

    if (!_GroupTitle.equals("")) setTitle(_GroupTitle);

    DispData(_datalist.get(idx),idx,loadimage,true);

    _CurrentIndex = idx;
    if (!_GroupTitle.equals("")) setTitle(_GroupTitle + "  (" + String.valueOf(_CurrentIndex + 1) + "/" + String.valueOf(_datalist.size()) + ")");
  }

  private void ShowCaption(LocationDataStruc data){
    if (data == null){
      _datatext.setText("");
    }else{
      _datatext.setText(new SimpleDateFormat(getString(R.string.format_calender_ymdhm2)).format(data.getDate()));
    }
  }
  public void DispData(LocationDataStruc data,int idx,boolean loadimage,boolean isfade) {

//    _currentdata = data;

    if (isfade) {
//      _attatchview.setInAnimation(null);
//      _attatchview.setOutAnimation(null);
      _attatchview.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.fadein));
      _attatchview.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.fadeout));
    }else{
      _attatchview.setInAnimation(null);
      _attatchview.setOutAnimation(null);
    }
    ShowCaption(data);
    _locationcap.setText(Misc.GetLATString(data.getLatitude()) + " / " + (Misc.GetLNGString(data.getLongitude())));
     _locationtxt.setText(data.getCaption());
     _tagtext.setText(data.GetTagNames());
    _comment.setText(data.getComment());

    if (data.getComment().trim().equals("")){
      _commentarea.setVisibility(View.GONE);
    }else{
      _commentarea.setVisibility(View.VISIBLE);
    }

    _attatchview.reset();
    if (data.FileCount() == 0){
      _attatcharea.setVisibility(View.INVISIBLE);
    }else{
      _attatcharea.setVisibility(View.VISIBLE);
    }

    if (data.isTweeted()){
      _twitbtn.setImageResource(R.drawable.icon_twit_checked_32);
    }else{
      _twitbtn.setImageResource(R.drawable.icon_twit_32);
    }

    if (loadimage) DispImage(idx,0);
  }

  private void SaveCurrentInfo(){

    SharedPreferences pref=this.getSharedPreferences(this.getClass().getSimpleName(),Context.MODE_PRIVATE);
    SharedPreferences.Editor editor=pref.edit();
    editor.putInt(Const.EDITOR_KEY_VIEW_CURRENTINDEX,_CurrentIndex);
    editor.putInt(Const.EDITOR_KEY_VIEW_IMAGEINDEX,_ImageIndex);
    editor.commit();

  }

  private void LoadCurrentInfo(){
    SharedPreferences pref=this.getSharedPreferences(this.getClass().getSimpleName(),Context.MODE_PRIVATE);
    _CurrentIndex = pref.getInt(Const.EDITOR_KEY_VIEW_CURRENTINDEX,0);
    _ImageIndex = pref.getInt(Const.EDITOR_KEY_VIEW_IMAGEINDEX,0);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    LoadCurrentInfo();
    _attatchview.setInAnimation(null);
    _attatchview.setOutAnimation(null);

    if (resultCode != RESULT_OK) {
      MoveCurrentData(_CurrentIndex,false);
      DispImage(_CurrentIndex,_ImageIndex);
//      _backedit = true;
      StartWaitthr();
      return;
    }

    if (requestCode == Const.REQUEST_CHANGEDATA) {
      LoadData(_keylist);
      MoveCurrentData(_CurrentIndex);
      _changedata = true;
    }
    StartWaitthr();
  }

  private void StartWaitthr(){
    WaitThread thr = new WaitThread(_handler);
    thr.start();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        // if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//        if (!_backedit) {
//          ClosePage();
//        }
        ClosePage();
        return true;
      }
    }
    return super.dispatchKeyEvent(event);
  }

  private void ClosePage(){
    Intent intent = new Intent();
    Bundle bundle = new Bundle();
    bundle.putInt(Const.INTENT_KEY_CURRENTIDX, _CurrentIndex);
    bundle.putBoolean(Const.INTENT_KEY_CHANGEDATA, _changedata);
    intent.putExtra(Const.INTENT_VIEW, bundle);
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override
  protected void onPause(){
    super.onPause();
  }

  private void DispImage(int dataidx, int index) {
    DispImage(dataidx,index,true);
  };

  private void DispImage(int dataidx, int index,boolean reload) {

    LocationDataStruc data = _datalist.get(dataidx);
//  _attatchtxt.setText("");
    if (data.FileCount() <= 1){
      _attatchtxt.setVisibility(View.GONE);
    }else{
      _attatchtxt.setVisibility(View.VISIBLE);
    }
    if (data.FileCount() == 0){
      return;
    }
//    _attatchtxt.setVisibility(View.VISIBLE);
    if (index < 0) index = data.FileCount() - 1;
    if (index >= data.FileCount()) index = 0;

    if ((!reload) && (index == _ImageIndex)) return;

    _attatchtxt.setText("(" + String.valueOf(index + 1) + "/" + String.valueOf(data.FileCount()) + ")");

    ImageInfo im = Misc.GetImageInfo(data.GetFile(index));
    System.gc(); // 念のため
    try {
      _bm = TripLogMisc.GetImage(this,Uri.withAppendedPath(im.uri, String.valueOf(im.idx)),_attatchview);
      _attatchview.setImageDrawable(new BitmapDrawable(_bm));
    } catch (IOException e) {
      if (_isDebug) Log.e(LOGTAG_CLASS, "onItemSelected:IOException");
    }finally{
      _bm = null;
    }
    _ImageIndex = index;
  }

  public View makeView() {
    ImageView i = new ImageView(this);
    i.setBackgroundColor(0xFFFFFFFF);
    i.setScaleType(ImageView.ScaleType.FIT_CENTER);
    i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    return i;
  }

  public boolean onDown(MotionEvent paramMotionEvent) {
    return false;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    gesture.onTouchEvent(event);
    return false;
  }

  public boolean onFling(MotionEvent e1, MotionEvent e2, float paramFloat1, float paramFloat2) {

    boolean ret = true;
    if (Math.abs(e1.getX() - e2.getX()) < 50) ret = false;
    if (Math.abs(e1.getY() - e2.getY()) > 100) ret = false;
    if (e1.getY() < _attatcharea.getTop()) ret = false;
    if (e1.getY() > _attatcharea.getBottom()) ret = false;

    if (!ret) return false;

    if (e1.getX() < e2.getX()){
      _attatchview.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_in_left));
      _attatchview.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_out_right));
      DispImage(_CurrentIndex, _ImageIndex - 1,false);
    }else if (e1.getX() > e2.getX()){
      _attatchview.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_in_right));
      _attatchview.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_out_left));
      DispImage(_CurrentIndex, _ImageIndex + 1,false);
    }
    return false;
  }

  public void onLongPress(MotionEvent paramMotionEvent) {
    Uri data = GetUri(_CurrentIndex, _ImageIndex);

    if (!TripLogMisc.ExistUri(this, data)) return;

//    _backedit = true;
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_VIEW);
    SaveCurrentInfo();
    if (data == null) return;
    intent.setData(data);
    startActivityForResult(intent,Const.REQUEST_VIEWPIC);
  }

  public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    return false;
  }

  public void onShowPress(MotionEvent paramMotionEvent) {
  }

  public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
    return false;
  }

  public void onLowMemory(){
    super.onLowMemory();
    System.gc();
  }

  public void ExecTweet(){
    final LocationDataStruc data = _datalist.get(_CurrentIndex);
    _contr.ClearRecord();
    _dao.list(_contr, null, null, null);
    _control = TripLogMisc.SetControlData(_contr);

    boolean flg = false;
    ArrayList<String>urllist = null;
    _tweetcomment = data.getComment();
    if (_control.getUploadCap() != 0){
      urllist = TripLogMisc.GetUrlList(_tweetcomment);
      if (urllist.size() > 0) flg = true;
    };

    if (flg){
      _dialog = ProgressDialog.show(ViewForm.this, "",getString(R.string.msg_url_shortlen), true);
      _dialog.setCancelable(true);
      _dialog.show();

      _urlthr = new UrlShortlenThread(_handler,_tweetcomment,urllist);
      _urlthr.start();
    }else{
      ShowTweetDlg();
    }

  }

  public void ShowTweetDlg(){

    final LocationDataStruc data = _datalist.get(_CurrentIndex);

    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_tweet, null);
    if (_twit == null) {
      _twit = TwitterOAuth.getInstance(new TokenInfo(_control.getToken(),_control.getTokenSecret()));
    }
    final TextView location = (TextView)entryView.findViewById(R.id.tweet_location);
    final EditText contents = (EditText)entryView.findViewById(R.id.tweet_contents);
    final TextView hashtag = (TextView)entryView.findViewById(R.id.tweet_tag);
    final ImageView img = (ImageView)entryView.findViewById(R.id.tweet_image);
    final LinearLayout imgarea = (LinearLayout)entryView.findViewById(R.id.tweet_image_area);
    final TextView imgsize = (TextView)entryView.findViewById(R.id.tweet_image_size);
    final Button imgbtn = (Button)entryView.findViewById(R.id.tweet_imagebtn);
    final String locationtxt = Misc.GetLATString(data.getLatitude()) + " / " + (Misc.GetLNGString(data.getLongitude()));

    if (_control.isUploadLocation()){
      location.setVisibility(View.VISIBLE);
    }else{
      location.setVisibility(View.GONE);
    }
    location.setText(locationtxt);
    _tweetlocation = true;
    location.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        _tweetlocation = !_tweetlocation;
        if (_tweetlocation){
          location.setText(locationtxt);
        }else{
          location.setText(ViewForm.this.getString(R.string.capdialog_twitter_location_off));
        }
      }
    });

    contents.setOnKeyListener(new OnKeyListener() {
      public boolean onKey(View paramView, int paramInt, KeyEvent paramKeyEvent) {
        if (paramInt == KeyEvent.KEYCODE_ENTER){
          return true;
        }
         return false;
      }
    });

    if ((_control.isUploadPic()) && (data.FileCount() > 0)){
      imgbtn.setVisibility(View.VISIBLE);
      imgarea.setVisibility(View.VISIBLE);
      _tweetimage = TripLogMisc.ShowImage(this,_bm,img, data, _ImageIndex,_isDebug);
      _imageper = TripLogMisc.GetBestImagePer(this, data, _tweetimage, _control.getPicSize());
//      _imagesize = TripLogMisc.GetImageSize(this, data, _tweetimage);
      _imagerect = TripLogMisc.GetImageRect(this, data, _tweetimage);
      if (_imagerect == null) return;
      imgsize.setText(TripLogMisc.GetImagesizeString(ViewForm.this,_imagerect,_imageper));
     }else{
      imgbtn.setVisibility(View.GONE);
      imgarea.setVisibility(View.GONE);
      _tweetimage = -1;
    }

    imgbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        if (imgarea.getVisibility() == View.INVISIBLE){
          imgbtn.setText(getText(R.string.capdialog_twitter_image_on).toString());
          imgarea.setVisibility(View.VISIBLE);
          _tweetimage = TripLogMisc.ShowImage(ViewForm.this,_bm,img, data, 0,_isDebug);
//          _imagesize = TripLogMisc.GetImageSize(ViewForm.this, data, _tweetimage);
          _imagerect = TripLogMisc.GetImageRect(ViewForm.this, data, _tweetimage);
          if (_imagerect == null) return;
          imgsize.setText(TripLogMisc.GetImagesizeString(ViewForm.this,_imagerect,_imageper));
        }else{
          imgbtn.setText(getText(R.string.capdialog_twitter_image_off).toString());
          imgarea.setVisibility(View.INVISIBLE);
          _tweetimage = -1;
        }
      }
    });

    img.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _tweetimage = TripLogMisc.ShowImage(ViewForm.this,_bm,img, data, _tweetimage + 1,_isDebug);
        if (_control.getPicSize() == 900) _imageper = TripLogMisc.GetBestImagePer(ViewForm.this, data, _tweetimage, _control.getPicSize());
//        _imagesize = TripLogMisc.GetImageSize(ViewForm.this, data, _tweetimage);
        _imagerect = TripLogMisc.GetImageRect(ViewForm.this, data, _tweetimage);
        if (_imagerect == null) return;
        imgsize.setText(TripLogMisc.GetImagesizeString(ViewForm.this,_imagerect,_imageper));
      }
    });

    imgsize.setOnClickListener(new OnClickListener() {
      public void onClick(View paramView) {
        _imageper = _imageper - 10;
        if (_imageper < 10) _imageper = 100;
        imgsize.setText(TripLogMisc.GetImagesizeString(ViewForm.this,_imagerect,_imageper));
      }
    });

    hashtag.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        TripLogMisc.SelectHashTag(ViewForm.this,hashtag,_control);
      }
    });

    String wk = "";
    int idx = _control.getUploadCap();
    if (idx == 0){
      wk = data.getCaption();
    }else if (idx == 1){
      wk = _tweetcomment;
    }else {
      wk = data.getCaption() + " " + _tweetcomment;
    }
    contents.setText(wk.replace("\n", " "));
    contents.setSelection(contents.getText().toString().length());
    hashtag.setText(TripLogMisc.LoadDefHashTag(this, _control));

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this)
        .setIcon(R.drawable.icon_twit_32_w)
        .setTitle(R.string.capdialog_twitter_send)
        .setPositiveButton(R.string.capdialog_tweetbutton, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            _AlertDialog.dismiss();
            _dialog = ProgressDialog.show(ViewForm.this, "",getString(R.string.msg_tweet_sending), true);
//            _dialog.setCancelable(false);
            _dialog.show();

            String image_url = "";
            if ((data.FileCount() > 0) && (_tweetimage != -1)) image_url = data.GetFile(_tweetimage);
            _tweetthr = new SendTweetThread(ViewForm.this,_twit,_handler,contents.getText().toString(),hashtag.getText().toString(),image_url,data.getLatitude().doubleValue(), data.getLongitude().doubleValue(),_control,_tweetlocation,_imageper,_isDebug);
            _tweetthr.start();
          }
        })
        .setOnCancelListener(new DialogInterface.OnCancelListener() {
          public void onCancel(DialogInterface dialog) {
            _AlertDialog.dismiss();
//            _backedit = false;
          }
        })
        .setNegativeButton(R.string.capdialog_cancelbutton, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            _AlertDialog.dismiss();
//            _backedit = false;
        }
    })
    .setView(entryView).create();
//    _backedit = true;
    _AlertDialog.show();
  }

  class WaitThread extends Thread {

    private Handler _handler;

    public WaitThread(Handler hnd) {
      this._handler = hnd;
    }

    @Override
    public void run() {

      try {
        Thread.sleep(1500);
      } catch (InterruptedException e) {
        if (_isDebug) Log.e("run", "sleep exception");
      }

      // 終了を通知
      Message msg = new Message();
      msg.what = 1;
      msg.obj = new Integer(_CurrentIndex);
      _handler.sendMessage(msg);
    }
  }

  class ShowImageThread extends Thread {

    private Handler _handler;

    public ShowImageThread(Handler hnd) {
      this._handler = hnd;
    }

    @Override
    public void run() {

      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        if (_isDebug) Log.e("run", "sleep exception");
      }

      // 終了を通知
      // 現在位置の住所取得
      Message msg = new Message();
      msg.what = 0;
      msg.obj = new Integer(_CurrentIndex);
      _handler.sendMessage(msg);
    }
  }


}
