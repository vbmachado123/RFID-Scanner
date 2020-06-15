package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.rfidscanner.R;

public class InventarioActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trImportar, trFazerInventario;
    private static final int IMPORTACAO = 1;
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
            abrirEscolha();
        }
    });

    }

    private void abrirEscolha() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(Intent.createChooser(intent, "Abrir CSV"), IMPORTACAO);
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
