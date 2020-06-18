package util;

import android.content.Context;

import androidx.room.Room;

import sql.Database;

public class DatabaseClient {

    private Context mmCtx;
    private static DatabaseClient mInstance;

    private Database database;

    private DatabaseClient(Context mmCtx){
        this.mmCtx = mmCtx;
        database = Room.databaseBuilder(mmCtx, Database.class,"rfid_database")/*.fallbackToDestructiveMigration()*/.build();
    }

    public static synchronized DatabaseClient getInstance(Context mCtx){
        if(mInstance == null)
            mInstance = new DatabaseClient(mCtx);

        return mInstance;
    }

    public Database getDatabase(){
        return database;
    }
}
