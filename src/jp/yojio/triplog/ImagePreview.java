package jp.yojio.triplog;

import jp.yojio.triplog.Common.Activity.ImageSwitcherBase;
import jp.yojio.triplog.Common.Common.Const;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ImagePreview extends ImageSwitcherBase {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    _doImagePreview = true;
  };

  // ///////////////////// メニュー関連 //////////////////////////
  // メニュー構築時に呼び出される
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    super.onCreateOptionsMenu(menu);

    // メニューアイテムの作成
    menu.add(0,  Const.MENUICON_TRASH, 0, R.string.menucap_trash).setIcon(R.drawable.icon_trash_32);
    return true;

  }

  // メニュー表示直前に呼び出される
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {

    return super.onPrepareOptionsMenu(menu);

  }

  // メニューの項目が呼び出された時に呼び出される
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == Const.MENUICON_TRASH){
      Intent intent = new Intent();
      Bundle bundle = new Bundle();
      bundle.putInt(Const.DELETE_FILE_IDX, _imageidx);
      intent.putExtra(Const.IMAGE_RESULT, bundle);
      setResult(Activity.RESULT_OK,intent);
      finish();
      return true;
    }

    return false;

  }

}
