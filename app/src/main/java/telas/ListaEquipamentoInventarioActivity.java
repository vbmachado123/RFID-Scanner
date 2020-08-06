package telas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapter.EquipamentoAdapter;
import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import dao.InventarioDao;
import dao.InventarioNegadoDao;
import dao.LocalDao;
import dao.StatusDao;
import dao.SubLocalDao;
import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import model.InventarioNegado;
import model.Local;
import model.Status;
import model.SubLocal;
import util.Data;
import util.InventoryModel;
import util.Preferencias;

public class ListaEquipamentoInventarioActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trNaoEncontrado, trNaoAtribuido, trEncontrado;
    private TextView txtNaoEncontrado, txtNaoAtribuido, txtEncontrado;
    private FloatingActionButton fabProsseguir;

    /* Listas */
    ArrayList<Equipamento> listaPrimeira, listaNaoEncontrado, listaNaoAtribuida, listaEncontrado;
    ArrayList<String> latitudeEncontrada, longitudeEncontrada, latitudeNaoAtribuida, longitudeNaoAtribuida;

    private Equipamento equipamento = new Equipamento();
    private Local local;
    private SubLocal subLocal;
    private Inventario inventario = new Inventario();
    private EquipamentoInventario equipamentoInventario = new EquipamentoInventario();

    /* LOCALIZAÇÃO */
    private Location location;
    private LocationManager locationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String data = "";
    private Address endereco;

    private String dados, textoTag, dataFinal;

    private EquipamentoDao equipamentoDao;
    private EquipamentoInventarioDao equipamentoInventarioDao;
    private InventarioDao inventarioDao;
    private InventarioNegadoDao inventarioNegadoDao;
    private StatusDao statusDao;
    private LocalDao localDao;
    private SubLocalDao subLocalDao;

    private SeekBar sbPotencia;
    private TextView tvPotencia;

    /* IDs */
    private int localId, sublocalId, equipamentoId, inventarioId, equipamentoInventarioId;

    /* Adapter */
    private EquipamentoAdapter adapter;

    private InventoryModel mModel;
    private AsciiCommander commander;
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    private int backButtonCount = 0;
    private boolean inventarioI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_equipamento_inventario);

        local = new Local();
        subLocal = new SubLocal();

        equipamentoDao = new EquipamentoDao(this);
        equipamentoInventarioDao = new EquipamentoInventarioDao(this);
        inventarioDao = new InventarioDao(this);
        inventarioNegadoDao = new InventarioNegadoDao(this);
        statusDao = new StatusDao(this);
        localDao = new LocalDao(this);
        subLocalDao = new SubLocalDao(this);

        mModel = new InventoryModel();
        commander = getCommander();
        mModel.setCommander(commander);
        Intent it = getIntent();
        Bundle extras = it.getExtras();
        if (extras != null) {
            local = (Local) extras.getSerializable("local");
            subLocal = (SubLocal) extras.getSerializable("sublocal");
            localId = local.getId();
        } else {
            Inventario i = inventarioDao.recupera();
            local = localDao.getById(i.getIdLocal());
            subLocal = subLocalDao.getById(i.getIdSubLocal());
            localId = local.getId();
         //   sublocalId = subLocal.getId();
            inventarioI = true;
        }

        validaCampo();
        sbPotencia.setOnSeekBarChangeListener(mPowerSeekBarListener);
        defineLimitesPotencia();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (!inventarioI) {
            inventario.setIdLocal(localId);
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
        }

        registrarBluetoothReceiver();
    }

    /* SeekBar -> Alterar potencia */
    private void defineLimitesPotencia() {
        DeviceProperties deviceProperties = getCommander().getDeviceProperties();

        sbPotencia.setMax(deviceProperties.getMaximumCarrierPower() - deviceProperties.getMinimumCarrierPower());
        mPowerLevel = deviceProperties.getMaximumCarrierPower();
        sbPotencia.setProgress(mPowerLevel - deviceProperties.getMinimumCarrierPower());
    }

    private SeekBar.OnSeekBarChangeListener mPowerSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Nothing to do here
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            // Update the reader's setting only after the user has finished changing the value
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + seekBar.getProgress());
            mModel.getCommand().setOutputPower(mPowerLevel);
            mModel.updateConfiguration();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + progress);
        }
    };

    private void updatePowerSetting(int level) {
        mPowerLevel = level;
        tvPotencia.setText(mPowerLevel + " dBm");
    }


    @SuppressLint("NewApi")
    private void validaCampo() {
        trNaoEncontrado = (TableRow) findViewById(R.id.trNaoEncontrado);
        txtNaoEncontrado = (TextView) findViewById(R.id.txtNaoEncontrado);
        trNaoAtribuido = (TableRow) findViewById(R.id.trNaoAtribuido);
        txtNaoAtribuido = (TextView) findViewById(R.id.txtNaoAtribuido);
        trEncontrado = (TableRow) findViewById(R.id.trEncontrado);
        txtEncontrado = (TextView) findViewById(R.id.txtEncontrado);
        fabProsseguir = (FloatingActionButton) findViewById(R.id.fabProsseguir);
        sbPotencia = (SeekBar) findViewById(R.id.sbPotencia);
        tvPotencia = (TextView) findViewById(R.id.tvPotencia);

        recuperaEquipamentos();

        abrirListas();
        fabProsseguir.getBackground().mutate().setTint(ContextCompat.getColor(ListaEquipamentoInventarioActivity.this, R.color.vermelhodesativado));

    }

    @Override
    public void onBackPressed() {

        if (backButtonCount >= 1) {
            AlertDialog dialog = new AlertDialog.Builder(ListaEquipamentoInventarioActivity.this, R.style.Dialog)
                    .setTitle("Atenção")
                    .setMessage("Deseja realmente sair? as informações serão perdidas!")
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("Sair", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            inventarioDao.limparTabela();
                            equipamentoInventarioDao.limparTabela();
                            inventarioNegadoDao.limparTabela();

                            Intent intent = new Intent(ListaEquipamentoInventarioActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).create();
            dialog.show();
        } else {
            Toast.makeText(this, "Pressione novamente para sair do inventário.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    private void recuperaEquipamentos() { /* Carrega Lista Primeira */
        listaPrimeira = new ArrayList<>();
        listaEncontrado = new ArrayList<>();
        listaNaoEncontrado = new ArrayList<>();
        listaNaoAtribuida = new ArrayList<>();
        /* Para a localizacao das tags */
        latitudeEncontrada = new ArrayList<>();
        longitudeEncontrada = new ArrayList<>();
        latitudeNaoAtribuida = new ArrayList<>();
        longitudeNaoAtribuida = new ArrayList<>();

        listaPrimeira = (ArrayList<Equipamento>) equipamentoDao.getByLocal(local.getId());

        if (subLocal != null) {/* SubLocal Anteriormente Selecionado*/
            for (Equipamento e : listaPrimeira) {
                Log.i("Copulando", String.valueOf(e.getLocalId()) + " 1 " + e.getSubLocalId());
                if (e.getSubLocalId() == subLocal.getId()) {
                    listaNaoEncontrado.add(e);
                    Log.i("Copulando-Filtro", String.valueOf(e.getLocalId()) + " " + e.getSubLocalId());
                }
            }
        } else
            listaNaoEncontrado.addAll(listaPrimeira);

        txtNaoEncontrado.setText(String.valueOf(listaNaoEncontrado.size()));
        txtNaoAtribuido.setText(String.valueOf(0));
        txtEncontrado.setText(String.valueOf(0));
    }

    private void abrirListas() {

        trNaoEncontrado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!listaNaoEncontrado.isEmpty()) {
                    adapter = new EquipamentoAdapter(ListaEquipamentoInventarioActivity.this, listaNaoEncontrado);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ListaEquipamentoInventarioActivity.this, R.style.Dialog);
                    View view = getLayoutInflater().inflate(R.layout.lista_leituras, null);
                    builder.setAdapter(adapter, null);
                    builder.setTitle("Não Encontrados");
                    builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        trNaoAtribuido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!listaNaoAtribuida.isEmpty()) {
                    adapter = new EquipamentoAdapter(ListaEquipamentoInventarioActivity.this, listaNaoAtribuida);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ListaEquipamentoInventarioActivity.this, R.style.Dialog);
                    View view = getLayoutInflater().inflate(R.layout.lista_leituras, null);
                    builder.setAdapter(adapter, null);
                    builder.setTitle("Não Atribuidos");
                    builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else
                    Toast.makeText(ListaEquipamentoInventarioActivity.this, "A lista está vazia!", Toast.LENGTH_SHORT).show();
            }
        });

        trEncontrado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!listaEncontrado.isEmpty()) {
                    adapter = new EquipamentoAdapter(ListaEquipamentoInventarioActivity.this, listaEncontrado);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ListaEquipamentoInventarioActivity.this, R.style.Dialog);
                    View view = getLayoutInflater().inflate(R.layout.lista_leituras, null);
                    builder.setAdapter(adapter, null);
                    builder.setTitle("Encontrados");
                    builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else
                    Toast.makeText(ListaEquipamentoInventarioActivity.this, "A lista está vazia!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void salvar() {

        String dataSalvamento = Data.getDataEHoraAual("dd/MM/yyyy - HH:mm");
        final ProgressDialog progressDialog = new ProgressDialog(ListaEquipamentoInventarioActivity.this, R.style.Dialog);

        class SalvarAsync extends AsyncTask<Void, Void, Void> {
            int progresso = 0;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog.setTitle("Salvando Listas...");
                progressDialog.show();
                progressDialog.setMessage("Aguarde...");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (!listaEncontrado.isEmpty()) { /* Ainda existem itens nao encontrados */
                    if (!listaNaoEncontrado.isEmpty()) { /* Ainda existem itens nao encontrados */
                        //listaNaoAtribuida.addAll(listaNaoEncontrado);
                        for (int i = 0; i < listaNaoEncontrado.size(); i++) {

                            Equipamento e = listaNaoEncontrado.get(i);
                            equipamentoInventario = new EquipamentoInventario();
                            InventarioNegado inventarioNegado = new InventarioNegado();

                            /*latitudeNaoAtribuida.add(String.valueOf(latitude));
                            longitudeNaoAtribuida.add(String.valueOf(longitude));
*/
                            /* INSERE EQUIPAMENTO NA TABELA INVENTARIONEGADO */
             /*
                            model.Status status = new model.Status();
                            status.setStatus("Nao_Encontrado");
                            Long statusId = statusDao.inserir(status);
                            inventarioNegado.setIdStatus(Math.toIntExact(statusId));
                            inventarioNegado.setIdInventario(inventarioId);
                            inventarioNegado.setDataHora(dataSalvamento);
                            inventarioNegado.setLatitude(String.valueOf(latitude));
                            inventarioNegado.setLongitude(String.valueOf(longitude));
                           *//* inventarioNegado.setLatitude(latitudeNaoAtribuida.get(i));
                            inventarioNegado.setLongitude(longitudeNaoAtribuida.get(i));*//*
                            inventarioNegado.setNumeroTag(e.getNumeroTag());
                            inventarioNegadoDao.inserir(inventarioNegado);*/

                            /* INSERE EQUIPAMENTO NA TABELA EQUIPAMENTOINVENTARIO */
                            model.Status status = new model.Status();
                            status.setStatus("Nao_Encontrado");
                            Long statusId = statusDao.inserir(status);
                            equipamentoInventario.setDataHora(dataSalvamento);
                        /*equipamentoInventario.setLatitude(String.valueOf(latitude));
                         equipamentoInventario.setLongitude(String.valueOf(longitude));*/
                            equipamentoInventario.setLatitude(String.valueOf(latitude));
                            equipamentoInventario.setLongitude(String.valueOf(longitude));
                            equipamentoInventario.setIdInventario(inventarioId);
                            equipamentoInventario.setIdEquipamento(e.getId());
                            equipamentoInventario.setIdStatus(Math.toIntExact(statusId));
                            Long idEquipamento = equipamentoInventarioDao.inserir(equipamentoInventario);
                            Log.i("Salvando", "Salva - EquipamentoInventario: " + idEquipamento);
                            Log.i("Salvando", "Salva - EquipamentoInventario idStatus: " + statusId);

                        }
                    } /*else { *//* Salvar na InventarioNegado */
                    int i = 0;

                    for (Equipamento e : listaNaoAtribuida) {
                        InventarioNegado inventarioNegado = new InventarioNegado();
                        model.Status status = new model.Status();
                        status.setStatus("Nao_Atribuida");
                        Long statusId = statusDao.inserir(status);
                        inventarioNegado.setIdStatus(Math.toIntExact(statusId));
                        inventarioNegado.setIdInventario(inventarioId);
                        inventarioNegado.setDataHora(dataSalvamento);
                        /*inventarioNegado.setLatitude(String.valueOf(latitude));
                        inventarioNegado.setLongitude(String.valueOf(longitude));*/
                        inventarioNegado.setLatitude(latitudeNaoAtribuida.get(i));
                        inventarioNegado.setLongitude(longitudeNaoAtribuida.get(i));
                        inventarioNegado.setNumeroTag(e.getNumeroTag());
                        Long inventarioNegadoId = inventarioNegadoDao.inserir(inventarioNegado);


                        // progresso++;
                        i++;
                        Log.i("Salvando", "Salva - InventarioNegado: " + inventarioNegadoId);
                    }
                    // } /* Salvar EquipamentoInventario */

                    int x = 0;
                    //progressDialog.setMessage("Salvando Equipamento Inventaio");
                    for (Equipamento e : listaEncontrado) {
                        equipamentoInventario = new EquipamentoInventario();
                        model.Status status = new model.Status();
                        status.setStatus("Encontrada");
                        Long statusId = statusDao.inserir(status);
                        equipamentoInventario.setDataHora(dataSalvamento);
                        /*equipamentoInventario.setLatitude(String.valueOf(latitude));
                         equipamentoInventario.setLongitude(String.valueOf(longitude));*/
                        equipamentoInventario.setLatitude(latitudeEncontrada.get(x));
                        equipamentoInventario.setLongitude(longitudeEncontrada.get(x));
                        equipamentoInventario.setIdInventario(inventarioId);
                        equipamentoInventario.setIdEquipamento(e.getId());
                        equipamentoInventario.setIdStatus(Math.toIntExact(statusId));
                        Long idEquipamento = equipamentoInventarioDao.inserir(equipamentoInventario);

                        Log.i("Salvando", "Salva - EquipamentoInventario: " + idEquipamento);
                        Log.i("Salvando", "Salva - EquipamentoInventario idStatus: " + statusId);

                        x++;

                        // progresso++;
                    }

                    progressDialog.dismiss();
                    Intent it = new Intent(ListaEquipamentoInventarioActivity.this, ListaInventarioActivity.class);
                    startActivity(it);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
                Intent it = new Intent(ListaEquipamentoInventarioActivity.this, ListaInventarioActivity.class);
                startActivity(it);
            }
        }
        (new SalvarAsync()).execute();

        /*if (!listaEncontrado.isEmpty()) {  *//* Necessariamente ao menos 1 item encontrado *//*

            if (!listaNaoEncontrado.isEmpty()) { *//* Ainda existem itens nao encontrados *//*
                listaNaoAtribuida.addAll(listaNaoEncontrado);
            } *//*else { *//**//* Salvar na InventarioNegado *//*
                int i = 0;
                for (Equipamento e : listaNaoAtribuida) {
                    InventarioNegado inventarioNegado = new InventarioNegado();
                    Status status = new Status();
                    status.setStatus("Nao_Atribuida");
                    Long statusId = statusDao.inserir(status);
                    inventarioNegado.setIdStatus(Math.toIntExact(statusId));
                    inventarioNegado.setIdInventario(inventarioId);
                    inventarioNegado.setDataHora(dataSalvamento);
                  *//*  inventarioNegado.setLatitude(String.valueOf(latitude));
                    inventarioNegado.setLongitude(String.valueOf(longitude));*//*
                    inventarioNegado.setLatitude(latitudeNaoAtribuida.get(i));
                    inventarioNegado.setLongitude(longitudeNaoAtribuida.get(i));
                    inventarioNegado.setNumeroTag(e.getNumeroTag());
                    Long inventarioNegadoId = inventarioNegadoDao.inserir(inventarioNegado);

                    Log.i("Salvando", "Salva - InventarioNegado: " + inventarioNegadoId);
                }
            } *//* Salvar EquipamentoInventario *//*

            int x = 0;
            for (Equipamento e : listaEncontrado) {
                equipamentoInventario = new EquipamentoInventario();
                Status status = new Status();
                status.setStatus("Encontrada");
                Long statusId = statusDao.inserir(status);
                equipamentoInventario.setDataHora(dataSalvamento);
                *//*equipamentoInventario.setLatitude(String.valueOf(latitude));
                equipamentoInventario.setLongitude(String.valueOf(longitude));*//*
                equipamentoInventario.setLatitude(latitudeEncontrada.get(x));
                equipamentoInventario.setLongitude(longitudeEncontrada.get(x));
                equipamentoInventario.setIdInventario(inventarioId);
                equipamentoInventario.setIdEquipamento(e.getId());
                equipamentoInventario.setIdStatus(Math.toIntExact(statusId));
                Long idEquipamento = equipamentoInventarioDao.inserir(equipamentoInventario);

                Log.i("Salvando", "Salva - EquipamentoInventario: " + idEquipamento);

            }

            progressDialog.dismiss();
            Intent it = new Intent(ListaEquipamentoInventarioActivity.this, ListaInventarioActivity.class);
            startActivity(it);
            //finish();
        } else { *//* Lista ENCONTRADOS vazia *//*
            Toast.makeText(this, "A lista ENCONTRADOS não precisa ter ao menos um item!", Toast.LENGTH_SHORT).show();
        } */
    }

    /* RECEBER LEITURA */
    private void registrarBluetoothReceiver() {
        BluetoothReceiver.bindListener(new BluetoothListener() {
            @Override
            public void messageReceived(Intent intent) {

                dados = intent.getStringExtra("resposta");

                runOnUiThread(new Runnable() {

                    @SuppressLint("MissingPermission")
                    public void run() {
                        if (dados != null && dados.contains("EP: ")) { /* Tag */ /* PREENCHER A LISTA COM OS VALORES */

                            equipamento = new Equipamento();
                            textoTag = dados.replaceAll("EP: ", "");

                            dataFinal = Data.getDataEHoraAual("dd/MM/yyyy - HH:mm");
                            equipamento.setNumeroTag(textoTag);

                            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                longitude = location.getLongitude();
                                latitude = location.getLatitude();
                            }

                            boolean adicionar = true;
                            /* Verifica se já existe na lista */
                            if (!listaNaoEncontrado.isEmpty()) {
                                for (Equipamento e : listaNaoEncontrado) {
                                    if (e.getNumeroTag().equals(equipamento.getNumeroTag())) { /* Existe na listaNaoEncontrado */
                                        if (!listaEncontrado.isEmpty()) {
                                            for (Equipamento equipamentoEncontrado : listaEncontrado) {
                                                if (e.getNumeroTag().equals(equipamentoEncontrado)) /* Já foi inserido na Lista */
                                                    break;
                                                else { /* Ainda nao foi inserido */
                                                    listaEncontrado.add(e);
                                                    latitudeEncontrada.add(String.valueOf(latitude));
                                                    longitudeEncontrada.add(String.valueOf(longitude));
                                                    txtEncontrado.setText(String.valueOf(listaEncontrado.size()));
                                                    adicionar = false;
                                                    ativaSalvar();
                                                    break;
                                                }
                                            }
                                        } else {
                                            Log.i("Teste", "Recebido5: " + e.getNumeroTag());
                                            listaEncontrado.add(e);
                                            latitudeEncontrada.add(String.valueOf(latitude));
                                            longitudeEncontrada.add(String.valueOf(longitude));
                                            txtEncontrado.setText(String.valueOf(listaEncontrado.size()));
                                            adicionar = false;
                                            break;
                                        }
                                    }
                                } //else { /* Não existe na listaNaoEncontrado */
                                if (adicionar) {
                                    if (!listaNaoAtribuida.isEmpty()) {
                                        adicionar = true;
                                        for (Equipamento equipamentoFinal : listaEncontrado) {
                                            if (equipamentoFinal.getNumeroTag().equals(equipamento.getNumeroTag())) {
                                                adicionar = false;
                                                break;
                                            }
                                        }
                                        for (Equipamento equipamentoNaoAtribuido : listaNaoAtribuida) {
                                            if (equipamentoNaoAtribuido.getNumeroTag().equals(equipamento.getNumeroTag())) {
                                                adicionar = false;
                                                break;
                                            }
                                        }
                                        if (adicionar) {
                                            latitudeNaoAtribuida.add(String.valueOf(latitude));
                                            longitudeNaoAtribuida.add(String.valueOf(longitude));
                                            listaNaoAtribuida.add(equipamento);
                                            txtNaoAtribuido.setText(String.valueOf(listaNaoAtribuida.size()));
                                        }

                                    } else {

                                        for (Equipamento equipamentoFinal : listaEncontrado) {
                                            if (equipamentoFinal.getNumeroTag().equals(equipamento.getNumeroTag())) {
                                                adicionar = false;
                                                break;
                                            }
                                        }
                                        if (adicionar) {
                                            listaNaoAtribuida.add(equipamento);
                                            latitudeNaoAtribuida.add(String.valueOf(latitude));
                                            longitudeNaoAtribuida.add(String.valueOf(longitude));
                                            txtNaoAtribuido.setText(String.valueOf(listaNaoAtribuida.size()));
                                        }
                                    }
                                }

                            } else { /* Lista vazia, todos os itens ja foram adicionados */
                                if (adicionar) {
                                    if (!listaNaoAtribuida.isEmpty()) {
                                        for (Equipamento equipamentoFinal : listaEncontrado) {
                                            if (equipamentoFinal.getNumeroTag().equals(equipamento.getNumeroTag())) {
                                                adicionar = false;
                                                break;
                                            }
                                        }
                                        for (Equipamento equipamentoNaoAtribuido : listaNaoAtribuida) {
                                            if (equipamentoNaoAtribuido.getNumeroTag().equals(equipamento.getNumeroTag())) {
                                                adicionar = false;
                                                break;
                                            }
                                        }
                                        if (adicionar) {
                                            latitudeNaoAtribuida.add(String.valueOf(latitude));
                                            longitudeNaoAtribuida.add(String.valueOf(longitude));
                                            listaNaoAtribuida.add(equipamento);
                                            txtNaoAtribuido.setText(String.valueOf(listaNaoAtribuida.size()));
                                        }

                                    } else {
                                        for (Equipamento equipamentoFinal : listaEncontrado) {
                                            if (equipamentoFinal.getNumeroTag().equals(equipamento.getNumeroTag())) {
                                                adicionar = false;
                                                break;
                                            }
                                        }
                                        if (adicionar) {
                                            listaNaoAtribuida.add(equipamento);
                                            latitudeNaoAtribuida.add(String.valueOf(latitude));
                                            longitudeNaoAtribuida.add(String.valueOf(longitude));
                                            txtNaoAtribuido.setText(String.valueOf(listaNaoAtribuida.size()));
                                        }
                                    }
                                }

                            }

                            /* Remover as tags encontradas */
                            for (Equipamento equipamentoEncontrado : listaEncontrado) {
                                //while (!listaNaoEncontrado.isEmpty()){
                                if (listaNaoEncontrado.contains(equipamentoEncontrado)) {
                                    listaNaoEncontrado.remove(equipamentoEncontrado);
                                    txtNaoEncontrado.setText(String.valueOf(listaNaoEncontrado.size()));
                                }
                                //}
                            }
                        }
                    }
                });
            }
        });
    }

    private void ativaSalvar() {
        fabProsseguir.getBackground().mutate().setTint(ContextCompat.getColor(ListaEquipamentoInventarioActivity.this, R.color.colorPrimary));
        fabProsseguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvar();
            }
        });
    }

    private boolean iniciarInventario() {
        boolean iniciar;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.i("Inventario", "Latitude: " + latitude);
            Log.i("Inventario", "Longitude: " + longitude);

        }
        try {
            endereco = buscaEndereco(latitude, longitude);
            inventario.setEndereco(endereco.getAddressLine(0));
            Log.i("Inventario", "Endereco: " + endereco.getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataFinal = Data.getDataEHoraAual("dd/MM/yyyy - HH:mm");
        data = dataFinal;

        inventario.setDataHora(dataFinal);
        inventario.setLatitude(String.valueOf(latitude));
        inventario.setLongitude(String.valueOf(longitude));
        inventarioId = (int) inventarioDao.inserir(inventario);

        if (inventarioId >= 0)
            iniciar = true;
        else iniciar = false;

        return iniciar;
    }

    private Address buscaEndereco(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        Address address = null;
        List<Address> addresses;

        geocoder = new Geocoder(getApplicationContext());
        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if (addresses.size() > 0) {
            address = addresses.get(0);
        }
        return address;
    }

    public AsciiCommander getCommander() {
        return AsciiCommander.sharedInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_atualizar, menu);
        return true;
    }
}
