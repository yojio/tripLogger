package jp.yojio.triplog.Common.Misc;

import jp.yojio.triplog.R;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CheckableAdapter<T extends CheckableData> extends ArrayAdapter<T> {

  private ArrayList<T> items;

  private LayoutInflater inflater;

  public CheckableAdapter(Context context, int textViewResourceId, ArrayList<T> items) {

    super(context, textViewResourceId, items);

    this.items = items;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

  }

  public View getView(int position, View convertView, ViewGroup parent) {

    View view = convertView;

    if (view == null) {
      view = inflater.inflate(R.layout.item_checkablerow, null);
    }

    T item = items.get(position);

    if (item != null) {
      TextView cap = (TextView) view.findViewById(R.id.row_text);
      CheckBox chk = (CheckBox) view.findViewById(R.id.row_check);
      chk.setOnCheckedChangeListener(null);
      cap.setText(item.getCaption());
      chk.setChecked(item.isChecked());
      final int p = position;
      chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton paramCompoundButton, boolean paramBoolean) {
          items.get(p).setChecked(paramBoolean);
        }
      });
      chk.setChecked(items.get(position).isChecked());
    }


    return view;

  }

}
