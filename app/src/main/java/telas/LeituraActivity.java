package telas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import dao.LeituraDao;
import helper.LeituraAdapter;
import helper.LeituraHelper;
import model.Leitura;
import model.Lista;
import sql.Database;
import util.Csv;
import util.Permissao;

public class LeituraActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final int REQUISICAO_NOVA_TAG = 5;

    private String dados;
    private ListView lista;
    private TableRow trLeitura, trLocalizar, trExpandir, trExportar;
    private String textoTag = "", dataFinal = "";

    private SimpleDateFormat dataFormatada;
    private Date date;
    private ArrayList<Leitura> leituras;
    private LeituraAdapter adapter;
    private Leitura l = new Leitura();
    private FloatingActionButton fabAbrir;
    private Lista oLista = new Lista();
    private Context context = this;
    private int tamanhoLista = 0;
    private Database db;
    private LeituraHelper helper;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitura);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        helper = new LeituraHelper(context);
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
    trExportar = (TableRow) findViewById(R.id.trExportar);
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

    trExportar.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(leituras != null){
                helper.exportar();
                File exportDir = new File(Environment.getExternalStorageDirectory(), "");
                String nomePasta = exportDir + "/SOS RFiD";
                Toast.makeText(context, "Arquivo salvo em: " +
                        nomePasta + "/Leituras Realizadas " + l.getDataHora() + ".csv", Toast.LENGTH_SHORT).show();
                helper.limparBanco();
            } else
                Toast.makeText(context, "Nenhuma leitura foi realizada!", Toast.LENGTH_SHORT).show();
        }
    });
    }

    private void abrirLista() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Dialog);
        View v = getLayoutInflater().inflate(R.layout.lista_leituras, null);
        builder.setAdapter(adapter, null);
        builder.setTitle("Leituras Realizadas");
        builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                salvarLeitura();
            }
        });

       // builder.setView(v);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /* RECEBER LEITURA */
    private void registrarBluetoothReceiver() {
        BluetoothReceiver.bindListener(new BluetoothListener() {
            @Override
            public void messageReceived(Intent intent) {

                dados = intent.getStringExtra("resposta");

                runOnUiThread(new Runnable() {

                    public void run() {
                        if (dados != null && dados.contains("EP: ")) { /* Tag */ /* PREENCHER A LISTA COM OS VALORES */
                            dataFormatada = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                            date = new Date();
                            l = new Leitura();
                            String textoTag = dados.replaceAll("EP: ", "");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            Date dataAtual = calendar.getTime();
                            dataFinal = dataFormatada.format(dataAtual);
                            l.setNumeroTag(textoTag);
                            l.setDataHora(dataFinal);

                            /* Verifica se já existe na lista */
                            if (!leituras.isEmpty()) {
                                for (int i = 0; i < tamanhoLista; i++) {
                                    Leitura leitura1 = leituras.get(i);
                                    if (!l.getNumeroTag().equals(leitura1.getNumeroTag())) {
                                        leituras.add(l);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            } else { /* Primeira leitura */
                                leituras.add(l);
                                adapter.notifyDataSetChanged();
                            }
                            tamanhoLista = leituras.size();
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

    private void salvarLeitura() {
        for (Leitura l : leituras) {
                db = Database.getDatabase(context);
                if(l.getNumeroTag() != null) helper.atualizar(l);
        }

            leituras.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(context, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();

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

