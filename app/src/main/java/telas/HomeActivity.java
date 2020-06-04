package telas;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.BluetoothReaderService;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.responders.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import gen.FuncoesSOS;
import services.BluetoothService;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trConectar, trLeitura, trGravacao, trInventario, trConfiguracoes;
    private Context context = this;
    private static boolean conexao;

    private BluetoothAdapter adapter = null;
    private static BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private BluetoothReaderService readerService;
    private AsciiCommander commander;
    private LoggerResponder responder;
    private SynchronousDispatchResponder dispatchResponder;
    private TransponderResponder transponderResponder;
    private SwitchResponder switchResponder;
    private ReadTransponderCommand transponderCommand;

    private Intent service;
    private BluetoothService bluetoothService;

    private static final int SOLICITA_BLUETOOTH = 1, SOLICITA_CONEXAO = 2, MESSAGE_READ = 3;
    private static String MAC = null;
    private static final String TAG = "Bluetooth", TAGLEITURA = "Teste";
    private TextView tvConectar, tvLeitura, tvGravacao;

    Handler handler = null; //Temporario
    OutputStream outputStream;
    InputStream inputStream;

    BluetoothReceiver receiver;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @SuppressLint({"ResourceAsColor", "NewApi", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        receiver = new BluetoothReceiver();
        registerReceiver(receiver, new IntentFilter("GET_CONEXAO"));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* api RFiD */
        commander = new AsciiCommander(context);
        transponderCommand = new ReadTransponderCommand();
        switchResponder = new SwitchResponder();
        dispatchResponder = new SynchronousDispatchResponder();
        transponderResponder = new TransponderResponder();
        responder = new LoggerResponder();

        bluetoothService = new BluetoothService();

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
            Toast.makeText(context, "Bluetooth não suportado", Toast.LENGTH_SHORT).show();
        } else if(!adapter.isEnabled()) {
            Intent ativaIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaIntent, SOLICITA_BLUETOOTH);
        }
        else { /* faz algo */ }

        /* VERIFICANDO SE POSSUI CONEXÃO ATIVA COM O LEITOR */
        if(!conexao)
            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
        else {
            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            tvConectar.setText("Desconectar");
            /*Log.i(TAGLEITURA, commander.getLastCommandLine());*/

        }

        validaCampo();
    }

    private void validaCampo() {
        trConectar = (TableRow) findViewById(R.id.trConectar);
        trLeitura = (TableRow) findViewById(R.id.trLeitura);
        trGravacao = (TableRow) findViewById(R.id.trGravacao);
        trInventario = (TableRow) findViewById(R.id.trInventario);
        trConfiguracoes = (TableRow) findViewById(R.id.trConfiguracao);
        tvConectar = (TextView) findViewById(R.id.tvConectar);
        tvLeitura = (TextView) findViewById(R.id.tvLeitura);
        tvGravacao = (TextView) findViewById(R.id.tvGravacao);

        trConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //listar
                
               if(conexao){ //conexao ativa -> desconectar
                   conexao = false;
                   bluetoothService.setConexao(conexao);
                   /*commander.disconnect(); //Encerrando conexão*/
                   tvConectar.setText("Conectar");
                   toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
               } else { //conexao desativada
                   Intent lista = new Intent( HomeActivity.this, DeviceListActivity.class);
                   startActivityForResult(lista , SOLICITA_CONEXAO);
               }
            }
        });

        trLeitura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao == true) {
                    acessaActivity(LeituraActivity.class);
                } else
                    Toast.makeText(context, "Conecte com o leitor para prosseguir", Toast.LENGTH_SHORT).show();
            }
        });

        trGravacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao == true) acessaActivity(GravacaoActivity.class);
                else Toast.makeText(context, "Conecte com o leitor para prosseguir", Toast.LENGTH_SHORT).show();
            }
        });

        trInventario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acessaActivity(InventarioActivity.class);
            }
        });

        trConfiguracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acessaActivity(ConfiguracaoActivity.class);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case SOLICITA_CONEXAO:
                    MAC = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    Log.i(TAG, "> MAC foi recebido: " + MAC);
                    validaConexao();
                    break;
                case SOLICITA_BLUETOOTH:
                    //Faz algo
                    break;
                default:
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /* UTILIZANDO A API RFID */
    private void validaConexao() {

        iniciarServer();
        conexao = true;
        //bluetoothService.setConexao(conexao);

/*
        device = adapter.getRemoteDevice(MAC);
        commander.connect(device);
        commander.addResponder(responder);
*/

        /*try {
            socket = device.createRfcommSocketToServiceRecord(uuid);

            if(commander.hasConnectedSuccessfully()){


                commander.addSynchronousResponder();

                *//*transponderResponder.setTransponderReceivedHandler(transponderCommand.getTransponderReceivedDelegate());
        device = adapter.getRemoteDevice(MAC);
        commander.connect(device);

                if(transponderResponder.getAccessErrorCode() != null) {
                    Log.i("Resposta", String.valueOf(transponderResponder.getAccessErrorCode()));
                } else if(transponderResponder != null) {
                    handler = (Handler) transponderResponder.getTransponderReceivedHandler();
                    readerService = new BluetoothReaderService(handler);
                    Log.i("Resposta", transponderResponder.getEpc());
                    readerService.connect(device, true);
                    readerService.connected(socket, device, CONNECTIVITY_SERVICE);
                }*//*

                if(commander.getConnectedDeviceName() == null) {
                    Toast.makeText(context, "Conectado com sucesso: " + device.getName()
                            *//*asciiCommander.getConnectedDeviceName() *//* , Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Conectado com sucesso: " + *//*device.getName()*//*
                            commander.getConnectedDeviceName()  , Toast.LENGTH_SHORT).show();
                    Log.i(TAGLEITURA, commander.getConnectionState().toString());
                    Log.i(TAGLEITURA, commander.getLastCommandLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        tvConectar.setText("Desconectar");
    }

    private void iniciarServer() {
        service = new Intent(this, BluetoothService.class);
        service.putExtra("address", MAC);
        startService(service);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    private void acessaActivity(Class c){
        Intent it = new Intent(HomeActivity.this, c);
        startActivity(it);
    }

     static class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("GET_CONEXAO"))
            {
                conexao = intent.getBooleanExtra("conexao", false);
               // commander = (AsciiCommander) intent.getSerializableExtra("commander");
                device = intent.getParcelableExtra("device");
            }
        }
    }
}

