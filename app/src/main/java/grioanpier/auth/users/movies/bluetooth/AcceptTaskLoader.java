package grioanpier.auth.users.movies.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Ioannis on 13/4/2015.
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
        System.out.println("loadInBackground");
        try {
            Log.v(LOG_TAG, "getting the server socket");
            mBtServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mUUID.toString(), mUUID);
            if (mBtServerSocket != null) {
                Log.v(LOG_TAG, "mBtServerSocket accepting...");
                //Cancel the Bluetooth Discovery (if active) just before accepting so that other people can find you
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                mBtSocket = mBtServerSocket.accept();
            }
        } catch (IOException e) {
            Log.v(LOG_TAG, e.getMessage());
        }
        try {
            if (mBtServerSocket != null)
                mBtServerSocket.close();
        } catch (IOException e) {
            Log.v(LOG_TAG, e.getMessage());
        }
        return mBtSocket;
    }

    @Override
    public void deliverResult(final BluetoothSocket socket) {
//        Log.v("AppLog", "deliverResult");
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

    protected void onReleaseResources(BluetoothSocket socket) {
        Log.v("AppLog", "onReleaseResources");
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
        }
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
}
