package sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Conexao extends SQLiteOpenHelper {

    private static final String name = "banco.db";
    private static final int version = 1;

    public Conexao(Context context){
        super(context, name, null, version);
    }

    private String tabelaLeitura = "CREATE TABLE IF NOT EXISTS leitura(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "numeroTag TEXT, dataHora TEXT, vezesLida INTEGER)";

    private String tabelaLocal = "CREATE TABLE IF NOT EXISTS local(id INTEGER PRIMARY KEY, " +
            "descricao TEXT)";

    private String tabelaSubLocal = "CREATE TABLE IF NOT EXISTS subLocal(id INTEGER PRIMARY KEY, " +
            "idLocal INTEGER, descricao TEXT)";

    private String tabelaEquipamento = "CREATE TABLE IF NOT EXISTS equipamento(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "idLocal INTEGER, idSubLocal INTEGER , numeroTag TEXT, descricao TEXT)";

    private String tabelaStatus = "CREATE TABLE IF NOT EXISTS status(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "descricao TEXT)";

    private String tabelaInventario = "CREATE TABLE IF NOT EXISTS inventario(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "idLocal INTEGER, idSubLocal INTEGER, dataHora TEXT, latitude TEXT, longitude TEXT, endereco TEXT)";

    private String tabelaEquipamentoInventario = "CREATE TABLE IF NOT EXISTS equipamentoInventario(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "idInventario INTEGER, idEquipamento INTEGER, idStatus INTEGER, dataHora TEXT, latitude TEXT, longitude TEXT)";

    private String tabelaInventarioNegado = "CREATE TABLE IF NOT EXISTS inventarioNegado(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "idInventario INTEGER, idStatus INTEGER, numeroTag TEXT, dataHora TEXT, latitude TEXT, longitude TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(tabelaLeitura);
        db.execSQL(tabelaLocal);
        db.execSQL(tabelaSubLocal);
        db.execSQL(tabelaEquipamento);
        db.execSQL(tabelaStatus);
        db.execSQL(tabelaInventario);
        db.execSQL(tabelaEquipamentoInventario);
        db.execSQL(tabelaInventarioNegado);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(tabelaLeitura);
        db.execSQL(tabelaLocal);
        db.execSQL(tabelaSubLocal);
        db.execSQL(tabelaEquipamento);
        db.execSQL(tabelaStatus);
        db.execSQL(tabelaInventario);
        db.execSQL(tabelaEquipamentoInventario);
        db.execSQL(tabelaInventarioNegado);
    }
}
