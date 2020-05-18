package telas;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rfidscanner.R;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trConectar, trLeitura, trGravacao, trInventario, trConfiguracoes;
    private Context Context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        validaCampo();


    }

    private void validaCampo() {
        trConectar = (TableRow) findViewById(R.id.trConectar);
        trLeitura = (TableRow) findViewById(R.id.trLeitura);
        trGravacao = (TableRow) findViewById(R.id.trGravacao);
        trInventario = (TableRow) findViewById(R.id.trInventario);
        trConfiguracoes = (TableRow) findViewById(R.id.trConfiguracao);

        trConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Conectar pressionado", Toast.LENGTH_SHORT).show();
            }
        });

        trLeitura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Leitura pressionado", Toast.LENGTH_SHORT).show();
            }
        });

        trGravacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Gravação pressionado", Toast.LENGTH_SHORT).show();
            }
        });

        trInventario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Inventário pressionado", Toast.LENGTH_SHORT).show();
            }
        });

        trConfiguracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Configurações pressionado", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }
}
