package telas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;

import com.example.rfidscanner.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;

public class LeituraActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final int REQUISICAO_NOVA_TAG = 5;

    private String dados;
    private ListView lista;
    private TableRow trLeitura, trLocalizar, trExpandir;
    private String textoTag = "", dataFinal = "";

    private SimpleDateFormat dataFormatada;
    private Date date;
    private ArrayList<String> leituras;
    private ArrayAdapter<String> adapter;
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

        dataFormatada = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        date = new Date();

        leituras = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_2, leituras);
        lista.setAdapter(adapter);
        registrarBluetoothReceiver();
    }

    private void validaCampo() {
    trLeitura = (TableRow) findViewById(R.id.trler);
    trLocalizar = (TableRow) findViewById(R.id.trLocalizar);
    trExpandir = (TableRow) findViewById(R.id.trExpandir);
    lista = (ListView) findViewById(R.id.lvLista);

    trLeitura.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LeituraActivity.this, LerActivity.class);
            startActivityForResult(intent, REQUISICAO_NOVA_TAG);
        }
    });
    }

    /* RECEBER LEITURA */
    private void registrarBluetoothReceiver() {
        BluetoothReceiver.bindListener(new BluetoothListener() {
            @Override
            public void messageReceived(Intent intent) {

                dados = intent.getStringExtra("resposta");

                runOnUiThread(new Runnable() {

                    public void run() {
                        if(dados != null && dados.contains("EP: ")){ /* Tag */ /* PREENCHER A LISTA COM OS VALORES */
                            String textoTag =  dados.replaceAll("EP:","");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            Date dataAtual = calendar.getTime();
                            dataFinal = dataFormatada.format(dataAtual);
                            leituras.add(textoTag);
                            leituras.add(dataFinal);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUISICAO_NOVA_TAG && resultCode == RESULT_OK) {
            String recebido = data.getDataString();
            String[] f = recebido.split("_");
        } else {

        }
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
