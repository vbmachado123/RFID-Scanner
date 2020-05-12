package telas;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Set;

public class ListaDispositivos extends ListActivity {

    private BluetoothAdapter adapter = null;
    static String ENDERECO_MAC = null;
    private static final String TAG = "Bluetooth";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "> Iniciando a ListaDispositivos");

        ArrayAdapter<String> arrayAdapterBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        adapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> dispositivosPareados = adapter.getBondedDevices();

        if(dispositivosPareados.size() > 0){
            for(BluetoothDevice dispositivo : dispositivosPareados) {
                String nomeBt = dispositivo.getName();
                String macBt = dispositivo.getAddress();
                Log.i(TAG, "> Procurando dispositivos pareados: " + nomeBt + "\n" + macBt);
                arrayAdapterBluetooth.add(nomeBt + "\n" + macBt);
            }
        }

        setListAdapter(arrayAdapterBluetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String informacaoGeral = ((TextView) v).getText().toString();
        /*Log.i(TAG, "> Item selecionado: " + informacaoGeral);*/

        String enderecoMac = informacaoGeral.substring(informacaoGeral.length() - 17);

        /*Log.i(TAG, "> Enredeço MAC: " + enderecoMac);*/

        Intent retornaMAC = new Intent();
        retornaMAC.putExtra(ENDERECO_MAC, enderecoMac);
        setResult(RESULT_OK, retornaMAC);
        finish();
    }
}
