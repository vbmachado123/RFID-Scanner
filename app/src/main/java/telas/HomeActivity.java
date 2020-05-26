package telas;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.*;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.io.Reader;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DiscoveryCallback;
import model.Inventario;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trConectar, trLeitura, trGravacao, trInventario, trConfiguracoes;
    private Context Context = this;
    private boolean conexao;
    private Bluetooth bluetooth;
    private BluetoothCallback bluetoothCallback;
    private BluetoothAdapter adapter = null;
    private static final int SOLICITA_BLUETOOTH = 1, SOLICITA_CONEXAO = 2;
    private static String MAC = null;
    private static final String TAG = "Bluetooth";
    private TextView tvConectar, tvLeitura, tvGravacao;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
        } else if(!adapter.isEnabled()) {
            Intent ativaIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaIntent, SOLICITA_BLUETOOTH);
        }

        bluetooth = new Bluetooth(this);
        bluetooth.setBluetoothCallback(bluetoothCallback);

        /* VERIFICANDO SE POSSUI CONEXÃO ATIVA COM O LEITOR */
        if(!conexao)
            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
        else
            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        validaCampo();
        /*AsciiCommander.createSharedInstance(getApplicationContext());*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        if(bluetooth.isEnabled()){
             //doStuffWhenBluetoothOn() ...
        } else {
            bluetooth.enable();
        }
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
                   bluetooth.disconnect(); //Encerrando conexão
                   tvConectar.setText("Conectar");
                   toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
               } else { //conexao desativada
                   Intent lista = new Intent( HomeActivity.this, ListaDispositivos.class);
                   startActivityForResult(lista , SOLICITA_CONEXAO);
               }
            }
        });

        trLeitura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao == true) acessaActivity(LeituraActivity.class);
                else Toast.makeText(Context, "Conecte com o leitor para prosseguir", Toast.LENGTH_SHORT).show();
            }
        });

        trGravacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao == true) acessaActivity(GravacaoActivity.class);
                else Toast.makeText(Context, "Conecte com o leitor para prosseguir", Toast.LENGTH_SHORT).show();
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
                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);
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

    @SuppressLint("ResourceAsColor")
    private void validaConexao() {
        bluetooth.connectToAddress(MAC); //Conectando
        conexao = true;
        toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        tvConectar.setText("Desconectar");
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
}

