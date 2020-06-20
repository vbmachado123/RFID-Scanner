package telas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import adapter.LocalAdapter;
import adapter.SubLocalAdapter;
import dao.LocalDao;
import dao.SubLocalDao;
import model.Local;
import model.SubLocal;

public class EscolhaLocalActivity extends AppCompatActivity {

    private EditText etLocal, etSubLocal; //Servem como filtro de pesquisa
    private ImageButton addLocal, addSubLocal;
    private Toolbar toolbar;
    private ListView lista;
    private FloatingActionButton fabProsseguir;
    private TextView tvTextoEscolhido;

    protected ArrayList<Local> listaLocal, filtroListaLocal;
    protected ArrayList<SubLocal> listaSublocal, filtroListaSubLocal;
    private LocalAdapter localAdapter;
    private SubLocalAdapter subLocalAdapter;
    private Local local;
    private SubLocal subLocal;
    private boolean completo = false;
    private LocalDao localDao;
    private SubLocalDao subLocalDao;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escolha_local);

        localDao = new LocalDao(this);
        subLocalDao = new SubLocalDao(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toast.makeText(this, "Clique longo na lista para selecionar", Toast.LENGTH_LONG).show();

        listaLocal = new ArrayList<>();
        filtroListaLocal = new ArrayList<>();
        listaSublocal = new ArrayList<>();
        filtroListaSubLocal = new ArrayList<>();

        local = new Local();
        subLocal = new SubLocal();

        validaCampo();
        recuperaListas();

        tvTextoEscolhido.setText("Selecione o Local do Inventário");
        if (etLocal.getText().toString().isEmpty()) {
            etSubLocal.setEnabled(false);
            lista.setAdapter(localAdapter);
            fabProsseguir.getBackground().mutate().setTint(ContextCompat.getColor(this, R.color.vermelhodesativado));
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
                if (l != null) {
                    if (!etLocal.getText().toString().isEmpty()) { /* Foi digitado algo */
                        if (!filtroListaLocal.isEmpty()) { /* Possui Registro no banco */
                            l = filtroListaLocal.get(0);
                        } else if (filtroListaLocal.isEmpty())
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
                if (filtroListaLocal.isEmpty()) {
                    SubLocal l = new SubLocal();
                    if (l != null) {
                        if (!etSubLocal.getText().toString().isEmpty()) {
                            if (!filtroListaSubLocal.isEmpty()) {
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
        completo = true;
    }

    @SuppressLint("ResourceAsColor")
    private void selecionarLocal() { /* Inverter Edittext */
        etLocal.setText(local.getDescricao());
        etLocal.setEnabled(false);
        listaLocal.clear();
        etSubLocal.setEnabled(true);
        fabProsseguir.getBackground().mutate().setTint(ContextCompat.getColor(this, R.color.colorPrimary));

        for (SubLocal l : listaSublocal) {
            if (l.getIdLocal() == local.getId()) {
                filtroListaSubLocal.add(l);
            }
        }

        fabProsseguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (local != null) { /* Local foi selecionado */
                    if (!completo) { /* Somente Local Selecionado */
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
                                        acessaActivity();
                                    }
                                }).create();
                        dialog.show();
                    } else { /* Local e subLocal selecionado! */
                        acessaActivity();
                    }
                }
            }
        });

        lista.setAdapter(subLocalAdapter);

        if (filtroListaSubLocal.isEmpty())
            tvTextoEscolhido.setText("O Local " + local.getDescricao() + " não possui sublocais cadastrados");
        else
            tvTextoEscolhido.setText(local.getDescricao() + " - Sublocal: ");
    }

    private void acessaActivity() {
        /* Intent it = new Intent(EscolhaLocalActivity.this, ListaInventarioActivity.class);*/
        Intent it = new Intent(EscolhaLocalActivity.this, ListaEquipamentoInventarioActivity.class);
        it.putExtra("local", local);
        if (subLocal != null)
            it.putExtra("sublocal", subLocal);

        startActivity(it);
    }

    private void recuperaListas() {
        listaLocal = (ArrayList<Local>) localDao.obterTodos();
        filtroListaLocal.addAll(listaLocal);

        listaSublocal = (ArrayList<SubLocal>) subLocalDao.obterTodos();

    }
}