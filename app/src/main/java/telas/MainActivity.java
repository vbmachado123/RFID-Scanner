package telas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.rfidscanner.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/* CODIGO MODELO DE CONEXÃO AO DISPOSITIVO
* REALIZA A CONEXAO E A LEITURA DAS TAGS
* UTILIZANDO A API BLUETOOTH NATIVA DO ANDROID */

public class MainActivity extends AppCompatActivity {

    private Context context = this;
    private ConnectThread connectThread;

    private static final int SOLICITA_BLUETOOTH = 1, SOLICITA_CONEXAO = 2, MESSAGE_READ = 3;
    private static final String TAG = "Bluetooth";
    private static String MAC = null;

    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;

    private StringBuilder dadosBluetooth = new StringBuilder();

    private Button btConexao, btEnviar;
    private boolean conexao = false;
    private Handler handler;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "> Iniciando a MainActivity");

        btConexao = (Button) findViewById(R.id.btnConexao);
        btEnviar = (Button) findViewById(R.id.btnEnviar);

        Log.i(TAG, "> Verificando o bluetooth");
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
            Log.i(TAG, "> Bluetooth não suportado");
        } else if(!adapter.isEnabled()) {
            Log.i(TAG, "> Bluetooth desativado");
            Intent ativaIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaIntent, SOLICITA_BLUETOOTH);
        }
        else {
            Log.i(TAG, "> Bluetooth suportado");
        }

        btConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "> Botão conexão pressionado");

                if(conexao) {
                    //desconectar
                    try{
                        Log.i(TAG, "> Conexão encerrada com sucesso");
                        socket.close();
                        conexao = false;
                        btConexao.setText("Conectar");
                    } catch (IOException e){
                        Log.i(TAG, "> Erro ao encerrar a conexão: " + e);
                    }
                } else {
                    //conectar
                    Intent lista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(lista, SOLICITA_CONEXAO);
                }
            }
        });

        btEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enviar = "12345678900";
                Log.i(TAG, "> Enviando ao scanner: " + enviar);
                connectThread.enviar(enviar);
            }
        });

        handler = new Handler(){

            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == MESSAGE_READ) {
                    String recebido = (String) msg.obj;
                    Log.i(TAG, "> Mensagem recebida: " + recebido );
                    if(recebido.length() > 19) {
                       limparTexto(recebido);
                    }
                }
            }
        };
    }

      private void limparTexto(String recebido) {
        /* RECUPERANDO A DATA */
        SimpleDateFormat dataFormatada = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Date dataAtual = calendar.getTime();
        String dataFinal = dataFormatada.format(dataAtual);

        String[] textoRemover = new String[]{" ", "\n", "\t", "P:", "OK:", "P: ", "OK: ", "\\s"};

        String textoTag = "";
        String textoExibe = "";

        for(int i = 0; i < textoRemover.length; i++){
            textoTag =  recebido.replaceAll(textoRemover[i],"");

            if( !textoTag.contains("E:"))
                textoExibe = textoTag;
            else
                textoExibe = "Erro, tente novamente";
        }
        String tFinal = textoExibe;
        dadosBluetooth.append(tFinal);

        String t1 = "" , t2 = "";
        if(tFinal.length() >= 19) {
            if(tFinal.contains(textoRemover[3]) || tFinal.contains(textoRemover[4])) {
                t1 =  tFinal.replaceAll(textoRemover[3],"");
                t2 =  t1.replaceAll(textoRemover[4],"");
                tFinal = t2;
            } else {
                dadosBluetooth.append(tFinal);
                Log.i(TAG, "> Mensagem recebida: " + recebido + " as " + dataFinal);
                Log.i(TAG, "-------------------");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(TAG, "> onActivityResult chamado");
       if(resultCode == RESULT_OK) {
           switch (requestCode) {
               case SOLICITA_BLUETOOTH:
                   Log.i(TAG, "> Bluetooth ativado");
                   break;
               case  SOLICITA_CONEXAO:
                   Log.i(TAG, "> MAC foi recebido");
                   Log.i(TAG, "-------------------");
                   MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);
                   Log.i(TAG, "> MAC foi recebido: " + MAC);
                    device = adapter.getRemoteDevice(MAC);

                    try{
                        Log.i(TAG, "> Tentando inciar a conexão");
                        socket = device.createRfcommSocketToServiceRecord(uuid);
                        socket.connect();
                        Log.i(TAG, "> Conexão criada");
                        conexao = true;

                        connectThread = new ConnectThread(socket);
                        connectThread.start();

                        btConexao.setText("Desconectar");
                    }catch (IOException e) {
                        conexao = false;
                        Log.i(TAG, "> Erro ao iniciar a conexão: " + e);
                    }

                    break;
                   default:
                   super.onActivityResult(requestCode, resultCode, data);
           }
       }
    }

    public class ConnectThread extends Thread {

       private InputStream inputStream;
       private OutputStream outputStream;

        public ConnectThread(BluetoothSocket mSocket) {

            Log.i(TAG, "> ConnectThread foi chamado");

            Log.i(TAG,  device.getAddress() + "localizado");

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
            Log.i(TAG,  "run() foi chamado");
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
