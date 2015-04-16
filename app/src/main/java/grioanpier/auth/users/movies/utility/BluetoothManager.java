package grioanpier.auth.users.movies.utility;

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
import android.util.Log;

import java.util.Set;
import java.util.UUID;

import grioanpier.auth.users.movies.bluetooth.AcceptTaskLoader;

/**
 * Created by Ioannis on 13/4/2015.
 */
public class BluetoothManager extends Fragment {

    private static BluetoothManager singleton;
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


    public static BluetoothManager getInstance() {
        if (mBluetoothAdapter == null)
            return null;
        else
            return singleton;
    }

    public static boolean isBluetoothAvailable() {
        return (BluetoothAdapter.getDefaultAdapter() != null);
    }

    public static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
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
            //noinspection UnnecessaryReturnStatement
            return;
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_STATE_CHANGED);
            mBluetoothStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int extra = intent.getIntExtra(EXTRA_STATE, 42);
                    switch (extra) {
                        case STATE_ON:
                            Log.v(LOG_TAG, "STATE ON");
                            if (bluetoothRequestEnableListener != null)
                                bluetoothRequestEnableListener.onEnabled();
                            break;
                        case STATE_OFF:
                            //Log.v(LOG_TAG, "STATE OFF");
                            break;
                        case STATE_TURNING_OFF:
                            //Log.v(LOG_TAG, "STATE TURNING OFF");
                            break;
                        case STATE_TURNING_ON:
                            //Log.v(LOG_TAG, "STATE TURNING ON");
                            break;
                    }
                }
            };
            getActivity().registerReceiver(mBluetoothStateReceiver, filter);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    public void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != isDiscoverable) {
            Intent makeDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //makeDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
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
        Log.v(LOG_TAG, "discoveryBroadcast for Bluetooth!");
        switch (action) {
            case ACTION_DISCOVERY_STARTED:
                Log.v(LOG_TAG, "Discovery Started");
                break;
            // When discovery finds a device
            case ACTION_DISCOVERY_FINISHED:
                Log.v(LOG_TAG, "Discovery Finished and Cancelled");
                if (mDiscoveryReceiver != null) {
                    getActivity().unregisterReceiver(mDiscoveryReceiver);
                    mDiscoveryReceiver = null;
                }
                break;
            case ACTION_FOUND:
                // Get the BluetoothDevice object from the Intent
                Log.v(LOG_TAG, "ACTION_FOUND");
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
                Log.v(LOG_TAG + " " + AcceptTaskLoader.class.getSimpleName(), "onCreateLoader for uuid: " + uuid);
                return new AcceptTaskLoader(getActivity(), uuid);
            }

            @Override
            public void onLoadFinished(Loader<BluetoothSocket> loader, BluetoothSocket bluetoothSocket) {
                Log.v(LOG_TAG + " " + AcceptTaskLoader.class.getSimpleName(), "onLoadFinished");
                if (bluetoothSocket != null)
                    ApplicationHelper.getInstance().addPlayerSocket(bluetoothSocket);

                if (serverListenForConnectionsListener != null){
                    if (bluetoothSocket!=null)
                        serverListenForConnectionsListener.onConnectionEstablished(true, bluetoothSocket.getRemoteDevice().getName());
                    else
                        serverListenForConnectionsListener.onConnectionEstablished(false, null);
                }
            }

            @Override
            public void onLoaderReset(Loader<BluetoothSocket> loader) {
                Log.v(LOG_TAG + " " + AcceptTaskLoader.class.getSimpleName(), "onLoaderReset");
            }
        });

    }

    public void prepareServerListenForConnections() {
        getLoaderManager().destroyLoader(ACCEPT_LOADER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDiscoveryReceiver != null) {
            getActivity().unregisterReceiver(mDiscoveryReceiver);
            mDiscoveryReceiver = null;
        }
        if (mBluetoothStateReceiver != null) {
            getActivity().unregisterReceiver(mBluetoothStateReceiver);
            mBluetoothStateReceiver = null;
        }
    }


    public interface BluetoothRequestEnableListener {
        /**
         * Invoked when the user decides wether to enable the Bluetooth or not.
         *
         * @param enabled true if the user activated the bluetooth, false otherwise. Note: true doesn't mean the Bluetooth is already active.
         */
        public void onResult(boolean enabled);

        /**
         * Invoked when the Bluetooth is fully active.
         */
        public void onEnabled();
    }
    public interface BluetoothRequestDiscoverableListener {
        public void onResult(boolean enabled);
    }
    public interface BluetoothGetAvailableDevicesListener {
        /**
         * @param device the (@link BluetoothDevice) that was found.
         */
        public void onDeviceFound(BluetoothDevice device);
    }
    public interface ServerListenForConnectionsListener {
        //Invoked when a connection was established. The result is saved in ApplicationHelper.hostSockets
        public void onConnectionEstablished(boolean established, String name);
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
