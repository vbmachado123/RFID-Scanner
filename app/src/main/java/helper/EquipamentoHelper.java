package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import dao.EquipamentoDao;
import dao.InventarioDao;
import model.Equipamento;
import model.Inventario;
import sql.Database;

public class EquipamentoHelper {

    private Database db;
    private EquipamentoDao dao;
    private Cursor cursor;
    private List<Equipamento> equipamentoList;

    public EquipamentoHelper(Context context){
        db = Database.getDatabase(context);
        dao = db.equipamentoDao();
    }

    // Inserir Equipamento
    @SuppressLint("StaticFieldLeak")
    public void inserir(final Equipamento equipamento) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Long id = db.equipamentoDao().inserir(equipamento);
                Log.i("Salvando", " > [Equipamento] Registro: " + id);
                return null;
            }
        }.execute();
    }

    // Atualizar Equipamento
    @SuppressLint("StaticFieldLeak")
    public void atualizar(final Equipamento equipamento) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.equipamentoDao().atualizar(equipamento);
                Log.i("Salvando", " > [Equipamento] Atualizando Equipamento: " + equipamento.getId());
                return null;
            }
        }.execute();
    }

    // Carregar Equipamento
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                cursor = db.equipamentoDao().carregarTodos();
                Log.i("Salvando", " > [Equipamento] Carregando cursor");
                return null;
            }
        }.execute();

        return cursor;
    }

    // Carregar Equipamento
    @SuppressLint("StaticFieldLeak")
    public List pegaLista() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                equipamentoList = db.equipamentoDao().getAll();
                Log.i("Salvando", " > [Equipamento] Carregando Lista");
                return null;
            }
        }.execute();

        return equipamentoList;
    }

    // Excluir Equipamento
    @SuppressLint("StaticFieldLeak")
    public void limparEquipamento() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                db.equipamentoDao().deleteAll();
                Log.i("Salvando", " > [Equipamento] Limpando tabela Equipamento");
                return null;
            }
        }.execute();

    }

}
