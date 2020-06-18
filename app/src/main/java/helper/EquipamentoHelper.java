package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import dao.EquipamentoDao;
import model.Equipamento;
import sql.Database;

public class EquipamentoHelper {

    private Database db;
    private EquipamentoDao dao;
    private Equipamento equipamento;
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
                Long id = dao.inserir(equipamento);
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
                dao.atualizar(equipamento);
                Log.i("Salvando", " > [Equipamento] Atualizando Equipamento: " + equipamento.getId());
                return null;
            }
        }.execute();
    }

    // pegaUm Equipamento
    @SuppressLint("StaticFieldLeak")
    public Equipamento pegaUm(final int id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                equipamento = dao.pegaUm(id);
                return null;
            }
        }.execute();

        return equipamento;
    }

    // Carregar Equipamento
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {

        final Cursor[] cursor1 = new Cursor[1];

        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                cursor1[0] = dao.carregarTodos();
                if(cursor1[0].moveToFirst())
                Log.i("Salvando", " > [Equipamento] Carregando cursor " + cursor1[0].getString(0));
                return cursor1[0];
            }
        }.execute();

        return cursor1[0];
    }

    // Carregar Equipamento
    @SuppressLint("StaticFieldLeak")
    public List pegaLista() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                equipamentoList = dao.getAll();
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
                dao.deleteAll();
                Log.i("Salvando", " > [Equipamento] Limpando tabela Equipamento");
                return null;
            }
        }.execute();

    }

}
