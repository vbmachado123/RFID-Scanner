package sql;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import dao.InventarioDao;
import dao.InventarioNegadoDao;
import dao.LeituraDao;
import dao.LocalDao;
import dao.StatusDao;
import dao.SubLocalDao;
import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import model.InventarioNegado;
import model.Leitura;
import model.Local;
import model.SubLocal;
import model.Status;

@androidx.room.Database(
        entities = {Equipamento.class, EquipamentoInventario.class,
                Inventario.class, InventarioNegado.class, Leitura.class,
        Local.class, Status.class, SubLocal.class}, version = 3, exportSchema = false)
public abstract class Database extends RoomDatabase {

    public abstract EquipamentoDao equipamentoDao();
    public abstract EquipamentoInventarioDao equipamentoInventarioDao();
    public abstract InventarioDao inventarioDao();
    public abstract InventarioNegadoDao inventarioNegadoDao();
    public abstract LeituraDao leituraDao();
    public abstract LocalDao localDao();
    public abstract StatusDao statusDao();
    public abstract SubLocalDao subLocalDao();

    private static Database INSTANCE;

    public static Database getDatabase(Context context) {
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), Database.class,"rfid_database").fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onOpen (SupportSQLiteDatabase db){
            super.onOpen(db);
        }
    };

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final EquipamentoDao equipamentoDao;
        private final EquipamentoInventarioDao equipamentoInventarioDao;
        private final InventarioDao inventarioDao;
        private final InventarioNegadoDao inventarioNegadoDao;
        private final LeituraDao leituraDao;
        private final LocalDao localDao;
        private final StatusDao statusDao;
        private final SubLocalDao subLocalDao;

        PopulateDbAsync(Database db) {
            equipamentoDao = db.equipamentoDao();
            equipamentoInventarioDao = db.equipamentoInventarioDao();
            inventarioDao = db.inventarioDao();
            inventarioNegadoDao = db.inventarioNegadoDao();
            leituraDao = db.leituraDao();
            localDao = db.localDao();
            statusDao = db.statusDao();
            subLocalDao = db.subLocalDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {

            return null;
        }
    }
}
