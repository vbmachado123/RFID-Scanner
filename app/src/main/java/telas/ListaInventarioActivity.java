package telas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import adapter.EquipamentoAdapter;
import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import dao.InventarioDao;
import dao.InventarioNegadoDao;
import dao.StatusDao;
import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import model.Status;
import model.Lista;
import model.Local;
import model.SubLocal;
import util.Data;
import util.Xlsx;

public class ListaInventarioActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ArrayList<Equipamento> equipamentos;

    private Local local = new Local();
    private SubLocal subLocal = new SubLocal();
    private Inventario inventario = new Inventario();
    private EquipamentoInventario equipamentoInventario = new EquipamentoInventario();
    private EquipamentoInventarioDao equipamentoInventarioDao;
    private EquipamentoDao equipamentoDao;
    private Status status;
    private StatusDao statusDao;
    private EquipamentoAdapter adapter;
    private InventarioDao inventarioDao;
    private InventarioNegadoDao inventarioNegadoDao;

    private TableRow trAlterarDescricao, trExportar, trAbrirLista;
    private ListView listaEquipamentos;
    private FloatingActionButton fabSalvar;
    private TextView tamanhoLista;

    private int backButtonCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_inventario);

        equipamentoInventarioDao = new EquipamentoInventarioDao(this);
        equipamentoDao = new EquipamentoDao(this);
        inventarioDao = new InventarioDao(this);
        inventarioNegadoDao = new InventarioNegadoDao(this);
        statusDao = new StatusDao(this);

        equipamentos = new ArrayList<>();

        tamanhoLista = (TextView) findViewById(R.id.txtTamanhoLista);

        copulaLista();
        validaCampo();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void validaCampo() {
        trAlterarDescricao = (TableRow) findViewById(R.id.trAdicionarDescricao);
        trExportar = (TableRow) findViewById(R.id.trExportar);
        trAbrirLista = (TableRow) findViewById(R.id.trExpandir);
        listaEquipamentos = (ListView) findViewById(R.id.lvLista);
        fabSalvar = (FloatingActionButton) findViewById(R.id.botaoSalvar);

        adapter = new EquipamentoAdapter(this, equipamentos);

        listaEquipamentos.setAdapter(adapter);

        registerForContextMenu(listaEquipamentos);

        trAlterarDescricao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(ListaInventarioActivity.this, AlterarDescricaoActivity.class);
                startActivity(it);
                finish();
            }
        });

        trExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportar();
            }
        });

        trAbrirLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListaInventarioActivity.this, R.style.Dialog);
                View view = getLayoutInflater().inflate(R.layout.lista_leituras, null);
                builder.setAdapter(adapter, null);
                builder.setTitle("Equipamentos do Inventário");
                builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                //builder.setView(view);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        fabSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportar();
            }
        });
    }

    private void exportar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListaInventarioActivity.this, R.style.Dialog);
        View view = getLayoutInflater().inflate(R.layout.lista_leituras, null);
        builder.setTitle("Atenção");
        builder.setMessage("O banco será limpo após a exportação, essa ação não pode ser desfeita! Deseja continuar?");
        builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton("Exportar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exportarBanco();
            }
        });

        registerForContextMenu(view);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exportarBanco() {
        Xlsx xlsx = new Xlsx(this);
        boolean exportar = xlsx.exportarTabela();

        if (exportar) {
            Toast.makeText(this, "A exportação foi concluída!", Toast.LENGTH_SHORT).show();
            Intent it = new Intent(ListaInventarioActivity.this, HomeActivity.class);
            startActivity(it);
            finish();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.menu_contexto_editar, menu);
    }

    private void copulaLista() {

        ArrayList<EquipamentoInventario> equipamentoInventarioList = (ArrayList<EquipamentoInventario>) equipamentoInventarioDao.obterTodos();

        for (int i = 0; i < equipamentoInventarioList.size(); i++) {
            EquipamentoInventario ei = equipamentoInventarioList.get(i);
            Equipamento equipamento = equipamentoDao.getById(ei.getIdEquipamento());
            Status status = statusDao.getById(ei.getIdStatus());
            if(status.getStatus().equals("Encontrada")){
                if (equipamento != null)
                    equipamentos.add(equipamento);
            }

            Log.i("Salvando", "EquipamentoInventario - " + equipamento.getNumeroTag());
        }

        tamanhoLista.setText(String.valueOf(equipamentos.size()));
    }


    @Override
    public void onBackPressed() {

        if (backButtonCount >= 1) {
            AlertDialog dialog = new AlertDialog.Builder(ListaInventarioActivity.this, R.style.Dialog)
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

                            Intent intent = new Intent(ListaInventarioActivity.this, HomeActivity.class);
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

}