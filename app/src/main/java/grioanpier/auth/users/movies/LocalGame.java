package grioanpier.auth.users.movies;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import grioanpier.auth.users.movies.bluetooth.ConnectTaskLoader;
import grioanpier.auth.users.movies.utility.ApplicationHelper;
import grioanpier.auth.users.movies.utility.BluetoothManager;
import grioanpier.auth.users.movies.utility.Constants;


public class LocalGame extends ActionBarActivity {

    private static final String LOG_TAG = LocalGame.class.getSimpleName();
    private static final String sBluetoothManagerFragmentTag = "bluetoothmanager";
    private static final String sPlaceholderFragmentTag = "localplaceholder";
    private static final int SOURCE_BUTTON_JOIN = 0;
    private static final int SOURCE_BUTTON_SPECTATE = 1;
    private boolean restored;

    private BluetoothManager btManager;
    private PlaceholderFragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_game);

        //Finish the activity if there is no Bluetooth on the device.
        if (!BluetoothManager.isBluetoothAvailable()) {
            //We are killing the activity in onCreate so the View hasn't been inflated yet.
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        if (savedInstanceState == null) {
            restored = false;
            btManager = new BluetoothManager();
            frag = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, frag, sPlaceholderFragmentTag)
                    .add(btManager, sBluetoothManagerFragmentTag)
                    .commit();
        } else {
            restored = true;
            btManager = (BluetoothManager) getSupportFragmentManager().findFragmentByTag(sBluetoothManagerFragmentTag);
            frag = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag(sPlaceholderFragmentTag);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_local_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        Log.v(LOG_TAG, "onStart");
        super.onStart();

        if (BluetoothManager.isBluetoothEnabled()){
            if (!restored)
                //If this is the first time running, set the Paired devices.
                frag.setDevicesList(btManager.getPairedDevices());
        }
        else{
            //The Bluetooth isn't enabled. Set a listener to listen for the result and prompts the user to enable it.
            btManager.setBluetoothRequestEnableListener(new BluetoothManager.BluetoothRequestEnableListener() {
                @Override
                public void onResult(boolean enabled) {
                    if (!enabled) {
                        Toast.makeText(getApplicationContext(), "Bluetooth is needed for Local Game", Toast.LENGTH_LONG).show();
                        btManager.getActivity().finish();
                    }
                }

                @Override
                public void onEnabled() {
                    frag.setDevicesList(btManager.getPairedDevices());
                }
            });
            btManager.ensureEnabled();
        }

        //Set a listener for {onDeviceFound} Events. Send the found device to the {@link BluetoothChatFragment}
        btManager.setBluetoothGetAvailableDevicesListener(new BluetoothManager.BluetoothGetAvailableDevicesListener() {
            @Override
            public void onDeviceFound(BluetoothDevice device) {
                frag.addDeviceInList(device);
            }
        });

        //The {@link BluetoothManager} starts the discovery only if it isn't already discovering.
        btManager.getAvailableDevices();

        //TODO remove. These are for debugging purposes.
        ApplicationHelper app = ApplicationHelper.getInstance();
        Log.v(LOG_TAG, "player sockets:");
        for (BluetoothSocket socket: app.getPlayerSockets())
            Log.v(LOG_TAG, socket.getRemoteDevice().getName());
        System.out.println("host Socket:");
        if (app.getHostSocket()!=null)
            Log.v(LOG_TAG, app.getHostSocket().getRemoteDevice().getName());
    }

    @Override
    public void onStop() {
        super.onStop();
        //Make sure to cancel the bluetooth discovery.
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }


    /**
     * A placeholder fragment containing this screen's view. A list of BluetoothDevices
     * and buttons to join, spectate, host
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String LOG_TAG = LocalGame.class.getSimpleName() + PlaceholderFragment.class.getSimpleName();
        private static final int CONNECT_LOADER = 0;
        private static final String bundleDeviceList = "devicesListForSaveInstance";
        private int devicesNumber=0;

        private ListView listView;
        private Button spectate_button;
        private Button join_button;
        private Button host_button;
        private Button refresh_button;

        private View selectedView;
        private String selectedMAC;

        private ArrayAdapter<String> devicesAdapter = null;
        private ArrayList<String> devicesList = new ArrayList<>();
        // HashSet to back up devicesList to prevent duplicates.
        private HashSet<String> devicesSet = new HashSet<>();

        private final static String LOADER_STATE = "loader state";
        private int connectLoaderState = STATE_NONE;
        private static int STATE_NONE = 0;
        private static int STATE_RUNNING = 1;

        private static int button_source;
        private static BluetoothDevice connectedDevice;

        public PlaceholderFragment() {
        }

        @Override
        public void onSaveInstanceState(Bundle bundle) {
            super.onSaveInstanceState(bundle);

            if (devicesList != null)
                bundle.putStringArrayList(bundleDeviceList, devicesList);
            if (prev != null)
                bundle.putString(prevDeviceToAttemptToConnect, prev.getAddress());
            bundle.putInt(LOADER_STATE, connectLoaderState);
        }



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);

            if (savedInstanceState != null) {
                devicesList = savedInstanceState.getStringArrayList(bundleDeviceList);
                devicesNumber = devicesList.size();
                devicesSet = new HashSet<>(devicesList);

                String prevTemp = savedInstanceState.getString(prevDeviceToAttemptToConnect);
                if (prevTemp != null)
                    prev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(prevTemp);

                connectLoaderState = savedInstanceState.getInt(LOADER_STATE);
            }
        }

        @Override
        public void onResume(){
            Log.v(LOG_TAG, "onResume");
            super.onResume();
            if (connectLoaderState!=STATE_NONE){
                Log.v(LOG_TAG, "if statement");
                getLoaderManager().initLoader(CONNECT_LOADER, null, connectLoader );
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_local_game_screen, container, false);
            //call findViewById for the join and spectate buttons and assign them their respective onClickListeners
            listView = (ListView) rootView.findViewById(R.id.pairedDevicesList);




            devicesAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1,
                    devicesList);

            listView.setAdapter(devicesAdapter);

            //Sets the current selectedView to transparent color when scrolling,
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    //Sets the current selectedView to transparent color when scrolling,
                    //otherwise it might go off the visible Views and mess things up
                    // onScrollStateChanged was selected over onScroll because it fires less times.
                    if (selectedView != null) {
                        selectedView.setBackgroundColor(getResources().getColor(R.color.transparent));
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });

            //Highlights the selected MAC and stores it in selectedMac variable
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Set the previous selectedView to transparent color
                    if (selectedView != null) {
                        selectedView.setBackgroundColor(getResources().getColor(R.color.transparent));
                    }

                    //Set the current selectedView to the selected color
                    selectedView = view;
                    selectedView.setBackgroundColor(getResources().getColor(R.color.red));

                    //Extract MAC address
                    selectedMAC = devicesAdapter.getItem(position);
                    selectedMAC = selectedMAC.substring(selectedMAC.indexOf("\n") + 1, selectedMAC.length());
                }
            });

            spectate_button = (Button) rootView.findViewById(R.id.spectate_button);
            join_button = (Button) rootView.findViewById(R.id.join_button);
            host_button = (Button) rootView.findViewById(R.id.hostgame_button);
            refresh_button = (Button) rootView.findViewById(R.id.refresh_button);


            spectate_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connect(selectedMAC, SOURCE_BUTTON_SPECTATE);
                }
            });
            join_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connect(selectedMAC, SOURCE_BUTTON_JOIN);
                }
            });
            host_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    host();
                }
            });
            refresh_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refresh();
                }
            });

            return rootView;
        }

        public void refresh(){
            ApplicationHelper.getInstance().prepareNewGame();
        }

        //The previous BluetoothDevice that we tried to connect to.
        private BluetoothDevice prev = null;
        private final static String prevDeviceToAttemptToConnect = "prevdevicetojoin";

        /**
         * Creates a {@link grioanpier.auth.users.movies.bluetooth.ConnectTaskLoader} to try and connect to the specified device.
         * If a connection is established, it calls the respective method for the supplied {source}
         * @param MACaddress the MAC Address of the target device.
         * @param source Either {SOURCE_BUTTON_JOIN} or {SOURCE_BUTTON_SPECTATE}
         */
        private void connect(String MACaddress, final int source) {
            if (MACaddress == null)
                return;

            String hostAddress = ApplicationHelper.getInstance().getHostAddress();
            if (hostAddress==null)
                Log.v(LOG_TAG, "hostAddress was null");
            else if (MACaddress.equals(hostAddress)){
                Toast.makeText(getActivity(), "Already connected", Toast.LENGTH_SHORT).show();
                switch (source){
                    case SOURCE_BUTTON_JOIN:
                        join();
                        return;
                    case SOURCE_BUTTON_SPECTATE:
                        spectate();
                        return;
                }
            }else{
                System.out.println(MACaddress + " VS " + hostAddress);
            }

            final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MACaddress);


            if (prev == null) {
                prev = device;
            } else if (!prev.getAddress().equals(device.getAddress())) {
                getLoaderManager().destroyLoader(CONNECT_LOADER);
                prev = device;
            }

            connectLoaderState = STATE_RUNNING;
            button_source = source;
            connectedDevice = device;
            getLoaderManager().initLoader(CONNECT_LOADER, null, connectLoader );

        }



        LoaderManager.LoaderCallbacks<BluetoothSocket> connectLoader = new LoaderManager.LoaderCallbacks<BluetoothSocket>() {
            @Override
            public Loader<BluetoothSocket> onCreateLoader(int id, Bundle args) {
                Log.v(LOG_TAG + " " + ConnectTaskLoader.class.getSimpleName(), "onCreateLoader");
                return new ConnectTaskLoader(getActivity(), connectedDevice, Constants.sUUIDs);
            }

            @Override
            //Attempts to connect to the device. If successful, calls spectate(), join() respectively
            public void onLoadFinished(Loader<BluetoothSocket> loader, BluetoothSocket btSocket) {
                connectLoaderState = STATE_NONE;
                Log.v(LOG_TAG + " " + ConnectTaskLoader.class.getSimpleName(), "onLoadFinished");
                if (btSocket != null) {
                    String name = btSocket.getRemoteDevice().getName();
                    Toast.makeText(getActivity(), "Connected to " + name, Toast.LENGTH_LONG).show();
                    ApplicationHelper.getInstance().setHostSocket(btSocket);
                    switch (button_source){
                        case SOURCE_BUTTON_JOIN:
                            join();
                            break;
                        case SOURCE_BUTTON_SPECTATE:
                            spectate();
                            break;
                    }
                } else {
                    Toast.makeText(getActivity(), "Couldn't connect to the specified device", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoaderReset(Loader<BluetoothSocket> loader) {
                Log.v(LOG_TAG + " " + ConnectTaskLoader.class.getSimpleName(), "onLoaderReset");
            }
        };


        private void spectate() {
            Intent intent = new Intent(getActivity(), WaitingScreen.class);
            ApplicationHelper.getInstance().isHost=false;
            intent.putExtra(Constants.DEVICE_TYPE, Constants.DEVICE_SPECTATOR);
            startActivity(intent);
        }

        private void join() {
            Intent intent = new Intent(getActivity(), WaitingScreen.class);
            ApplicationHelper.getInstance().isHost=false;
            intent.putExtra(Constants.DEVICE_TYPE, Constants.DEVICE_PLAYER);
            startActivity(intent);
        }

        private void host() {
            cancelDiscovery();
            //Since the user wants to become a host, clear the host socket, if there is one.

            Intent intent = new Intent(getActivity(), WaitingScreen.class);
            ApplicationHelper.getInstance().isHost=true;
            intent.putExtra(Constants.DEVICE_TYPE, Constants.DEVICE_HOST);
            startActivity(intent);
        }

        private void cancelDiscovery() {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        }

        public void setDevicesList(Set<BluetoothDevice> list) {
            String string;
            for (BluetoothDevice device : list) {
                string = device.getName() + "\n" + device.getAddress();
                if (devicesSet.add(string)) {
                    devicesAdapter.add(string);
                    devicesNumber++;
                }
            }
        }

        public void addDeviceInList(BluetoothDevice device) {
            String string = device.getName() + "\n" + device.getAddress();
            if (devicesSet.add(string)) {
                devicesAdapter.add(string);
                devicesNumber++;
            }
        }

    }
}
