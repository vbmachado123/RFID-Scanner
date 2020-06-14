package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;


import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import dao.LeituraDao;
import helper.LeituraAdapter;
import model.Leitura;
import model.Lista;
import sql.Database;
import util.DatabaseInitializer;

public class ListaLeituraActivity extends AppCompatActivity {

    private Lista lista;
    private ListView listaSalvar;
    private FloatingActionButton fabSalvar;
    private Toolbar toolbar;
    private LeituraAdapter adapter;
    private String dados;
    private SimpleDateFormat dataFormatada;
    private Date date;
    private Leitura leitura;
    private String textoTag = "", dataFinal = "";
    private ArrayList<Leitura> dadosArray, listaVerifica;
    private Database db;
    private int tamanhoLista = 0;
    private Context context;
    private ImageButton btFechar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_leitura);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        db = Database.getDatabase(this);
        lista = new Lista();
        validaCampo();
        lista = (Lista) getIntent().getSerializableExtra("lista");

        if (lista != null) {
            dadosArray = lista.getLeituras();
            listaVerifica = dadosArray;
        }

        adapter = new LeituraAdapter(this, dadosArray);

        listaSalvar.setAdapter(adapter);
        registrarBluetoothReceiver();
    }

    private void validaCampo() {
        listaSalvar = (ListView) findViewById(R.id.lista_Formularios);
        fabSalvar = (FloatingActionButton) findViewById(R.id.botaoSave);
        btFechar = (ImageButton) findViewById(R.id.fechar);
        fabSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Salvando", "> Iniciando o banco");
                salvarLeitura();
            }
        });

        btFechar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fecharActivity();
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
                        if (dados != null && dados.contains("EP: ")) { /* Tag */ /* PREENCHER A LISTA COM OS VALORES */
                            dataFormatada = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                            date = new Date();
                            leitura = new Leitura();
                            String textoTag = dados.replaceAll("EP: ", "");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            Date dataAtual = calendar.getTime();
                            dataFinal = dataFormatada.format(dataAtual);
                            leitura.setNumeroTag(textoTag);
                            leitura.setDataHora(dataFinal);

                            /* Verifica se já existe na lista */
                            if (!dadosArray.isEmpty()) {
                                for (int i = 0; i < tamanhoLista; i++) {
                                    Leitura l = dadosArray.get(i);
                                    if (!l.getNumeroTag().equals(leitura.getNumeroTag())) {
                                        dadosArray.add(leitura);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            } else { /* Primeira leitura */
                                dadosArray.add(leitura);
                                adapter.notifyDataSetChanged();
                            }
                            tamanhoLista = dadosArray.size();
                        }
                    }
                });
            }
        });
    }

    private void salvarLeitura() {
        PopulateDbAsync task;
        for (int i = 0; i < tamanhoLista; i++) {
            while (!dadosArray.isEmpty()) {
                db = Database.getDatabase(context);
                Leitura l;

                l = dadosArray.get(i);
                task = new PopulateDbAsync(db, l);
                task.execute();
                dadosArray.remove(i);

                adapter.notifyDataSetChanged();
            }
        }
        if (dadosArray.isEmpty()) {
            Toast.makeText(context, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
            fecharActivity();
        }
    }

    private void fecharActivity() {
        lista = new Lista();
        Intent it = new Intent(ListaLeituraActivity.this, LeituraActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        lista.setLeituras(listaVerifica);
        it.putExtra("lista", lista);
        startActivity(it);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                fecharActivity();
                return true;
            case R.id.fechar:
                fecharActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final Database mDb;
        private Leitura leitura;

        PopulateDbAsync(Database db, Serializable leitura) {
            mDb = db;
            this.leitura = (Leitura) leitura;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            LeituraDao dao = mDb.leituraDao();
            Leitura l = dao.pegaUm(leitura.getNumeroTag());
            if(l != null){  /* Verifica se já possui cadastro no banco */
                l.setVezesLida(leitura.getVezesLida() + 1);
                dao.atualizar(l);
                Log.i("Salvando", " > " + leitura.getNumeroTag() + " incrementada: " + l.getVezesLida());
            } else{ /* Caso negativo, insere */
                Long id = dao.inserir(leitura);
                Log.i("Salvando", " > " + leitura.getNumeroTag() + " salva: " + id);
            }
            return null;
        }
    }
}