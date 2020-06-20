package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import model.Equipamento;
import sql.Conexao;

public class EquipamentoDao {

    private Equipamento equipamento = null;

    private Conexao conexao;
    private SQLiteDatabase banco;

    public EquipamentoDao(Context context) {
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public Equipamento recupera() {
        Cursor cursor = banco.rawQuery("SELECT * FROM equipamento", null);

        while (cursor.moveToNext()) {
            equipamento = new Equipamento();
            equipamento.setId(cursor.getInt(0));
            equipamento.setLocalId(cursor.getInt(1));
            equipamento.setSubLocalId(cursor.getInt(2));
            equipamento.setNumeroTag(cursor.getString(3));
            equipamento.setDescricao(cursor.getString(4));
        }
        return equipamento;
    }

    public long inserir(Equipamento equipamento) {

        ContentValues values = new ContentValues();
        values.put("idLocal", equipamento.getLocalId());
        values.put("idSubLocal", equipamento.getSubLocalId());
        values.put("numeroTag", equipamento.getNumeroTag());
        values.put("descricao", equipamento.getDescricao());

        return banco.insert("equipamento", null, values);
    }

    public void atualizar(Equipamento equipamento) {

        ContentValues values = new ContentValues();
        values.put("idLocal", equipamento.getLocalId());
        values.put("idSubLocal", equipamento.getSubLocalId());
        values.put("numeroTag", equipamento.getNumeroTag());
        values.put("descricao", equipamento.getDescricao());

        banco.update("equipamento", values, "id = ?",
                new String[]{String.valueOf(equipamento.getId())});
    }

    public void limparTabela(){
         banco.execSQL("DELETE FROM equipamento");
    }
}
