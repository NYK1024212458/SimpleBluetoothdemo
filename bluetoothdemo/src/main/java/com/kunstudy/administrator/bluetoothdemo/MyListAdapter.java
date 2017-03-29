package com.kunstudy.administrator.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/25.
 */
public class MyListAdapter extends BaseAdapter {
    private List<BluetoothDevice> mData= new ArrayList<>();
    public MyListAdapter(List<BluetoothDevice> list) {
        mData=list;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //创建textview
        TextView textView = new TextView(parent.getContext());
        textView.setText(mData.get(position).getAddress()+",,,,,"+mData.get(position).getName());
        return textView;
    }
}
