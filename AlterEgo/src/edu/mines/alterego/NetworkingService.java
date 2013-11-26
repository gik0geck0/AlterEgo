package edu.mines.alterego;

import android.app.Service;
import android.content.Intent;
import android.util.Log;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import java.net.InetAddress;
import java.net.MulticastSocket;
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

    public NetworkingService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        myIpAddr = bundle.getInt("IP", 0);
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

        Executor exec = new ThreadPerTaskExecutor();
        // Kick off the receiver thread
        new receivingTask().executeOnExecutor(exec, "");
        // Kick off the sender thread
        new sendingTask().executeOnExecutor(exec, "");

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
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
            }, mSocket, myIpAddr);
            Log.d("AlterEgo::MapFragment", "Starting the receiving task");
            mTcpReceiver.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            // TODO: Send a broadcast intent stating values can be retrieved
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

}
