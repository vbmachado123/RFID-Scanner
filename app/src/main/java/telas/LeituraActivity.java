package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.util.UUID;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import services.BluetoothService;


public class LeituraActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AsciiCommander commander;
    private static String MAC = null;

    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private String dados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitura);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(device != null)
            Toast.makeText(this, device.getName(), Toast.LENGTH_SHORT).show();

        registrarBluetoothReceiver();
    }

    /* RECEBER LEITURA */
    private void registrarBluetoothReceiver() {
        BluetoothReceiver.bindListener(new BluetoothListener() {
            @Override
            public void messageReceived(Intent intent) {

                dados = intent.getStringExtra("dados");

                runOnUiThread(new Runnable() {

                    public void run() {
                        if (dados != null){ /* PREENCHER A LISTA COM OS VALORES */

                        }else { /*  */

                        }
                    }
                });
            }
        });
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
