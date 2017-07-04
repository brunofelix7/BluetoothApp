package br.com.example.bluetoothapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

import br.com.example.bluetoothapp.receivers.LocalBroadcastExample;

public class BrConnectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private StringBuilder messages;
    private BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "TestBluetooth";
    private ArrayList<BluetoothDevice> listDevices;
    private BrListDevicesAdapter adapter;
    private BluetoothDevice mBTDevice;
    private BluetoothConnectionService bluetoothConnectionService;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    //  Layouts
    private ListView listView;
    private EditText et_message;
    private TextView tv_status;
    private TextView tv_get_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_br_connect);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bondDevice();
        findViews();
        messages = new StringBuilder();
        listView.setOnItemClickListener(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //  Recupera a mensagem
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        //  LocalBroadcastManager
        LocalBroadcastExample.registerReceiver(receiver, this);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d(TAG, "Minha mensagem: " + message);
            Toast.makeText(BrConnectActivity.this, "Minha mensagem: " + message, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
        LocalBroadcastExample.unregisterReceiver(receiver, this);
        unregisterReceiver(broadcastReceiver1);
        unregisterReceiver(broadcastReceiver2);
        unregisterReceiver(broadcastReceiver3);
        unregisterReceiver(broadcastReceiver4);
    }

    private void findViews(){
        listView = (ListView) findViewById(R.id.lv_devices);
        et_message = (EditText) findViewById(R.id.et_message);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_get_message = (TextView) findViewById(R.id.tv_get_message);
    }

    /**
     * Liga/desliga o Bluetooth
     */
    public void enableDisableBluetooth(View view){
        Log.d(TAG, "onClick: enableDisableBluetooth()");
        if(bluetoothAdapter != null){
            if(!bluetoothAdapter.isEnabled()){
                Log.d(TAG, "enableDisableBluetooth: Enabling Bluetooth");
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);

                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(broadcastReceiver1, intentFilter);
            }else{
                Log.d(TAG, "enableDisableBluetooth: Disabling Bluetooth");
                bluetoothAdapter.disable();
                IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(broadcastReceiver1, intentFilter1);
            }
        }else{
            Log.d(TAG, "Seu dispositivo não suporta Bluetooth.");
        }
    }

    /**
     * Torna o dispositivo visível para outros dispositivos
     */
    public void makeVisible(View view){
        Log.d(TAG, "onClick: makeVisible");
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(intent);

        IntentFilter intentFilter2 = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcastReceiver2, intentFilter2);
    }

    /**
     * Mostra uma lista de dispositivos
     */
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

    /**
     * Run-Time Permission
     */
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

    /**
     * Registra um BroadcastReceiver que verifica se o dispositivos está sendo pareado com outro
     */
    private void bondDevice(){
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver4, intentFilter);
    }

    /**
     * Chama o método que inicia a comunicação com o outro dispositivo
     */
    public void startConnection(View view){
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    /**
     * Envia uma mensagem de texto para o outro dispositivo
     */
    public void sendMessage(View view){
        byte[] bytes = et_message.getText().toString().getBytes(Charset.defaultCharset());
        bluetoothConnectionService.write(bytes);
        et_message.setText("");
    }

    /**
     * Inicia a conexão com o outro dispositivo
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        bluetoothConnectionService.startClient(device, uuid);
    }

    /**
     * Cria o pareamento ao clicar no dispositivo na lista
     */
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
        //  NOTE: Requer uma API >= 19
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Log.d(TAG, "Trying to pair with " + deviceName);
            listDevices.get(position).createBond();
            mBTDevice = listDevices.get(position);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_status.setText("Pair with: " + mBTDevice.getName());
                }
            });
            bluetoothConnectionService = new BluetoothConnectionService(BrConnectActivity.this);
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_status.setText("Pair it's not support");
                }
            });
            Log.d(TAG, "Pair it's not support");
        }
    }

    /**
     * BroadcastReceiver #1 - Verifica os status do Bluetooth
     */
    private final BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "broadcastReceiver: STATE OFF");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("OFF");
                            }
                        });
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "broadcastReceiver: STATE TURNING OFF");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("TURNING OFF");
                            }
                        });
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "broadcastReceiver: STATE ON");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("ON");
                            }
                        });
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "broadcastReceiver: STATE TURNING ON");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("TURNING ON");
                            }
                        });
                        break;
                }
            }
        }
    };

    /**
     * BroadcastReceiver #2 - Verifica a conexão com outros dispositivos
     */
    private final BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "broadcastReceiver2: Discoverability Enabled.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("Discoverability Enabled");
                            }
                        });
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "broadcastReceiver2: Discoverability Enabled. Able to receive connections.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("Discoverability Enabled. Able to receive connections");
                            }
                        });
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "broadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("Discoverability Disabled. Not able to receive connections");
                            }
                        });
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "broadcastReceiver2: Connecting...");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("Connecting...");
                            }
                        });
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "broadcastReceiver2: Connected.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_status.setText("Connected");
                            }
                        });
                        break;
                }
            }
        }
    };

    /**
     * BroadcastReceiver #3 - Verifica com qual dispositivo foi pareado
     */
    private final BroadcastReceiver broadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "broadcastReceiver3: ACTION FOUND.");
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                listDevices = new ArrayList<>();
                final BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listDevices.add(device);
                        adapter = new BrListDevicesAdapter(context, R.layout.device_adapter_view, listDevices);
                        listView.setAdapter(adapter);
                    }
                });
                Log.d(TAG, "broadcastReceiver3: " + device.getName() + ": " + device.getAddress());
            }
        }
    };

    /**
     * BroadcastReceiver #4 - Verifica se os dois dispositivos estão pareados
     */
    private final BroadcastReceiver broadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "broadcastReceiver4: Called.");
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "broadcastReceiver3: BOND_BONDED.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_status.setText("BOND_BONDED");
                        }
                    });
                    mBTDevice = mDevice;
                }if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "broadcastReceiver3: BOND_BONDING.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_status.setText("BOND_BONDING");
                        }
                    });
                }if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "broadcastReceiver3: BOND_NONE.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_status.setText("BOND_NONE");
                        }
                    });
                }
            }
        }
    };

    /**
     * BroadcastReceiver - Recupera minha mensagem
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getExtras().getString("message");
            messages.append(text + "\n");
            tv_get_message.setText(messages);
        }
    };
}
