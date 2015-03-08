package jp.yojio.triplog.Common.Misc;

import jp.yojio.triplog.R;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListBtnAdapter<T extends ListBtnData> extends ArrayAdapter<T> {

  private ArrayList<T> items;

  private LayoutInflater inflater;

  public ListBtnAdapter(Context context, int textViewResourceId, ArrayList<T> items) {

    super(context, textViewResourceId, items);

    // TODO 自動生成されたコンストラクター・スタブ

    this.items = items;

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

  }

  public View getView(int position, View convertView, ViewGroup parent) {

    View view = convertView;

    if (view == null) {
      view = inflater.inflate(R.layout.item_listbtnrow, null);
    }

    T item = items.get(position);

    if (item != null) {
      TextView cap = (TextView) view.findViewById(R.id.Caption);
      ImageView icon = (ImageView) view.findViewById(R.id.Icon);
      cap.setText(item.getCaption());
      icon.setImageDrawable(item.getIcon());
    }

    return view;

  }
}
