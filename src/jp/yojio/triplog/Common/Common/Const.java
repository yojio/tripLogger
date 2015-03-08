package jp.yojio.triplog.Common.Common;

import android.view.ViewGroup;

public class Const {

  // レイアウト関係の定数
  public static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
  public static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
  public static final float WIDGET_WEIGHT = 1f;
  public static final int BODER_WIDTH = 2;

  // 地図・GPS関連
  public static final int LOCATIONUPDATE_MINTIME = 180000; // 更新する間隔 ３分
  public static final int LOCATIONUPDATE_REG_MINTIME = 300000; // 更新する間隔 ５分
  public static final int LOCATIONUPDATE_MINDISTANCE = 50; // 更新する移動距離 50m
  public static final String INIT_POS = "INIT_POS";
  public static final String INIT_LATITUDE = "INIT_LATITUDE";
  public static final String INIT_LONGITUDE = "INIT_LONGITUDE";
  public static final String RESULT_POS = "RESULT_POS";
  public static final String RESULT_LATITUDE = "RESULT_LATITUDE";
  public static final String RESULT_LONGITUDE = "RESULT_LONGITUDE";
  public static final String RESULT_BOOKMARKFLG = "RESULT_BOOKMARKFLG";
  public static final String RESULT_BOOKMARKCAP = "RESULT_BOOKMARKCAP";
  public static final String RESULT_YES = "RESULT_YES";
  public static final String RESULT_NO = "RESULT_NO";
  public static final double INIT_POS_LATITUDE = 35.68430;
  public static final double INIT_POS_LONGITUDE = 139.7542;
  public static final int MAX_ADDRESS_SEARCH_RESULT = 20;
  public static final int INIT_ZOOMLEVEL = 15;

  // 添付ファイル関連
  public static final String IMAGE_PARAM = "IMAGE_PARAM";
  public static final String FILE_LIST = "FILE_LIST";
  public static final String IMAGE_RESULT = "IMAGE_RESULT";
  public static final String DELETE_FILE_IDX = "DELETE_FILE_IDX";

  // 画面遷移用識別子
  public static final int REQUEST_CAMERA = 0;
  public static final int REQUEST_GALLERY = 1;
  public static final int REQUEST_GET_POS = 2;
  public static final int REQUEST_IMAGEPREVIEW = 3;
  public static final int REQUEST_CHANGEDATA = 4;
  public static final int REQUEST_VIEWDATA = 5;
  public static final int REQUEST_VIEWPIC = 6;
  public static final int REQUEST_SETTING = 7;
  public static final int REQUEST_GET_LOGIN = 100;
  public static final int REQUEST_GET_TABLES = 101;

  // INTENT用
  public static final String INTENT_INIT = "INTENT_INIT";
  public static final String INTENT_HOME = "INTENT_HOME";
  public static final String INTENT_LIST = "INTENT_LIST";
  public static final String INTENT_MAP = "INTENT_MAP";
  public static final String INTENT_MAPDATA = "INTENT_MAPDATA";
  public static final String INTENT_CURRENTIDX = "INTENT_CURRENTIDX";
  public static final String INTENT_GROUPCAP = "INTENT_GROUPCAP";
  public static final String INTENT_VIEW = "INTENT_VIEW";

  // INTENTキー
  public static final String INTENT_KEY_TRNID = "INTENT_KEY_TRNID";
  public static final String INTENT_KEY_TRNID_ARR = "INTENT_KEY_TRNID_ARR";
  public static final String INTENT_KEY_CURRENTIDX = "INTENT_KEY_CURRENTIDX";
  public static final String INTENT_KEY_CHANGEDATA = "INTENT_KEY_CHANGEDATA";
  public static final String INTENT_KEY_TITLE = "INTENT_KEY_TITLE";
  public static final String DATACLASS_NAME = "reg_data";

  // ボタンタイプ
  public static final int BUTTON_MAIN = 0;
  public static final int BUTTON_LIST = 1;
  public static final int BUTTON_MAP = 2;

  // UI更新ハンドラーで、別スレッドからの処理を特定するための定数
  public static final int MESSAGE_WHAT_SEARCHADDRESS_START = 11;
  public static final int MESSAGE_WHAT_SEARCHADDRESS_END = 12;
  public static final int MESSAGE_WHAT_SELECTADDRESS_END = 13;
  public static final int MESSAGE_WHAT_CURRENTPOS_END = 14;
  public static final int MESSAGE_WHAT_SEARCHADDRESS_ERROR = -11;
  public static final int MESSAGE_WHAT_LASTKNOWNPOINT_END = 1;
  public static final int MESSAGE_WHAT_LASTKNOWNPOINT2_END = 2;
  public static final int MESSAGE_WHAT_SCROLL_END = 15;
  public static final int MESSAGE_WHAT_LASTKNOWNPOINT_ERROR = -1;
  public static final int MESSAGE_WHAT_LASTKNOWNPOINT_TOADDRESS_ERROR = -2;

  // Google Data Api Access Thread
  public static final int GDATA_ACCESS_TYPE_GET_TABLE_LIST = 1;
  public static final int GDATA_ACCESS_TYPE_CHECK_TABLE_COLUMNS = 2;
  public static final int GDATA_ACCESS_TYPE_UPLOAD_DATA = 3;
//  public static final int GDATA_ACCESS_TYPE_UPLOAD_IMAGE = 4;

