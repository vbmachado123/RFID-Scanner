package services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.Executor;

import gen.FuncoesSOS;

/* Responsavel por iniciar e tornar publica a conexão com o dispositivo */
public class BluetoothService extends IntentService {
    private PowerManager.WakeLock lock;

    private static String MAC = null;
    private AsciiCommander commander;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public BluetoothService() {
        super("BluetoothService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BluetoothService(String name) {
        super(name);
    }

    public BluetoothService(Context context) {
        super("BluetoothService");
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            /* device ta retornando NULLPOINTER */
            MAC = (String) intent.getExtras().get("address");
            // MAC = "88:6B:0F:F0:52:78";
            /*adapter = BluetoothAdapter.getDefaultAdapter();*/
            device = adapter.getRemoteDevice(MAC);
            socket = device.createRfcommSocketToServiceRecord(uuid);

            if(device != null) {
                commander.connect(device);
            }

            if(commander.hasConnectedSuccessfully()) {
                Log.i("Conexao_Bluetooth", "conexao bem sucedida");
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Conexao_Bluetooth", "erro IOException: " + e);
        } catch (Exception e){
            e.printStackTrace();
            Log.e("Conexao_Bluetooth", "erro exception: " + e);
        }

        startForeground(FuncoesSOS.NOTIFICATION_ID_PADRAO, FuncoesSOS.sendNotificationPadrao(getApplicationContext()));


        lock = ((PowerManager) getSystemService(
                Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP, "BluetoothService");
        lock.acquire();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= 26)
            startForeground(FuncoesSOS.NOTIFICATION_ID_PADRAO, FuncoesSOS.sendNotificationPadrao(getApplicationContext()));
    }

    public void onDestroy() {
        super.onDestroy();
        lock.release();
    }
}
