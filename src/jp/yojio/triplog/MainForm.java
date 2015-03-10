package jp.yojio.triplog;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.yojio.triplog.Common.Activity.LocationBaseForm;
import jp.yojio.triplog.Common.Common.Const;
import jp.yojio.triplog.Common.DB.record.RecordBase;
import jp.yojio.triplog.Common.Map.LocationInfo;
import jp.yojio.triplog.Common.Misc.ListBtnAdapter;
import jp.yojio.triplog.Common.Misc.ListBtnData;
import jp.yojio.triplog.Common.Misc.Misc;
import jp.yojio.triplog.DBAccess.DBCommon;
import jp.yojio.triplog.DBAccess.TranRecord;
import jp.yojio.triplog.Record.LocationDataStruc;
import jp.yojio.triplog.misc.TripLogMisc;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.admob.android.ads.AdManager;

/**
 * WalkingLog MainForm
 *
 * @author yojio
 *
 */
public class MainForm extends LocationBaseForm {

  private ListBtnAdapter<ListBtnData> _lstbtnA;
//  private EditText _searchtxt;
  private Button _searchbtn;
  private RecordBase _tran;
  private LocationInfo _infobk = null;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.frm_main);

    if (_isDebug) {
      AdManager.setTestDevices(new String[] {
          AdManager.TEST_EMULATOR,
          "XXXXXXXXXXXXXXXX",
          "XXXXXXXXXXXXXXXX"
      });
    }

    _tran = DBCommon.GetTable(_dao, DBCommon.TRAN,_isDebug);

    final TextView clocktxt = (TextView) findViewById(R.id.DigitalClockText);
    DigitalClock clock = (DigitalClock) findViewById(R.id.DigitalClock);
    clock.addTextChangedListener(new TextWatcher() {

      public void onTextChanged(CharSequence s, int start, int before, int count) {
        clocktxt.setText(new SimpleDateFormat("MM/dd HH:mm").format(new Date()));
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void afterTextChanged(Editable s) {
      }
    });

    // リストボタンの追加
    // リスト表示
    ListView listView = (ListView) findViewById(R.id.itemlist);
    _lstbtnA = new ListBtnAdapter<ListBtnData>(this, R.layout.item_listbtnrow, new ArrayList<ListBtnData>());
    listView.setAdapter(_lstbtnA);

    // リスナーの設定
    listView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        switch (position) {
        case 0:
          GetLocationButtonClicked();
          break;
        case 1:
          DetailButtonClicked();
          break;
        case 2:
          SearchLocationClicked();
        default:
        }
      }
    });
    // GPS追加ボタン
    _lstbtnA.add(new ListBtnData(getResources().getDrawable(R.drawable.icon_gps_24), getResources().getString(R.string.btncapGetGeoLocation)));
    // 詳細追加ボタン
    _lstbtnA.add(new ListBtnData(getResources().getDrawable(R.drawable.icon_map_24), getResources().getString(R.string.btncapSetLocation)));
    // // 周辺検索ボタン
    // _lstbtnA.add(new
    // ListBtnData(getResources().getDrawable(R.drawable.icon_search),
    // getResources().getString(R.string.btncapSearchLocation)));

