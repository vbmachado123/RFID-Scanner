package telas;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rfidscanner.R;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import bluetooth.Bluetooth;

import static bluetooth.Bluetooth.startFindDevices;


public class HomeActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final int SOLICITA_ACESSO_LOCALIZACAO = 1;
    private static final int SOLICITA_BLUETOOTH = 2;
    private ListView listaDispositivos;
    private Button botaoConectar;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> listAdapter;
    private BluetoothDevice scannerDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listaDispositivos = (ListView) findViewById(R.id.lvDispositivosEnxergados);
        botaoConectar = (Button) findViewById(R.id.btConectar);

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
        listaDispositivos.setAdapter(listAdapter);

        verificaEstadoBluetooth();

        registerReceiver(devicesFoundReceivers, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReceivers, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReceivers, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        botaoConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    if(verificaLocalizacao()) {
                        listAdapter.clear();
                        bluetoothAdapter.startDiscovery();
                    }
                } else {
                    verificaEstadoBluetooth();
                }
            }
        });

        verificaLocalizacao();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(devicesFoundReceivers);
    }

    private boolean verificaLocalizacao() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, SOLICITA_ACESSO_LOCALIZACAO);
            return false;
        } else {
            return true;
        }

    }

    private void verificaEstadoBluetooth() {
        if(bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não é suportado no dispositivo!", Toast.LENGTH_SHORT).show();
        } else {
            if(bluetoothAdapter.isEnabled()) {
                if(bluetoothAdapter.isDiscovering()){
                    Toast.makeText(this, "Procurando dispositivos...", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Bluetooth foi ativado!", Toast.LENGTH_SHORT).show();
                    botaoConectar.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "Por favor, ative o bluetooth!", Toast.LENGTH_SHORT).show();
                Intent ativaIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(ativaIntent, SOLICITA_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SOLICITA_BLUETOOTH && resultCode == RESULT_OK) {
            verificaEstadoBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case SOLICITA_ACESSO_LOCALIZACAO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Acesso a localização permitido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Acesso a localização negado", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private final BroadcastReceiver  devicesFoundReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listAdapter.add(device.getName() + "\n" + device.getAddress());
                if(device.getName().contains("001836")) {
                    bluetoothAdapter.cancelDiscovery();
                    Toast.makeText(context, "RFID Scanner detected: " + device.getAddress(), Toast.LENGTH_SHORT).show();
                    scannerDevice = device;
                }
                listAdapter.notifyDataSetChanged();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                botaoConectar.setText("Procurando dispositivos...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                botaoConectar.setText("Procurando, aguarde...");
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Iniciar conexão com o dispositivo

        scannerDevice.getAddress();
    }
}
