package telas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import adapter.LeituraAdapter;
import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import dao.InventarioDao;
import dao.InventarioNegadoDao;
import dao.LeituraDao;
import dao.LocalDao;
import dao.StatusDao;
import dao.SubLocalDao;
import model.Leitura;
import model.Lista;
import service.BluetoothService;
import sql.Conexao;
import util.Data;
import util.InventoryModel;
import util.Preferencias;

public class LeituraActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final int REQUISICAO_NOVA_TAG = 5;

    private String dados;
    private ListView lista;
    private TableRow trLeitura, trLocalizar, trExpandir, trExportar;
    private String textoTag = "", dataFinal = "";

    private SimpleDateFormat dataFormatada;
    private Date date;
    private ArrayList<Leitura> leituras, validador;
    private LeituraAdapter adapter;
    private Leitura l = new Leitura();
    private FloatingActionButton fabAbrir;
    private Lista oLista = new Lista();
    private Context context = this;
    private int tamanhoLista = 0;
    private Conexao db;
    private LeituraDao leituraDao;
    private TextView tagsLidas;
    Cursor cursor;
    private InventoryModel mModel;
    private SeekBar sbPotencia;
    private TextView tvPotencia;
    private int potencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitura);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        leituraDao = new LeituraDao(context);
        validaCampo();
        oLista = new Lista();
        leituras = new ArrayList<>();
        validador = new ArrayList<>();
        oLista = (Lista) getIntent().getSerializableExtra("lista");
        if (oLista != null) leituras.addAll(oLista.getLeituras());

        adapter = new LeituraAdapter(this, leituras);

        lista.setAdapter(adapter);
        registrarBluetoothReceiver();
    }

    @SuppressLint("NewApi")
    private void validaCampo() {
        trLocalizar = (TableRow) findViewById(R.id.trLocalizar);
        trExpandir = (TableRow) findViewById(R.id.trExpandir);
        trExportar = (TableRow) findViewById(R.id.trExportar);
        lista = (ListView) findViewById(R.id.lvLista);
        tagsLidas = (TextView) findViewById(R.id.txtTagsLidas);
        fabAbrir = (FloatingActionButton) findViewById(R.id.botaoAbrir);
        sbPotencia = (SeekBar) findViewById(R.id.sbPotencia);
        tvPotencia = (TextView) findViewById(R.id.tvPotencia);

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
                if (leituras != null) {
                    salvarLeitura();
                    boolean exportar = leituraDao.exportar();
                    if (exportar) {
                        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
                        String nomePasta = exportDir + "/SOS RFiD";
                        String dataArquivo = Data.getDataEHoraAual("ddMMyyyy_HHmm");
                        Toast.makeText(context, "Arquivo salvo em: " +
                                nomePasta + "/Leituras Realizadas " + dataArquivo + ".csv", Toast.LENGTH_SHORT).show();
                        limparBanco();
                    }
                } else
                    Toast.makeText(context, "Nenhuma leitura foi realizada!", Toast.LENGTH_SHORT).show();
            }
        });


        sbPotencia.setMax(30);
        sbPotencia.setMin(2);
        sbPotencia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPotencia.setText(progress + " dBm");
                potencia = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { /* Passar para a service */
                Preferencias preferencias = new Preferencias(LeituraActivity.this);
                preferencias.salvarPotencia(potencia);
            }
        });
    }

    private void limparBanco() {
        EquipamentoDao equipamentoDao = new EquipamentoDao(this);
        EquipamentoInventarioDao equipamentoInventarioDao = new EquipamentoInventarioDao(this);
        InventarioDao inventarioDao = new InventarioDao(this);
        InventarioNegadoDao inventarioNegadoDao = new InventarioNegadoDao(this);
        LocalDao localDao = new LocalDao(this);
        StatusDao statusDao = new StatusDao(this);
        SubLocalDao subLocalDao = new SubLocalDao(this);
        leituraDao.limparTabela();
        equipamentoDao.limparTabela();
        equipamentoInventarioDao.limparTabela();
        inventarioDao.limparTabela();
        inventarioNegadoDao.limparTabela();
        localDao.limparTabela();
        statusDao.limparTabela();
        subLocalDao.limparTabela();
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

                            l = new Leitura();
                            textoTag = dados.replaceAll("EP: ", "");

                            dataFinal = Data.getDataEHoraAual("dd/MM/yyyy - HH:mm");
                            l.setNumeroTag(textoTag);
                            l.setDataHora(dataFinal);

                            /* Funcionando */
                            if (!leituras.isEmpty()) { /* Verifica se ja existe na lista */
                                boolean inserir = true;
                                for (Leitura leitura : leituras) {
                                    if (leitura.getNumeroTag().equals(l.getNumeroTag())) {
                                        inserir = false;
                                        break;
                                    }
                                }

                                if (inserir) {
                                    leituras.add(l);
                                    adapter.notifyDataSetChanged();
                                    tagsLidas.setText(String.valueOf(leituras.size()));
                                }

                            } else {
                                leituras.add(l);
                                adapter.notifyDataSetChanged();
                                tagsLidas.setText(String.valueOf(leituras.size()));
                            }

                            /* Verifica se já existe na lista */
                         /*   tamanhoLista = leituras.size();
                            if (!leituras.isEmpty()) {

                                validador.addAll(leituras);

                                if(!leituras.contains(l.getNumeroTag())){
                                    leituras.add(l);
                                    adapter.notifyDataSetChanged();
                                    tagsLidas.setText(String.valueOf(leituras.size()));
                                }
                           //funciona
                                *//*     boolean inserir= true;
                                for (int i = 0; i < tamanhoLista; i++) {
                                    Leitura leitura1 = leituras.get(i);
                                    if (l.getNumeroTag().equals(leitura1.getNumeroTag())) {
                                        inserir =false;
                                        break;
                                    }
                                }
                                if(inserir){
                                    leituras.add(l);
                                    adapter.notifyDataSetChanged();
                                }*//*
                            } else { *//* Primeira leitura *//*
                                leituras.add(l);
                                adapter.notifyDataSetChanged();
                            }
                            tamanhoLista = leituras.size();*/
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
        ArrayList<Leitura> arrayList = (ArrayList<Leitura>) leituraDao.getAll();

        for (Leitura l : leituras) {
            if (!arrayList.isEmpty()) {
                for (Leitura leitura : arrayList) {
                    if (l.getNumeroTag().equals(leitura.getNumeroTag())) { /* Já existe no banco */
                        leitura.setVezesLida(leitura.getVezesLida() + 1);
                        leituraDao.atualizar(leitura);
                        break;
                    } else { /* Não existe no banco */
                        leituraDao.inserir(l);
                        break;
                    }
                    /*if(leitura != null) { *//* Verifica se já existe no banco *//*
                  leitura.setVezesLida(leitura.getVezesLida() + 1);
              } else { *//* Ainda nao existe no banco *//*
                  leituraDao.inserir(l);
              }*/
                }
            } else { /* Não existe no banco */
                l.setVezesLida(1);
                leituraDao.inserir(l);
            }
            /*if(l.getNumeroTag() != null) leituraDao.inserir(l);*/
        }

        leituras.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(context, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.limpar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            case R.id.item_limpar:
                leituras.clear();
                adapter.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

