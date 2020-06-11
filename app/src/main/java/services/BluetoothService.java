package services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.util.UUID;

import gen.FuncoesSOS;
import bluetooth.BluetoothReceiver;
import telas.HomeActivity;
import util.Preferencias;

/* Responsavel por iniciar e tornar publica a conexão com o dispositivo */
public class BluetoothService extends Service {
    private static String MAC = null;
    private AsciiCommander commander;
    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public  BluetoothService(){}
    public boolean conexao = false;
    private Preferencias preferencias;

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

        preferencias = new Preferencias(getApplicationContext());
        conexao = true;
        //preferencias.salvarConexao(conexao);

        enviarDadosActivity();

        startForeground(FuncoesSOS.NOTIFICATION_ID_PADRAO, FuncoesSOS.sendNotificationPadrao(getApplicationContext(), device.getName()));
        return START_REDELIVER_INTENT;
    }

    /* Teste de envio do estado da conexao para a home */
    private void enviarDadosActivity() { /* Teste de envio para as telas */
        Intent enviar = new Intent(this, BluetoothReceiver.class);
        enviar.setAction("GET_CONEXAO");
        enviar.putExtra( "conexao",conexao);
        sendBroadcast(enviar);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferencias = new Preferencias(getApplicationContext());
        commander = new AsciiCommander(getApplicationContext());
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (Build.VERSION.SDK_INT >= 26)
            startForeground(FuncoesSOS.NOTIFICATION_ID_PADRAO, FuncoesSOS.sendNotificationPadrao(getApplicationContext(), device.getName()));
    }

    @SuppressLint("WrongConstant")
    public void onDestroy() {
        super.onDestroy();
        preferencias = new Preferencias(getApplicationContext());
        commander.disconnect();
        conexao = false;
        //preferencias.salvarConexao(conexao);
    }


    public boolean isConexao() {
        return conexao;
    }

    public void setConexao(boolean conexao) {
        this.conexao = conexao;
    }

}
