package service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.responders.IAsciiCommandResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderResponder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import gen.FuncoesSOS;
import bluetooth.BluetoothReceiver;
import util.InventoryModel;
import util.Preferencias;

/* Responsavel por iniciar e tornar publica a conexão com o dispositivo */
public class BluetoothService extends Service implements Serializable {
    private static String tag = BluetoothService.class.getName();
    private static String MAC = null;
    private AsciiCommander commander;
    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private ReadTransponderCommand responder;
    private TransponderResponder transponderResponder;
    private LoggerResponder logger;
    private String LeituraTag = "";
    private String nomeDispositivo = "";

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public BluetoothService() {
    }

    public boolean conexao = false;
    private Preferencias preferencias;
    private int potencia = 30;
    private boolean mudou = false;
    private InventoryModel model;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       nomeDispositivo = intent.getStringExtra("nome");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AsciiCommander commander = AsciiCommander.sharedInstance();

        commander.addResponder(/*responder*/ new IAsciiCommandResponder() {
            @Override
            public boolean isResponseFinished() {

                return false;
            }

            @Override
            public void clearLastResponse() {
            }

            @Override
            public boolean processReceivedLine(String s, boolean b) throws Exception {
                Log.i(tag, ">" + s + " - " + b);
                Log.i("Device", String.valueOf(commander.getDeviceProperties().getMaximumCarrierPower()));
                LeituraTag = s;
                enviarDadosActivity();

                return false;
            }
        });

        conexao = true;

        enviarDadosActivity();

        startForeground(FuncoesSOS.NOTIFICATION_ID_PADRAO, FuncoesSOS.sendNotificationPadrao(getApplicationContext(), commander.getConnectedDeviceName()));
        return START_REDELIVER_INTENT;
    }

    /* Teste de envio do estado da conexao para a home */
    private void enviarDadosActivity() { /* Teste de envio para as telas */
        Intent enviar = new Intent(this, BluetoothReceiver.class);
        enviar.setAction("GET_CONEXAO");
        enviar.putExtra("conexao", conexao);
        enviar.putExtra("resposta", LeituraTag);

        sendBroadcast(enviar);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferencias = new Preferencias(getApplicationContext());
       commander = new AsciiCommander(getApplicationContext());
        // commander = new Commander(getApplicationContext());
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    @SuppressLint("WrongConstant")
    public void onDestroy() {
        super.onDestroy();
        preferencias = new Preferencias(getApplicationContext());
        //commander
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
