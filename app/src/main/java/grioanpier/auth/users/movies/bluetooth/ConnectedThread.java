package grioanpier.auth.users.movies.bluetooth;
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
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import grioanpier.auth.users.movies.utility.ApplicationHelper;

/**
 * A {@link Thread} that keeps the bluetooth connection with another device.
 * It's also the point of communication.
 */
public class ConnectedThread extends Thread {
    private static final String LOG_TAG = ConnectedThread.class.getSimpleName();

    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;
    private final Handler mHandler;
    public final int ID = hashCode();

    private static final int MESSAGE_SIZE = 1024;
    public static final int THREAD_READ = 0;
    public static final int THREAD_DISCONNECTED = 1;
    public static final int THREAD_STREAM_ERROR = 42;

    private boolean isActive;

    public ConnectedThread(BluetoothSocket socket, ApplicationHelper.ApplicationHandler handler) {
        mSocket = socket;
        InputStream tempIn = null;
        OutputStream tempOut = null;
        mHandler = handler;
        isActive=true;

        try {
            tempIn = socket.getInputStream();
            tempOut = socket.getOutputStream();
        } catch (IOException e) {
            mHandler.obtainMessage(THREAD_STREAM_ERROR).sendToTarget();
        }

        mInStream = tempIn;
        mOutStream = tempOut;
    }

    public void run(){
        byte[] buffer = new byte[MESSAGE_SIZE];
        int numOfBytes;

        // Keep listening to the InputStream while connected
        while (isActive) {
            try {
                numOfBytes = mInStream.read(buffer);
                if (numOfBytes==-1){
                    continue;
                }
                mHandler.obtainMessage(THREAD_READ, numOfBytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                mHandler.obtainMessage(THREAD_DISCONNECTED, ID, -1, mSocket.getRemoteDevice().getName()).sendToTarget();
                cancel();
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer the buffer to write to the stream
     */
    public synchronized void write(byte[] buffer) {
        try {
            mOutStream.write(buffer);
        } catch (IOException e) {}
    }

    public synchronized void cancel() {
        isActive=false;

        try{
            if (mInStream!=null)
                mInStream.close();
        } catch (IOException e) {}

        try{
            if (mOutStream!=null)
                mOutStream.close();
        } catch (IOException e) {}

        try {
            mSocket.close();
        } catch (IOException e) {}

        this.interrupt();

    }


}
