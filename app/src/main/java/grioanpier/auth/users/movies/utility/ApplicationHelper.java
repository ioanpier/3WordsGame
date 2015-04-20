package grioanpier.auth.users.movies.utility;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;
import java.util.UUID;

import grioanpier.auth.users.movies.StartingScreen;
import grioanpier.auth.users.movies.bluetooth.ConnectedThread;

/**
 * Created by Ioannis on 13/4/2015.
 */
public class ApplicationHelper extends Application {
    public static final ArrayList<UUID> sAvailableUUIDs = new ArrayList<>(Arrays.asList(Constants.sUUIDs));
    private static ApplicationHelper singleton;
    private final static String LOG_TAG = ApplicationHelper.class.getSimpleName();
    private static ApplicationHandler applicationHandler;
    private final static String DEVICE_NAME;
    public boolean isHost = false;
    public boolean GAME_HAS_STARTED = false;
    private int whoIsPlaying;
    public static boolean myTurn = false;
    public static boolean firstTurn = false;
    public static String STORY_HEAD;

    //for the debug button
    public int getWhoIsPlaying() {
        return whoIsPlaying;
    }

    static {
        DEVICE_NAME = BluetoothAdapter.getDefaultAdapter().getName();
    }

    public static ApplicationHelper getInstance() {
        return singleton;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setIsHost(boolean flag) {
        isHost = flag;
    }

    public static UUID getNextAvailableUUID() {
        if (sAvailableUUIDs.isEmpty())
            return null;
        else
            return sAvailableUUIDs.get(0);
    }

    /**
     * @return true if there was something to remove, false otherwise.
     */
    public static boolean removeNextAvailableUUID() {
        if (sAvailableUUIDs.isEmpty())
            return false;
        else {
            sAvailableUUIDs.remove(0);
            return true;
        }
    }

    //Clears everything.
    public void prepareNewGame() {
        if (sAvailableUUIDs.size() == Constants.sUUIDs.length)
            return;
        sAvailableUUIDs.clear();
        sAvailableUUIDs.ensureCapacity(10);
        sAvailableUUIDs.addAll(Arrays.asList(Constants.sUUIDs));

        debug_clearConnectedThreads();
        debug_closePlayerSockets();
        debug_closeHostSocket();

        isHost = false;
        GAME_HAS_STARTED = false;
        whoIsPlaying = -1;
        myTurn = false;


    }

    //Prepares what is needed for the new Story.
    public void prepareNewStory() {
        Log.v(LOG_TAG, "prepareNewStory");
        //The host is positioned at -1
        whoIsPlaying = -1;
    }

    private int getNextPlayer() {

        whoIsPlaying++;
        if (whoIsPlaying == connectedThreads.size())
            whoIsPlaying = -1;

        Log.v(LOG_TAG, "getNextPlayer=" + whoIsPlaying);
        return whoIsPlaying;
    }

    public void notifyNextPlayer() {
        int next = getNextPlayer();
        write(String.valueOf(YOUR_TURN), ACTIVITY_CODE, next);

    }

    /**
     * Called when a player leaves and ensures the correct player is up next.
     */
    public void ensureGameIsPlaying() {
        //TODO do this correctly.
        //though this should work. If the player who left was lower than the player playing now, it will tell the same player to play.
        //If the player who left was also his turn,
        whoIsPlaying--;
        notifyNextPlayer();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        applicationHandler = new ApplicationHandler(getApplicationContext());
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.v(LOG_TAG, "onTerminate");
        Log.e(LOG_TAG, "onTerminate");
        //TODO implement me!
        //note I need to re-establish the connections if the user hits the home button and afterwards returns to the application.
    }

    /**
     * The connected {@link BluetoothSocket} for every player. This is used by the host-player.
     */
    private static TreeMap<Integer, BluetoothSocket> playerSockets = new TreeMap<>();
    /**
     * The connected {@link BluetoothSocket} for the host. This is used by the non-host-players.
     */
    private static BluetoothSocket hostSocket;

    public void addPlayerSocket(BluetoothSocket btSocket) {


        ConnectedThread thread = new ConnectedThread(btSocket, applicationHandler);
        thread.start();
        connectedThreads.put(thread.ID, thread);
        playerSockets.put(thread.ID, btSocket);
    }

    public Collection<BluetoothSocket> getPlayerSockets() {
        return playerSockets.values();
    }

    public void setHostSocket(BluetoothSocket btSocket) {
        hostSocket = btSocket;
        ConnectedThread thread = new ConnectedThread(btSocket, applicationHandler);
        thread.start();
        connectedThreads.put(thread.ID, thread);
    }

    public BluetoothSocket getHostSocket() {
        return hostSocket;
    }

    public String getHostAddress() {
        if (hostSocket == null)
            return null;
        else
            return hostSocket.getRemoteDevice().getAddress();
    }

    public void debug_closePlayerSockets() {
        Log.v(LOG_TAG, "debug_closePlayerSockets");
        for (BluetoothSocket socket : playerSockets.values()) {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        playerSockets = new TreeMap<>();

    }

    public void debug_closeHostSocket() {
        Log.v(LOG_TAG, "debug_closeHostSocket");
        try {
            if (hostSocket != null)
                hostSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            hostSocket = null;
        }
    }

    public void debug_clearConnectedThreads() {
        for (ConnectedThread thread : connectedThreads.values())
            thread.cancel();
        connectedThreads.clear();
    }

    private static final TreeMap<Integer, ConnectedThread> connectedThreads = new TreeMap<>();

    private static Handler chatHandler;

    public void setChatHandler(Handler handler) {
        chatHandler = handler;
    }

    public void unregisterChatHandler() {
        if (chatHandler != null)
            chatHandler = null;
    }

    private static Handler storyHandler;

    public void setStoryHandler(Handler handler) {
        storyHandler = handler;
    }

    public void unregisterStoryHandler() {
        if (storyHandler != null)
            storyHandler = null;
    }

    private static Handler activityHandler;

    public void setActivityHandler(Handler handler) {
        activityHandler = handler;
    }

    public void unregisterActivityHandler() {
        if (activityHandler != null)
            activityHandler = null;
    }


    /**
     * Synchronization lock to be used by the write methods in order to avoid possible messages that are sent the same time
     * and things mess up.
     */
    private final static Object Write_Lock = new Object();

    /**
     * When the user clicks "send", this method is invoked.
     *
     * @param message the message to send.
     * @param source  the source of the message. It can be {CHAT} or {STORY}
     */
    public synchronized void write(String message, int source) {

        synchronized (Write_Lock) {
            //Format the message.
            //This is okay because my constants are in the range of 1-127
            StringBuilder builder = new StringBuilder();


            switch (source) {
                case CHAT:
                    Log.v(LOG_TAG, "CHAT");
                    //builder.append(String.valueOf(HANDLER_CHAT_WRITE));
                    builder.append(HANDLER_CHAT_WRITE);
                    //Add the device's name to the message.
                    if (DEVICE_NAME.length() < 10) {
                        builder.append(0);
                    }
                    builder.append(DEVICE_NAME.length())
                            .append(DEVICE_NAME);
                    break;
                case STORY:
                    Log.v(LOG_TAG, "STORY");
                    builder.append(HANDLER_STORY_WRITE);

                    break;
                case ACTIVITY_CODE:
                    builder.append(ACTIVITY_CODE);
                    break;
            }

            builder.append(message);

            Log.v(LOG_TAG + " write to all threads", "message to be sent: " + builder.toString());
            Toast.makeText(getApplicationContext(), "write to all threads", Toast.LENGTH_SHORT).show();
            byte[] buffer = builder.toString().getBytes();

            //Send the message to every connectedThread as well as to yourself.
            for (ConnectedThread thread : connectedThreads.values())
                thread.write(buffer);
            applicationHandler.obtainMessage(THREAD_READ, buffer.length, -1, buffer).sendToTarget();
        }

    }

    public void write(String message, int source, int threadPos) {
        synchronized (Write_Lock) {
            StringBuilder builder = new StringBuilder();


            switch (source) {
                case ACTIVITY_CODE:
                    builder.append(ACTIVITY_CODE);
                    break;
            }

            builder.append(message);

            Log.v(LOG_TAG + " write to single thread", "message to be sent: " + builder.toString());
            Toast.makeText(getApplicationContext(), "write to single threads", Toast.LENGTH_SHORT).show();
            byte[] buffer = builder.toString().getBytes();

            //It's the host turn. This method is only called by the host, therefor send it directly to the host's Handler
            if (threadPos == -1) {
                Log.v(LOG_TAG, "Notify the host (yourself)");
                applicationHandler.obtainMessage(THREAD_READ, buffer.length, -1, buffer).sendToTarget();
                return;
            }


            Collection<ConnectedThread> col = connectedThreads.values();
            if (!col.isEmpty()) {
                ConnectedThread thread = (ConnectedThread) col.toArray()[threadPos];
                if (thread != null) {
                    Log.v(LOG_TAG, "Notify a player (someone else)");
                    thread.write(buffer);
                } else {
                    Log.v(LOG_TAG, "Thread was null!");
                }
            } else {
                Log.v(LOG_TAG, "Collection was empty!");
            }
        }
    }

    public static class ApplicationHandler extends Handler {

        private Context mContext;

        public ApplicationHandler(Context context) {
            super();
            mContext = context;
        }

        @Override
        public synchronized void handleMessage(Message msg) {
            /*TODO Store the various Handlers inside an ArrayList.
            *  Every time a message is received, it will be forwarded to every Handler.
            *  The Handlers themselves will decide if the message is for them or not.
            */

            //Log.v(LOG_TAG, new String((byte[]) msg.obj, 0, msg.arg1));
            switch (msg.what) {
                case THREAD_READ:
                    int numOfBytes = msg.arg1;
                    StringBuilder builder = new StringBuilder(new String((byte[]) msg.obj, 0, numOfBytes));
                    int messageType = builder.charAt(0) - 48;
                    String message;
                    switch (messageType) {
                        case HANDLER_CHAT_WRITE:
                            //The length is saved in the 2nd and 3rd byte.
                            //Reconstruct it: 56 = 5*10 + 6
                            int nameLength = (builder.charAt(1) - 48) * 10 + (builder.charAt(2) - 48);
                            String deviceName;


                            deviceName = builder.substring(3, nameLength + 3);
                            message = builder.substring(nameLength + 3, builder.length());


                            Log.v(LOG_TAG, "number of bytes: " + numOfBytes);
                            Log.v(LOG_TAG, "messageType: " + messageType);
                            Log.v(LOG_TAG, "deviceName length: " + nameLength);
                            Log.v(LOG_TAG, "deviceName: " + deviceName);
                            Log.v(LOG_TAG, "message: " + message);
                            if (chatHandler == null) {
                                Log.v(LOG_TAG, "chat handler was null!");
                                return;
                            }

                            int code;
                            if (deviceName.equals(DEVICE_NAME)) {
                                code = MESSAGE_ME;
                                chatHandler.obtainMessage(code, "You: " + message).sendToTarget();
                            } else {
                                code = MESSAGE_OTHER;
                                chatHandler.obtainMessage(code, deviceName + ": " + message).sendToTarget();
                            }

                            Log.v(LOG_TAG, "sent to chatHandler");

                            break;


                        case HANDLER_STORY_WRITE:
                            message = builder.substring(1, builder.length());
                            storyHandler.obtainMessage(STORY, message).sendToTarget();
                            break;
                        case ACTIVITY_CODE:

                            Log.v(LOG_TAG, activityHandler.getClass().getCanonicalName());
                            if (activityHandler != null) {
                                activityHandler.obtainMessage(ACTIVITY_CODE, builder.substring(1, builder.length())).sendToTarget();
                            } else {
                                Log.v(LOG_TAG, "activityHandler was null");
                            }
                            break;
                        default:
                            Log.v(LOG_TAG, "default");
                            Log.v(LOG_TAG, "messageType: " + messageType);
                            Log.v(LOG_TAG, "message: " + builder.toString());
                            break;
                    }
                    break;
                case THREAD_DISCONNECTED:
                    //ConnectedThread calls ConnectedThread.cancel() internally which closes the streams and the socket.
                    //Remove it from the list as well. The thread returns its hash code in msg.arg1
                    connectedThreads.remove(msg.arg1);

                    //Also remove the socket from our list.
                    String who;
                    BluetoothSocket socket;
                    if (playerSockets.containsKey(msg.arg1)) {
                        Log.v(LOG_TAG, "A player has disconnected!");
                        //The user who left was a player. Simply remove him.
                        playerSockets.remove(msg.arg1);
                        who = (String) msg.obj;
                        //Check if it was the player's who left turn
                        //and recalculate the next player's turn;
                        ApplicationHelper.getInstance().ensureGameIsPlaying();

                    } else {
                        Log.v(LOG_TAG, "The host has disconnected!");
                        hostSocket = null;
                        who = "The host";

                        //Wrap up the game
                        //TODO show the full story in the a separate activity
                        ApplicationHelper.getInstance().GAME_HAS_STARTED = false;
                        ApplicationHelper.getInstance().prepareNewGame();

                        Intent intent = new Intent(mContext, StartingScreen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }

                    if (activityHandler == null) {
                        Log.v(LOG_TAG, "activityHandler was null!");
                        return;
                    }

                    //Inform the activity handler who disconnected.
                    activityHandler.obtainMessage(PLAYER_DISCONNECTED, who).sendToTarget();


                    break;
                case ConnectedThread.THREAD_STREAM_ERROR:
                    Toast.makeText(mContext, "Stream couldn't be retrieved", Toast.LENGTH_SHORT).show();
                    Log.v(LOG_TAG, "Stream couldn't be retrieved");
                    break;
                default:
                    Log.v(LOG_TAG, "default: " + msg.what);
                    break;
            }

        }

    }

    //Receive a message from a ConnectedThread.
    private static final int THREAD_READ = ConnectedThread.THREAD_READ;
    //Receive a message from a ConnectedThread that it has disconnected
    private static final int THREAD_DISCONNECTED = ConnectedThread.THREAD_DISCONNECTED;
    //Send a messsage to the bluetooth chat
    public static final int HANDLER_CHAT_WRITE = 2;
    //Write to the story
    public static final int HANDLER_STORY_WRITE = 3;
    //These are used by the activities for code messages.
    public static final int ACTIVITY_CODE = 4;
    public static final int YOUR_TURN = 5;


    //These are provided as int
    public static final int PLAYER_CONNECTED = 10;
    public static final int PLAYER_DISCONNECTED = 11;
    public static final int HOST_DISCONNECTED = 12;


    //Source codes for the write method.
    public static final int CHAT = 0;
    public static final int STORY = 1;
    public static final int STORY_INITIAL = 2;
    //Codes for the chat
    public static final int MESSAGE_ME = 2;
    public static final int MESSAGE_OTHER = 3;

    /*Codes for the activity handler*/
    public static final int START_GAME = 4; //The host has started the game. Next screen please!
    public static final int STORY_TURN = 5; //It's your turn to write 3 words to the story.
    public static final int PASS = 6; //The player has passed his turn. This is used by spectators.
}
