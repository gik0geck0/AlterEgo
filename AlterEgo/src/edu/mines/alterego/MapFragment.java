package edu.mines.alterego;

import android.app.Activity;
import android.content.Context;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.Executor;

import edu.mines.alterego.MyCustomAdapter;
import edu.mines.alterego.TCPReceiver;
import edu.mines.alterego.TCPSender;

/**
 * Description: This class defines the functionality for the map fragment.
 * It handles starting the map activity
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */

public class MapFragment extends Fragment implements TCPReceiver.OnMessageReceived {

    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private TCPReceiver mTcpReceiver;
    private TCPSender mTcpSender;

    private MulticastSocket mSocket;
    public static final int GROUPPORT = 4444;
    InetAddress groupAddr;
    private int myIpAddr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (GameActivity.mCharId == -1) {
            // Yes, this is annoying, but it'll make an error VERY obvious. In
            // testing, I have never seen this toast/error message. But ya never
            // know
            Toast.makeText(getActivity(), "No character. Please make one!", Toast.LENGTH_SHORT)
                .show();
            Log.e("AlterEgo:MapFragment", "No valid character. The user needs to make one.");
        }

        WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
        multicastLock.acquire();
        WifiInfo myWifiInfo = wm.getConnectionInfo();
        myIpAddr = myWifiInfo.getIpAddress();

        // Inflate the layout for this fragment
        View mapView = inflater.inflate(R.layout.map_fragment,
                container, false);

        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) mapView.findViewById(R.id.editText);
        Button send = (Button) mapView.findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        mList = (ListView) mapView.findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(getActivity(), arrayList);
        mList.setAdapter(mAdapter);

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


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);

                //sends the message to the server
                mTcpSender.sendMessage(message);

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

        return mapView;
    }

    @Override
    public void messageReceived(String message) {
        Log.d("AlterEgo::MapFragment::Msging", "Message received: " + message);
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

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
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

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }
    }
}
