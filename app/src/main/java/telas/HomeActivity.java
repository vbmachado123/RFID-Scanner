package telas;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.*;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.BluetoothReaderService;
import com.uk.tsl.rfid.asciiprotocol.commands.BatteryStatusCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.ChargeState;
import com.uk.tsl.rfid.asciiprotocol.responders.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trConectar, trLeitura, trGravacao, trInventario, trConfiguracoes;
    private Context context = this;
    private boolean conexao;

    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private BluetoothReaderService readerService;

    private static final int SOLICITA_BLUETOOTH = 1, SOLICITA_CONEXAO = 2, MESSAGE_READ = 3;
    private static String MAC = null;
    private static final String TAG = "Bluetooth", TAGLEITURA = "Teste";
    private TextView tvConectar, tvLeitura, tvGravacao;

    private AsciiCommander commander;
    private LoggerResponder responder;
    Handler handler = null; //Temporario
    private ConnectThread connectThread;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @SuppressLint({"ResourceAsColor", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* api RFiD */
        commander = new AsciiCommander(this);

       readerService = new BluetoothReaderService(
               handler = new Handler(){
                   @Override
                   public void handleMessage(@NonNull Message msg) {
                       Toast.makeText(context, responder.toString(), Toast.LENGTH_SHORT).show();
               }
    });

        responder = new LoggerResponder();

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
        else
            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

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
                   commander.disconnect(); //Encerrando conexão
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
                if (conexao == true) acessaActivity(LeituraActivity.class);
                else Toast.makeText(context, "Conecte com o leitor para prosseguir", Toast.LENGTH_SHORT).show();
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
        device = adapter.getRemoteDevice(MAC);
        commander.connect(device);
        conexao = true;

        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            readerService.connected(socket, device, CONNECTIVITY_SERVICE);
            //socket.connect();
            //  connectThread = new ConnectThread(socket);
            // connectThread.start();
            commander.addResponder(responder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        tvConectar.setText("Desconectar");

        Toast.makeText(context, "Conectado com sucesso: " + device.getName()
                /*commander.getConnectedDeviceName()*/  , Toast.LENGTH_SHORT).show();
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

    private class ConnectThread extends Thread {

        private InputStream inputStream;
        private OutputStream outputStream;

        public ConnectThread(BluetoothSocket mSocket) {
            BluetoothSocket tmp = null;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.i(TAG,  "tentando conectar");
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }

            socket = tmp;
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            while (true){
                try {
                    byte[] buffer = new byte[1024];
                    int bytes;
                    bytes = inputStream.read(buffer);

                    String dadosBt = new String(buffer, 0, bytes);

                    handler.obtainMessage(MESSAGE_READ, bytes, -1, dadosBt)
                            .sendToTarget();
                } catch (IOException connectException) {
                    break;
                }
            }
        }

        public void enviar(String enviar) {
            byte[] msg = enviar.getBytes();
            try{
                outputStream.write(msg);
                Log.i(TAG, "> Enviado ao scanner: " + enviar);
            } catch (IOException e) { }
        }
    }
}

