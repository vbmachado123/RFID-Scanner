package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import model.Leitura;
import model.Local;
import model.Status;
import sql.Conexao;

public class StatusDao {
    private Conexao conexao;
    private SQLiteDatabase banco;
    private Status status;

    public StatusDao(Context context) {
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public Status recupera() {
        Cursor cursor = banco.rawQuery("SELECT * FROM status", null);

        while (cursor.moveToNext()) {
            status = new Status();
            status.setId(cursor.getInt(0));
            status.setStatus(cursor.getString(2));
        }
        return status;
    }

    public Cursor pegaCursor(){
        Cursor cursor = banco.rawQuery("SELECT * FROM status", null);
        return cursor;
    }

    public long inserir(Status status) {

        ContentValues values = new ContentValues();
        values.put("descricao", status.getStatus());
        return banco.insert("status", null, values);
    }

    public void atualizar(Status status) {

        ContentValues values = new ContentValues();
        values.put("descricao", status.getStatus());
        banco.update("status", values, "id = ?",
                new String[]{String.valueOf(status.getId())});
    }

    public void limparTabela() {
        banco.execSQL("DELETE FROM status");
    }

}
