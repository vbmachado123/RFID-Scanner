package telas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import helper.LocalAdapter;
import helper.SubLocalAdapter;
import model.Local;
import model.SubLocal;

public class EscolhaLocalActivity extends AppCompatActivity {

    private EditText etLocal, etSubLocal; //Servem como filtro de pesquisa
    private ImageButton addLocal, addSubLocal;
    private Toolbar toolbar;
    private ListView lista;
    private FloatingActionButton fabProsseguir;
    private TextView tvTextoEscolhido;

    private ArrayList<Local> listaLocal, filtroListaLocal;
    private ArrayList<SubLocal> listaSublocal, filtroListaSubLocal;
    private LocalAdapter localAdapter;
    private SubLocalAdapter subLocalAdapter;
    private Local local;
    private SubLocal subLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escolha_local);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listaLocal = new ArrayList<>();
        filtroListaLocal = new ArrayList<>();
        listaSublocal = new ArrayList<>();
        filtroListaSubLocal = new ArrayList<>();

        local = new Local();
        subLocal = new SubLocal();

        validaCampo();
        copulaArray(); /* Temporário */

        tvTextoEscolhido.setText("Selecione o Local do Inventário");
        if (etLocal.getText().toString().isEmpty()) {
            etSubLocal.setEnabled(false);
            lista.setAdapter(localAdapter);
        }
    }

    private void validaCampo() {
        etLocal = (EditText) findViewById(R.id.etLocal);
        etSubLocal = (EditText) findViewById(R.id.etSubLocal);
        addLocal = (ImageButton) findViewById(R.id.ibAddLocal);
        addSubLocal = (ImageButton) findViewById(R.id.ibAddSubLocal);
        tvTextoEscolhido = (TextView) findViewById(R.id.tvLocalEscolhido);
        lista = (ListView) findViewById(R.id.lvLista);
        fabProsseguir = (FloatingActionButton) findViewById(R.id.fabProsseguir);

        localAdapter = new LocalAdapter(this, filtroListaLocal);
        subLocalAdapter = new SubLocalAdapter(this, filtroListaSubLocal);

        /* EditText */
        listener();
        adicionar();

        registerForContextMenu(lista);
    }

    private void adicionar() {
        addLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Local l = new Local();
                if(l != null){
                    if (!etLocal.getText().toString().isEmpty()) { /* Foi digitado algo */
                        if (!filtroListaLocal.isEmpty()) { /* Possui Registro no banco */
                            l = filtroListaLocal.get(0);
                        } else if(filtroListaLocal.isEmpty())
                            Toast.makeText(EscolhaLocalActivity.this, "O Local já foi selecionado!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EscolhaLocalActivity.this, "Insira o Local!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addSubLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(filtroListaLocal.isEmpty()){
                   SubLocal l = new SubLocal();
                   if(l != null){
                       if(!etSubLocal.getText().toString().isEmpty()){
                           if(!filtroListaSubLocal.isEmpty()){
                               l = filtroListaSubLocal.get(0);
                           } else if (filtroListaSubLocal.isEmpty())
                               Toast.makeText(EscolhaLocalActivity.this, "O SubLocal já foi selecionado!", Toast.LENGTH_SHORT).show();
                       } else {
                           Toast.makeText(EscolhaLocalActivity.this, "Insira o SubLocal!", Toast.LENGTH_SHORT).show();
                       }
                   }
               }
            }
        });


    }

    private void listener() {

        /* EditText Local */
        etLocal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroListaLocal.clear();

                for (Local l : listaLocal) {
                    if (l.getDescricao().toLowerCase().contains(s.toString().toLowerCase())) {
                        filtroListaLocal.add(l);
                    }
                }

                lista.invalidateViews();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        /* EditText SubLocal */
        etSubLocal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filtroListaSubLocal.clear();

                for (SubLocal l : listaSublocal) {
                    if (l.getDescricao().toLowerCase().contains(s.toString().toLowerCase())) {
                        filtroListaSubLocal.add(l);
                    }
                }

                lista.invalidateViews();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
                Intent it = new Intent(EscolhaLocalActivity.this, EscolhaLocalActivity.class);
                startActivity(it);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.menu_contexto_selecionar, menu);
    }

    private void copulaArray() {

        Local local1 = new Local();
        local1.setId(1);
        local1.setDescricao("EEE 4");
        listaLocal.add(local1);

        Local local2 = new Local();
        local2.setId(2);
        local2.setDescricao("CRAT Interlagos");
        listaLocal.add(local2);

        Local local3 = new Local();
        local3.setId(3);
        local3.setDescricao("CRAT Campo Belo");
        listaLocal.add(local3);

        Local local4 = new Local();
        local4.setId(4);
        local4.setDescricao("Eletromecanica");
        listaLocal.add(local4);

        filtroListaLocal.addAll(listaLocal);

        SubLocal subLocal1 = new SubLocal();
        subLocal1.setId(1);
        subLocal1.setIdLocal(1);
        subLocal1.setDescricao("Almoxarifado");
        listaSublocal.add(subLocal1);

        SubLocal subLocal2 = new SubLocal();
        subLocal2.setId(2);
        subLocal2.setIdLocal(4);
        subLocal2.setDescricao("Oficina");
        listaSublocal.add(subLocal2);

        SubLocal subLocal3 = new SubLocal();
        subLocal3.setId(3);
        subLocal3.setIdLocal(3);
        subLocal3.setDescricao("Casa de Bombas");
        listaSublocal.add(subLocal3);

        SubLocal subLocal4 = new SubLocal();
        subLocal4.setId(1);
        subLocal4.setIdLocal(2);
        subLocal4.setDescricao("Sala dos Paineis");
        listaSublocal.add(subLocal4);

        //filtroListaSubLocal.addAll(listaSublocal);
    }

    public void selecionar(MenuItem item) { /* Após selecionar algum item do menu */

        AdapterView.AdapterContextMenuInfo menuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (!listaLocal.isEmpty()) { /* Foi Selecionado na Lista do Local */
            local = filtroListaLocal.get(menuInfo.position);
            selecionarLocal();
        } else { /* Foi Selecionado na Lista do SubLocal */
            subLocal = filtroListaSubLocal.get(menuInfo.position);
            selecionarSubLocal();
        }
    }

    private void selecionarSubLocal() {
        etSubLocal.setText(subLocal.getDescricao());
    }

    private void selecionarLocal() { /* Inverter Edittext */
        etLocal.setText(local.getDescricao());
        etLocal.setEnabled(false);
        listaLocal.clear();
        etSubLocal.setEnabled(true);

        for(SubLocal l : listaSublocal){
            if(l.getIdLocal() == local.getId()){
                filtroListaSubLocal.add(l);
            }
        }

        fabProsseguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(local != null){ /* Local foi selecionado */
                    if(subLocal == null){ /* Somente Local Selecionado */

                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(EscolhaLocalActivity.this, R.style.Dialog)
                                .setTitle("Atenção")
                                .setMessage("Deseja selecionar somente o LOCAL? todos os sublocais referentes serão selecionados!")
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { /* Fechar */

                                    }
                                })
                                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        /* Iniciar InventárioEquipamento */
                                    }
                                }).create();
                        dialog.show();
                    }
                }
            }
        });

        lista.setAdapter(subLocalAdapter);

        tvTextoEscolhido.setText(local.getDescricao() + " - Sublocal: ");
    }
}