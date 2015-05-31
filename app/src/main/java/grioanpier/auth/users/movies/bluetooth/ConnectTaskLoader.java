package grioanpier.auth.users.movies.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

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
    BluetoothSocket mBtSocket;

    public ConnectTaskLoader(final Context context, BluetoothDevice bluetoothDevice, UUID... uuids) {
        super(context);
        mUUIDs=uuids;
        mBtDevice=bluetoothDevice;
    }

    @Override
    public void deliverResult(final BluetoothSocket socket) {
        Log.v(LOG_TAG, "deliverResult");
        if (isReset()) {
            // An async query came in while the loader is stopped.  We don't need the result.
            if (socket != null) {
                Log.v(LOG_TAG, "deliverResult | socket!=null");
                //onReleaseResources(socket);
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

        //Whenever the user went back to LocalGame screen while the game was playing, these lines resulted in disconencting the
        //player from the host.
        //if (oldData != null) {
        //    Log.v(LOG_TAG, "deliverResult | oldData!=null");
        //    onReleaseResources(oldData);
        //}
    }

    @Override
    protected void onStartLoading() {
        Log.v(LOG_TAG, "onStartLoading");
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
        Log.v(LOG_TAG, "loadInBackground");
        BluetoothSocket btSocket;
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        //Cycles through the available UUIDs and tries to connect to the specified device
        int index=0;
        do{
            Log.v(LOG_TAG, "doing with uuid: " + mUUIDs[index]);

            try {
                Thread.sleep(100);
                btSocket = mBtDevice.createRfcommSocketToServiceRecord(mUUIDs[index]);
                btSocket.connect();
                Log.v(LOG_TAG, "connected");
            } catch (IOException e) {
                //Log.v(LOG_TAG, Utility.getStackTraceString(e.getStackTrace()));
                btSocket=null;
                index++;
            } catch (InterruptedException e) {
                btSocket=null;
                e.printStackTrace();
            }

        }while (btSocket==null && index < mUUIDs.length );

        return btSocket;
    }

    protected void onReleaseResources(BluetoothSocket socket) {
        Log.v(LOG_TAG, "onReleaseResources");
        try {
            if (socket!=null)
            {
                Log.v(LOG_TAG, "closing socket: "+socket.getRemoteDevice().getName());
                socket.close();
            }

        } catch (IOException e) {}
    }




}


