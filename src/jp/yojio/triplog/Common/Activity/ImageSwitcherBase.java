package jp.yojio.triplog.Common.Activity;

import java.io.IOException;
import java.util.ArrayList;

import jp.yojio.triplog.R;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.Misc.ImageInfo;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.misc.TripLogMisc;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;
import android.widget.Gallery.LayoutParams;

public class ImageSwitcherBase extends Activity implements AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory, OnGestureListener {

  private static final String LOGTAG_CLASS = ImageSwitcherBase.class.getSimpleName();
  private String[] _ImageUri;
  private ArrayList<ImageInfo> _ImageList;
  private Bitmap _bm;
  protected int _imageidx;
  private GestureDetector gesture;
  Gallery _g;
  private ImageSwitcher mSwitcher;
  protected boolean _doImagePreview = true;
  private static boolean _isDebug;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    _isDebug = Misc.isDebug(this);

    _imageidx = -1;

    // 連携情報がある場合
    Intent it = getIntent();
    Bundle bundle = it.getBundleExtra(Const.IMAGE_PARAM);
    if (bundle != null) {
      _ImageUri = bundle.getStringArray(Const.FILE_LIST);
    }else{
      _ImageUri = new String[0];
    }
    _ImageList = Misc.GetImageInfo(_ImageUri);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setContentView(R.layout.frm_image_switcher);

    mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
    mSwitcher.setFactory(this);
    gesture = new GestureDetector(this);

    _g = (Gallery) findViewById(R.id.gallery);
    _g.setAdapter(new ImageAdapter(this));
    _g.setOnItemSelectedListener(this);

  }

  public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
    ChangeImage(position);
  }

  public void onNothingSelected(AdapterView<?> parent) {
  }

  @Override
  public void onResume(){
    super.onResume();
  }

  public View makeView() {
    ImageView i = new ImageView(this);
    i.setBackgroundColor(0xFF000000);
    i.setScaleType(ImageView.ScaleType.FIT_CENTER);
    i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    return i;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    gesture.onTouchEvent(event);
    return false;
  }

  public boolean onDown(MotionEvent arg0) {
     return false;
  }

  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

    boolean ret = true;
    if (Math.abs(e1.getX() - e2.getX()) < 50) ret = false;
    if (Math.abs(e1.getY() - e2.getY()) > 100) ret = false;
    if (e1.getY() < mSwitcher.getTop()) ret = false;
    if (e1.getY() > mSwitcher.getBottom()) ret = false;

    if (!ret) return false;

    if (e1.getX() < e2.getX()){
      ChangeImage(_imageidx - 1);
    }else if (e1.getX() > e2.getX()){
      ChangeImage(_imageidx + 1);
    }
    return false;
  }

  private void ChangeImage(int index){

    boolean b = false;
    if (index < 0) {
      index = _ImageList.size() - 1;
      b = true;
    }
    if (index >= _ImageList.size()) {
      index = 0;
      b = true;
    }

    if (_imageidx == -1){
      mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.fadein));
      mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.fadeout));
    }else if (index > _imageidx){
      if (b){
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_in_left));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_out_right));
      }else{
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_in_right));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_out_left));
      }
    }else if (index < _imageidx){
      if (b){
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_in_right));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_out_left));
      }else{
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_in_left));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.xml.slide_out_right));
      }
    }else{
      return;
    }

    DispImage(index);

  }

  public void DispImage(int index){
    if ((index < 0) || (index >= _ImageList.size())) return;
    System.gc(); // 念のため
    try {
      ImageInfo im = _ImageList.get(index);
      _bm = TripLogMisc.GetImage(this,Uri.withAppendedPath(im.uri,String.valueOf(im.idx)),mSwitcher);
      mSwitcher.setImageDrawable(new BitmapDrawable(_bm));
      _g.setSelection(index);
      _imageidx = index;
    } catch (IOException e) {
      if (_isDebug) Log.e(LOGTAG_CLASS,"onItemSelected:IOException");
    } finally {
      _bm = null;
    }
  }

  public void onLongPress(MotionEvent e) {

    if (!_doImagePreview) return;

    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_VIEW);
    ImageInfo im = _ImageList.get(_imageidx);
    Uri data = Uri.withAppendedPath(im.uri,String.valueOf(im.idx));
    if (data == null) return;
    if (!TripLogMisc.ExistUri(this, data)) return;

    intent.setData(data);
    startActivityForResult(intent,Const.REQUEST_VIEWPIC);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);
    ChangeImage(_imageidx);

  }

  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  public void onShowPress(MotionEvent e) {
    // TODO 自動生成されたメソッド・スタブ

  }

  public boolean onSingleTapUp(MotionEvent e) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }

  private Context mContext;

  public class ImageAdapter extends BaseAdapter {
    public ImageAdapter(Context c) {
      mContext = c;
    }

    public int getCount() {
      return _ImageList.size();
    }

    public Object getItem(int position) {
      return new Integer(position);
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ImageView i = new ImageView(mContext);

      ImageInfo im = _ImageList.get(position);
      _bm = TripLogMisc.getThumbnailBitmap(ImageSwitcherBase.this, im.idx, im.uri);

      i.setImageBitmap(_bm);
      _bm = null;
      i.setAdjustViewBounds(true);
      i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      i.setBackgroundResource(R.drawable.picture_frame);
      return i;
    }


  }


}
