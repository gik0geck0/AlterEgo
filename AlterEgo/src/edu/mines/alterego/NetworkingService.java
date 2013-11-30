package edu.mines.alterego;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Binder;
import android.os.IBinder;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import edu.mines.alterego.TCPReceiver;
import edu.mines.alterego.TCPSender;

public class NetworkingService extends Service {
    private TCPReceiver mTcpReceiver;
    private TCPSender mTcpSender;

    private MulticastSocket mSocket;
    public static final int GROUPPORT = 4444;
    InetAddress groupAddr;
    private int myIpAddr;

    private final IBinder mBinder = new MyBinder();
    // private ArrayList<String> receiveStack;
    private Context mServiceContext;
    private CharacterDBHelper mDbHelper;

    public NetworkingService() { mServiceContext = this; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AlterEgo::NetworkingService", "Starting the Networking Service! YAY!");
        Bundle bundle = intent.getExtras();
        myIpAddr = bundle.getInt("IP", 0);
        // receiveStack = new ArrayList<String>();

        if (myIpAddr == 0) {
            Log.e("AlterEgo::NetworkingService", "ERROR: Did not receive the host's IP address in the intent. This is required to properly send and receive/filter messages");
            return Service.START_NOT_STICKY;
        }

        try {
            groupAddr = InetAddress.getByName("228.5.6.7");
            mSocket = new MulticastSocket(GROUPPORT);
            mSocket.joinGroup(groupAddr);
        } catch(Exception e) {
            e.printStackTrace();
        }

        mDbHelper = new CharacterDBHelper(this);

        Executor exec = new ThreadPerTaskExecutor();
        // Kick off the receiver thread
        new receivingTask().executeOnExecutor(exec, "");
        // Kick off the sender thread
        new sendingTask().executeOnExecutor(exec, "");

        // Register self as a BroadcastReceiver for incoming messages
        this.registerReceiver(mReceiver, new IntentFilter("edu.mines.alterego.outgoingmessage"));

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Register self as a BroadcastReceiver for incoming messages
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return mBinder;
    }

    class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    public class receivingTask extends AsyncTask<String,String,TCPReceiver> {

        @Override
        protected TCPReceiver doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpReceiver = new TCPReceiver(new TCPReceiver.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            }, mSocket, myIpAddr, mDbHelper);
            Log.d("AlterEgo::MapFragment", "Starting the receiving task");
            mTcpReceiver.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            /*
             * We don't care about that here anymore. It's in the DB
            for (String s : values) {
                receiveStack.add(s);
            }

            // Limit the receiveStack to 100 elements
            while(receiveStack.size() > 100) {
                receiveStack.remove(0);
            }
            */


            // Send a broadcast intent containing the new values
            Intent intent = new Intent();
            // intent.putExtra("newvalues", values);
            intent.setAction("edu.mines.alterego.incomingmessage");
            mServiceContext.sendBroadcast(intent);
        }
    }

    public class sendingTask extends AsyncTask<String,String,TCPSender> {

        @Override
        protected TCPSender doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpSender = new TCPSender(mSocket, myIpAddr);
            Log.d("AlterEgo::MapFragment", "Starting the sending task");
            mTcpSender.run();

            return null;
        }

        // @Override protected void onProgressUpdate(String... values) { super.onProgressUpdate(values); }
    }

    public class MyBinder extends Binder {
        NetworkingService getService() {
            return NetworkingService.this;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AlterEgo::NetworkingService", "Received an outbound message!");
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString("message");

            mTcpSender.sendMessage(msg);
        }
    };
}
