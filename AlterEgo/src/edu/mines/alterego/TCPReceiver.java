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

public class TCPReceiver {

    private String serverMessage;
    //public static final String SERVERIP = "192.168.1.82"; //your computer IP address
    //public static final int SERVERPORT = 4444;
    public static final int GROUPPORT = 4444;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    MulticastSocket mSocket;
    //WifiManager mWifi;
    InetAddress groupAddr;
    int myIp;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPReceiver(OnMessageReceived listener, MulticastSocket sock, int myIp) {
        mMessageListener = listener;
        mSocket = sock;
        this.myIp = myIp;
        //mWifi = wifi;
        try {
            groupAddr = InetAddress.getByName("228.5.6.7");
        } catch(Exception e) {
            e.printStackTrace();
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


            //create a socket to make the connection with the server

            try {

                //receive the message which the server sends back
                // in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    Log.e("AlterEgo::TCPReceiver", "R: Waiting for incoming packets");
                    // Wait for incoming packets
                    byte[] buf = new byte[10000];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    mSocket.receive(recv);
                    String rawBuffer = new String(buf, 0, recv.getLength());
                    Log.e("AlterEgo::TCPReceiver", "S: Received Message: '" + rawBuffer + "'");

                    JSONObject asJson;

                    try {
                        asJson= new JSONObject(serverMessage);
                    } catch (Exception e) {
                        // String is not JSON. Ignore it
                        asJson = null;
                        serverMessage = null;
                    }

                    if (asJson != null && asJson.has("senderIP") && asJson.getInt("senderIP") != myIp) {
                        if (asJson.isNull("body"))
                            Log.e("AlterEgo::TCPReceiver", "JSON Message did not contain a body!");
                        else {
                            if (asJson.isNull("subject"))
                                Log.e("AlterEgo::TCPReceiver", "JSON Message did not contain a subject!");
                            serverMessage = asJson.getString("body");
                        }
                    }
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;

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
