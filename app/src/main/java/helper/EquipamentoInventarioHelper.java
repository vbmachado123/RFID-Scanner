package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import model.Equipamento;
import model.EquipamentoInventario;
import sql.Database;

public class EquipamentoInventarioHelper {

    private Database db;
    private EquipamentoInventarioDao dao;
    private Cursor cursor;
    private List<EquipamentoInventario> equipamentoInventarioList;

    EquipamentoInventarioHelper(Context context) {
        db = Database.getDatabase(context);
        dao = db.equipamentoInventarioDao();
    }


    // Inserir EquipamentoInventario
    @SuppressLint("StaticFieldLeak")
    public void inserir(final EquipamentoInventario equipamento) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Long id = db.equipamentoInventarioDao().inserir(equipamento);
                Log.i("Salvando", " > [EquipamentoInventario] Registro: " + id);
                return null;
            }
        }.execute();
    }

    // Atualizar EquipamentoInventario
    @SuppressLint("StaticFieldLeak")
    public void atualizar(final EquipamentoInventario equipamento) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.equipamentoInventarioDao().atualizar(equipamento);
                Log.i("Salvando", " > [EquipamentoInventario] Atualizando Equipamento: " + equipamento.getId());
                return null;
            }
        }.execute();
    }

    // Carregar EquipamentoInventario
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cursor = db.equipamentoInventarioDao().carregarTodos();
                Log.i("Salvando", " > [EquipamentoInventario] Carregando cursor");
                return null;
            }
        }.execute();

        return cursor;
    }

    // Carregar EquipamentoInventario
    @SuppressLint("StaticFieldLeak")
    public List pegaLista() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                equipamentoInventarioList = db.equipamentoInventarioDao().getAll();
                Log.i("Salvando", " > [EquipamentoInventario] Carregando Lista");
                return null;
            }
        }.execute();

        return equipamentoInventarioList;
    }

    // Excluir EquipamentoInventario
    @SuppressLint("StaticFieldLeak")
    public void limparEquipamentoInvetario() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.equipamentoInventarioDao().deleteAll();
                Log.i("Salvando", " > [EquipamentoInventario] Limpando tabela EquipamentoInventario");
                return null;
            }
        }.execute();
    }
}
