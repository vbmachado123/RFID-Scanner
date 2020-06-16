package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.List;

import dao.InventarioDao;
import model.Inventario;
import sql.Database;

public class InventarioHelper {

    private InventarioDao dao;
    private Database db;
    private Cursor cursor;
    private List<Inventario> inventarioList;

    InventarioHelper(Context context){
        db = Database.getDatabase(context);
        dao = db.inventarioDao();
    }

    // Inserir Inventario
    @SuppressLint("StaticFieldLeak")
    public void inserir(final Inventario inventario) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Long id = db.inventarioDao().inserir(inventario);
                Log.i("Salvando", " > [Inventario] Registro: " + id);
                return null;
            }
        }.execute();
    }

    // Atualizar Inventario
    @SuppressLint("StaticFieldLeak")
    public void atualizar(final Inventario inventario) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.inventarioDao().atualizar(inventario);
                Log.i("Salvando", " > [Inventario] Atualizando Inventario: " + inventario.getId());
                return null;
            }
        }.execute();
    }

    // Carregar Inventario
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cursor = db.inventarioDao().carregarTodos();
                Log.i("Salvando", " > [Inventario] Carregando cursor");
                return null;
            }
        }.execute();

        return cursor;
    }

    // Carregar Inventario
    @SuppressLint("StaticFieldLeak")
    public List pegaLista() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                inventarioList = db.inventarioDao().getAll();
                Log.i("Salvando", " > [Inventario] Carregando Lista");
                return null;
            }
        }.execute();

        return inventarioList;
    }

    // Excluir Inventario
    @SuppressLint("StaticFieldLeak")
    public void limparInventario() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.inventarioDao().deleteAll();
                Log.i("Salvando", " > [Inventario] Limpando tabela Inventario");
                return null;
            }
        }.execute();
    }
}
