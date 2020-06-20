package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import model.EquipamentoInventario;
import model.InventarioNegado;
import sql.Conexao;

public class InventarioNegadoDao {

    private Conexao conexao;
    private SQLiteDatabase banco;
    private InventarioNegado inventarioNegado;

    public InventarioNegadoDao(Context context) {
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public InventarioNegado recupera() {
        Cursor cursor = banco.rawQuery("SELECT * FROM inventarioNegado", null);

        while (cursor.moveToNext()) {
            inventarioNegado = new InventarioNegado();
            inventarioNegado.setId(cursor.getInt(0));
            inventarioNegado.setIdInventario(cursor.getInt(1));
            inventarioNegado.setIdStatus(cursor.getInt(2));
            inventarioNegado.setNumeroTag(cursor.getString(3));
            inventarioNegado.setDataHora(cursor.getString(4));
            inventarioNegado.setLatitude(cursor.getString(5));
            inventarioNegado.setLongitude(cursor.getString(6));
        }
        return inventarioNegado;
    }

    public long inserir(InventarioNegado inventarioNegado) {

        ContentValues values = new ContentValues();
        values.put("idInventario", inventarioNegado.getIdInventario());
        values.put("numeroTag", inventarioNegado.getNumeroTag());
        values.put("idStatus", inventarioNegado.getIdStatus());
        values.put("dataHora", inventarioNegado.getDataHora());
        values.put("latitude", inventarioNegado.getLatitude());
        values.put("longitude", inventarioNegado.getLongitude());

        return banco.insert("inventarioNegado", null, values);
    }

    public void atualizar(InventarioNegado inventarioNegado) {

        ContentValues values = new ContentValues();
        values.put("idInventario", inventarioNegado.getIdInventario());
        values.put("numeroTag", inventarioNegado.getNumeroTag());
        values.put("idStatus", inventarioNegado.getIdStatus());
        values.put("dataHora", inventarioNegado.getDataHora());
        values.put("latitude", inventarioNegado.getLatitude());
        values.put("longitude", inventarioNegado.getLongitude());

        banco.update("inventarioNegado", values, "id = ?",
                new String[]{String.valueOf(inventarioNegado.getId())});
    }

    public List<InventarioNegado> obterTodos(){

        List<InventarioNegado> inventarioNegados = new ArrayList<>();
        Cursor cursor = banco.query("inventarioNegado", new String[]{"id", "idInventario", "idStatus", "numeroTag", "dataHora",
                "latitude", "longitude"}, null, null, null, null, null);

        while(cursor.moveToNext()){
            inventarioNegado = new InventarioNegado();
            inventarioNegado.setId(cursor.getInt(0));
            inventarioNegado.setIdInventario(cursor.getInt(1));
            inventarioNegado.setIdStatus(cursor.getInt(2));
            inventarioNegado.setNumeroTag(cursor.getString(3));
            inventarioNegado.setDataHora(cursor.getString(4));
            inventarioNegado.setLatitude(cursor.getString(5));
            inventarioNegado.setLongitude(cursor.getString(6));

            inventarioNegados.add(inventarioNegado);
        }
        return inventarioNegados;
    }

    public Cursor pegaCursor(){
        Cursor cursor = banco.query("inventarioNegado", new String[]{"id", "idInventario", "idStatus", "numeroTag", "dataHora",
                "latitude", "longitude"}, null, null, null, null, null);

        return cursor;
    }

    public void limparTabela(){
        banco.execSQL("DELETE FROM inventarioNegado");
    }

}
