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
 * This is the Boss https://www.youtube.com/watch?v=NisCkxU544c
 */
public class ApplicationHelper extends Application {
    private static final ArrayList<UUID> sAvailableUUIDs = new ArrayList<>(Arrays.asList(Constants.sUUIDs));
    private static ApplicationHelper singleton;
    private final static String LOG_TAG = ApplicationHelper.class.getSimpleName();
    private static ApplicationHandler applicationHandler;
    public static String DEVICE_NAME = BluetoothAdapter.getDefaultAdapter().getName();
    public boolean isHost = false;
    public int DEVICE_TYPE;
    public boolean GAME_HAS_STARTED = false;
    private int whoIsPlaying;
    public static boolean myTurn = false;
    private static boolean firstTurn = false;
    public static String STORY_HEAD;
    public static boolean twoPane = false;

    public int getWhoIsPlaying() {
        return whoIsPlaying;
    }

    public static ApplicationHelper getInstance() {
        return singleton;
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
        sAvailableUUIDs.clear();
        sAvailableUUIDs.ensureCapacity(10);
        sAvailableUUIDs.addAll(Arrays.asList(Constants.sUUIDs));

        clearConnectedThreads();
        closePlayerSockets();
        closeHostSocket();

        isHost = false;
        GAME_HAS_STARTED = false;
        whoIsPlaying = -1;
        myTurn = false;
        DEVICE_TYPE = -1;

        story = new ArrayList<>();
        chat = new ArrayList<>();

    }

    //Prepares what is needed for the new Story.
    public void prepareNewStory() {
        //The host is positioned at -1
        whoIsPlaying = -1;
        GAME_HAS_STARTED = true;
        firstTurn = true;
        if (isHost)
            myTurn = true;
        story = new ArrayList<>();
    }

    private int getNextPlayer() {
        whoIsPlaying++;
        if (whoIsPlaying == connectedThreads.size())
            whoIsPlaying = -1;

        return whoIsPlaying;
    }

    public void notifyNextPlayer() {
        int next = getNextPlayer();
        write(String.valueOf(YOUR_TURN), STORY_CODE, next);
    }

