package util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.Leitura;

public class Csv {

    private Cursor cursor;

    public Csv(Cursor cursor) {
        this.cursor = cursor;
    }

    public boolean ImportCSV(File file){
        boolean isCreate = false;

        if(!file.exists()){ /* Arquivo invalido */
            isCreate = false;
        } else { /* Arquivo valido */
            String separa;
            String[] leituraTabela;

            try{
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while ((separa = bufferedReader.readLine()) != null){
                    leituraTabela = separa.split(",");

                    /* Inserindo no banco */
                    ContentValues linhaTabela = new ContentValues();


                }

                isCreate = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isCreate;
    }

    public File exportDB() {

        String dataFinal = Data.getDataEHoraAual("ddMMyyyy_HHmm");

        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SOSRFiD");
        if (!exportDir.exists()) exportDir.mkdirs();

        File file = new File(exportDir, "LeiturasRealizadas" + dataFinal + ".csv"); /* Temporario */
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            cursor.moveToFirst();
            csvWrite.writeNext(cursor.getColumnNames());
            while(cursor.moveToNext()) {
                String arrStr[] ={cursor.getString(0),cursor.getString(1), cursor.getString(2), cursor.getString(3)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            cursor.close();
        }
        catch(Exception sqlEx) {
            Log.e("Salvando", sqlEx.getMessage(), sqlEx);
        }
        return file;
    }
}
