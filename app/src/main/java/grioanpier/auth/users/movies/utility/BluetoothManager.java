package grioanpier.auth.users.movies.utility;
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
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.util.Set;
import java.util.UUID;

import grioanpier.auth.users.movies.bluetooth.AcceptTaskLoader;

/**
 * A {@link Fragment} that contains various useful methods regarding the Bluetooth.
 */
public class BluetoothManager extends Fragment {

    private static final String LOG_TAG = BluetoothManager.class.getSimpleName();
    private static final int ACCEPT_LOADER = 0;
    private static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    //Possible values for resultCode that is passed on the onActivityResult
    private final static int RESULT_CANCELED = Activity.RESULT_CANCELED;
    private final static int RESULT_OK = Activity.RESULT_OK;

    private final static String ACTION_FOUND = BluetoothDevice.ACTION_FOUND;
    private final static String ACTION_DISCOVERY_STARTED = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
    private final static String ACTION_DISCOVERY_FINISHED = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;

    private final static int SCAN_MODE_CONNECTABLE = BluetoothAdapter.SCAN_MODE_CONNECTABLE;
    private final static int SCAN_MODE_CONNECTABLE_DISCOVERABLE = BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;

    private final static String ACTION_STATE_CHANGED = BluetoothAdapter.ACTION_STATE_CHANGED;
    private final static String EXTRA_STATE = BluetoothAdapter.EXTRA_STATE;
    private final static int STATE_OFF = BluetoothAdapter.STATE_OFF;
    private final static int STATE_TURNING_ON = BluetoothAdapter.STATE_TURNING_ON;
    private final static int STATE_ON = BluetoothAdapter.STATE_ON;
    private final static int STATE_TURNING_OFF = BluetoothAdapter.STATE_TURNING_OFF;


    //Locally defined ints that the system passes back to me in the onActivityResult() implementation as the requestCode parameter.
    private final static int REQUEST_ENABLE_BLUETOOTH = 1;
    private final static int REQUEST_MAKE_DISCOVERABLE = 2;

    // Create a BroadcastReceiver for ACTION_FOUND
    private BroadcastReceiver mDiscoveryReceiver = null;
    private BroadcastReceiver mBluetoothStateReceiver = null;
    private final static int isDiscoverable = BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;

    public static boolean isBluetoothAvailable() {
        return (BluetoothAdapter.getDefaultAdapter() != null);
    }

