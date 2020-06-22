package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument;

import java.io.File;

import model.Equipamento;
import model.Leitura;
import sql.Conexao;
import util.Csv;
import java.util.ArrayList;
import java.util.List;

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

    public List<Leitura> getAll(){
       ArrayList<Leitura> leituras = new ArrayList<>();
        Cursor cursor = banco.query("leitura", new String[]{"id", "numeroTag", "dataHora", "vezesLida"}, null, null, null, null, null);

        while (cursor.moveToNext()) {
            leitura = new Leitura();
            leitura.setId(cursor.getInt(0));
            leitura.setNumeroTag(cursor.getString(1));
            leitura.setDataHora(cursor.getString(2));
            leitura.setVezesLida(cursor.getInt(3));

            leituras.add(leitura);
        }
        return leituras;
    }

    public boolean exportar() {
        boolean exportar;

        Cursor cursor = banco.rawQuery("SELECT * FROM leitura", null);
        Csv csv = new Csv(cursor);
        File file = csv.exportDB();
        if (file.canRead())
            exportar = true;
        else
            exportar = false;

        return exportar;
    }

    public Cursor pegaCursos(){
        Cursor cursor = banco.rawQuery("SELECT * FROM leitura", null);
        return cursor;
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

    public void limparTabela() {
        banco.execSQL("DELETE FROM leitura");
    }

}
