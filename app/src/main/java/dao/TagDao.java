package dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import model.Tag;
import sql.Conexao;

public class TagDao {
    private Conexao conexao;
    private SQLiteDatabase banco;
    private Tag tag;

    public TagDao(Context context){
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
        banco = conexao.getReadableDatabase();
    }

    public Tag recupera(){
        Cursor cursor = banco.rawQuery("SELECT * FROM tag", null);

        while (cursor.moveToNext()){
            tag = new Tag();
            tag.setId(cursor.getInt(0));
            tag.setNomeTag(cursor.getString(1));
            tag.setDataLeitura(cursor.getString(2));
            tag.setVezesLida(cursor.getInt(3));
        }
        return tag;
    }

    public Tag getById(int id){
        Cursor cursor = banco.rawQuery("SELECT * FROM tag WHERE id = " + id, null);

        while (cursor.moveToNext()){
            tag = new Tag();
            tag.setId(cursor.getInt(0));
            tag.setNomeTag(cursor.getString(1));
            tag.setDataLeitura(cursor.getString(2));
            tag.setVezesLida(cursor.getInt(3));
        }
        return tag;
    }

    public void inserir(Tag tag){
        Cursor verifica = banco.rawQuery("SELECT * FROM tag WHERE nomeTag = " + tag.getNomeTag(), null);

        ContentValues values = new ContentValues();
        values.put("nomeTag", tag.getNomeTag());
        values.put("dataLeitura", tag.getDataLeitura());
        values.put("vezesLida", tag.getVezesLida());

        //if(verifica != null)
        if(verifica.moveToFirst()){ //Já possui registro -> adicionar +1 na vezesLida
            Tag tagAdiciona = new Tag();
            tagAdiciona.setId(verifica.getInt(0));
            tagAdiciona.setNomeTag(verifica.getString(1));
            tagAdiciona.setDataLeitura(verifica.getString(2));
            tagAdiciona.setVezesLida(verifica.getInt(3));

            tag.setVezesLida(tagAdiciona.getVezesLida() + 1);
            banco.update("tag", values, "id = ?",
                    new String[]{String.valueOf(tag.getId())});
        } else //Não possui registro -> adicionar
            banco.insert("tag", null, values);
    }

    public List<Tag> obterTodos(){ //Listar na MAIN as tags
        List<Tag> tags = new ArrayList<>();
        Cursor cursor = banco.query("tag", new String[]{"id", "nomeTag", "dataLeitura", "vezesLida"}, null, null, null, null, null);
        cursor.moveToFirst();

        while (cursor.moveToNext()) {
            tag = new Tag();
            tag.setId(cursor.getInt(0));
            tag.setNomeTag(cursor.getString(1));
            tag.setDataLeitura(cursor.getString(2));
            tag.setVezesLida(cursor.getInt(3));
            tags.add(tag);
        }
        return tags;
    }
}
