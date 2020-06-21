package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument;

import model.Equipamento;
import model.EquipamentoInventario;
import sql.Conexao;

public class EquipamentoInventarioDao {

    private Conexao conexao;
    private SQLiteDatabase banco;
    private EquipamentoInventario equipamentoInventario;

    public EquipamentoInventarioDao(Context context) {
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public EquipamentoInventario recupera() {
        Cursor cursor = banco.rawQuery("SELECT * FROM equipamentoInventario", null);

        while (cursor.moveToNext()) {
            equipamentoInventario = new EquipamentoInventario();
            equipamentoInventario.setId(cursor.getInt(0));
            equipamentoInventario.setIdInventario(cursor.getInt(1));
            equipamentoInventario.setIdEquipamento(cursor.getInt(2));
            equipamentoInventario.setIdStatus(cursor.getInt(3));
            equipamentoInventario.setDataHora(cursor.getString(4));
            equipamentoInventario.setLatitude(cursor.getString(5));
            equipamentoInventario.setLongitude(cursor.getString(6));
        }
        return equipamentoInventario;
    }

    public long inserir(EquipamentoInventario equipamentoInventario) {

        ContentValues values = new ContentValues();
        values.put("idInventario", equipamentoInventario.getIdInventario());
        values.put("idEquipamento", equipamentoInventario.getIdEquipamento());
        values.put("idStatus", equipamentoInventario.getIdStatus());
        values.put("dataHora", equipamentoInventario.getDataHora());
        values.put("latitude", equipamentoInventario.getLatitude());
        values.put("longitude", equipamentoInventario.getLongitude());

        return banco.insert("equipamentoInventario", null, values);
    }

    public void atualizar(EquipamentoInventario equipamentoInventario) {

        ContentValues values = new ContentValues();
        values.put("idInventario", equipamentoInventario.getIdInventario());
        values.put("idEquipamento", equipamentoInventario.getIdEquipamento());
        values.put("idStatus", equipamentoInventario.getIdStatus());
        values.put("dataHora", equipamentoInventario.getDataHora());
        values.put("latitude", equipamentoInventario.getLatitude());
        values.put("longitude", equipamentoInventario.getLongitude());

        banco.update("equipamentoInventario", values, "id = ?",
                new String[]{String.valueOf(equipamentoInventario.getId())});
    }

    public List<EquipamentoInventario> obterTodos(){

        List<EquipamentoInventario> equipamentoInventarios = new ArrayList<>();
        Cursor cursor = banco.query("equipamentoInventario", new String[]{"id", "idInventario", "idEquipamento", "idStatus", "dataHora",
                "latitude", "longitude"}, null, null, null, null, null);

        if(cursor.moveToFirst()){
            while(cursor.moveToNext()){
                equipamentoInventario = new EquipamentoInventario();
                equipamentoInventario.setId(cursor.getInt(0));
                equipamentoInventario.setIdInventario(cursor.getInt(1));
                equipamentoInventario.setIdEquipamento(cursor.getInt(2));
                equipamentoInventario.setIdStatus(cursor.getInt(3));
                equipamentoInventario.setDataHora(cursor.getString(4));
                equipamentoInventario.setLatitude(cursor.getString(5));
                equipamentoInventario.setLongitude(cursor.getString(6));

                equipamentoInventarios.add(equipamentoInventario);
            }
        }

        return equipamentoInventarios;
    }

    public Cursor pegaCursor(){
        Cursor cursor = banco.rawQuery("SELECT * FROM equipamentoInventario", null);
         return cursor;
    }

    public void limparTabela(){
        banco.execSQL("DELETE FROM equipamentoInventario");
    }
}
