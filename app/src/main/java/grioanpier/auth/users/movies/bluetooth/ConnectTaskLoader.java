package grioanpier.auth.users.movies.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Ioannis on 13/4/2015.
 */
public class ConnectTaskLoader extends android.support.v4.content.AsyncTaskLoader<BluetoothSocket> {
    private static final String LOG_TAG = ConnectTaskLoader.class.getSimpleName();
    private final BluetoothDevice mBtDevice;
    private final UUID[] mUUIDs;
    BluetoothSocket mBtSocket;

    public ConnectTaskLoader(final Context context, BluetoothDevice bluetoothDevice, UUID... uuids) {
        super(context);
        mUUIDs=uuids;
        mBtDevice=bluetoothDevice;
    }

    @Override
    public void deliverResult(final BluetoothSocket socket) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We don't need the result.
            if (socket != null) {
                onReleaseResources(socket);
            }
        }
        BluetoothSocket oldData = mBtSocket;
        mBtSocket = socket;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(socket);
        }

        // At this point we can release the resources associated with
        // 'oldData' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldData != null) {
            onReleaseResources(oldData);
        }
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
        System.out.println("loadInBackground");
        BluetoothSocket btSocket;
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        //Cycles through the available UUIDs and tries to connect to the specified device
        int index=0;
        do{
            Log.v(LOG_TAG, "doing with uuid: " + mUUIDs[index]);

            try {

                btSocket = mBtDevice.createRfcommSocketToServiceRecord(mUUIDs[index]);
                btSocket.connect();
                Log.v(LOG_TAG, "connected");
            } catch (IOException e) {
                //Log.v(LOG_TAG, Utility.getStackTraceString(e.getStackTrace()));
                btSocket=null;
                index++;
            }
        }while (btSocket==null && index < mUUIDs.length );

        return btSocket;
    }

     @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        // At this point we can release the resources associated with 'mBtSocket' if needed.
        if (mBtSocket != null) {
            onReleaseResources(mBtSocket);
            mBtSocket = null;
        }
    }

    protected void onReleaseResources(BluetoothSocket socket) {
        Log.v("AppLog", "onReleaseResources");
        try {
            if (socket!=null)
                socket.close();
        } catch (IOException e) {}
    }




}


