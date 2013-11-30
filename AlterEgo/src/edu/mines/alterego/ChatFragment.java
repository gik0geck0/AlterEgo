package edu.mines.alterego;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import edu.mines.alterego.MyCustomAdapter;

/**
 * Description: This class defines the functionality for the map fragment.
 * It handles starting the map activity
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */

public class ChatFragment extends Fragment {

    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private Context mContext;

    private SimpleCursorAdapter mMessageAdapter;

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

        // Inflate the layout for this fragment
        View mapView = inflater.inflate(R.layout.chat_fragment,
                container, false);

        mContext = getActivity();

        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) mapView.findViewById(R.id.editText);
        Button send = (Button) mapView.findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        mList = (ListView) mapView.findViewById(R.id.list);

        CharacterDBHelper dbHelper = new CharacterDBHelper(getActivity());
        /* Manual adapter
         */
        mAdapter = new MyCustomAdapter(getActivity(), arrayList, dbHelper, GameActivity.mGameId);
        mList.setAdapter(mAdapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                // arrayList.add("c: " + message);

                //sends the message to the server
                // TODO: Send a broadcast intent for the service to pick up
                Intent intent = new Intent();
                intent.putExtra("message", message);
                intent.setAction("edu.mines.alterego.outgoingmessage");
                mContext.sendBroadcast(intent);
                Log.d("ChatFragment::Broadcast", "Broadcast message sent!");

                // mTcpSender.sendMessage(message);

                //refresh the list
                mAdapter.notifyDataSetChanged();
                // mMessageAdapter;
                editText.setText("");
            }
        });

        return mapView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register self as a BroadcastReceiver for incoming messages
        getActivity().registerReceiver(mReceiver, new IntentFilter("edu.mines.alterego.incomingmessage"));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Register self as a BroadcastReceiver for incoming messages
        getActivity().unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
            Bundle bundle = intent.getExtras();
            String[] newMsgs = bundle.getStringArray("newvalues");

            for (String s : newMsgs) {
                Log.d("AlterEgo::MapFragment::Msging", "Message received: " + s);
            }
            */

            // The database has been updated with some new messages
            Log.d("AlterEgo::MapFragment::Msging", "At least one new message was received");
            mAdapter.refreshDB();
        }
    };
}
