package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import dao.InventarioNegadoDao;
import dao.LocalDao;
import model.InventarioNegado;
import model.Local;
import sql.Database;

public class LocalHelper {

    private Database db;
    private LocalDao dao;
    private Cursor cursor;
    private List<Local> localList;

    public LocalHelper(Context context) {
        db = Database.getDatabase(context);
        dao = db.localDao();
    }

    // Inserir Local
    @SuppressLint("StaticFieldLeak")
    public void inserir(final Local local) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Long id = db.localDao().inserir(local);
                Log.i("Salvando", " > [Local] Registro: " + id);
                return null;
            }
        }.execute();
    }

    // Atualizar Local
    @SuppressLint("StaticFieldLeak")
    public void atualizar(final Local local) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.localDao().atualizar(local);
                Log.i("Salvando", " > [Local] Atualizando Local: " + local.getId());
                return null;
            }
        }.execute();
    }

    // Carregar Local
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cursor = db.localDao().carregarTodos();
                Log.i("Salvando", " > [Local] Carregando cursor");
                return null;
            }
        }.execute();

        return cursor;
    }

    // Carregar Local
    @SuppressLint("StaticFieldLeak")
    public List pegaLista() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                localList = db.localDao().getAll();
                Log.i("Salvando", " > [Local] Carregando Lista");
                return null;
            }
        }.execute();

        return localList;
    }

    // Excluir Local
    @SuppressLint("StaticFieldLeak")
    public void limparLocal() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.localDao().deleteAll();
                Log.i("Salvando", " > [Local] Limpando tabela Local");
                return null;
            }
        }.execute();
    }
}
