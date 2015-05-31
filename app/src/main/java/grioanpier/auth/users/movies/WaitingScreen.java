package grioanpier.auth.users.movies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

import grioanpier.auth.users.movies.utility.ApplicationHelper;
import grioanpier.auth.users.movies.utility.BluetoothManager;
import grioanpier.auth.users.movies.utility.Constants;
import grioanpier.auth.users.movies.utility.SplitView;


public class WaitingScreen extends ActionBarActivity implements WaitingScreenFragment.StartGameButtonClicked {

    private static final String LOG_TAG = WaitingScreen.class.getSimpleName();

    private static int deviceType;

    private static WaitingScreenFragment waitingScreenFragment;
    private BluetoothChatFragment bluetoothChatFragment;
    private static PlayFragment playFragment;
    private BluetoothManager btManager;
    private static final String sBluetoothManagerFragmentTag = "bluetoothmanager";
    private static boolean mTwoPane = false;
    private static SplitView splitView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_screen);

        deviceType = ApplicationHelper.getInstance().DEVICE_TYPE;
        //Those two fragments have been statically added inside the activity's xml!
        bluetoothChatFragment = (BluetoothChatFragment) getSupportFragmentManager().findFragmentById(R.id.chat_fragment);
        waitingScreenFragment = (WaitingScreenFragment) getSupportFragmentManager().findFragmentById(R.id.waiting_screen_fragment);
        waitingScreenFragment.setUserVisibleHint(false);


        if (findViewById(R.id.handle) != null) {
            mTwoPane = true;
            splitView = ((SplitView) findViewById(R.id.split_view));
            playFragment = (PlayFragment) getSupportFragmentManager().findFragmentById(R.id.play_fragment);
        } else {
            mTwoPane = false;
        }

        if (savedInstanceState == null) {
            btManager = new BluetoothManager();
            getSupportFragmentManager().beginTransaction()
                    .add(btManager, sBluetoothManagerFragmentTag)
                    .commit();

            ApplicationHelper.twoPane = mTwoPane;

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
                        if (!enabled) {
                            Intent intent = new Intent(getApplicationContext(), StartingScreen.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onEnabled() {
                        if (!ApplicationHelper.getInstance().GAME_HAS_STARTED) {
                            //Start listening for incoming connections as soon as the bluetooth is enabled if the game hasn't started
                            serverListenForConnections();
                        }
                    }
                });
                btManager.ensureEnabled();

                //TODO Ask once to make the device discoverable when the activity starts for the first time. Include "make discoverable" in options menu
                //Prompt the user to make the device discoverable
                btManager.setBluetoothRequestDiscoverableListener(new BluetoothManager.BluetoothRequestDiscoverableListener() {
                    @Override
                    public void onResult(boolean enabled) {
                        if (!enabled)
                            Toast.makeText(getApplicationContext(), "Non-paired devices won't be able to find you", Toast.LENGTH_SHORT).show();
                    }
                });
                if (!ApplicationHelper.getInstance().GAME_HAS_STARTED)
                    btManager.ensureDiscoverable();
                break;
            }
        }

        if(mTwoPane && ApplicationHelper.getInstance().GAME_HAS_STARTED){
            getSupportFragmentManager().beginTransaction().hide(waitingScreenFragment).commit();
            splitView.maximizeSecondaryContent();
        }

        ApplicationHelper.getInstance().setActivityHandler(mHandler);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ApplicationHelper.getInstance().unregisterActivityHandler();
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

    public void serverListenForConnections() {
        btManager.setServerListenForConnectionsListener(new BluetoothManager.ServerListenForConnectionsListener() {
            @Override
            public void onConnectionEstablished(boolean established, String name) {
                if (established) {
                    Log.v(LOG_TAG, "ConnectionEstablished! with: " + name);
                    Toast.makeText(getApplicationContext(), "Connected with " + name, Toast.LENGTH_SHORT).show();
                    ApplicationHelper.removeNextAvailableUUID();
                    waitingScreenFragment.playersJoinedIncrement();
                } else {
                    Toast.makeText(getApplicationContext(), "Connection NOT Established!", Toast.LENGTH_SHORT).show();
                    Log.v(LOG_TAG, "Connection NOT Established!");
                }

                UUID uuid = ApplicationHelper.getNextAvailableUUID();
                if (uuid != null) {
                    btManager.prepareServerListenForConnections();
                    btManager.serverListenForConnections(uuid);
                }
            }
        });

        UUID uuid = ApplicationHelper.getNextAvailableUUID();
        if (uuid != null)
            btManager.serverListenForConnections(uuid);
    }


    @Override
    public void onStartGameButtonClicked() {

        if (!ApplicationHelper.getInstance().GAME_HAS_STARTED) {
            //When the game starts, assign a random name to the story. Use a randomUUID :D
            ApplicationHelper.getInstance().prepareNewStory();
            String randomUUID = UUID.randomUUID().toString();
            Log.v(LOG_TAG, "randomUUID headStory is " + randomUUID);
            ApplicationHelper.getInstance().write(String.valueOf(ApplicationHelper.START_GAME) + randomUUID, ApplicationHelper.ACTIVITY_CODE);
        } else {
            Intent intent = new Intent(this, Play.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }

    }


    private ActivityHandler mHandler = new ActivityHandler(this);

    public static class ActivityHandler extends Handler {

        static Context mContext;

        protected ActivityHandler(Context context) {
            super();
            mContext = context;
        }

        @Override
        public synchronized void handleMessage(Message msg) {
            switch (msg.what) {
                case ApplicationHelper.PLAYER_CONNECTED:
                    if (deviceType != Constants.DEVICE_HOST)
                        waitingScreenFragment.playersJoinedIncrement();
                    break;
                case ApplicationHelper.PLAYER_DISCONNECTED:
                    Toast.makeText(mContext, msg.obj + " disconnected", Toast.LENGTH_SHORT).show();
                    waitingScreenFragment.playersJoinedDecrement();
                    break;
                case ApplicationHelper.ACTIVITY_CODE:
                    String message = (String) msg.obj;
                    Log.v(LOG_TAG, "ACTIVITY_CODE case: " + message);
                    int mySwitch = message.charAt(0) - 48;
                    switch (mySwitch) {
                        //Receive the code to start the game
                        case ApplicationHelper.START_GAME:
                            //Initialize the Story Head
                            ApplicationHelper.STORY_HEAD = message.substring(1, message.length());
                            Log.v(LOG_TAG, "STORY HEAD IS " + ApplicationHelper.STORY_HEAD);
                            ApplicationHelper.getInstance().prepareNewStory();

                            if (!mTwoPane) {
                                //Start the Play activity
                                Intent intent = new Intent(mContext, Play.class);
                                mContext.startActivity(intent);
                            } else {

                                ((WaitingScreen) mContext).getSupportFragmentManager().beginTransaction()
                                        .hide(waitingScreenFragment)
                                        .commit();

                                splitView.resetToMiddle();

                                //The adapter inside the playfragment needs to be reset to the arraylist iof the story
                                playFragment.gameHasStarted();
                            }

                            break;
                        default:
                            Log.v(LOG_TAG, "other");
                            Log.v(LOG_TAG, "other, the switch is " + mySwitch);
                            break;

                    }
                    break;
                default:
                    Log.v(LOG_TAG, "switch: " + msg.what);
            }
        }
    }


}//Activity
