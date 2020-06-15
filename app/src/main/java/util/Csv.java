package util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import model.Leitura;

public class Csv {

    private Cursor cursor;
    private SimpleDateFormat dataFormatada;
    private Date date;

    public Csv(Cursor cursor) {
        this.cursor = cursor;
    }

    public void ImportCSV(){ }

    public File exportDB() {

        /* RECUPERANDO A DATA E HORA ATUAL */
        dataFormatada = new SimpleDateFormat("ddMMyyyy_HH:mm");
        date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Date dataAtual = calendar.getTime();
        String dataFinal = dataFormatada.format(dataAtual);

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
                Log.i("Salvando", cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2));
            }
            csvWrite.close();
            cursor.close();
        }
        catch(Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
        return file;
    }
}
