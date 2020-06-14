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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import helper.LeituraAdapter;
import model.Leitura;
import model.Lista;

public class LeituraActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final int REQUISICAO_NOVA_TAG = 5;

    private String dados;
    private ListView lista;
    private TableRow trLeitura, trLocalizar, trExpandir;
    private String textoTag = "", dataFinal = "";

    private SimpleDateFormat dataFormatada;
    private Date date;
    private ArrayList<Leitura> leituras;
    private LeituraAdapter adapter;
    private Leitura l;
    private FloatingActionButton fabAbrir;
    Lista oLista = new Lista();
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
        oLista = new Lista();
        leituras = new ArrayList<>();
        oLista = (Lista) getIntent().getSerializableExtra("lista");
        if(oLista != null) leituras.addAll(oLista.getLeituras());

        adapter = new LeituraAdapter(this, leituras);

        lista.setAdapter(adapter);
        registrarBluetoothReceiver();
    }

    private void validaCampo() {
    trLeitura = (TableRow) findViewById(R.id.trler);
    trLocalizar = (TableRow) findViewById(R.id.trLocalizar);
    trExpandir = (TableRow) findViewById(R.id.trExpandir);
    lista = (ListView) findViewById(R.id.lvLista);
    fabAbrir = (FloatingActionButton) findViewById(R.id.botaoAbrir);

    trLeitura.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LeituraActivity.this, LerActivity.class);
            startActivityForResult(intent, REQUISICAO_NOVA_TAG);
        }
    });

    trExpandir.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            abrirLista();
        }
    });

    fabAbrir.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            abrirLista();
        }
    });

    }

    private void abrirLista() {
        Lista lista = new Lista();
        Intent it = new Intent(LeituraActivity.this, ListaLeituraActivity.class);
        lista.setLeituras(leituras);
        it.putExtra("lista", lista);
        startActivity(it);
        finish();
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
                            dataFormatada = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                            date = new Date();
                            l = new Leitura();
                            String textoTag =  dados.replaceAll("EP:","");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            Date dataAtual = calendar.getTime();
                            dataFinal = dataFormatada.format(dataAtual);
                            l.setNumeroTag(textoTag);
                            l.setDataHora(dataFinal);
                            leituras.add(l);

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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

