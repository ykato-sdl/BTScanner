package com.example.btscanner;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BTDevListAdapter extends ArrayAdapter<BluetoothDevice> {
    private int resource;
    private ArrayList<BluetoothDevice> devList;
    private LayoutInflater inflater;

    public BTDevListAdapter(Context context, int resource, ArrayList<BluetoothDevice> devList) {
        super(context, resource, devList);
        this.resource = resource;
        this.devList = devList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null)
            view = convertView;
        else
            view = inflater.inflate(resource, null);
        BluetoothDevice dev = devList.get(position);
        String bonded = dev.getBondState() == BluetoothDevice.BOND_BONDED ? " *" : "";
        ((TextView) view.findViewById(android.R.id.text2)).setText(dev.getAddress());
        ((TextView) view.findViewById(android.R.id.text1)).setText(dev.getName() + bonded);
        return view;
    }
}
