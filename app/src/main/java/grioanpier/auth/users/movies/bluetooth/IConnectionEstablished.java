package grioanpier.auth.users.movies.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * Created by Ioannis on 21/2/2016.
 */
public interface IConnectionEstablished {
    void onConnectionEstablished(BluetoothSocket bluetoothSocket);
}
