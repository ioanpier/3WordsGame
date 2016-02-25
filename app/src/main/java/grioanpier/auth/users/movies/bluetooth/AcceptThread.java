package grioanpier.auth.users.movies.bluetooth;

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
