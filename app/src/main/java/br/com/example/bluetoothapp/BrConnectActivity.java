package br.com.example.bluetoothapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;

public class BrConnectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "TestBluetooth";

    //  List Devices
    private ArrayList<BluetoothDevice> listDevices;
    private BrListDevicesAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_br_connect);
        listView = (ListView) findViewById(R.id.lv_devices);
        listDevices = new ArrayList<>();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver4, intentFilter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver1);
        unregisterReceiver(broadcastReceiver2);
        unregisterReceiver(broadcastReceiver3);
        unregisterReceiver(broadcastReceiver4);
    }

    private boolean hasBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            Log.d(TAG, "hasBluetooth: Your device supports Bluetooth");
            return true;
        }else{
            Log.d(TAG, "hasBluetooth: Your device does not support Bluetooth");
            return false;
        }
    }

    private BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "broadcastReceiver: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "broadcastReceiver: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "broadcastReceiver: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "broadcastReceiver: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "broadcastReceiver2: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "broadcastReceiver2: Discoverability Enabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "broadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "broadcastReceiver2: Connected.");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "broadcastReceiver3: ACTION FOUND.");
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                listDevices.clear();
                listDevices.add(device);
                Log.d(TAG, "broadcastReceiver3: " + device.getName() + ": " + device.getAddress());
                adapter = new BrListDevicesAdapter(context, R.layout.device_adapter_view, listDevices);
                listView.setAdapter(adapter);
            }
        }
    };

    private BroadcastReceiver broadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //  3 cases:
                //  case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "broadcastReceiver3: BOND_BONDED.");
                }
                //  case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "broadcastReceiver3: BOND_BONDING.");
                }
                //  case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "broadcastReceiver3: BOND_NONE.");
                }
            }
        }
    };

    public void enableDisableBluetooth(View view){
        Log.d(TAG, "onClick: enableDisableBluetooth()");
        if(hasBluetooth()){
            if(!bluetoothAdapter.isEnabled()){
                Log.d(TAG, "enableDisableBluetooth: Enabling Bluetooth");
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);

                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(broadcastReceiver1, intentFilter);
            }else{
                Log.d(TAG, "enableDisableBluetooth: Disabling Bluetooth");
                bluetoothAdapter.disable();
                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(broadcastReceiver1, intentFilter);
            }
        }else{
            Log.d(TAG, "Seu dispositivo não suporta Bluetooth.");
        }
    }

    public void enableDevices(View view){
        Log.d(TAG, "onClick: enableDevices");
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(intent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcastReceiver2, intentFilter);
    }

    public void showDevices(View view){
        Log.d(TAG, "onClick: showDevices");
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "showDevices: Canceling discovery");

            checkPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver3, intentFilter);
        }if(!bluetoothAdapter.isDiscovering()){
            checkPermissions();
            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver3, intentFilter);
        }
    }

    private void checkPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
            }
        }else{
            Log.d(TAG, "checkPermissions: No need to check permissions. SDK version < M.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //  first cancel discovery because its very memory intensive.
        bluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");

        String deviceName = listDevices.get(position).getName();
        String deviceAddress = listDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //  create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            listDevices.get(position).createBond();
        }
    }
}
