package grioanpier.auth.users.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

import grioanpier.auth.users.movies.utility.ApplicationHelper;
import grioanpier.auth.users.movies.utility.BluetoothManager;
import grioanpier.auth.users.movies.utility.Constants;


public class WaitingScreen extends ActionBarActivity implements WaitingScreenFragment.StartGameButtonClicked {

    private static final String LOG_TAG = WaitingScreen.class.getSimpleName();

    private int deviceType;

    private WaitingScreenFragment waitingScreenFragment;
    private BluetoothChatFragment bluetoothChatFragment;
    private BluetoothManager btManager;
    private static final String sBluetoothManagerFragmentTag = "bluetoothmanager";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_screen);

        deviceType = getIntent().getIntExtra(Constants.DEVICE_TYPE, Constants.DEVICE_SPECTATOR);

        //Those two fragments have been statically added inside the activity's xml!
        bluetoothChatFragment = (BluetoothChatFragment) getSupportFragmentManager().findFragmentById(R.id.chat_fragment);
        waitingScreenFragment = (WaitingScreenFragment) getSupportFragmentManager().findFragmentById(R.id.waiting_screen_fragment);

        if (savedInstanceState == null) {
            btManager = new BluetoothManager();
            getSupportFragmentManager().beginTransaction()
                    .add(btManager, sBluetoothManagerFragmentTag)
                    .commit();
        } else {
            btManager = (BluetoothManager) getSupportFragmentManager().findFragmentByTag(sBluetoothManagerFragmentTag);
        }


    }



    @Override
    public void onStart() {
        super.onStart();
        switch (deviceType) {

            case Constants.DEVICE_HOST: {
                Log.v(LOG_TAG, "device host");

                //Make sure the Bluetooth is enabled. When it is, start listening for incoming connections.
                btManager.setBluetoothRequestEnableListener(new BluetoothManager.BluetoothRequestEnableListener() {
                    @Override
                    public void onResult(boolean enabled) {
                        if (!enabled){
                            Intent intent  = new Intent(getApplicationContext(), StartingScreen.class);
                            startActivity(intent);
                        }
                    }
                    @Override
                    public void onEnabled() {
                        //Start listening for incoming connections as soon as the bluetooth is enabled.
                        serverListenForConnections();
                    }
                });
                btManager.ensureEnabled();

                //Prompt the user to make the device discoverable
                btManager.setBluetoothRequestDiscoverableListener(new BluetoothManager.BluetoothRequestDiscoverableListener() {
                    @Override
                    public void onResult(boolean enabled) {
                        if (!enabled)
                            Toast.makeText(getApplicationContext(), "Non-paired devices won't be able to find you", Toast.LENGTH_SHORT).show();
                    }
                });
                btManager.ensureDiscoverable();
                break;
            }
            case Constants.DEVICE_PLAYER:
                Log.v(LOG_TAG, "device player");
                break;
            case Constants.DEVICE_SPECTATOR:
                //TODO implement me
                //TODO I need to let the host know what that the device is a spectator. I can accomplish this by sending a first message with the type.
                Toast.makeText(getApplicationContext(),"DEVICE_SPECTATOR CASE HASN'T BEEN IMPLEMENTED YET", Toast.LENGTH_LONG).show();
                Log.v(LOG_TAG, "DEVICE_SPECTATOR CASE HASN'T BEEN IMPLEMENTED YET");
                this.finish();
                break;
            default:
                //TODO delete this when done with the app
                Log.v(LOG_TAG, "deviceType was wrong!");
                Toast.makeText(getApplicationContext(), "deviceType was wrong!", Toast.LENGTH_LONG).show();
                this.finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_waiting_screen, menu);
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

    public void serverListenForConnections(){
        btManager.setServerListenForConnectionsListener(new BluetoothManager.ServerListenForConnectionsListener() {
            @Override
            public void onConnectionEstablished(boolean established, String name) {
                if (established){
                    Log.v(LOG_TAG, "ConnectionEstablished! with: " + name);
                    Toast.makeText(getApplicationContext(), "Connected with " + name, Toast.LENGTH_SHORT).show();
                    ApplicationHelper.removeNextAvailableUUID();
                    waitingScreenFragment.playersJoinedIncrement();
                }else{
                    Toast.makeText(getApplicationContext(), "Connection NOT Established!", Toast.LENGTH_SHORT).show();
                    Log.v(LOG_TAG, "Connection NOT Established!");
                }

                UUID uuid = ApplicationHelper.getNextAvailableUUID();
                if (uuid!=null){
                    btManager.prepareServerListenForConnections();
                    btManager.serverListenForConnections(uuid);
                }
            }
        });

        UUID uuid = ApplicationHelper.getNextAvailableUUID();
        if (uuid!=null)
            btManager.serverListenForConnections(uuid);
    }


    @Override
    public void onStartGameButtonClicked() {
                Log.v(LOG_TAG, "start game button clicked");
    }



}//Activity
