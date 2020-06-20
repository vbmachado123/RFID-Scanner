package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import model.Leitura;
import model.Local;
import sql.Conexao;
import java.util.ArrayList;
import java.util.List;

public class LocalDao {

    private Conexao conexao;
    private SQLiteDatabase banco;
    private Local local;

    public LocalDao(Context context) {
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public Local recupera() {
        Cursor cursor = banco.rawQuery("SELECT * FROM local", null);

        while (cursor.moveToNext()) {
            local = new Local();
            local.setId(cursor.getInt(0));
            local.setDescricao(cursor.getString(1));
        }
        return local;
    }

    public List<Local> obterTodos(){

        List<Local> localList = new ArrayList<>();
        Cursor cursor = banco.query("local", new String[]{"id", "descricao"}, null, null, null, null, null);

        while(cursor.moveToNext()){
            local = new Local();
            local.setId(cursor.getInt(0));
            local.setDescricao(cursor.getString(1));

            localList.add(local);
        }
        return localList;
    }


    public long inserir(Local local) {

        ContentValues values = new ContentValues();
        values.put("descricao", local.getDescricao());
        return banco.insert("local", null, values);
    }

    public void atualizar(Leitura leitura) {

        ContentValues values = new ContentValues();
        values.put("descricao", local.getDescricao());
        banco.update("local", values, "id = ?",
                new String[]{String.valueOf(leitura.getId())});
    }

    public void limparTabela() {
        banco.execSQL("DELETE FROM local");
    }
}
