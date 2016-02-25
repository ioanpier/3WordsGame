package grioanpier.auth.users.movies.bluetooth;

/*
Copyright (c) <2015> Ioannis Pierros (ioanpier@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
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
