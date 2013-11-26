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

public class TCPClient {

    private String serverMessage;
    //public static final String SERVERIP = "192.168.1.82"; //your computer IP address
    //public static final int SERVERPORT = 4444;
    public static final int GROUPPORT = 4444;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    MulticastSocket mSocket;
    //WifiManager mWifi;
    InetAddress groupAddr;

    // PrintWriter out;
    BufferedReader in;

    byte[] mOutBuf;
    PriorityBlockingQueue<String> mInputQueue;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener /*, WifiManager wifi */ ) {
        mMessageListener = listener;
        //mWifi = wifi;
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
            byte[] buf = message.getBytes();
            //try {
                mInputQueue.put(message);

            /*
            } catch (Exception e) {
                Log.e("AlterEgo::TCPClient", "Bad news. An error ocurred while writing a message.");
                e.printStackTrace();
            }
            */
            Log.d("AlterEgo::TCPClient", "Done.");
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

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            mSocket = new MulticastSocket(GROUPPORT);
            mSocket.joinGroup(groupAddr);

            try {
                mOutBuf = new byte[1024];
                mInputQueue = new PriorityBlockingQueue<String>();

                //receive the message which the server sends back
                // in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    Log.d("AlterEgo::TCPClient", "Waiting for outgoing messages");
                    String message = mInputQueue.take();

                    Log.d("AlterEgo::TCPClient", "Writing message: " + message);
                    char[] buffer = message.toCharArray();
                    CharBuffer cBuffer = ByteBuffer.wrap(mOutBuf).asCharBuffer();
                    for(int i = 0; i < buffer.length; i++)
                        cBuffer.put(buffer[i]);
                    DatagramPacket send = new DatagramPacket(mOutBuf, mOutBuf.length, groupAddr, GROUPPORT);
                    Log.d("AlterEgo::TCPClient", "Sending message...");
                    mSocket.send(send);
                    Log.d("AlterEgo::TCPClient", "Sent!");

                    // Wait for incoming packets
                    byte[] buf = new byte[10000];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    mSocket.receive(recv);
                    serverMessage = new String(buf);

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;

                }


                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");


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
