package br.com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

class BrListDevicesAdapter extends ArrayAdapter<BluetoothDevice>{

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int  mViewResourceId;

    BrListDevicesAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);
        BluetoothDevice device = mDevices.get(position);
        if (device != null) {
            TextView tv_device_name = (TextView) convertView.findViewById(R.id.tv_device_name);
            TextView tv_device_address = (TextView) convertView.findViewById(R.id.tv_device_address);
            if (tv_device_name != null) {
                tv_device_name.setText(device.getName());
            }if (tv_device_address != null) {
                tv_device_address.setText(device.getAddress());
            }
        }
        return convertView;
    }
}
