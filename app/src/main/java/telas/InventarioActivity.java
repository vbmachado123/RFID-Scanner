package telas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.rfidscanner.R;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import model.XYValue;
import util.Xlsx;

public class InventarioActivity extends AppCompatActivity {

    private static final String TAG = "Importacao";
    private Toolbar toolbar;
    private TableRow trImportar, trFazerInventario;
    private ListView lvDiretorio;
    private static final int IMPORTACAO = 1;

    // Declare variables
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] listFile;
    File file;

    Button btnUpDirectory, btnSDCard;

    ArrayList<String> pathHistory;
    String lastDirectory;
    int count = 0;

    ArrayList<XYValue> uploadData;

    ListView lvInternalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        validaCampo();
    }

    private void validaCampo() {
        trImportar = (TableRow) findViewById(R.id.trCarregar);
        trFazerInventario = (TableRow) findViewById(R.id.trFazerInventario);

        trImportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarArquivo();
            }
        });

    }

    private void selecionarArquivo() {
        String[] mimeTypes =
                {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" /*"application/xlsx","text/csv" */ /*.xls & .xlsx*/};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        Toast.makeText(this, "Selecione a tabela para prosseguir!", Toast.LENGTH_LONG).show();
        startActivityForResult(Intent.createChooser(intent,"Selecione a tabela"), IMPORTACAO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(data!= null){
            switch (requestCode){
                case IMPORTACAO:
                    Uri uri = data.getData();
                    file = new File(uri.getPath());
                    Xlsx xlsx = new Xlsx(this);
                    boolean importar = xlsx.importarTabela(file);
                    if(importar) Toast.makeText(this, "Importação concluída!", Toast.LENGTH_LONG).show();
                    else Toast.makeText(this, "Não foi possível importar, tente novamente!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
