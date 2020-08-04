package telas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import dao.EquipamentoInventarioDao;
import dao.InventarioDao;
import dao.InventarioNegadoDao;
import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import util.Permissao;
import util.RealPathUri;
import util.Xlsx;

import static androidx.core.content.FileProvider.getUriForFile;

public class InventarioActivity extends AppCompatActivity {

    private static final String TAG = "Importacao";
    private Toolbar toolbar;
    private TableRow trImportar, trFazerInventario;
    private ListView lvDiretorio;
    private static final int IMPORTACAO = 1;
    private Uri uri;
    private boolean arqImportado = false;
    private ProgressDialog progressDialog;

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
                if (e != null) {
                    InventarioDao inventarioDao = new InventarioDao(InventarioActivity.this);
                    EquipamentoInventarioDao equipamentoInventarioDao = new EquipamentoInventarioDao(InventarioActivity.this);
                    InventarioNegadoDao inventarioNegadoDao = new InventarioNegadoDao(InventarioActivity.this);
                    Inventario i = inventarioDao.recupera();
                    if(i != null){

                        AlertDialog dialog = new AlertDialog.Builder(InventarioActivity.this, R.style.Dialog)
                                .setTitle("Atenção")
                                .setMessage("Já existe um inventário em andamento, deseja continuar-lo?")
                                .setNegativeButton("Limpar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        inventarioDao.limparTabela();
                                        equipamentoInventarioDao.limparTabela();
                                        inventarioNegadoDao.limparTabela();
                                        Toast.makeText(InventarioActivity.this, "Inventário limpo", Toast.LENGTH_SHORT).show();
                                        acessaActivity(EscolhaLocalActivity.class);
                                    }
                                })
                                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        /* Inserir lógica para continuar o inventário */
                                        EquipamentoInventario ei = equipamentoInventarioDao.recupera();
                                        if(ei != null) {
                                            acessaActivity(ListaInventarioActivity.class);
                                            Log.i("Inventario", "Tabela EquipamentoInventario não esta vazia");
                                        } else {
                                            acessaActivity(ListaEquipamentoInventarioActivity.class);
                                            Log.i("Inventario", "Tabela EquipamentoInventario esta vazia");
                                        }
                                    }
                                }).create();
                        dialog.show();
                    } else acessaActivity(EscolhaLocalActivity.class);
                } else { /* Já possui equipamentos cadastrados */
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
        file = new File(String.valueOf(data.getData()));
        uri = data.getData();
        //importarBackground(data);
        Permissao.Permissoes(InventarioActivity.this);
        ImportarAsync importarAsync = new ImportarAsync();
        importarAsync.execute();

       /* try {
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
        } if(!xiaomi){ *//* Não é um xiaomi *//*
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
        }*/
    }

    private class ImportarAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(InventarioActivity.this, R.style.Dialog);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Aguarde");
            progressDialog.setMessage("Importando o Banco...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMax(100);
            progressDialog.show();

        }

       /* @Override
        protected Boolean doInBackground(Void... voids) {
            boolean xiaomi = false;
            boolean importar = false;
            try {
                File f = new File(util.Uri.getPath(InventarioActivity.this, uri));
                Xlsx xlsx = new Xlsx(InventarioActivity.this);
                importar = xlsx.importarTabela(f);
                if (importar) {
                    xiaomi = true;
                    iniciarInventario();
                    Log.i("Importacao", "Importacao 1");
                } else {
                    xiaomi = false;

                    // Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 1", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Importacao", "Importacao 1 - " + e.getMessage());
            }
            *//*try {
                File f = new File(RealPathUri.getRealPath(InventarioActivity.this, uri));
                Xlsx xlsx = new Xlsx(InventarioActivity.this);
                importar = xlsx.importarTabela(f);
                if (importar) {
                    xiaomi = true;
                    iniciarInventario();
                    Log.i("Importacao", "Importacao 1");
                } else {
                    xiaomi = false;

                    // Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 1", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Importacao", "Importacao 1 - " + e.getMessage());
            }*//*
            if (!xiaomi) { *//* Não é um xiaomi *//*
                Xlsx xlsx = new Xlsx(InventarioActivity.this);
                file = new File(uri.getPath());
                importar = false;

            *//*    try {

                    Uri uri = FileProvider.getUriForFile(InventarioActivity.this, "com.example.rfidscanner", file);
                    File f = new File(uri.getPath());
                    importar = xlsx.importarTabela(f);
                    Log.i("Importacao", "Importacao 2 - " + f.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("Importacao", "Importacao 2 - " + e.getMessage());
                }*//*
                try {
                    File f = new File(RealPathUri.getRealPath(InventarioActivity.this, uri));
                    importar = xlsx.importarTabela(f);
                    importar = true;
                    Log.i("Importacao", "Importando - " + f.getAbsolutePath());
                    Log.i("Importacao", "Importacao 2");
                } catch (Exception e) {
                    Log.i("Importacao", "Erro 2: " + e.getMessage());
                }
                if (!importar) {
                    try {
                        file = new File(RealPathUri.getFilePath(InventarioActivity.this, uri));
                        importar = xlsx.importarTabela(new File(getFilePathFromUri(InventarioActivity.this, uri)));
                        importar = true;
                        Log.i("Importacao", "Importacao 3");
                    } catch (Exception e) {
                        Log.i("Importacao", "Importacao 1 - " + e.getMessage());
                    }
                }
                if (!importar) {
                    try {
                        file = new File(RealPathUri.getRealPath(InventarioActivity.this, FileProvider.getUriForFile(InventarioActivity.this, "com.example.rfidscanner", file)));
                        importar = xlsx.importarTabela(file);
                        importar = true;
                        Log.i("Importacao", "Importacao 4");
                    } catch (Exception e) {
                        Log.i("Importacao", "Importacao 1 - " + e.getMessage());
                    }
                }
                if (!importar) {
                    try {
                        file = new File(RealPathUri.getRealPathFromURI_BelowAPI11(InventarioActivity.this, uri));
                        importar = xlsx.importarTabela(file);
                        importar = true;
                        Log.i("Importacao", "Importacao 5");
                    } catch (Exception e) {
                        Log.i("Importacao", "Importacao 1 - " + e.getMessage());
                    }
                }
                if (!importar) {
                    try {
                        file = new File(RealPathUri.getRealPathFromURI_API11to18(InventarioActivity.this, uri));
                        importar = xlsx.importarTabela(file);
                        importar = true;
                        Log.i("Importacao", "Importacao 6");
                    } catch (Exception e) {
                        Log.i("Importacao", "Importacao 1 - " + e.getMessage());
                    }
                }

                if (!importar) {
                    try {
                        file = new File(getFilePathFromUri(InventarioActivity.this, uri));
                        importar = xlsx.importarTabela(file);
                        importar = true;
                        Log.i("Importacao", "Importacao 7");
                    } catch (Exception e) {
                        Log.i("Importacao", "Importacao 1 - " + e.getMessage());
                    }
                }
                *//*Funcionando*//*
                if (!importar) {
                    try {
                        importar = xlsx.importarTabela(file);
                        importar = true;
                        //  return importar;
                        Log.i("Importacao", "Importacao 8");
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.i("Importacao", "Importacao 1 - " + e.getMessage());
                    }
                }
               *//* if (importar) iniciarInventario();
                else
                    importar = false;*//*
                //return importar;
                // Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 2", Toast.LENGTH_SHORT).show();
            }
            return importar;
        }*/

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean xiaomi = false;
            boolean importar = false;
            try {
                // Uri uri = FileProvider.getUriForFile(InventarioActivity.this, "com.example.rfidscanner.fileprovider", file);
                File f = new File(RealPathUri.getFilePath(InventarioActivity.this, uri));
                Xlsx xlsx = new Xlsx(InventarioActivity.this);
                importar = xlsx.importarTabela(f);
                /*File f = new File(util.Uri.getPath(InventarioActivity.this, uri));
                Xlsx xlsx = new Xlsx(InventarioActivity.this);
                importar = xlsx.importarTabela(f);
                importar = true;
                if (importar) {
                    xiaomi = true;
                    iniciarInventario();
                } else {
                    xiaomi = false;
                    // Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 1", Toast.LENGTH_SHORT).show();
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!xiaomi) { /* Não é um xiaomi */
                Xlsx xlsx = new Xlsx(InventarioActivity.this);
                file = new File(uri.getPath());
                importar = false;
                try {
                    Uri uri1 = FileProvider.getUriForFile(InventarioActivity.this, "com.example.rfidscanner", file);
                    File f = new File(uri1.getPath());
                    importar = xlsx.importarTabela(f);
                } catch (Exception e) {

                }

                if (!importar) {
                    try {
                        File f = new File(RealPathUri.getFilePath(InventarioActivity.this, uri));
                        importar = xlsx.importarTabela(f);

                        if (!importar) {
                            f = new File(RealPathUri.getRealPath(InventarioActivity.this, uri));
                            importar = xlsx.importarTabela(f);
                        }

                        if (!importar) {
                            f = new File(getFilePathFromUri(InventarioActivity.this, uri));
                            importar = xlsx.importarTabela(f.getAbsoluteFile());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!importar) {
                    try {
                        importar = xlsx.importarTabela(file);
                        importar = true;
                        return importar;
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
             /*   if (importar) iniciarInventario();
                else
                    importar = false;*/
                return importar;
                // Toast.makeText(this, "Não foi possível importar, tente novamente! Erro: 2", Toast.LENGTH_SHORT).show();
            }
            return importar;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean importado) {
            super.onPostExecute(importado);
            progressDialog.dismiss();
            if (importado) { /* Valida importação */
                EquipamentoDao dao = new EquipamentoDao(InventarioActivity.this);
                Equipamento e = dao.recupera();
                if (e != null) {
                    iniciarInventario();
                    arqImportado = true;
                } else
                    Toast.makeText(InventarioActivity.this, "Não foi possível importar!", Toast.LENGTH_SHORT).show();
            } else {
                arqImportado = false;
                Toast.makeText(InventarioActivity.this, "Não foi possível importar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String getFilePathFromUri(Context context, Uri _uri) {
        String filePath = "";
        if (_uri != null && "content".equals(_uri.getScheme())) {
            //Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            //context.revokeUriPermission(_uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(_uri,
                        new String[]
                                {
                                        MediaStore.Images.ImageColumns.DATA,
                                        MediaStore.Images.Media.DATA,
                                        MediaStore.Images.Media.MIME_TYPE,
                                        MediaStore.Video.VideoColumns.DATA,
                                }, null, null, null);
                cursor.moveToFirst();
                filePath = cursor.getString(0);
            } catch (SecurityException e) {
                //if file open with third party application
                if (_uri.toString().contains("/storage/emulated/0")) {
                    filePath = "/storage/emulated/0" + _uri.toString().split("/storage/emulated/0")[1];
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }

        } else {
            filePath = _uri.getPath();
        }
        return filePath;
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
