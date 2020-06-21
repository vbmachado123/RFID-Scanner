package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import model.Equipamento;
import model.Local;
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

    public Equipamento getById(int id){
        Cursor cursor = banco.rawQuery("SELECT * FROM equipamento WHERE id=" + id, null);

        if(cursor.moveToFirst()){
            equipamento = new Equipamento();
            equipamento.setId(cursor.getInt(0));
            equipamento.setLocalId(cursor.getInt(1));
            equipamento.setSubLocalId(cursor.getInt(2));
            equipamento.setNumeroTag(cursor.getString(3));
            equipamento.setDescricao(cursor.getString(4));
        }

        return equipamento;
    }

    public List<Equipamento> getByLocal(int idLocal) {

        List<Equipamento> equipamentoList = new ArrayList<>();
        Cursor cursor = banco.query("equipamento", new String[]{"id", "idLocal", "idSubLocal", "numeroTag", "descricao"},
                null, null, null, null, null);

        cursor.moveToFirst();

        while (cursor.moveToNext()) {
            equipamento = new Equipamento();
            equipamento.setId(cursor.getInt(0));
            equipamento.setLocalId(cursor.getInt(1));
            equipamento.setSubLocalId(cursor.getInt(2));
            equipamento.setNumeroTag(cursor.getString(3));
            equipamento.setDescricao(cursor.getString(4));

            if (equipamento.getLocalId() == idLocal)
                equipamentoList.add(equipamento);
        }

        return equipamentoList;
    }

    public Cursor pegaCursor(){
        Cursor cursor = banco.rawQuery("SELECT * FROM equipamento", null);
        return cursor;
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

    public void limparTabela() {
        banco.execSQL("DELETE FROM equipamento");
    }
}
