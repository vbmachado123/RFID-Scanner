package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import model.Inventario;
import model.InventarioNegado;
import sql.Conexao;

public class InventarioDao {

    private Conexao conexao;
    private SQLiteDatabase banco;
    private Inventario inventario;

    public InventarioDao(Context context) {
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public Inventario recupera() {
        Cursor cursor = banco.rawQuery("SELECT * FROM inventario", null);

        while (cursor.moveToNext()) {
            inventario = new Inventario();
            inventario.setId(cursor.getInt(0));
            inventario.setIdLocal(cursor.getInt(1));
            inventario.setIdSubLocal(cursor.getInt(2));
            inventario.setDataHora(cursor.getString(3));
            inventario.setLatitude(cursor.getString(4));
            inventario.setLongitude(cursor.getString(5));
            inventario.setEndereco(cursor.getString(6));
        }
        return inventario;
    }

    public long inserir(Inventario inventario) {

        ContentValues values = new ContentValues();
        values.put("idLocal", inventario.getIdLocal());
        values.put("idSubLocal", inventario.getIdSubLocal());
        values.put("dataHora", inventario.getDataHora());
        values.put("latitude", inventario.getLatitude());
        values.put("longitude", inventario.getLongitude());
        values.put("endereco", inventario.getEndereco());

        return banco.insert("inventario", null, values);
    }

    public void atualizar(Inventario inventario) {

        ContentValues values = new ContentValues();
        values.put("idLocal", inventario.getIdLocal());
        values.put("idSubLocal", inventario.getIdSubLocal());
        values.put("dataHora", inventario.getDataHora());
        values.put("latitude", inventario.getLatitude());
        values.put("longitude", inventario.getLongitude());
        values.put("endereco", inventario.getEndereco());

        banco.update("inventario", values, "id = ?",
                new String[]{String.valueOf(inventario.getId())});
    }

    public List<Inventario> obterTodos(){

        List<Inventario> inventarios = new ArrayList<>();
        Cursor cursor = banco.query("inventario", new String[]{"id", "idLocal", "idSubLocal", "dataHora", "latitude",
                "longitude", "endereco"}, null, null, null, null, null);

        while(cursor.moveToNext()){
            inventario = new Inventario();
            inventario = new Inventario();
            inventario.setId(cursor.getInt(0));
            inventario.setIdLocal(cursor.getInt(1));
            inventario.setIdSubLocal(cursor.getInt(2));
            inventario.setDataHora(cursor.getString(3));
            inventario.setLatitude(cursor.getString(4));
            inventario.setLongitude(cursor.getString(5));
            inventario.setEndereco(cursor.getString(6));

            inventarios.add(inventario);
        }
        return inventarios;
    }

    public Cursor pegaCursor(){
        Cursor cursor = banco.rawQuery("SELECT * FROM inventario", null);
        return cursor;
    }

    public void limparTabela(){
        banco.execSQL("DELETE FROM inventario");
    }
}
