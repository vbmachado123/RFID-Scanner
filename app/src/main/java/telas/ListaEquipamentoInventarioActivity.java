package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import model.Local;
import model.SubLocal;
import util.Data;

public class ListaEquipamentoInventarioActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trNaoEncontrado, trNaoAtribuido, trEncontrado;
    private TextView txtNaoEncontrado, txtNaoAtribuido, txtEncontrado;
    private FloatingActionButton fabProsseguir;

    /* Listas */
    ArrayList<Equipamento> listaPrimeira, listaNaoEncontrado, listaNaoAtribuida, listaEncontrado;

    private Equipamento equipamento = new Equipamento();
    private Local local = new Local();
    private SubLocal subLocal = new SubLocal();
    private Inventario inventario = new Inventario();
    private EquipamentoInventario equipamentoInventario = new EquipamentoInventario();

    /* LOCALIZAÇÃO */
    private Location location;
    private LocationManager locationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String data = "";

    private String dados, textoTag, dataFinal;

    /*IDs*/
    private int localId, sublocalId, equipamentoId, inventarioId, equipamentoInventarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_equipamento_inventario);

        Intent it = getIntent();
        Bundle extras = it.getExtras();
        local = (Local) extras.getSerializable("local");
        subLocal = (SubLocal) extras.getSerializable("sublocal");

        localId = local.getId();

        validaCampo();
        inventario.setIdLocal(localId);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (subLocal != null) {/* Foi escolhido na tela anterior */
            sublocalId = subLocal.getId();
            inventario.setIdSubLocal(subLocal.getIdLocal());
            boolean iniciado = iniciarInventario();
            if (iniciado)
                Toast.makeText(this, "O inventário " + local.getDescricao() + " - " + subLocal.getDescricao() + " foi iniciado em: " + data, Toast.LENGTH_LONG).show();
        } else { /* Não foi escolhido na tela anterior */
            boolean iniciado = iniciarInventario();
            if (iniciado)
                Toast.makeText(this, "O inventário " + local.getDescricao() + " foi iniciado em: " + data, Toast.LENGTH_LONG).show();
        }

        registrarBluetoothReceiver();
    }

    private void validaCampo() {
        trNaoEncontrado = (TableRow) findViewById(R.id.trNaoEncontrado);
        txtNaoEncontrado = (TextView) findViewById(R.id.txtNaoEncontrado);
        trNaoAtribuido = (TableRow) findViewById(R.id.trNaoAtribuido);
        txtNaoAtribuido = (TextView) findViewById(R.id.txtNaoAtribuido);
        trEncontrado = (TableRow) findViewById(R.id.trEncontrado);
        txtEncontrado = (TextView) findViewById(R.id.txtEncontrado);
        fabProsseguir = (FloatingActionButton) findViewById(R.id.fabProsseguir);

//        recuperaEquipamentos();

    }

    private void recuperaEquipamentos() { /* Carrega Lista Primeira */
        carregarListaNaoEncontrado();

        if (sublocalId < 0) { /* SubLocal Anteriormente Selecionado */
            for (Equipamento e : listaPrimeira) {
                if (e.getSubLocalId() == sublocalId) {
                    listaNaoEncontrado.add(e);
                }
            }
        } else
            listaNaoEncontrado.addAll(listaPrimeira);

        txtNaoEncontrado.setText(listaNaoEncontrado.size());
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

                            equipamento = new Equipamento();
                            textoTag = dados.replaceAll("EP: ", "");

                            dataFinal = Data.getDataEHoraAual("dd/MM/yyyy - HH:mm");
                            equipamento.setNumeroTag(textoTag);

                            /* Verifica se já existe na lista */
                            /*tamanhoLista = leituras.size();
                            if (!leituras.isEmpty()) {

                                validador.addAll(leituras);

                                if(!leituras.contains(l.getNumeroTag())){
                                    leituras.add(l);
                                    adapter.notifyDataSetChanged();
                                }*/
                            //funciona
                                /*     boolean inserir= true;
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
                                }*/
                            /* } else { *//* Primeira leitura *//*
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

    private boolean iniciarInventario() {
        boolean iniciar;

        return iniciar = true;
    }

    private void carregarListaNaoEncontrado() {

    }
}