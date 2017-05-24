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
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Ioannis on 21/2/2016.
 */
public class AcceptThread extends Thread {
    private final static String LOG_TAG = AcceptThread.class.getSimpleName();

    private final UUID[] mUUIDs;
    private int uuidIndex = 0;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket mBtServerSocket;
    private BluetoothSocket mBtSocket;
    IConnectionEstablished connectionEstablishedListener;
    private boolean isRunning = false;
    ArrayList<BluetoothSocket> unclaimedSockets = new ArrayList<>();

    public AcceptThread(UUID... uuids) {
        mUUIDs = uuids;
    }

    public AcceptThread(IConnectionEstablished listener, UUID... uuids) {
        mUUIDs = uuids;
        connectionEstablishedListener = listener;
    }

    public void setConnectionEstablishedListener(IConnectionEstablished listener) {
        connectionEstablishedListener = listener;
        if (!unclaimedSockets.isEmpty()) {
            for (BluetoothSocket socket : unclaimedSockets)
                connectionEstablishedListener.onConnectionEstablished(socket);
        }

        unclaimedSockets = new ArrayList<>();
    }

    public void run() {
        if (uuidIndex >= mUUIDs.length)
            return;

        isRunning = true;
        try {
            mBtServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mUUIDs.toString(), mUUIDs[uuidIndex]);
            if (mBtServerSocket != null) {
                //Cancel the Bluetooth Discovery (if active) just before accepting so that other people can find you
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                mBtSocket = mBtServerSocket.accept();
            }
        } catch (IOException e) {
        }
        try {
            if (mBtServerSocket != null)
                mBtServerSocket.close();
        } catch (IOException e) {
        }

        uuidIndex++;

        if (connectionEstablishedListener != null)
            connectionEstablishedListener.onConnectionEstablished(mBtSocket);
        else
            unclaimedSockets.add(mBtSocket);

        isRunning = false;
    }



}
