package br.com.example.bluetoothapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;

import br.com.example.bluetoothapp.receivers.LocalBroadcastExample;

public class MainActivity extends AppCompatActivity {

    private Button btn_connect;
    private boolean isConnected = false;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private static final String TAG = "TestBluetooth";
    private static final int REQUEST_CODE_CONNECT = 2;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static String MAC = null;
    private static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_connect = (Button) findViewById(R.id.btn_connect);

        LocalBroadcastExample.sendMessage("Ola mundo LBM", this);
    }

    /**
     * Verifica se o dispositivo possui Bluetooth
     */
    private boolean hasBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            Log.d(TAG, "Dispositivo possui Bluetooth");
            return true;
        }else{
            Log.e(TAG, "Dispositivo não possui Bluetooth");
            return false;
        }
    }

    /**
     * Habilita o Bluetooth do dispositivo
     */
    public void enableBluetooth(View view){
        if(hasBluetooth()){
            if(!bluetoothAdapter.isEnabled()){
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
            }else{
                Toast.makeText(this, "Seu Bluetooth já está habilitado.", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, "Seu dispositivo não suporta Bluetooth.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Se conecta a algum dispositivo com Bluetooth ativo
     */
    public void connect(View view){
        if(isConnected){
            try{
                bluetoothSocket.close();
                isConnected = false;
                btn_connect.setText(getResources().getString(R.string.text_conectar));
                Toast.makeText(this, "Bluetooth foi desconectado", Toast.LENGTH_LONG).show();
            }catch(IOException e){
                e.printStackTrace();
                Log.d(TAG, "Falha ao desconectar: " + e.getMessage());
            }
        }else{
            Intent intent = new Intent(this, ListDevices.class);
            startActivityForResult(intent, REQUEST_CODE_CONNECT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_ENABLE_BLUETOOTH:
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(this, "O Bluetooth foi ativado", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, "O Bluetooth não foi ativado", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CODE_CONNECT:
                if(resultCode == Activity.RESULT_OK){
                    MAC = data.getExtras().getString("MAC");
                    Log.d(TAG, "MAC: " + MAC);

                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);
                    try{
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                        bluetoothSocket.connect();
                        isConnected = true;
                        btn_connect.setText(getResources().getString(R.string.text_desconectar));
                        Toast.makeText(this, "Você foi conectado com: " + MAC, Toast.LENGTH_LONG).show();
                    }catch(IOException e){
                        isConnected = false;
                        e.printStackTrace();
                        Toast.makeText(this, "Falha ao se conectar com: " + MAC, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Error: " + e.getMessage());
                    }
                }else{
                    Toast.makeText(this, "Falha ao obter o MAC", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void brConnect(View view){
        startActivity(new Intent(this, BrConnectActivity.class));
    }
}