    /**
     * Called when a player leaves and ensures the correct player is up next.
     */
    private void ensureGameIsPlaying() {
        //Notifying the next player increases the counter internally so it must be decreased first.
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

        Log.e(LOG_TAG, "onTerminate");
        prepareNewGame();
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

    public void closePlayerSockets() {
        for (BluetoothSocket socket : playerSockets.values()) {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        playerSockets.clear();

    }

    private void closeHostSocket() {
        try {
            if (hostSocket != null)
                hostSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            hostSocket = null;
        }
    }

    private void clearConnectedThreads() {
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

    public String getStory() {
        StringBuilder builder = new StringBuilder();
        for (String line : ApplicationHelper.getInstance().story) {
            builder.append(line).append(" ");
        }
        return builder.toString();
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

        introduceDelay(250);

        synchronized (Write_Lock) {
            //Format the message.
            StringBuilder builder = new StringBuilder();
            switch (source) {
                case CHAT:
                    builder.append(CHAT);
                    //Add the device's name to the message.
                    if (DEVICE_NAME.length() < 10) {
                        // 01,02,...09
                        builder.append(0);
                    }
                    builder.append(DEVICE_NAME.length())
                            .append(DEVICE_NAME);
                    break;
                case STORY:
                    builder.append(STORY);
                    break;
                case ACTIVITY_CODE:
                    builder.append(ACTIVITY_CODE);
                    break;
            }

            builder.append(message);
            byte[] buffer = builder.toString().getBytes();

            if (isHost)
                //The message is relayed, if needed, inside the obtainMessage method.
                applicationHandler.obtainMessage(THREAD_READ, buffer.length, -1, buffer).sendToTarget();
            else
                //This list only has a single thread.
                for (ConnectedThread thread : connectedThreads.values())
                    thread.write(buffer);
        }
    }

    private synchronized void relay(String message) {
        byte[] buffer = message.getBytes();
        introduceDelay(250);

        for (ConnectedThread thread : connectedThreads.values()) {
            thread.write(buffer);
        }
    }

    private void write(String message, int source, int threadPos) {
        synchronized (Write_Lock) {
            introduceDelay(250);
            StringBuilder builder = new StringBuilder();

            if (threadPos == -1)
                builder.append(HOST_ONLY);
            builder.append(source)
                    .append(message);

            byte[] buffer = builder.toString().getBytes();

            //It's the host turn. This method is only called by the host, therefor send it directly to the host's Handler
            if (threadPos == -1) {
                applicationHandler.obtainMessage(THREAD_READ, buffer.length, -1, buffer).sendToTarget();
                return;
            }


            Collection<ConnectedThread> col = connectedThreads.values();
            if (!col.isEmpty()) {
                ConnectedThread thread = (ConnectedThread) col.toArray()[threadPos];
                if (thread != null) {
                    thread.write(buffer);
                }
            }

        }//Write lock
    }

    public ArrayList<String> story = new ArrayList<>();
    public ArrayList<String> chat = new ArrayList<>();

    //Don't want messages to be relayed too fast in succession because they entangled.
    private void introduceDelay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {}
    }

    public static class ApplicationHandler extends Handler {

        private Context mContext;

        public ApplicationHandler(Context context) {
            super();
            mContext = context;
        }

        @Override
        public synchronized void handleMessage(Message msg) {
            switch (msg.what) {
                case THREAD_READ:
                    int numOfBytes = msg.arg1;
                    StringBuilder builder = new StringBuilder(new String((byte[]) msg.obj, 0, numOfBytes));
                    int messageType = builder.charAt(0) - 48;
                    String message;

                    if (ApplicationHelper.getInstance().isHost) {
                        if (messageType == HOST_ONLY) {
                            builder.deleteCharAt(0);
                            messageType = builder.charAt(0) - 48;
                        } else {
                            ApplicationHelper.getInstance().relay(builder.toString());
                        }
                    }

                    switch (messageType) {
                        case CHAT:
                            //The length is saved in the 2nd and 3rd byte.
                            //Reconstruct it: 56 = 5*10 + 6
                            int nameLength = (builder.charAt(1) - 48) * 10 + (builder.charAt(2) - 48);
                            String deviceName = builder.substring(3, nameLength + 3);
                            message = builder.substring(nameLength + 3, builder.length());

                            if (chatHandler != null) {
                                if (deviceName.equals(DEVICE_NAME)) {
                                    chatHandler.obtainMessage(MESSAGE_ME, "You: " + message).sendToTarget();
                                } else {
                                    chatHandler.obtainMessage(MESSAGE_OTHER, deviceName + ": " + message).sendToTarget();
                                }
                            }
                            break;
                        case STORY:
                            if (storyHandler != null)
                                storyHandler.obtainMessage(STORY, builder.substring(1, builder.length())).sendToTarget();
                            break;
                        case STORY_CODE:
                            if (storyHandler != null) //This will be null if the host has left the "Game" screen
                                storyHandler.obtainMessage(STORY_CODE, builder.substring(1, builder.length())).sendToTarget();
                            break;
                        case ACTIVITY_CODE:
                            if (activityHandler != null) {
                                activityHandler.obtainMessage(ACTIVITY_CODE, builder.substring(1, builder.length())).sendToTarget();
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case THREAD_DISCONNECTED:
                    //ConnectedThread calls ConnectedThread.cancel() internally which closes the streams and the socket.
                    //Remove it from the list as well. The thread returns its hash code in msg.arg1
                    connectedThreads.remove(msg.arg1);

                    //Also remove the socket from our list.
                    String who;
                    if (playerSockets.containsKey(msg.arg1)) {
                        //The user who left was a player. Simply remove him.
                        playerSockets.remove(msg.arg1);
                        who = (String) msg.obj;
                        //Check if it was the player's who left turn
                        //and recalculate the next player's turn;
                        ApplicationHelper.getInstance().ensureGameIsPlaying();

                    } else {
                        hostSocket = null;
                        who = "The host";

                        //Wrap up the game
                        ApplicationHelper.getInstance().GAME_HAS_STARTED = false;
                        ApplicationHelper.getInstance().prepareNewGame();

                        Intent intent = new Intent(mContext, StartingScreen.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }

                    if (activityHandler != null) {
                        //Inform the activity handler who disconnected.
                        activityHandler.obtainMessage(PLAYER_DISCONNECTED, who).sendToTarget();
                    }

                    break;
                case THREAD_STREAM_ERROR:
                    Toast.makeText(mContext, "Stream couldn't be retrieved", Toast.LENGTH_SHORT).show();
                    break;
                default:

                    break;
            }

        }

    }

    //Receive a message from a ConnectedThread.
    private static final int THREAD_READ = ConnectedThread.THREAD_READ;
    //Receive a message from a ConnectedThread that it has disconnected
    private static final int THREAD_DISCONNECTED = ConnectedThread.THREAD_DISCONNECTED;
    //Receive a message from a ConnectedThread that it couldn't retrieve the Input or Output Stream
    private static final int THREAD_STREAM_ERROR = ConnectedThread.THREAD_STREAM_ERROR;

    /*Codes for the activity handler*/
    public static final int YOUR_TURN = 6; //It's the player's turn to play.
    public static final int PASS = 7; //The player has passed his turn. This is used by spectators.
    public static final int START_GAME = 8; //The host has started the game. Next screen please!
    private static final int HOST_ONLY = 9; //The host has started the game. Next screen please!

    //These are provided as int in the msg.arg
    public static final int PLAYER_CONNECTED = 10;
    public static final int PLAYER_DISCONNECTED = 11;

    //Codes for the various handlers.
    public static final int CHAT = 0;
    public static final int STORY = 1;
    public static final int STORY_CODE = 2;
    public static final int ACTIVITY_CODE = 3;

    //Codes for the the sender of a chat message. Currently not used.
    private static final int MESSAGE_ME = 4;
    private static final int MESSAGE_OTHER = 5;


}
