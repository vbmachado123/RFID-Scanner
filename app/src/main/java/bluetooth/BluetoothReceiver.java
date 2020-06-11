package bluetooth;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.ambrosus.sdk.NetworkCall;

import bluetooth.Bluetooth;
import bluetooth.BluetoothListener;

public class BluetoothReceiver extends BroadcastReceiver {

    //interface
    private static BluetoothListener mListener = null;
    private BluetoothHandler bluHandlerHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        ((BluetoothHandler) new BluetoothHandler(intent)).execute();
    }

    public static void bindListener(BluetoothListener listener) {
        mListener = listener;
    }

    public class BluetoothHandler extends AsyncTask<String, String, String> {
        Intent intent;

        public BluetoothHandler(final Intent i) {
            this.intent= i;
        }

        @Override
        protected String doInBackground(String... params) {
           mListener.messageReceived(intent);
           return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... values) {

        }
    }
}