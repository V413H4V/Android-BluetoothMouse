package mouse.v413h4v.com.bluetoothmouse;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by V413H4V on 10/1/2017.
 */

public class BluetoothServerConnection {
    private static final String TAG = "BluetoothServerConn";
    private static final String appName = "BLUETOOTHMOUSE";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private ConnectedThread mConnectedThread;

    private BluetoothAdapter btAdapter;
    Context mContext;

    private connAcceptThread acceptThread;

    public BluetoothServerConnection(Context context){
        mContext = context;
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        acceptThread = new connAcceptThread();
        acceptThread.start();
    }

    public void setUIStatus(String status){
        TouchpadInterface.statusText.setText(status);
    }

    private class connAcceptThread extends Thread {
        private final BluetoothServerSocket btServerSocket;

        public connAcceptThread(){
            BluetoothServerSocket temp = null;
            try {
                temp = btAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID_INSECURE);
                Log.d(TAG,"Used UUID for server: " + MY_UUID_INSECURE);

                TouchpadInterface.connectionStatus = "Server Started!";

            } catch (Exception e) {
                e.printStackTrace();
                if(temp != null){
                    try {
                        temp.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            btServerSocket = temp;
        }

        @Override
        public void run(){
            BluetoothSocket btSocket = null;
            try {
                Log.d(TAG,"Inside run(): Accept Thread Started...Accepting connections.");
                btSocket = btServerSocket.accept();
//                TouchpadInterface.statusText.setText("Connection Accepted!");
//                setUIStatus("Connection Accepted!");
                Log.d(TAG,"Inside run(): Connection Accepted.");
                TouchpadInterface.connectionStatus = "CONNECTED";
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Failed to accept connections.");
            }

            if(btSocket != null){
                connected(btSocket);
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        try{
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }catch (Exception e){
            Log.d(TAG,"Error Occured while starting ConnectedThread.");
            e.printStackTrace();
            mConnectedThread.cancel();
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

       /* public void sendMessage(String msg){
            String to_send = msg + "|";
            byte[] bytes = to_send.getBytes();
            write(bytes);
        }*/

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {e.printStackTrace(); }
        }
    }

    /*public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }*/

    public void write(String msg) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        String to_send = msg + "|";
        byte[] bytes = to_send.getBytes();

        mConnectedThread.write(bytes);
    }

}
