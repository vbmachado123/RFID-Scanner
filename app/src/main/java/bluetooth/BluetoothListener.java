package bluetooth;

import android.os.Bundle;

/**
 * Created by OTAVIO on 13/09/2017.
 */

public interface BluetoothListener {
    public void messageReceived(Bundle messageText);
}