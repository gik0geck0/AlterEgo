package edu.mines.alterego;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.PriorityBlockingQueue;

import org.json.JSONObject;

public class TCPSender {

    //public static final String SERVERIP = "192.168.1.82"; //your computer IP address
    //public static final int SERVERPORT = 4444;
    public static final int GROUPPORT = 4444;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    MulticastSocket mSocket;
    //WifiManager mWifi;
    InetAddress groupAddr;
    int myIp;

    PriorityBlockingQueue<String> mInputQueue;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPSender(MulticastSocket sock, int myIp) {
        mSocket = sock;
        this.myIp = myIp;
        try {
            groupAddr = InetAddress.getByName("228.5.6.7");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){

        if (mSocket != null) {
            mInputQueue.put(message);

            Log.d("AlterEgo::TCPSender", "Done.");
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            //InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            InetAddress groupAddr = InetAddress.getByName("228.5.6.7");

            try {
                mInputQueue = new PriorityBlockingQueue<String>();

                //receive the message which the server sends back
                // in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    Log.d("AlterEgo::TCPSender", "Waiting for outgoing messages");
                    String message = mInputQueue.take();

                    // Convert it into JSON
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("senderIP", myIp);
                    jsonObj.put("subject", "chat");
                    jsonObj.put("body", message);

                    // Throw it in the byte-buffer
                    String jsonMessage = jsonObj.toString();
                    Log.d("AlterEgo::TCPSender", "Writing message: " + jsonMessage);
                    byte[] outBuf = jsonMessage.getBytes();

                    // Send it
                    DatagramPacket send = new DatagramPacket(outBuf, outBuf.length, groupAddr, GROUPPORT);
                    Log.d("AlterEgo::TCPSender", "Sending message...");
                    mSocket.send(send);
                    Log.d("AlterEgo::TCPSender", "Sent!");
                }

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                mSocket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
