package grioanpier.auth.users.movies.utility;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

import grioanpier.auth.users.movies.bluetooth.ConnectedThread;

/**
 * Created by Ioannis on 21/2/2016.
 */
public class SocketManager {

    private BluetoothSocket hostSocket;
    private TreeMap<Integer, BluetoothSocket> playerSockets = new TreeMap<>();
    private final TreeMap<Integer, ConnectedThread> connectedThreads = new TreeMap<>();
    private static final String LOG_TAG = SocketManager.class.getCanonicalName();

    public <T extends Handler> void addPlayerSocket(BluetoothSocket btSocket, T handler) {
        Log.i(LOG_TAG, "A new player socket was added: " + btSocket.hashCode());
        ConnectedThread thread = new ConnectedThread(btSocket, handler);
        thread.start();
        connectedThreads.put(thread.ID, thread);
        playerSockets.put(thread.ID, btSocket);
    }

    public boolean removePlayerSocket(Integer key) {
        Log.i(LOG_TAG, "Removing player socket: " + key);
        if (playerSockets.containsKey(key)) {
            try {
                playerSockets.remove(key).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else
            return false;
    }

    public Collection<BluetoothSocket> getPlayerSockets() {
        return playerSockets.values();
    }

    public <T extends Handler> void setHostSocket(BluetoothSocket btSocket, T handler) {
        hostSocket = btSocket;
        ConnectedThread thread = new ConnectedThread(btSocket, handler);
        thread.start();
        connectedThreads.put(thread.ID, thread);
    }

    public boolean removeHostSocket(){
        if (hostSocket==null)
            return false;
        else{
            try {
                hostSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            hostSocket=null;
            return true;
        }
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

    public void removeThread(Integer key){
        connectedThreads.remove(key);
    }

    public int threadsSize(){
        return connectedThreads.size();
    }

    public void writeToAll(String message){
        Log.i(LOG_TAG, "Relaying message: " + message);
        byte[] buffer = message.getBytes();
        introduceDelay(250);
        for (ConnectedThread thread : connectedThreads.values())
            thread.write(buffer);
    }

    public void writeTo(String message, int threadIndex){
        introduceDelay(250);
        Collection<ConnectedThread> col = connectedThreads.values();
        if (!col.isEmpty()) {
            //TODO wtf? connectedThreads is an array, this might break the proper turn
            //Or not? As soon as the game starts, more players can't join so the ordering stays the same
            ConnectedThread thread = (ConnectedThread) col.toArray()[threadIndex];
            if (thread != null) {
                thread.write(message.getBytes());
            }
        }
    }

    //Don't want messages to be relayed too fast in succession because they get entangled.
    private void introduceDelay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    private void closePlayerSockets() {
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

    public void clear() {
        closeHostSocket();
        closePlayerSockets();
        clearConnectedThreads();
    }
}
