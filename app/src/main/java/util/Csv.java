package util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.List;

import model.Leitura;

public class Csv {

    private Cursor cursor;
    private String nomeArquivo;

    public Csv(Cursor cursor, String nome) {
        this.cursor = cursor;
        this.nomeArquivo = nome;
    }

    public File exportDB() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        String nomePasta = "/SOS RFiD";
        File f = new File(exportDir, nomePasta);
        if (!f.exists())
        {
            f.mkdirs();
        }

        File file = new File(f, "LeiturasRealizadas " + nomeArquivo + ".csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            csvWrite.writeNext(cursor.getColumnNames());
            while(cursor.moveToNext())
            {
                //Which column you want to exprort
                String arrStr[] ={cursor.getString(0),cursor.getString(1), cursor.getString(2)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            cursor.close();
        }
        catch(Exception sqlEx)
        {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
        return file;
    }
}
