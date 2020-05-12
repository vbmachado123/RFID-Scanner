package telas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.rfidscanner.R;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int SOLICITA_BLUETOOTH = 1, SOLICITA_CONEXAO = 2;
    private static final String TAG = "Bluetooth";
    private static String MAC = null;

    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;

    private Button btConexao;
    private boolean conexao = false;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "> Iniciando a MainActivity");

        btConexao = (Button) findViewById(R.id.btnConexao);

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
                        new ConnectThread(device);
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

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            Log.i(TAG,  device.getAddress() + "localizado");

            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
                Log.i(TAG,  "tentando conectar");
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;

            try {
                Log.i(TAG, "> Input: " +  socket.getInputStream());
                Log.i(TAG, "> Output: " +  socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
