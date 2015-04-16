package grioanpier.auth.users.movies.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import grioanpier.auth.users.movies.Utility;
import grioanpier.auth.users.movies.utility.ApplicationHelper;

/**
 * Created by Ioannis on 12/4/2015.
 */
public class ConnectedThread extends Thread {
    private static final String LOG_TAG = ConnectedThread.class.getSimpleName();

    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;
    private final Handler mHandler;

    private boolean isActive = true;

    private static final int MESSAGE_SIZE = 2048;
    private static final int THREAD_READ = 0;

    private interface MessageListener{
        public void onMessageReceived(byte[] buffer, String deviceName);
        public void onConnectionLost(BluetoothSocket bluetoothSocket);
    }

    private MessageListener messageListener;

    public void setConnectionListener(MessageListener listener){
        messageListener = listener;
    }

    public ConnectedThread(BluetoothSocket socket, ApplicationHelper.ApplicationHandler handler) {
        mSocket = socket;
        InputStream tempIn = null;
        OutputStream tempOut = null;
        mHandler = handler;


        try {
            tempIn = socket.getInputStream();
            tempOut = socket.getOutputStream();
        } catch (IOException e) { }

        mInStream = tempIn;
        mOutStream = tempOut;
    }

    public void run(){
        Log.v(LOG_TAG, "BEGIN ConnectedThread");
        byte[] buffer = new byte[MESSAGE_SIZE];
        int numOfBytes;

        // Keep listening to the InputStream while connected
        while (isActive) {
            try {
                // Read from the InputStream
                numOfBytes= mInStream.read(buffer);
                //This is only okay because my constants start from 1 and are small so there is no overflow.

                StringBuilder builder = new StringBuilder(Arrays.toString(buffer));
                int messageType = builder.charAt(0);
                builder.deleteCharAt(0);

                // Send the obtained bytes to the UI Activity
                mHandler.obtainMessage(ApplicationHelper.HANDLER_READ, messageType, numOfBytes, builder.toString().getBytes())
                        .sendToTarget();

            } catch (IOException e) {
                Log.v(LOG_TAG, "disconnected", e);
                // fire event that the connection has been lost.
                if (messageListener !=null )
                    messageListener.onConnectionLost(mSocket);
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param messageType The message's type. The possible values are defined in {@link grioanpier.auth.users.movies.utility.ApplicationHelper}
     * @param message The message to send
     */
    public void write(int messageType, String message) {
        StringBuilder builder = new StringBuilder();
        byte[] buffer = builder
                //Warning: This only works because {messageType} is a small number (below 128)
                .append((byte) messageType)
                .append(message)
                .toString().getBytes();

        try {
            mOutStream.write(buffer);
        } catch (IOException e) {
            Log.v(LOG_TAG, "Exception during write");
            Log.v(LOG_TAG, Utility.getStackTraceString(e.getStackTrace()));
        }
    }

    public void cancel() {
        try{
            if (mInStream!=null)
                mInStream.close();
        } catch (IOException e) {}

        try{
            if (mOutStream!=null)
                mOutStream.close();
        } catch (IOException e) {}


        try {
            if (mSocket!=null)
            mSocket.close();
        } catch (IOException e) {}

        isActive=false;

    }




}
