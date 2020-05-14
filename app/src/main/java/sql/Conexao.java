package sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Conexao extends SQLiteOpenHelper {

    private static final String name = "banco.db";
    private static final int version = 1;

    public Conexao(Context context){
        super(context, name, null, version);
    }

    private String tabelaTag = "CREATE TABLE IF NOT EXISTS tag(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nomeTag TEXT, dataLeitura TEXT, vezesLida TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(tabelaTag);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(tabelaTag);
    }
}
