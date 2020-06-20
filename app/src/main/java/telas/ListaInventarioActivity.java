package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.rfidscanner.R;

import java.util.ArrayList;

import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import model.Local;
import model.SubLocal;
import util.Data;

public class ListaInventarioActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ArrayList<Equipamento> equipamentos;

    private Local local = new Local();
    private SubLocal subLocal = new SubLocal();
    private Inventario inventario = new Inventario();
    private EquipamentoInventario equipamentoInventario = new EquipamentoInventario();

    /* LOCALIZAÇÃO */
    private Location location;
    private LocationManager locationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_inventario);

        Intent it = getIntent();
        Bundle extras = it.getExtras();
        local = (Local) extras.getSerializable("local");
        subLocal = (SubLocal) extras.getSerializable("sublocal");

        boolean iniciado = iniciaInventario();

        inventario.setIdLocal(local.getId());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (subLocal != null) {/* Foi escolhido na tela anterior */
            inventario.setIdSubLocal(subLocal.getIdLocal());
            toolbar.setTitle(local.getDescricao() + " - " + subLocal.getDescricao());
            if(iniciado) Toast.makeText(this, "O inventário " + local.getDescricao() +  " - " + subLocal.getDescricao() + " foi iniciado em: " + data, Toast.LENGTH_LONG).show();
        } else {
            toolbar.setTitle("Inventário: " + local.getDescricao());
            if(iniciado) Toast.makeText(this, "O inventário " + local.getDescricao() +  " foi iniciado em: " + data, Toast.LENGTH_LONG).show();
        }
    }

    private boolean iniciaInventario() {
        boolean inicia;
         data = Data.getDataEHoraAual("dd/MM/yyyy - HH:mm");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        } else {
            longitude = 1.0;
            latitude = 1.0;
        }

        inicia = true;

        return inicia;
    }
}