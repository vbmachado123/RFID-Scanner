package util;


import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import model.Leitura;
import sql.Database;

public class DatabaseInitializer {

    private static final String TAG = DatabaseInitializer.class.getName();

    public static void populateAsync(final Database db) {
        PopulateDbAsync task = new PopulateDbAsync(db);
        task.execute();
    }

    public static void populateSync( final Database db) {
        populateWithTestData(db);
    }

    private static Leitura addLeitura(final Database db, Leitura leitura) {
        db.leituraDao().inserir(leitura);
        return leitura;
    }

    private static void populateWithTestData(Database db) {
        Leitura user = new Leitura();

        addLeitura(db, user);

        List<Leitura> userList = db.leituraDao().getAll();
        Log.d(DatabaseInitializer.TAG, "Rows Count: " + userList.size());
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final Database mDb;

        PopulateDbAsync(Database db) {
            mDb = db;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            populateWithTestData(mDb);
            return null;
        }

    }
}