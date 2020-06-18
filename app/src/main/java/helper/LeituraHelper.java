package helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

import dao.LeituraDao;
import model.Leitura;
import sql.Database;
import util.Csv;

public class LeituraHelper {

    private LeituraDao dao;
    private Database db;
    private Cursor cursor;

    public LeituraHelper(Context context){
        db = Database.getDatabase(context);
        dao = db.leituraDao();
    }

    // Inserir Leitura
    @SuppressLint("StaticFieldLeak")
    public void inserir(final Leitura leitura) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                dao.inserir(leitura);
                Log.i("Salvando", " >  Registro: " + leitura.getId());
                return null;
            }
        }.execute();
    }

    // Exportar Lista
    @SuppressLint("StaticFieldLeak")
    public void exportar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Cursor cursor = dao.carregarTodos();
                if(cursor != null){
                    Csv csv = new Csv(cursor);
                    File f = csv.exportDB();
                    Log.i("Salvando", " >  Exportando tabela");

                    if(f.canRead()){
                       db.leituraDao().deleteAll();
                        Log.i("Salvando", " >  O Banco foi limpo!");
                    }
                }
                return null;
            }
        }.execute();
    }

    // Carregar Cursor
    @SuppressLint("StaticFieldLeak")
    public Cursor carregar() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Cursor cursor;
                cursor = dao.carregarTodos();
                capturaCursor(cursor);
                Log.i("Salvando", " >  Carregando cursor");
                return null;
            }
        }.execute();
        return cursor;
    }

    private void capturaCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    // Atualizar Leitura
    @SuppressLint("StaticFieldLeak")
    public void atualizar(final Leitura leitura) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Leitura l = db.leituraDao().pegaUm(leitura.getNumeroTag());
               if(l != null){
                       l.setVezesLida(l.getVezesLida() + 1);
                   dao.atualizar(l);
                   Log.i("Salvando", " >  Registro - Atualizando: " + l.getId() + " " + l.getVezesLida());
               } else {
                   Long id = dao.inserir(leitura);
                   Log.i("Salvando", " >  Registro - Inserindo: " + id);
               }
                return null;
            }
        }.execute();
    }

    // Limpar Banco
    @SuppressLint("StaticFieldLeak")
    public void limparBanco() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                dao.deleteAll();
                Log.i("Salvando", " >  Banco limpo!");
                return null;
            }
        }.execute();
    }
}
