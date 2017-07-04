package br.com.example.bluetoothapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LocalBroadcastExample {

    private static final String TAG = "TestBluetooth";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_INTENT = "intent_local_broadcast";

    public static void sendMessage(String message, Context context){
        Log.d(TAG, "sendMessage: Called.");
        Intent intent = new Intent(KEY_INTENT);
        intent.putExtra(KEY_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void registerReceiver(BroadcastReceiver receiver, Context context){
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(KEY_INTENT));
    }

    public static void unregisterReceiver(BroadcastReceiver receiver, Context context){
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }
}
