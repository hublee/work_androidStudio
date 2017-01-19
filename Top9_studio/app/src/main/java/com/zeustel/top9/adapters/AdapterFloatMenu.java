package com.zeustel.top9.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zeustel.top9.R;
import com.zeustel.top9.bean.MenuItem;

import java.util.List;

/**
 * 悬浮菜单适配器
 *
 * @author NiuLei
 * @email niulei@zeustel.com
 * @date 2015/7/17 15:06
 */
public class AdapterFloatMenu extends BaseAdapter {
    private List<MenuItem> data;
    private LayoutInflater inflater;
    private Context context;
    private int position;

    public AdapterFloatMenu(List<MenuItem> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public MenuItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void select(int position) {
        this.position = position;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (context == null)
            context = parent.getContext().getApplicationContext();
        if (inflater == null)
            inflater = LayoutInflater.from(context);

        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.float_menu_item, null);
            mViewHolder.float_menu_item_img = (ImageView) convertView.findViewById(R.id.float_menu_item_img);
            mViewHolder.float_menu_item_text = (TextView) convertView.findViewById(R.id.float_menu_item_text);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        MenuItem menuItem = getItem(position);
        mViewHolder.float_menu_item_img.setImageResource(menuItem.getImg());
        mViewHolder.float_menu_item_text.setText(menuItem.getText());
        if (this.position == position) {
//            convertView.setSelected(true);
            convertView.setBackgroundColor(Color.RED);
        } else {
//            convertView.setSelected(false);
            convertView.setBackgroundResource(R.color.float_menu_bg);
        }
        return convertView;
    }

    private static class ViewHolder {
        private ImageView float_menu_item_img;
        private TextView float_menu_item_text;
    }
}
