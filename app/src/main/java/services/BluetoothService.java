package services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.Executor;

import gen.FuncoesSOS;

/* Responsavel por iniciar e tornar publica a conexão com o dispositivo */
public class BluetoothService extends Service {
    private PowerManager.WakeLock lock;

    private static String MAC = null;
    private AsciiCommander commander;
    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public  BluetoothService(){}
    public boolean conexao = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MAC =(String) intent.getExtras().get("address");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        commander = new AsciiCommander(getApplicationContext());
        adapter = BluetoothAdapter.getDefaultAdapter();
        MAC = (String) intent.getExtras().get("address");
        device = adapter.getRemoteDevice(MAC);
        commander.connect(device);

        conexao = true;
        //enviarDadosActivity();

        startForeground(FuncoesSOS.NOTIFICATION_ID_PADRAO, FuncoesSOS.sendNotificationPadrao(getApplicationContext(), device.getName()));
        return START_STICKY;
    }

    private void enviarDadosActivity() { /* Teste de envio para as telas */
        Intent enviar = new Intent();
        enviar.setAction("GET_CONEXAO");
        enviar.putExtra( "conexao",conexao);
        /*enviar.putExtra( "commander", String.valueOf(commander));*/
        enviar.putExtra( "device", device);
        sendBroadcast(enviar);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        commander = new AsciiCommander(getApplicationContext());
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (Build.VERSION.SDK_INT >= 26)
            startForeground(FuncoesSOS.NOTIFICATION_ID_PADRAO, FuncoesSOS.sendNotificationPadrao(getApplicationContext(), device.getName()));
    }

    public void onDestroy() {
        super.onDestroy();
        commander.disconnect();
        lock.release();
    }

    public boolean isConexao() {
        return conexao;
    }

    public void setConexao(boolean conexao) {
        this.conexao = conexao;
    }

    public static String getMAC() {
        return MAC;
    }

    public AsciiCommander getCommander() {
        return commander;
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }
}