    public static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public String getMacAddress() {
        if (mBluetoothAdapter != null)
            return mBluetoothAdapter.getAddress();
        else
            return "";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH: {
                //Requested to enable the bluetooth
                if (bluetoothRequestEnableListener != null) {
                    switch (resultCode) {
                        case RESULT_OK:
                            bluetoothRequestEnableListener.onResult(true);
                            break;
                        case RESULT_CANCELED:
                            bluetoothRequestEnableListener.onResult(false);
                            break;
                    }
                }
                break;
            }
            case REQUEST_MAKE_DISCOVERABLE: {
                if (bluetoothRequestDiscoverableListener != null)
                    bluetoothRequestDiscoverableListener.onResult(resultCode != RESULT_CANCELED);
            }
        }
    }

    //Request that Bluetooth is enabled and call getDevices()
    public void ensureEnabled() {
        if (mBluetoothAdapter.isEnabled()) {
            if (bluetoothRequestEnableListener != null)
                bluetoothRequestEnableListener.onEnabled();
            return;
        }

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STATE_CHANGED);
        mBluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int extra = intent.getIntExtra(EXTRA_STATE, 42);
                switch (extra) {
                    case STATE_ON:
                        if (bluetoothRequestEnableListener != null)
                            bluetoothRequestEnableListener.onEnabled();
                        break;
                    case STATE_OFF:
                        break;
                    case STATE_TURNING_OFF:
                        break;
                    case STATE_TURNING_ON:
                        break;
                }
            }
        };
        getActivity().registerReceiver(mBluetoothStateReceiver, filter);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
    }

    public void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != isDiscoverable) {
            Intent makeDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(makeDiscoverable, REQUEST_MAKE_DISCOVERABLE);
        }
    }

    public Set<BluetoothDevice> getPairedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    public void getAvailableDevices() {
        if (mBluetoothAdapter.isDiscovering())
            return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_FOUND);
        filter.addAction(ACTION_DISCOVERY_STARTED);
        filter.addAction(ACTION_DISCOVERY_FINISHED);

        mDiscoveryReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                discoveryBroadcast(intent);
            }
        };
        getActivity().registerReceiver(mDiscoveryReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }

    private void discoveryBroadcast(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            //When the discovery starts
            case ACTION_DISCOVERY_STARTED:
                break;
            // When discovery finds a device
            case ACTION_DISCOVERY_FINISHED:
                if (mDiscoveryReceiver != null) {
                    getActivity().unregisterReceiver(mDiscoveryReceiver);
                    mDiscoveryReceiver = null;
                }
                break;
            case ACTION_FOUND:
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && bluetoothGetAvailableDevicesListener != null) {
                    bluetoothGetAvailableDevicesListener.onDeviceFound(device);
                }
                break;
        }
    }

    /**
     * Creates an {@link grioanpier.auth.users.movies.bluetooth.AcceptTaskLoader} that listens for incoming connections for the provided {@link java.util.UUID}.
     * The results are stored in the {@link grioanpier.auth.users.movies.utility.ApplicationHelper}.
     *
     * @param uuid The {@link java.util.UUID} that will be used to listen for incoming connections.
     */
    public void serverListenForConnections(final UUID uuid) {
        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(ACCEPT_LOADER, null, new LoaderManager.LoaderCallbacks<BluetoothSocket>() {
            @Override
            public Loader<BluetoothSocket> onCreateLoader(int id, Bundle args) {
                return new AcceptTaskLoader(getActivity(), uuid);
            }

            @Override
            public void onLoadFinished(Loader<BluetoothSocket> loader, BluetoothSocket bluetoothSocket) {
                if (bluetoothSocket != null)
                    ApplicationHelper.getInstance().addPlayerSocket(bluetoothSocket);

                if (serverListenForConnectionsListener != null) {
                    if (bluetoothSocket != null)
                        serverListenForConnectionsListener.onConnectionEstablished(true, bluetoothSocket.getRemoteDevice().getName());
                    else
                        serverListenForConnectionsListener.onConnectionEstablished(false, null);
                }
            }

            @Override
            public void onLoaderReset(Loader<BluetoothSocket> loader) {}
        });

    }

    /**
     * Prepares the server to listen for incoming connections.
     */
    public void prepareServerListenForConnections() {
        getLoaderManager().destroyLoader(ACCEPT_LOADER);
    }

    @Override
    public void onDestroy() {
        if (mDiscoveryReceiver != null) {
            getActivity().unregisterReceiver(mDiscoveryReceiver);
            mDiscoveryReceiver = null;
        }
        if (mBluetoothStateReceiver != null) {
            getActivity().unregisterReceiver(mBluetoothStateReceiver);
            mBluetoothStateReceiver = null;
        }
        mBluetoothAdapter.cancelDiscovery();
        super.onDestroy();
    }


    public interface BluetoothRequestEnableListener {
        /**
         * Invoked when the user decides whether to enable the Bluetooth or not.
         *
         * @param enabled true if the user activated the bluetooth, false otherwise. Note: true doesn't mean the Bluetooth is already active.
         */
        void onResult(boolean enabled);

        /**
         * Invoked when the Bluetooth is fully active.
         */
        void onEnabled();
    }

    public interface BluetoothRequestDiscoverableListener {
        void onResult(boolean enabled);
    }

    public interface BluetoothGetAvailableDevicesListener {
        /**
         * @param device the (@link BluetoothDevice) that was found.
         */
        void onDeviceFound(BluetoothDevice device);
    }

    public interface ServerListenForConnectionsListener {
        //Invoked when a connection was established. The result is saved in ApplicationHelper.hostSockets
        void onConnectionEstablished(boolean established, String name);
    }

    BluetoothRequestEnableListener bluetoothRequestEnableListener;

    public void setBluetoothRequestEnableListener(BluetoothRequestEnableListener listener) {
        bluetoothRequestEnableListener = listener;
    }


    BluetoothRequestDiscoverableListener bluetoothRequestDiscoverableListener;

    public void setBluetoothRequestDiscoverableListener(BluetoothRequestDiscoverableListener listener) {
        bluetoothRequestDiscoverableListener = listener;
    }


    BluetoothGetAvailableDevicesListener bluetoothGetAvailableDevicesListener;

    public void setBluetoothGetAvailableDevicesListener(BluetoothGetAvailableDevicesListener listener) {
        bluetoothGetAvailableDevicesListener = listener;
    }


    ServerListenForConnectionsListener serverListenForConnectionsListener;

    public void setServerListenForConnectionsListener(ServerListenForConnectionsListener listener) {
        serverListenForConnectionsListener = listener;
    }

}
