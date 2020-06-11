package bluetooth;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by OTAVIO on 13/09/2017.
 */

public interface BluetoothListener {
    public void messageReceived(Intent messageText);
}