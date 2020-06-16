package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import dao.LocalDao;
import dao.StatusDao;
import model.Status;
import sql.Database;

public class StatusHelper {

    private Database db;
    private StatusDao dao;
    private Cursor cursor;
    private List<Status> statusList;

    public StatusHelper(Context context) {
        db = Database.getDatabase(context);
        dao = db.statusDao();
    }

    // Inserir Status
    @SuppressLint("StaticFieldLeak")
    public void inserir(final Status status) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Long id = db.statusDao().inserir(status);
                Log.i("Salvando", " > [Status] Registro: " + id);
                return null;
            }
        }.execute();
    }

    // Atualizar Status
    @SuppressLint("StaticFieldLeak")
    public void atualizar(final Status status) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.statusDao().atualizar(status);
                Log.i("Salvando", " > [Status] Atualizando Status: " + status.getId());
                return null;
            }
        }.execute();
    }

    // Carregar Status
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cursor = db.statusDao().carregarTodos();
                Log.i("Salvando", " > [Status] Carregando cursor");
                return null;
            }
        }.execute();

        return cursor;
    }

    // Carregar Status
    @SuppressLint("StaticFieldLeak")
    public List pegaLista() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                statusList = db.statusDao().getAll();
                Log.i("Salvando", " > [Status] Carregando Lista");
                return null;
            }
        }.execute();

        return statusList;
    }

    // Excluir Status
    @SuppressLint("StaticFieldLeak")
    public void limparStatus() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.localDao().deleteAll();
                Log.i("Salvando", " > [Status] Limpando tabela Status");
                return null;
            }
        }.execute();
    }
}
