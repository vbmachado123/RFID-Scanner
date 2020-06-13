package util;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dao.InventarioDao;
import dao.LeituraDao;
import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import model.InventarioNegado;
import model.Leitura;
import model.Local;
import model.Status;
import model.SubLocal;

@Database(entities = {Inventario.class, Leitura.class,
        Equipamento.class, EquipamentoInventario.class,
        InventarioNegado.class, Local.class, Status.class, SubLocal.class}, version = 1, exportSchema = false)
public abstract class DataBase extends RoomDatabase {

    public abstract LeituraDao leituraDao();
    public abstract InventarioDao InvetarioDao();

    private static volatile DataBase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static DataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DataBase.class, "word_database")
                            .build();
                }
            }

        }
        return INSTANCE;
    }
}