package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import model.Leitura;
import services.BluetoothService;


public class LeituraActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private String dados;
    private ListView listaTags;
    private TableRow trLeitura, trLocalizar, trExpandir;
    private List<Leitura> Leitura;
    private List<Leitura> LeiturasFiltradas = new ArrayList<>();
    private String textoTag = "";

    private SimpleDateFormat dataFormatada;
    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitura);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        validaCampo();
        registrarBluetoothReceiver();

         dataFormatada = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
         date = new Date();

        // listaTags.setAdapter();
    }

    private void validaCampo() {
    trLeitura = (TableRow) findViewById(R.id.trLeitura);
    trLocalizar = (TableRow) findViewById(R.id.trLocalizar);
    trExpandir = (TableRow) findViewById(R.id.trExpandir);
    listaTags = (ListView) findViewById(R.id.lvTags);

    }

    /* RECEBER LEITURA */
    private void registrarBluetoothReceiver() {
        BluetoothReceiver.bindListener(new BluetoothListener() {
            @Override
            public void messageReceived(Intent intent) {

                dados = intent.getStringExtra("resposta");

                runOnUiThread(new Runnable() {

                    public void run() {
                        if (dados != null){ /* PREENCHER A LISTA COM OS VALORES */
                            Log.i("Teste-Leitura", dados);
                            if(dados.contains("EP:")){ /* Tag */
                                String textoTag =  dados.replaceAll("EP:","");
                                /* RECUPERANDO A DATA */
                                Toast.makeText(LeituraActivity.this, textoTag, Toast.LENGTH_SHORT).show();
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                Date dataAtual = calendar.getTime();
                                String dataFinal = dataFormatada.format(dataAtual);



                            }
                        }else { /*  */

                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