//    final String intentmode = TripLogMisc.GetPrevFunction(getIntent());

    // メニューボタン
    TripLogMisc.CreateButtonArea(Const.BUTTON_MAIN, this,(LinearLayout)findViewById(R.id.buttonarea));

  }

  //////////////////////////////////// ボタンイベント //////////////////////////////////////////////
  // 簡単登録
  public void GetLocationButtonClicked() {

    if (_gpsthr != null) return;
    _infobk = null;
    _networkposget = false;
    StartGetLocationStatus();
  }

  // 詳細登録
  public void DetailButtonClicked() {
    TripLogMisc.RemoveTakePhotoFlg(getApplication());
    LocationDataStruc.DeleteTemp(getApplication());
    Intent intent = new Intent();
    intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".RegistForm");
    Bundle bundle = new Bundle();
    intent.putExtra(Const.INTENT_INIT, bundle);
    startActivity(intent);
    return;
  }

  // 周辺検索
  public void SearchLocationClicked(){
    LayoutInflater factory = LayoutInflater.from(this);
    View entryView = factory.inflate(R.layout.dlg_searchconf, null);
    // 写真挿入・撮影ボタン
//    _searchtxt = (EditText)entryView.findViewById(R.id.searchlocationtext);
    _searchbtn = (Button)entryView.findViewById(R.id.searchlocationbtn);
    _searchbtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        _AlertDialog.dismiss();
      }
    });

    // AlertDialog作成
    _AlertDialog = new AlertDialog.Builder(this)
        .setIcon(R.drawable.icon_search_w_32)
        .setTitle(R.string.btncapSearchLocation)
        .setView(entryView)
        .create();

    _AlertDialog.show();

  }

  @Override
  public void GetLocationEv(LocationInfo info) {
    String msg;
    if (ReLocationSearch()){
      _infobk = info;
      StartGetLocationStatus();
    }else{
      boolean ret = GetInfoResult(info);
      if ((!ret) && (_infobk != null)){
        ret = GetInfoResult(_infobk);
      };
      if (ret){
        msg = getResources().getString(R.string.registdata_ok);
        msg = msg + "\n\n" + info.getLocation();
      }else{
        msg = getResources().getString(R.string.registdata_ng);
      }
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
  }

  private boolean GetInfoResult(LocationInfo info){
    if (info.isReaded()){
      if (SaveData(info)){
        return true;
      }else{
        return false;
      }
    }else{
      return false;
    }
  }

  public boolean SaveData(LocationInfo info){

    _tran.ClearRecord();
    int row = _tran.AddRow();
    _tran.SetRowId(row, null);

    _tran.SetDouble(TranRecord.REGIST_TIME, row, new Double(new Date().getTime()));
    _tran.SetDouble(TranRecord.LATITUDE, row, new Double(info.getLatitude()));
    _tran.SetDouble(TranRecord.LONGITUDE, row, new Double(info.getLongitude()));
    String wk = info.getLocation().replace("\n", "");
    if (wk.trim().equals("")) wk = getString(R.string.unknown_place);
    _tran.SetString(TranRecord.CAPTION, row, wk);
    _tran.SetString(TranRecord.COMMENT, row, "");
    _tran.SetString(TranRecord.TAGS, row, "");
    _tran.SetInt(TranRecord.TWEET , row, 0);
    _tran.SetInt(TranRecord.G_UPLOAD , row, 0);
    _tran.SetString(TranRecord.LINKCODE, row, "");
    _tran.SetString(TranRecord.FILES, row, "");

    for (int i=0;i<_tran.RecordCount();i++){
      _dao.save(_tran, i);
    }

    return true;
  }

  public void onClick(View v) {
    // TODO 自動生成されたメソッド・スタブ

  }

  // ///////////////////// メニュー関連 //////////////////////////
  // メニュー構築時に呼び出される
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    super.onCreateOptionsMenu(menu);
    // メニューアイテムの作成
    menu.add(0, Const.MENUICON_SETTING, 0, R.string.menucap_setting).setIcon(R.drawable.icon_setting_32);
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

    if (item.getItemId() == Const.MENUICON_SETTING) {
      Misc.DeleteEnvValue(getApplication(), Const.OAUTH_TAG);
      Intent intent = new Intent();
      intent.setClassName(getPackageName(), getClass().getPackage().getName() + ".SettingFrm");
      startActivityForResult(intent, Const.REQUEST_SETTING);
      return true;
    }else{
      return false;
    }

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode != RESULT_OK) {
      return;
    }

    if (requestCode == Const.REQUEST_SETTING) {
    }

  }

}