  // Google Data Api Thread ResultCode
  public static final int GDATA_RETURN_GET_TABLE_LIST_NORMAL = 10;
  public static final int GDATA_RETURN_GET_TABLE_LIST_ERROR = -10;
  public static final int GDATA_RETURN_CHECK_TABLE_COLUMNS_NORMAL = 20;
  public static final int GDATA_RETURN_CHECK_TABLE_COLUMNS_ERROR = -20;
  public static final int GDATA_RETURN_UPLOAD_DATA_NORMAL = 30;
  public static final int GDATA_RETURN_UPLOAD_DATA_ALLERROR = -30;
  public static final int GDATA_RETURN_UPLOAD_DATA_ERROR = -31;
  public static final int GDATA_RETURN_UPLOAD_DATA_CREATETABLE_ERROR = -32;
  public static final int GDATA_RETURN_UPLOAD_DATA_OTHER_ERROR = -39;
//  public static final int GDATA_RETURN_UPLOAD_IMAGE_NORMAL = 40;
//  public static final int GDATA_RETURN_UPLOAD_IMAGE_ERROR = -40;

  // Google Fusion Tables Common Column names
  // table list {SHOW TABLES}
  public static final String FT_COL_TABLELIST_ID = "table id";
  public static final String FT_COL_TABLELIST_NAME = "name";
  // table {DESCRIBE}
  public static final String FT_COL_TABLE_COLID = "column id";
  public static final String FT_COL_TABLE_COLTYPE = "type";
  public static final String FT_COL_TABLE_COLNAME = "name";
  // columntype {DESCRIBE}
  public static final String FT_COLUMN_TYPE_TEXT = "string";
  public static final String FT_COLUMN_TYPE_NUMBER = "number";
  public static final String FT_COLUMN_TYPE_LOCATION = "location";
  public static final String FT_COLUMN_TYPE_DATETIME = "datetime";

  public static final String DB_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

  // メニューアイコン識別子
  public static final int MENUICON_TRASH = 0;
  public static final int MENUICON_BOOKMARK = 1;
  public static final int MENUICON_BOOKMARK_ADD = 2;
  public static final int MENUICON_NORMALMODE = 3;
  public static final int MENUICON_MULTISELECTMODE = 4;
  public static final int MENUICON_SELECTDELETE = 5;
  public static final int MENUICON_ADDTAG = 6;
  public static final int MENUICON_SETTING = 7;
  public static final int MENUICON_MAP_SEARCH = 8;
  public static final int MENUICON_UPLOAD_DATA = 9;

  public static final String DBNAME = "TripLogDB";

  // 設定画面用
  public static final int SET_OK = 1;
  public static final int SET_NG = 0;
  public static final int SET_CAP_LOCATION = 0;
  public static final int SET_CAP_COMMENT = 1;
  public static final int SET_CAP_FULL = 2;
  public static final int SET_LOCATION_HIGH = 0;
  public static final int SET_LOCATION_LOW = 1;
  public static final int SET_LOCATION_LOW_HIGH = 2;
  public static final int SET_AUTO_LOCATION_AUTO = 0;
  public static final int SET_AUTO_LOCATION_MANUAL = 1;

  // データ保存用キー
  public static final String EDITOR_KEY_VIEW_CURRENTINDEX = "EDITOR_KEY_VIEW_CURRENTINDEX";
  public static final String EDITOR_KEY_VIEW_IMAGEINDEX = "EDITOR_KEY_VIEW_IMAGEINDEX";

  // APIキー
  public static final String MAP_KEY_RELEASE = "0EYuw3eC5XVTyIZN-nS4j_9hB0DEP72PmIiNKFw";
  public static final String MAP_KEY_DEBUG = "0tJKKEZyE0nzwNgJykx4NYHor9JRFpMAP5MWlRA";
  // google-API
  public static final String GOOGLE_API_KEY = "AIzaSyAdUCjeC8lvEW0WXNfIvbma0DsxhV6h-G8";
  public static final String BIT_LY_USER_ID = "yojio";
  public static final String BIT_LY_API_KEY = "R_069932ecf8337791b347e0d6d5a82971";

// hageo_gold
//  public static final String TWITTER_CUNSUMER_KEY = "DaGOg5a1be7DXaLl2OTvA";
//  public static final String TWITTER_CONSUMER_SECRET = "NrySWKM7VynURNFktn5PsTSEBTyJK0GHzh6YFaryg";
// yojio_jp
  public static final String TWITTER_CUNSUMER_KEY = "GAA5Tf9dIxRy7NfDmF9Myw";
  public static final String TWITTER_CONSUMER_SECRET = "b4DTEYOFr1CS42JwcNdhv3ieVlGyyMitv71HB3Ki1Wk";
  public static final String TWITPIC_API_KEY = "fc90f07140f36f38218f2b3e82941a9a";
  public static final String PLIXI_API_KEY = "46e12e21-5186-4269-bda0-f792da42c74c";
  public static final String TWITTER_CALLBACK = "jp.yojio.triplog://SettingActivity";
  public static final String TWITTER_REQUEST_TOKEN = "http://twitter.com/oauth/request_token";
  public static final String TWITTER_ACCESS_TOKEN = "http://twitter.com/oauth/access_token";
  public static final String TWITTER_AUTH_SITE = "http://twitter.com/oauth/authorize";
  public static final String TINY_URL = "http://tinyurl.com/api-create.php?url={0}";
  public static final String OAUTH_TAG = "oauth";
  public static final String KEY_CURRENT_ADDR = "current_addr";

  public static final String ACCOUNT_TYPE = "com.google";
  public static final String SERVICE_ID_FT = "fusiontables";

  public static String MAP_KEY(boolean isDebug) {

    if (isDebug){
      return MAP_KEY_DEBUG;
    }else{
      return MAP_KEY_RELEASE;
    }
  }

}
