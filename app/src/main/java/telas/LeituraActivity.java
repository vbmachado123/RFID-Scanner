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
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.util.UUID;

import services.BluetoothService;


public class LeituraActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AsciiCommander commander;
    private static String MAC = null;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter adapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    BluetoothReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitura);

        receiver = new BluetoothReceiver();
        registerReceiver(receiver, new IntentFilter("GET_CONEXAO"));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(device != null)
            Toast.makeText(this, device.getName(), Toast.LENGTH_SHORT).show();

     /*   if(commander.isConnected()) {
            Toast.makeText(this, commander.getConnectedDeviceName(), Toast.LENGTH_SHORT).show();
        }*/
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

    class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("GET_CONEXAO"))
            {
                device = intent.getParcelableExtra("device");
               /* conexao = intent.getBooleanExtra("conexao", false);
                // commander = (AsciiCommander) intent.getSerializableExtra("commander");
             */
            }
        }
    }
}
