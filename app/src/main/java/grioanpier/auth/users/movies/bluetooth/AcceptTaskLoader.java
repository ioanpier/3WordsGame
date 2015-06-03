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
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;
import java.util.UUID;

/**
 * Attempts to connect to the specified device with the provided UUID.
 * Returns the {@link android.bluetooth.BluetoothSocket} (null if it failed).
 */
public class AcceptTaskLoader extends AsyncTaskLoader<BluetoothSocket> {

    private final static String LOG_TAG = AcceptTaskLoader.class.getSimpleName();

    private final UUID mUUID;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket mBtServerSocket;
    private BluetoothSocket mBtSocket = null;

    public AcceptTaskLoader(final Context context, UUID uuid) {
        super(context);
        mUUID = uuid;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mBtSocket != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mBtSocket);
        }
        if (takeContentChanged() || mBtSocket == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }


    @Override
    public BluetoothSocket loadInBackground() {
        try {
            mBtServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mUUID.toString(), mUUID);
            if (mBtServerSocket != null) {
                //Cancel the Bluetooth Discovery (if active) just before accepting so that other people can find you
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                mBtSocket = mBtServerSocket.accept();
            }
        } catch (IOException e) {}
        try {
            if (mBtServerSocket != null)
                mBtServerSocket.close();
        } catch (IOException e) {}
        return mBtSocket;
    }

    @Override
    public void deliverResult(final BluetoothSocket socket) {
        //The {@link AcceptTaskLoader} is for accepting incoming bluetooth connections, not for managing them.
        //Therefor we ignore the previous value of the {mBtSocket}
        mBtSocket = socket;
        if (isStarted())
            super.deliverResult(socket);
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        if (mBtSocket != null) {
            mBtSocket = null;
        }
    }
}
