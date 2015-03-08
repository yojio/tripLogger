package jp.yojio.triplog.Common.Tweet;

public class TokenInfo {

  public String token = "";
  public String tokenSecret = "";

  public TokenInfo(){

  }

  public TokenInfo(String ptoken,String ptokensecret){
    token = ptoken;
    tokenSecret = ptokensecret;
  }

}
