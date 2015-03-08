package jp.yojio.triplog.Common.Misc;

import jp.yojio.triplog.R;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupViewAdapter extends BaseExpandableListAdapter {

  private ArrayList<GroupViewParentData> _plist;
  private LayoutInflater inflater;
  private HashMap<GroupViewParentData, ArrayList<GroupViewChildData>> _idxlist = new HashMap<GroupViewParentData, ArrayList<GroupViewChildData>>();
  private boolean _multiselectmode;
  public static int _GroupPosition = -1;
  public static int _ChildPosition = -1;

  public GroupViewAdapter(Context context,ArrayList<GroupViewParentData> parentlist,boolean multiselectmode) {
    super();
    SetParentList(parentlist);
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    _multiselectmode = multiselectmode;
  }

  public void SetChildList(GroupViewParentData p,ArrayList<GroupViewChildData> lst){
    _idxlist.put(p, lst);
  }

  public ArrayList<GroupViewChildData> GetChildList(GroupViewParentData p){
    return _idxlist.get(p);
  }

  public void SetParentList(ArrayList<GroupViewParentData> parentlist){
    _plist = parentlist;
    _idxlist.clear();
  }

  public void DeleteChild(int  groupPosition,int  childPosition){
    GroupViewParentData p = _plist.get(groupPosition);
    ArrayList<GroupViewChildData> clst = _idxlist.get(p);
    clst.remove(childPosition);
  }

  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

    View view = convertView;

    if (view == null) {
      view = inflater.inflate(R.layout.item_simple_expandablelist1, null);
    }

    GroupViewParentData item = _plist.get(groupPosition);

    if (item != null) {
      TextView title = (TextView) view.findViewById(R.id.list_title_p);
      title.setText(item.GetCaption());
      title.setTextColor(item.GetColor());
    }

    _GroupPosition = groupPosition;
    _ChildPosition = -1;

    return view;

  }

  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

    View view = convertView;

    if (view == null) {
      view = inflater.inflate(R.layout.item_simple_expandablelist2, null);
    }

    GroupViewChildData item = null;
    GroupViewParentData p = _plist.get(groupPosition);
    ArrayList<GroupViewChildData> clst = _idxlist.get(p);
    if (clst != null){
      item = clst.get(childPosition);
    }

    if (item != null) {
//      LinearLayout base = (LinearLayout) view.findViewById(R.id.list_row_c);
      TextView header = (TextView) view.findViewById(R.id.list_date_c);
      TextView caption = (TextView) view.findViewById(R.id.list_caption_c);
      TextView tag = (TextView) view.findViewById(R.id.list_tag_c);
      ImageView pic = (ImageView) view.findViewById(R.id.list_pic_c);
      ImageView com = (ImageView) view.findViewById(R.id.list_comment_c);
      ImageView twit = (ImageView) view.findViewById(R.id.list_twit_c);
      final ImageView chk = (ImageView) view.findViewById(R.id.list_check_c);

      header.setText(item.getHeader());
      caption.setText(item.getCaption());
      tag.setText(item.getTag());

      if (item.isPhotoflg()){
        pic.setVisibility(View.VISIBLE);
      }else{
        pic.setVisibility(View.INVISIBLE);
      }
      if (item.isCommentflg()){
        com.setVisibility(View.VISIBLE);
      }else{
        com.setVisibility(View.INVISIBLE);
      }
      if (item.isTweeted()){
        twit.setVisibility(View.VISIBLE);
      }else{
        twit.setVisibility(View.INVISIBLE);
      }

      if (_multiselectmode){
        if (item.isChecked()){
          chk.setVisibility(View.VISIBLE);
        }else{
          chk.setVisibility(View.INVISIBLE);
        }
      }else{
        chk.setVisibility(View.GONE);
      }

//      final int pp = groupPosition;
//      final int cp = childPosition;
//      base.setOnTouchListener(new OnTouchListener() {
//
//        public boolean onTouch(View v, MotionEvent event) {
//          if (!_multiselectmode) return false;
//          GroupViewChildData item = null;
//          GroupViewParentData p = _plist.get(pp);
//          ArrayList<GroupViewChildData> clst = _idxlist.get(p);
//          if (clst != null){
//            item = clst.get(cp);
//            item.setChecked(!item.isChecked());
//            if (item.isChecked()){
//              chk.setVisibility(View.VISIBLE);
//            }else{
//              chk.setVisibility(View.INVISIBLE);
//            }
//          }
//          return false;
//        }
//      });
    }

    //スクロール位置を保存する
    _GroupPosition = groupPosition;
    _ChildPosition = childPosition;

    return view;

  }


  public Object getGroup(int groupPosition) {
    return _plist.get(groupPosition);
  }

  public int getGroupCount() {
    return _plist.size();
  }

  public long getGroupId(int groupPosition) {
    return groupPosition;
  }

  public Object getChild(int groupPosition, int childPosition) {
    GroupViewParentData p = _plist.get(groupPosition);
    ArrayList<GroupViewChildData> clst = _idxlist.get(p);
    if (clst == null){
      return null;
    } else {
      return clst.get(childPosition);
    }
  }

  public long getChildId(int groupPosition, int childPosition) {
    return childPosition;
  }

  public int getChildrenCount(int groupPosition) {
    GroupViewParentData p = _plist.get(groupPosition);
    ArrayList<GroupViewChildData> clst = _idxlist.get(p);
    if (clst == null){
      return 0;
    } else {
      return clst.size();
    }
  }

   public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }

  public boolean hasStableIds() {
    return true;
  }

  public ArrayList<GroupViewParentData> GetParentList(){
    return _plist;
  }

  public int GetParentCount(){
    return _plist.size();
  }

  public GroupViewParentData GetParent(int idx){
    if ((idx < 0) || (idx >= _plist.size())) return null;
    return _plist.get(idx);
  }

  public int GetParentIndex(GroupViewParentData p){

    for (int i = 0;i <_plist.size();i++){
      if (p == _plist.get(i)){
        return i;
      }
    }

    return -1;
  }

  public int GetCurrentGroupIndex(){
    return _GroupPosition;
  }

  public int GetCurrentChildIndex(){
    return _ChildPosition;
  }

  public void SetMultiSelectMode(boolean value){
    _multiselectmode = value;
  }
}
