package jp.yojio.triplog;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutFrm extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.frm_about);

    TextView version = (TextView)findViewById(R.id.about_version);
    TextView appinfo = (TextView)findViewById(R.id.about_info);
    TextView license = (TextView)findViewById(R.id.about_license);

    PackageManager pm = getPackageManager();
    try {
      PackageInfo info = null;
      info = pm.getPackageInfo(getApplication().getPackageName(), 0);
      version.setText(getString(R.string.app_name) + " " + getString(R.string.version_name) + info.versionName);
    } catch (NameNotFoundException e) {
      version.setText("");
    }

    // Copyright表記
    appinfo.setText(getString(R.string.copyright_string) + "\n");

    String s = "";
    // signpostライセンス表示
    // s = GetLicenseSentence_singpost();
    // special thanks表示
    s = s + GetSpecialThanksSentence();
    // twitter4jライセンス表示
    s = s + GetLicenseSentence_twitter4j();

    license.setText(s);


  };

//  private String GetLicenseSentence_singpost(){
//
//    StringBuffer sb = new StringBuffer();
//
//    sb
//    .append("Copyright (c) 2009 Matthias Kaeppler").append("\n")
//    .append("").append("\n")
//    .append("Licensed under the Apache License, Version 2.0 (the \"License\");")
//    .append("you may not use this file except in compliance with the License.")
//    .append("You may obtain a copy of the License at").append("\n")
//    .append("").append("\n")
//    .append("    http://www.apache.org/licenses/LICENSE-2.0").append("\n")
//    .append("").append("\n")
//    .append("Unless required by applicable law or agreed to in writing, software")
//    .append("distributed under the License is distributed on an \"AS IS\" BASIS,")
//    .append("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.")
//    .append("See the License for the specific language governing permissions and")
//    .append("limitations under the License.").append("\n")
//    .append("").append("\n");
//
//    return sb.toString();
//
//  }

  private String GetLicenseSentence_twitter4j(){
    StringBuffer sb = new StringBuffer();

    sb
    .append(getString(R.string.app_name)).append(" using Twitter4J").append("\n")
    .append("Twitter4J Copyright (c) 2007-2010, Yusuke Yamamoto All rights reserved. ").append("\n")
    .append("").append("\n");

    return sb.toString();
  }

  private String GetSpecialThanksSentence(){
    StringBuffer sb = new StringBuffer();

    sb
    .append(" Special Thanks to hiroko,kikuchi and mitsuwo").append("\n")
    .append("").append("\n");

    return sb.toString();
  }

}
