package telas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.rfidscanner.R;

import java.io.File;

import dao.EquipamentoDao;
import model.Equipamento;
import model.Inventario;
import util.Xlsx;

import static androidx.core.content.FileProvider.getUriForFile;

public class InventarioActivity extends AppCompatActivity {

    private static final String TAG = "Importacao";
    private Toolbar toolbar;
    private TableRow trImportar, trFazerInventario;
    private ListView lvDiretorio;
    private static final int IMPORTACAO = 1;

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        validaCampo();
    }

    private void validaCampo() {
        trImportar = (TableRow) findViewById(R.id.trCarregar);
        trFazerInventario = (TableRow) findViewById(R.id.trFazerInventario);

        trImportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEquipamentosAlert();
            }
        });

        trFazerInventario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EquipamentoDao dao = new EquipamentoDao(InventarioActivity.this);
                Equipamento e = dao.recupera();
                if (e != null)
                    acessaActivity(EscolhaLocalActivity.class);
                else {
                    AlertDialog dialog = new AlertDialog.Builder(InventarioActivity.this, R.style.Dialog)
                            .setTitle("Atenção")
                            .setMessage("O banco não possui registros! Deseja importá-los?")
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton("Importar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selecionarArquivo();
                                }
                            }).create();
                    dialog.show();
                }
            }
        });
    }

    private void getEquipamentosAlert() {

        EquipamentoDao dao = new EquipamentoDao(this);
        Equipamento e = dao.recupera();

        if (e != null) {
            AlertDialog dialog = new AlertDialog.Builder(InventarioActivity.this, R.style.Dialog)
                    .setTitle("Atenção")
                    .setMessage("Deseja substituir as informações do banco? Esta ação não pode ser desfeita!")
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selecionarArquivo();
                        }
                    }).create();
            dialog.show();

        } else selecionarArquivo();
    }

    private void selecionarArquivo() {
        String[] mimeTypes =
                {"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" /*"application/xlsx","text/csv" */ /*.xls & .xlsx*/};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
        }
        Toast.makeText(this, "Selecione a tabela para prosseguir!", Toast.LENGTH_LONG).show();
        startActivityForResult(Intent.createChooser(intent, "Selecione a tabela"), IMPORTACAO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            switch (requestCode) {
                case IMPORTACAO:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        receberArquivoM(data);
                    else
                        receberArquivo(data);
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    private void receberArquivoM(Intent data) {
        boolean xiaomi = false;
        file = new File(String.valueOf(data.getData()));
      /*  //Uri uri = getUriForFile(InventarioActivity.this, "com.example.rfidscanner", file);
        try {
            Uri uri = data.getData();
            File f = new File(util.Uri.getPath(this, uri));
            Xlsx xlsx = new Xlsx(this);
            boolean importar = xlsx.importarTabela(f);
            if (importar) {
                xiaomi = true;
                iniciarInventario();
            } else {
                xiaomi = false;
                Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 1", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!xiaomi) {
            try {
                Uri uri = FileProvider.getUriForFile(this, "com.example.rfidscanner", file);
                Xlsx xlsx = new Xlsx(this);
                if (uri != null) {
                    // boolean importar;
                    boolean importar = xlsx.importarTabela(new File(uri.getPath()));

                    if (importar) iniciarInventario();
                } else {
                    //Toast.makeText(this, "Não foi possível importar, tente novamente!", Toast.LENGTH_SHORT).show();
                    Uri uri1 = data.getData();
                   // file = new File(uri1.getPath());
                    boolean importar1 = xlsx.importarTabela(file);
                    if (importar1) iniciarInventario();
                    else
                        Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 2", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialog dialog = new AlertDialog.Builder(InventarioActivity.this, R.style.Dialog)
                        .setTitle("Atenção")
                        .setMessage("Não foi possível importar! ERRO: " + e.getMessage()
                        ).create();
                dialog.show();
            }
        }*/

        try {
            Uri uri = data.getData();
            File f = new File(util.Uri.getPath(this, uri));
            Xlsx xlsx = new Xlsx(this);
            boolean importar = xlsx.importarTabela(f);
            if (importar) {
                xiaomi = true;
                iniciarInventario();
            } else {
                xiaomi = false;
                Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 1", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } if(!xiaomi){ /* Não é um xiaomi */
            Uri uri = data.getData();
            file = new File(uri.getPath());
            Xlsx xlsx = new Xlsx(this);
            boolean importar = false;
            try {
                importar = xlsx.importarTabela(file);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (importar) iniciarInventario();
            else
                Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 2", Toast.LENGTH_SHORT).show();
        }
    }

    private void receberArquivo(Intent data) {
        Uri uri = data.getData();
        file = new File(uri.getPath());
        Xlsx xlsx = new Xlsx(this);
        boolean importar = false;
        try {
            importar = xlsx.importarTabela(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (importar) iniciarInventario();
        else
            Toast.makeText(this, "Não foi possível importar, tente novamente!", Toast.LENGTH_SHORT).show();

    }

    private void iniciarInventario() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Dialog)
                .setTitle("Atenção")
                .setMessage("A importação foi concluída com sucesso! Deseja iniciar o inventário?")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { /* Fechar */
                    }
                })
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* Iniciar Inventário */
                        acessaActivity(EscolhaLocalActivity.class);
                    }
                }).create();
        dialog.show();
    }

    private void acessaActivity(Class c) {
        Intent it = new Intent(InventarioActivity.this, c);
        startActivity(it);
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
