package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import model.Equipamento;
import model.Leitura;
import sql.Conexao;

public class LeituraDao {

    private Conexao conexao;
    private SQLiteDatabase banco;
    private Leitura leitura;

    public LeituraDao(Context context) {
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public Leitura recupera() {
        Cursor cursor = banco.rawQuery("SELECT * FROM leitura", null);

        while (cursor.moveToNext()) {
            leitura = new Leitura();
            leitura.setId(cursor.getInt(0));
            leitura.setNumeroTag(cursor.getString(1));
            leitura.setDataHora(cursor.getString(2));
            leitura.setVezesLida(cursor.getInt(3));

        }
        return leitura;
    }

    public long inserir(Leitura leitura) {

        ContentValues values = new ContentValues();
        values.put("numeroTag", leitura.getNumeroTag());
        values.put("dataHora", leitura.getDataHora());
        values.put("vezesLida", leitura.getVezesLida());

        return banco.insert("leitura", null, values);
    }

    public void atualizar(Leitura leitura) {

        ContentValues values = new ContentValues();
        values.put("numeroTag", leitura.getNumeroTag());
        values.put("dataHora", leitura.getDataHora());
        values.put("vezesLida", leitura.getVezesLida());

        banco.update("leitura", values, "id = ?",
                new String[]{String.valueOf(leitura.getId())});
    }

    public void limparTabela(){
        banco.execSQL("DELETE FROM leitura");
    }

}
