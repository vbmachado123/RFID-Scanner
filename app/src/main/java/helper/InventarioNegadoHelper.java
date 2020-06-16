package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import dao.EquipamentoInventarioDao;
import dao.InventarioNegadoDao;
import model.EquipamentoInventario;
import model.InventarioNegado;
import sql.Database;

public class InventarioNegadoHelper {

    private Database db;
    private InventarioNegadoDao dao;
    private Cursor cursor;
    private List<InventarioNegado> inventarioNegadoList;

    InventarioNegadoHelper(Context context) {
        db = Database.getDatabase(context);
        dao = db.inventarioNegadoDao();
    }


    // Inserir InventarioNegado
    @SuppressLint("StaticFieldLeak")
    public void inserir(final InventarioNegado inventarioNegado) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Long id = db.inventarioNegadoDao().inserir(inventarioNegado);
                Log.i("Salvando", " > [InventarioNegado] Registro: " + id);
                return null;
            }
        }.execute();
    }

    // Atualizar InventarioNegado
    @SuppressLint("StaticFieldLeak")
    public void atualizar(final InventarioNegado inventarioNegado) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.inventarioNegadoDao().atualizar(inventarioNegado);
                Log.i("Salvando", " > [InventarioNegado] Atualizando Equipamento: " + inventarioNegado.getId());
                return null;
            }
        }.execute();
    }

    // Carregar InventarioNegado
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cursor = db.inventarioNegadoDao().carregarTodos();
                Log.i("Salvando", " > [InventarioNegado] Carregando cursor");
                return null;
            }
        }.execute();

        return cursor;
    }

    // Carregar InventarioNegado
    @SuppressLint("StaticFieldLeak")
    public List pegaLista() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                inventarioNegadoList = db.inventarioNegadoDao().getAll();
                Log.i("Salvando", " > [InventarioNegado] Carregando Lista");
                return null;
            }
        }.execute();

        return inventarioNegadoList;
    }

    // Excluir InventarioNegado
    @SuppressLint("StaticFieldLeak")
    public void limparInventarioNegado() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.inventarioNegadoDao().deleteAll();
                Log.i("Salvando", " > [InventarioNegado] Limpando tabela Inventario");
                return null;
            }
        }.execute();
    }
}
