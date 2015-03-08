package jp.yojio.triplog.Common.Misc;

import android.graphics.Color;

public class GroupViewParentData {

  public static int DEFCOLOR = Color.argb(255, 51, 51, 51);
  public static int RESULTCOLOR = Color.argb(255, 49, 0, 189);

  private String _caption = "";
  private int _idx = -1;
  private long _stime = 0;
  private long _etime = 0;
  private int _color = DEFCOLOR;

  public GroupViewParentData(String Caption,long stime,long etime,int Index,int color){
    _caption = Caption;
    _idx = Index;
    _stime = stime;
    _etime = etime;
    _color = color;
  }

  public GroupViewParentData(String Caption,int Index,int color){
    _caption = Caption;
    _idx = Index;
    _color = color;
  }

  public String GetCaption(){
    return _caption == null?"":_caption;
  }

  public void SetCaption(String value){
    _caption = value;
  }

  public int GetIndex(){
    return _idx;
  }

  public void SetIndex(int idx){
    _idx = idx;
  }

  public long GetSTime(){
    return _stime;
  }

  public void SetSTime(long value){
    _stime = value;
  }

  public long GetETime(){
    return _etime;
  }

  public void SetETime(long value){
    _etime = value;
  }

  public void SetColor(int value){
    _color = value;
  }

  public int GetColor(){
    return _color;
  }

}
