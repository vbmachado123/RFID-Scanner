package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import dao.LocalDao;
import dao.SubLocalDao;
import model.Local;
import model.SubLocal;
import sql.Database;

public class SubLocalHelper {


    private Database db;
    private SubLocalDao dao;
    private Cursor cursor;
    private List<SubLocal> subLocalList;

    public SubLocalHelper(Context context) {
        db = Database.getDatabase(context);
        dao = db.subLocalDao();
    }

    // Inserir SubLocal
    @SuppressLint("StaticFieldLeak")
    public void inserir(final SubLocal subLocal) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Long id = db.subLocalDao().inserir(subLocal);
                Log.i("Salvando", " > [SubLocal] Registro: " + id);
                return null;
            }
        }.execute();
    }

    // Atualizar SubLocal
    @SuppressLint("StaticFieldLeak")
    public void atualizar(final SubLocal subLocal) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.subLocalDao().atualizar(subLocal);
                Log.i("Salvando", " > [SubLocal] Atualizando Local: " + subLocal.getId());
                return null;
            }
        }.execute();
    }

    // Carregar SubLocal
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cursor = db.subLocalDao().carregarTodos();
                Log.i("Salvando", " > [SubLocal] Carregando cursor");
                return null;
            }
        }.execute();

        return cursor;
    }

    // Carregar SubLocal
    @SuppressLint("StaticFieldLeak")
    public List pegaLista() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                subLocalList = db.subLocalDao().getAll();
                Log.i("Salvando", " > [SubLocal] Carregando Lista");
                return null;
            }
        }.execute();

        return subLocalList;
    }

    // Excluir SubLocal
    @SuppressLint("StaticFieldLeak")
    public void limparSubLocal() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.subLocalDao().deleteAll();
                Log.i("Salvando", " > [SubLocal] Limpando tabela Local");
                return null;
            }
        }.execute();
    }
}
