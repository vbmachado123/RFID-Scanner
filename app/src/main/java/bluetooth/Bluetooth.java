package bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


public class Bluetooth extends BroadcastReceiver {
    private Bluetooth.BluetoothListener listener;
    private ArrayList<BluetoothDevice> lista;
    private BluetoothAdapter dispositivo;


    public static Bluetooth getBondedDevices(Context applicationContext) throws IOException {
        Bluetooth bluetooth = new Bluetooth((Bluetooth.BluetoothListener)null);
        bluetooth.dispositivo = BluetoothAdapter.getDefaultAdapter();
        if (!bluetooth.dispositivo.isEnabled()) {
            bluetooth.dispositivo.enable();
        }

        Set<BluetoothDevice> pairedDevices = bluetooth.dispositivo.getBondedDevices();
        if (pairedDevices.size() > 0) {
            Iterator i$ = pairedDevices.iterator();

            while(i$.hasNext()) {
                BluetoothDevice device = (BluetoothDevice)i$.next();
                bluetooth.lista.add(device);
            }
        }

        return bluetooth;
    }

    public static Bluetooth startFindDevices(Context context, Bluetooth.BluetoothListener listener) throws IOException {
        Bluetooth bluetooth = new Bluetooth(listener);
        bluetooth.dispositivo = BluetoothAdapter.getDefaultAdapter();
        if (!bluetooth.dispositivo.isEnabled()) {
            bluetooth.dispositivo.enable();
            return null;
        } else {
            IntentFilter filter = new IntentFilter("android.bluetooth.device.action.FOUND");
            IntentFilter filter2 = new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
            IntentFilter filter3 = new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_STARTED");
            context.registerReceiver(bluetooth, filter);
            context.registerReceiver(bluetooth, filter2);
            context.registerReceiver(bluetooth, filter3);
            bluetooth.dispositivo.startDiscovery();
            return bluetooth;
        }
    }

    private Bluetooth(Bluetooth.BluetoothListener listener) {
        this.listener = listener;
        this.lista = new ArrayList();
    }

    public boolean cancelDiscovery() {
        return this.dispositivo.cancelDiscovery();
    }

    public ArrayList<BluetoothDevice> getDispositivos() {
        return this.lista;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.compareTo("android.bluetooth.device.action.FOUND") == 0) {
            BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            if (this.lista.contains(device)) {
                return;
            }

            this.lista.add(device);
        } else if (action.compareTo("android.bluetooth.adapter.action.DISCOVERY_FINISHED") == 0) {
            context.unregisterReceiver(this);
        }

        if (this.listener != null) {
            this.listener.action(action);
        }

    }

    public interface BluetoothListener {
        String ACTION_DISCOVERY_STARTED = "android.bluetooth.adapter.action.DISCOVERY_STARTED";
        String ACTION_FOUND = "android.bluetooth.device.action.FOUND";
        String ACTION_DISCOVERY_FINISHED = "android.bluetooth.adapter.action.DISCOVERY_FINISHED";

        void action(String var1);
    }
}
