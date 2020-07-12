package util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import dao.InventarioDao;
import dao.InventarioNegadoDao;
import dao.LeituraDao;
import dao.LocalDao;
import dao.StatusDao;
import dao.SubLocalDao;
import model.Equipamento;
import model.Inventario;
import model.Leitura;
import model.Local;
import model.Status;
import model.SubLocal;

public class Csv {

    private Cursor cursor;
    /* BANCO DE DADOS */
    private EquipamentoDao equipamentoDao;
    private EquipamentoInventarioDao equipamentoInventarioDao;
    private InventarioDao inventarioDao;
    private InventarioNegadoDao inventarioNegadoDao;
    private LeituraDao leituraDao;
    private LocalDao localDao;
    private StatusDao statusDao;
    private SubLocalDao subLocalDao;

    /* Models */
    private Leitura leitura = new Leitura();
    private Local local = new Local();
    private SubLocal subLocal = new SubLocal();
    private Equipamento equipamento = new Equipamento();
    private Status status = new Status();

    private Context context;

    public Csv(Cursor cursor) {
        this.cursor = cursor;
    }

    public Csv(Context context) {
        this.context = context;

        leituraDao = new LeituraDao(context);
        localDao = new LocalDao(context);
        subLocalDao = new SubLocalDao(context);
        equipamentoDao = new EquipamentoDao(context);
        statusDao = new StatusDao(context);
        equipamentoInventarioDao = new EquipamentoInventarioDao(context);
        inventarioDao = new InventarioDao(context);
        inventarioNegadoDao = new InventarioNegadoDao(context);
    }

    public boolean ImportCSV(File file) {
        boolean isCreate = false;

        if (!file.exists()) { /* Arquivo invalido */
            isCreate = false;
        } else { /* Arquivo valido */
            String separa;
            String[] leituraTabela;

            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while ((separa = bufferedReader.readLine()) != null) {
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

        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SOSRFiD/Leituras Realizadas");
        if (!exportDir.exists()) exportDir.mkdirs();

        File file = new File(exportDir, "LeiturasRealizadas" + dataFinal + ".csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            cursor.moveToFirst();
            csvWrite.writeNext(cursor.getColumnNames());
            while (cursor.moveToNext()) {
                String arrStr[] = {cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            cursor.close();
        } catch (Exception sqlEx) {
            Log.e("Salvando", sqlEx.getMessage(), sqlEx);
        }
        return file;
    }

    public boolean exportarInventario() {
        boolean exportado = false;

        String dataFinal = Data.getDataEHoraAual("ddMMyyyy_HHmm");

        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SOSRFiD/Inventarios Realizados");
        if (!exportDir.exists()) exportDir.mkdirs();

        Inventario inventario = inventarioDao.recupera();
        Local local = localDao.getById(inventario.getIdLocal());
        SubLocal subLocal = subLocalDao.getById(inventario.getIdSubLocal());

        String nome = "";
        if (subLocal != null)
            nome = local.getDescricao() + " - " + subLocal.getDescricao();

        else
            nome = local.getDescricao();

        File file = new File(exportDir, "Inventario Encontrado- " + nome + dataFinal + ".csv");
        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(file));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return exportado;
    }
}
