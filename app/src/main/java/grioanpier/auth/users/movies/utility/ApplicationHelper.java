package grioanpier.auth.users.movies.utility;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import grioanpier.auth.users.movies.bluetooth.ConnectedThread;

/**
 * Created by Ioannis on 13/4/2015.
 */
public class ApplicationHelper extends Application{
    public static final ArrayList<UUID> sAvailableUUIDs = new ArrayList<>(Arrays.asList(Constants.sUUIDs));
    private static ApplicationHelper singleton;
    private final static String LOG_TAG = ApplicationHelper.class.getSimpleName();
    private static ApplicationHandler applicationHandler;

    public static ApplicationHelper getInstance(){
        return singleton;
    }

    public static UUID getNextAvailableUUID(){
        if (sAvailableUUIDs.isEmpty())
            return null;
        else
            return sAvailableUUIDs.get(0);
    }

    /**
     *
     * @return true if there was something to remove, false otherwise.
     */
    public static boolean removeNextAvailableUUID(){
        if (sAvailableUUIDs.isEmpty())
            return false;
        else {
            sAvailableUUIDs.remove(0);
            return true;
        }
    }

    public static void restoreUUIDs(){
        sAvailableUUIDs.clear();
        sAvailableUUIDs.ensureCapacity(10);
        sAvailableUUIDs.addAll(Arrays.asList(Constants.sUUIDs));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        applicationHandler = new ApplicationHandler(getApplicationContext());
        System.out.println("WOLOLO");
        byte byto;



        //byto=(byte)HANDLER_CHAT_WRITE;
        byto=(byte)2;
        String msg = "wasrvdscresdvdd";
        StringBuilder s = new StringBuilder();
        s.append(byto).append(msg);
        byte[] buffer = s.toString().getBytes();
        StringBuilder builder = new StringBuilder(Arrays.toString(buffer));
        System.out.println("builder: " +s.toString());
        System.out.println(s.deleteCharAt(0).toString());
        System.out.println("WOLOLO");
        //new MyThread().start();
    }


    @Override
    public void onTerminate(){
        super.onTerminate();
        Log.v(LOG_TAG, "onTerminate");
        Log.e(LOG_TAG, "onTerminate");
        //TODO implement me!
        //note I need to re-establish the connections if the user hits the home button and afterwards returns to the application.
    }

    /**
     * The connected {@link BluetoothSocket} for every player. This is used by the host-player.
     */
    private ArrayList<BluetoothSocket> playerSockets = new ArrayList<>();
    /**
     * The connected {@link BluetoothSocket} for the host. This is used by the non-host-players.
     */
    private BluetoothSocket hostSocket;

    public boolean addPlayerSocket(BluetoothSocket btSocket){return playerSockets.add(btSocket);}

    public ArrayList<BluetoothSocket> getPlayerSockets(){
        return playerSockets;
    }

    public void setHostSocket(BluetoothSocket btSocket){hostSocket = btSocket;}

    public BluetoothSocket getHostSocket(){
        return hostSocket;
    }

    private static final ArrayList<ConnectedThread> connectedThreads = new ArrayList<>();

    private Handler chatHandler;

    public void setChatHandler(Handler handler){
        chatHandler = handler;
    }

    public void unregisterChatHandler(){
        if (chatHandler!=null)
            chatHandler = null;
    }

    private Handler storyHandler;

    public void setStoryHandler(Handler handler){
        storyHandler = handler;
    }

    public void unregisterStoryHandler(){
        if (storyHandler!=null)
            storyHandler = null;
    }

    private boolean playerThreadsRunning = false;
    private final Object playerThreadsLock = new Object();

    public void openPlayerThreads(){
        if (!playerThreadsRunning){
            synchronized (playerThreadsLock){
                playerThreadsRunning =true;
                ConnectedThread thread;
                for (BluetoothSocket socket : playerSockets){
                    thread = new ConnectedThread(socket, applicationHandler);
                    thread.start();
                    connectedThreads.add(thread);
                }
            }

        }
    }

    public void cancelPlayerThreads(){
        if (playerThreadsRunning){
            synchronized (playerThreadsLock){
                for (ConnectedThread thread : connectedThreads)
                    thread.cancel();
                connectedThreads.clear();
                playerThreadsRunning = false;
            }
        }
    }

    public void write(int messageType, String message){
        for (ConnectedThread thread : connectedThreads)
            thread.write(messageType, message);
    }

    public static class ApplicationHandler extends Handler {

        private Context context;

        public ApplicationHandler(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ApplicationHelper.HANDLER_IS_PLAYER:
                    break;
                default:
                    System.out.println("what: "+msg.what);
                    break;
            }
        }

    }

    //Send a messsage to the bluetooth chat
    public static final int HANDLER_CHAT_WRITE = 1;
    //Someone else sent a message to the bluetooth chat
    public static final int HANDLER_CHAT_READ = 2;
    //Inform you are a spectator
    public static final int HANDLER_IS_SPECTATOR = 3;
    //Inform you are a player
    public static final int HANDLER_IS_PLAYER = 4;
    //Inform you are leaving the game
    public static final int HANDLER_DISCONNECTING = 5;
    //Write to the story
    public static final int HANDLER_STORY_WRITE = 6;
    //Someone else wrote to the story
    public static final int HANDLER_STORY_READ = 7;
    //Constant for incoming messages.
    public static final int HANDLER_READ = 8;



}
