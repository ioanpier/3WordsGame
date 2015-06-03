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
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

/**
 * Listens for incoming connections on the provided UUIDs.
 * It listens for 1 UUID at every time and as soon as a connection is initialized, it moves to the next.
 */
public class ConnectTaskLoader extends android.support.v4.content.AsyncTaskLoader<BluetoothSocket> {
    private static final String LOG_TAG = ConnectTaskLoader.class.getSimpleName();
    private final BluetoothDevice mBtDevice;
    private final UUID[] mUUIDs;
    private BluetoothSocket mBtSocket;

    public ConnectTaskLoader(final Context context, BluetoothDevice bluetoothDevice, UUID... uuids) {
        super(context);
        mUUIDs=uuids;
        mBtDevice=bluetoothDevice;
    }

    @Override
    public void deliverResult(final BluetoothSocket socket) {
        mBtSocket = socket;
        if (isStarted())
            super.deliverResult(socket);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mBtSocket != null)
            deliverResult(mBtSocket);
        if (takeContentChanged() || mBtSocket == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }


    @Override
    public BluetoothSocket loadInBackground() {
        BluetoothSocket btSocket;
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        //Cycles through the available UUIDs and tries to connect to the specified device
        int index=0;
        do{
            try {
                Thread.sleep(100);
                btSocket = mBtDevice.createRfcommSocketToServiceRecord(mUUIDs[index]);
                btSocket.connect();
            } catch (IOException e) {
                btSocket=null;
                index++;
            } catch (InterruptedException e) {
                btSocket=null;
                e.printStackTrace();
            }

        }while (btSocket==null && index < mUUIDs.length );

        return btSocket;
    }
}


