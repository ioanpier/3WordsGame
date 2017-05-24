package grioanpier.auth.users.movies.bluetooth;
/*
Copyright {2016} {Ioannis Pierros (ioanpier@gmail.com)}

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Listens for incoming connections on the provided UUIDs.
 * It listens for 1 UUID at every time and as soon as a connection is initialized, it moves to the next.
 */
public class ConnectThread extends Thread {

    private final BluetoothDevice mBtDevice;
    private final UUID[] mUUIDs;
    private BluetoothSocket mBtSocket;
    IConnectionEstablished connectionEstablishedListener;
    private boolean hasFinished = false;
    private boolean isRunning = false;
    private boolean resultReturned = false;

    public ConnectThread(BluetoothDevice bluetoothDevice, UUID... uuids) {
        mUUIDs = uuids;
        mBtDevice = bluetoothDevice;
    }

    public ConnectThread(BluetoothDevice bluetoothDevice, IConnectionEstablished listener, UUID... uuids) {
        mUUIDs = uuids;
        mBtDevice = bluetoothDevice;
        connectionEstablishedListener = listener;
    }

    public void setConnectionEstablishedListener(IConnectionEstablished listener) {
        connectionEstablishedListener = listener;

        if (hasFinished && !resultReturned)
            connectionEstablishedListener.onConnectionEstablished(mBtSocket);
    }

    public void run() {
        isRunning = true;
        BluetoothSocket btSocket;
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        //Cycles through the available UUIDs and tries to connect to the specified device
        int index = 0;
        do {
            try {
                Thread.sleep(100);
                btSocket = mBtDevice.createRfcommSocketToServiceRecord(mUUIDs[index]);
                btSocket.connect();
            } catch (IOException e) {
                btSocket = null;
                index++;
            } catch (InterruptedException e) {
                btSocket = null;
                e.printStackTrace();
            }

        } while (btSocket == null && index < mUUIDs.length);

        mBtSocket = btSocket;

        if (connectionEstablishedListener != null) {
            connectionEstablishedListener.onConnectionEstablished(mBtSocket);
            resultReturned = true;
        }

        hasFinished = true;
        isRunning = false;

    }

}
