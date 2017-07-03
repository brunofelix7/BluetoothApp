package br.com.example.bluetoothapp;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.HashSet;
import java.util.Set;

public class ListDevices extends ListActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> listBluetooths;
    private Set<BluetoothDevice> listDevices;
    private String mac_address;
    private String deviceName;
    private String deviceAddress;
    private String bluetoothInfo;
    private static final String TAG = "TestBluetooth";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDevices();
    }

    private void getDevices(){
        listBluetooths = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listDevices = new HashSet<>();
        listDevices = bluetoothAdapter.getBondedDevices();
        if(listDevices.size() > 0){
            for(BluetoothDevice bluetoothDevice : listDevices){
                deviceName = bluetoothDevice.getName();
                deviceAddress = bluetoothDevice.getAddress();
                listBluetooths.add(deviceName + "\n" + deviceAddress);
            }
        }
        setListAdapter(listBluetooths);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        bluetoothInfo = ((TextView) v).getText().toString();
        Log.d(TAG, "Info: " + bluetoothInfo);

        mac_address = bluetoothInfo.substring(bluetoothInfo.length() - 17);
        Log.d(TAG, "MAC: " + mac_address);

        Intent intent = new Intent();
        intent.putExtra("MAC", mac_address);
        setResult(RESULT_OK, intent);
        finish();
    }
}
