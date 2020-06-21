package telas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument;

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
import dao.StatusDao;
import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import model.InventarioNegado;
import model.Local;
import model.Status;
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
    private Address endereco;

    private String dados, textoTag, dataFinal;

    private EquipamentoDao equipamentoDao;
    private EquipamentoInventarioDao equipamentoInventarioDao;
    private InventarioDao inventarioDao;
    private InventarioNegadoDao inventarioNegadoDao;
    private StatusDao statusDao;

    /* IDs */
    private int localId, sublocalId, equipamentoId, inventarioId, equipamentoInventarioId;

    /* Adapter */
    private EquipamentoAdapter adapter;

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

        equipamentoDao = new EquipamentoDao(this);
        equipamentoInventarioDao = new EquipamentoInventarioDao(this);
        inventarioDao = new InventarioDao(this);
        inventarioNegadoDao = new InventarioNegadoDao(this);
        statusDao = new StatusDao(this);

        validaCampo();
        inventario.setIdLocal(localId);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        recuperaEquipamentos();

        abrirListas();
        fabProsseguir.getBackground().mutate().setTint(ContextCompat.getColor(ListaEquipamentoInventarioActivity.this, R.color.vermelhodesativado));

    }

    private void recuperaEquipamentos() { /* Carrega Lista Primeira */
        listaPrimeira = new ArrayList<>();
        listaEncontrado = new ArrayList<>();
        listaNaoEncontrado = new ArrayList<>();
        listaNaoAtribuida = new ArrayList<>();

        listaPrimeira = (ArrayList<Equipamento>) equipamentoDao.getByLocal(localId);

        if (subLocal != null) {/* SubLocal Anteriormente Selecionado*/
            for (Equipamento e : listaPrimeira) {
                Log.i("Copulando", String.valueOf(e.getLocalId()) + " " + e.getSubLocalId());
                if (e.getSubLocalId() == subLocal.getId()) {
                    listaNaoEncontrado.add(e);
                    Log.i("Copulando", String.valueOf(e.getLocalId()) + " " + e.getSubLocalId());
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
        int progresso = 0;

        if (!listaEncontrado.isEmpty()) { /* Necessariamente ao menos 1 item encontrado */
            final ProgressDialog progressDialog = new ProgressDialog(this, R.style.Dialog);
            progressDialog.setTitle("Salvando Listas...");
            progressDialog.show();

            if (!listaNaoEncontrado.isEmpty()) { /* Ainda existem itens nao encontrados */
                listaNaoAtribuida.addAll(listaNaoEncontrado);
            } else { /* Salvar na InventarioNegado */
                for (Equipamento e : listaNaoAtribuida) {
                    InventarioNegado inventarioNegado = new InventarioNegado();
                    Status status = new Status();
                    status.setStatus("Nao_Atribuida");
                    Long statusId = statusDao.inserir(status);
                    inventarioNegado.setIdStatus(Math.toIntExact(statusId));
                    inventarioNegado.setIdInventario(inventarioId);
                    inventarioNegado.setDataHora(dataSalvamento);
                    inventarioNegado.setLatitude(String.valueOf(latitude));
                    inventarioNegado.setLongitude(String.valueOf(longitude));
                    inventarioNegado.setNumeroTag(e.getNumeroTag());
                    Long inventarioNegadoId = inventarioNegadoDao.inserir(inventarioNegado);

                    progressDialog.setMessage("Salvando " + progresso + "%");
                    progresso++;

                    Log.i("Salvando", "Salva - InventarioNegado: " + inventarioNegadoId);
                }
            } /* Salvar EquipamentoInventario */

            for(Equipamento e : listaEncontrado){
                equipamentoInventario = new EquipamentoInventario();
                Status status = new Status();
                status.setStatus("Encontrada");
                Long statusId = statusDao.inserir(status);
                equipamentoInventario.setDataHora(dataSalvamento);
                equipamentoInventario.setLatitude(String.valueOf(latitude));
                equipamentoInventario.setLongitude(String.valueOf(longitude));
                equipamentoInventario.setIdInventario(inventarioId);
                equipamentoInventario.setIdEquipamento(e.getId());
                equipamentoInventario.setIdStatus(Math.toIntExact(statusId));
                Long idEquipamento = equipamentoInventarioDao.inserir(equipamentoInventario);

                Log.i("Salvando", "Salva - EquipamentoInventario: " + idEquipamento);

                progressDialog.setMessage("Salvando " + progresso + "%");
                progresso++;
            }

            progressDialog.dismiss();
            Intent it = new Intent(ListaEquipamentoInventarioActivity.this, ListaInventarioActivity.class);
            startActivity(it);

        } else { /* Lista ENCONTRADOS vazia */
            Toast.makeText(this, "A lista ENCONTRADOS não precisa ter ao menos um item!", Toast.LENGTH_SHORT).show();
        }
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
                            if (!listaNaoEncontrado.isEmpty()) {
                                for (Equipamento e : listaNaoEncontrado) {
                                    Log.i("Teste", "Recebido1: " + e.getNumeroTag());
                                    if (e.getNumeroTag().equals(equipamento.getNumeroTag())) { /* Existe na listaNaoEncontrado */
                                        Log.i("Teste", "Recebido2: " + e.getNumeroTag());
                                        if (!listaEncontrado.isEmpty()) {
                                            for (Equipamento equipamentoEncontrado : listaEncontrado) {
                                                Log.i("Teste", "Recebido3: " + equipamentoEncontrado.getNumeroTag());
                                                if (e.getNumeroTag().equals(equipamentoEncontrado)) /* Já foi inserido na Lista */
                                                    break;
                                                else { /* Ainda nao foi inserido */
                                                    Log.i("Teste", "Recebido4: " + equipamentoEncontrado.getNumeroTag());
                                                    listaEncontrado.add(e);
                                                    txtEncontrado.setText(String.valueOf(listaEncontrado.size()));
                                                    ativaSalvar();
                                                    break;
                                                }
                                            }
                                        } else {
                                            Log.i("Teste", "Recebido5: " + e.getNumeroTag());
                                            listaEncontrado.add(e);
                                            txtEncontrado.setText(String.valueOf(listaEncontrado.size()));
                                            break;
                                        }
                                    } else { /* Não existe na listaNaoEncontrado */
                                        for (Equipamento equipamentoEncontrado : listaEncontrado) {
                                            if (equipamentoEncontrado.getNumeroTag().equals(equipamento.getNumeroTag())) /* Verifica se ja foi adicionado e encontrado */
                                                break;
                                            else { /* Não existe na listaEncontrado e nem na naoEncontrado */
                                                for (Equipamento equipamentoNaoAtribuido : listaNaoAtribuida) {
                                                    if (e.getNumeroTag().equals(equipamentoNaoAtribuido.getNumeroTag()))
                                                        break;
                                                    else {
                                                        listaNaoAtribuida.add(e);
                                                        txtNaoAtribuido.setText(String.valueOf(listaNaoAtribuida.size()));
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else { /* Lista vazia, todos os itens ja foram adicionados */
                                if (!listaNaoAtribuida.isEmpty()) {
                                    boolean adicionar = true;
                                    for (Equipamento equipamentoNaoAtribuido : listaNaoAtribuida) {
                                        if (equipamentoNaoAtribuido.getNumeroTag().equals(equipamento.getNumeroTag())) {
                                            adicionar = false;
                                            break;
                                        }
                                    }
                                    if (adicionar) {
                                        listaNaoAtribuida.add(equipamento);
                                        txtNaoAtribuido.setText(String.valueOf(listaNaoAtribuida.size()));
                                    }
                                } else {
                                    listaNaoAtribuida.add(equipamento);
                                    txtNaoAtribuido.setText(String.valueOf(listaNaoAtribuida.size()));
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
        }
        try {
            endereco = buscaEndereco(latitude, longitude);
            inventario.setEndereco(endereco.getAddressLine(0));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_atualizar, menu);
        return true;
    }
}
