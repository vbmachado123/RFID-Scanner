package telas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.rfidscanner.R;

import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import dao.InventarioDao;
import dao.InventarioNegadoDao;
import dao.LeituraDao;
import dao.LocalDao;
import dao.StatusDao;
import dao.SubLocalDao;

public class ConfiguracaoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trLimparBanco;
    private EquipamentoDao equipamentoDao;
    private EquipamentoInventarioDao equipamentoInventarioDao;
    private InventarioDao inventarioDao;
    private InventarioNegadoDao inventarioNegadoDao;
    private LeituraDao leituraDao;
    private LocalDao localDao;
    private StatusDao statusDao;
    private SubLocalDao subLocalDao;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        context = this;
        leituraDao = new LeituraDao(context);
        localDao = new LocalDao(context);
        subLocalDao = new SubLocalDao(context);
        equipamentoDao = new EquipamentoDao(context);
        statusDao = new StatusDao(context);
        equipamentoInventarioDao = new EquipamentoInventarioDao(context);
        inventarioDao = new InventarioDao(context);
        inventarioNegadoDao = new InventarioNegadoDao(context);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        validaCampo();
    }

    private void validaCampo() {
        trLimparBanco = (TableRow) findViewById(R.id.trLimpar);

        trLimparBanco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguracaoActivity.this, R.style.Dialog);
                builder.setTitle("Atenção");
                builder.setMessage("Deseja realmente limpar o banco? essa ação não pode ser desfeita!");
                builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        equipamentoDao.limparTabela();
                        equipamentoInventarioDao.limparTabela();
                        inventarioDao.limparTabela();
                        inventarioNegadoDao.limparTabela();
                        leituraDao.limparTabela();
                        localDao.limparTabela();
                        statusDao.limparTabela();
                        subLocalDao.limparTabela();
                        Toast.makeText(context, "O banco de dados foi limpo com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                });

                // builder.setView(v);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
