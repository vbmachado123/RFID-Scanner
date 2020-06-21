package dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import model.Local;
import model.SubLocal;
import sql.Conexao;

public class SubLocalDao {

    private Conexao conexao;
    private SQLiteDatabase banco;
    private SubLocal subLocal;

    public SubLocalDao(Context context) {
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public SubLocal recupera() {
        Cursor cursor = banco.rawQuery("SELECT * FROM subLocal", null);

        while (cursor.moveToNext()) {
            subLocal = new SubLocal();
            subLocal.setId(cursor.getInt(0));
            subLocal.setIdLocal(cursor.getInt(1));
            subLocal.setDescricao(cursor.getString(2));
        }
        return subLocal;
    }

    public SubLocal getById(int id){
        Cursor cursor = banco.rawQuery("SELECT * FROM subLocal WHERE id=" + id, null);

        if (cursor.moveToFirst()) {
            subLocal = new SubLocal();
            subLocal.setId(cursor.getInt(0));
            subLocal.setIdLocal(cursor.getInt(1));
            subLocal.setDescricao(cursor.getString(2));
        }
        return subLocal;
    }

    public List<SubLocal> obterTodos(){

        List<SubLocal> subLocalList = new ArrayList<>();
        Cursor cursor = banco.query("subLocal", new String[]{"id","idLocal" , "descricao"}, null, null, null, null, null);

        while(cursor.moveToNext()){
            subLocal = new SubLocal();
            subLocal.setId(cursor.getInt(0));
            subLocal.setIdLocal(cursor.getInt(1));
            subLocal.setDescricao(cursor.getString(2));

            subLocalList.add(subLocal);
        }
        return subLocalList;
    }

    public Cursor pegaCursor(){
        Cursor cursor = banco.rawQuery("SELECT * FROM subLocal", null);
        return cursor;
    }

    public long inserir(SubLocal subLocal) {

        ContentValues values = new ContentValues();
        values.put("idLocal", subLocal.getIdLocal());
        values.put("descricao", subLocal.getDescricao());
        return banco.insert("subLocal", null, values);
    }

    public void atualizar(SubLocal subLocal) {

        ContentValues values = new ContentValues();
        values.put("idLocal", subLocal.getIdLocal());
        values.put("descricao", subLocal.getDescricao());

        banco.update("subLocal", values, "id = ?",
                new String[]{String.valueOf(subLocal.getId())});
    }

    public void limparTabela() {
        banco.execSQL("DELETE FROM subLocal");
    }

}
