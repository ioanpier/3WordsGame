package grioanpier.auth.users.movies;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class LocalGame extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_game);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        //TODO add a broadcast receiver that will hear for Bluetooth enabled device and start getDevices and startDiscovery

        private static final String LOG_TAG = PlaceholderFragment.class.getSimpleName();

        static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        private BluetoothSocket mBtSocket = null;
        BluetoothDevice GM = null;

        //Possible values for resultCode that is passed on the onActivityResult
        private final static int RESULT_CANCELED = Activity.RESULT_CANCELED;
        private final static int RESULT_OK = Activity.RESULT_OK;
        private final static String ACTION_FOUND = BluetoothDevice.ACTION_FOUND;
        private final static String ACTION_DISCOVERY_STARTED = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
        private final static String ACTION_DISCOVERY_FINISHED = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;


        //Locally defined integer that the system passes back to me in the onActivityResult() implementation as the requestCode parameter.
        private final static int REQUEST_ENABLE_BLUETOOTH = 1;

        private ListView listView;
        private Button spectate_button;
        private Button join_button;
        private Button host_button;

        private View selectedView;
        private String selectedMAC;

        ArrayAdapter<String> deviceAdapter = null;
        final ArrayList<String> list = new ArrayList();

        // Create a BroadcastReceiver for ACTION_FOUND
        private BroadcastReceiver mReceiver = null;
        //UUID was acquired from UUID.randomUUID() once and is now hardcoded
        //bluetooth client and server must use the same UUID
        UUID mUuid = UUID.fromString("32276490-f1ce-4c7d-a7ef-01eb87424ecb");
        String mUuidString = "32276490-f1ce-4c7d-a7ef-01eb87424ecb";

        private void initBroadcastReceiver() {
            mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    receivedBroadcast(intent);
                }
            };
        }


        private void receivedBroadcast(Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG, "receivedBroadcast for Bluetooth!");
            switch (action){
                case ACTION_DISCOVERY_STARTED:
                    Log.v(LOG_TAG, "Discovery Started");
                    break;
                // When discovery finds a device
                case ACTION_DISCOVERY_FINISHED:
                    Log.v(LOG_TAG, "Discovery Finished and Cancelled");
                    //if (mBluetoothAdapter!=null)
                    // mBluetoothAdapter.cancelDiscovery();
                    break;
                case ACTION_FOUND:
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device==null)
                        System.out.println("bluetooth was null!");
                    else{
                        // Add the name and address to an array adapter to show in a ListView
                        System.out.println("found one!");
                        System.out.println(device.getName());
                        list.add(device.getName() + "\n" + device.getAddress());
                        deviceAdapter.notifyDataSetChanged();
                        //mBluetoothAdapter.cancelDiscovery();

                    }
                    break;

            }
        }

        public PlaceholderFragment() {
        }

        public void onStop(){
            super.onStop();
            if (mReceiver != null) {
                getActivity().unregisterReceiver(mReceiver);
                mReceiver = null;
            }

            if (mBluetoothAdapter!=null){
                mBluetoothAdapter.cancelDiscovery();
            }

            if (mBtSocket !=null){
                try {
                    mBtSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.local_game_screen, container, false);
            //call findViewById for the join and spectate buttons and assign them their respective onClickListeners
            listView = (ListView) rootView.findViewById(R.id.pairedDevicesList);
            deviceAdapter = new ArrayAdapter(getActivity(),
                    android.R.layout.simple_list_item_1,
                    list);
            listView.setAdapter(deviceAdapter);

            Log.v(LOG_TAG, mUuid.toString());

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
                    selectedMAC = deviceAdapter.getItem(position);
                    selectedMAC = selectedMAC.substring(selectedMAC.indexOf("\n") + 1, selectedMAC.length());
                }
            });

            spectate_button = (Button) rootView.findViewById(R.id.spectate_button);
            join_button = (Button) rootView.findViewById(R.id.join_button);
            host_button = (Button) rootView.findViewById(R.id.hostgame_button);


            spectate_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {spectate(selectedMAC);
                }
            });
            join_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    join(selectedMAC);
                }
            });
            host_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    host();
                }
            });

            //Request that Bluetooth is enabled and call getDevices()
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            }
            else {
                //getDevices is also called inside the enableBtIntent if the user clicked enable
                getDevices();
            }


            return rootView;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
                //The system requested to enable the bluetooth
                switch (resultCode) {
                    case RESULT_OK:
                        getDevices();
                        break;
                    case RESULT_CANCELED:
                        //AlertDialog that informs the player that bluetooth is necessary and requests to enable it.
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Bluetooth is needed in order to connect locally with your friends. Please turn on the Bluetooth")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        break;
                    default:
                        Log.v(LOG_TAG, "other onActivityResult with code: " + resultCode);
                        break;
                }
            } else {
                Log.v(LOG_TAG, "other requestCode " + requestCode);
            }

        }

        //Gets the
        public void getDevices() {
            //getPairedDevices();
            getAvailableDevices();
        }

        public void getPairedDevices(){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                System.out.println("pairedDevices.size()>0");
                for (BluetoothDevice device : pairedDevices) {
                    list.add(device.getName() + "\n" + device.getAddress());
                }
                deviceAdapter.notifyDataSetChanged();

            } else {
                //If no paired devices found
                //do nothing
            }
        }


        public void getAvailableDevices() {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            initBroadcastReceiver();
            getActivity().registerReceiver(mReceiver, filter);
            mBluetoothAdapter.startDiscovery();
        }

        private void spectate(String MACaddress) {
            new AlertDialog.Builder(getActivity())
                    .setMessage("Bluetooth is needed in order to connect locally with your friends. Please turn on the Bluetooth")
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing

                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


        }

        private void join(String MACaddress) {
            if (MACaddress==null)
                return;
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MACaddress);
            if (device==null){
                Log.v(LOG_TAG, "couldn't get RemoteDevice");
                return;
            }
            else {
                Log.v(LOG_TAG, "Got RemoteDevice");
            }

            try {
                mBtSocket = device.createRfcommSocketToServiceRecord(mUuid);
                mBluetoothAdapter.cancelDiscovery();
                mBtSocket.connect();
            } catch (IOException e) {
                Log.v(LOG_TAG, e.getMessage());
            }

        }

        private void host(){
            Intent makeDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            makeDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(makeDiscoverable);

            try {
                BluetoothServerSocket btServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mUuidString, mUuid);
                if (btServerSocket!=null){
                    BluetoothSocket btSocket = btServerSocket.accept();
                    if (btSocket!=null)
                    {
                        Log.v(LOG_TAG, "got the btSocket!");
                        return;
                    }
                    Log.v(LOG_TAG, "btSocket was null!");
                }
            } catch (IOException e) {
                Log.v(LOG_TAG, "host button crashed!");
                Log.v(LOG_TAG, e.getMessage());
            }


        }


    }
}
