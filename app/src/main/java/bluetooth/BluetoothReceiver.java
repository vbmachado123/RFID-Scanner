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
    private static BluetoothListener mListener;
    private BluetoothHandler bluHandlerHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        BluetoothHandler bluHandler = new BluetoothHandler(data);
        bluHandlerHandler.execute();
    }

    public static void bindListener(BluetoothListener listener) {
        mListener = listener;
    }

    public class BluetoothHandler extends AsyncTask<String, String, String> {
        String messageText = "";

        Bundle data;

        public BluetoothHandler(final Bundle data) {
            this.data = data;

        }

        @Override
        protected String doInBackground(String... params) {
         if (data != null) {
           mListener.messageReceived(data);
                }

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