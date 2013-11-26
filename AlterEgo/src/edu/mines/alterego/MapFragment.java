package edu.mines.alterego;

import android.app.Activity;
import android.content.Context;

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

import java.util.ArrayList;

import edu.mines.alterego.MyCustomAdapter;

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
        View mapView = inflater.inflate(R.layout.map_fragment,
                container, false);

        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) mapView.findViewById(R.id.editText);
        Button send = (Button) mapView.findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        mList = (ListView) mapView.findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(getActivity(), arrayList);
        mList.setAdapter(mAdapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);

                //sends the message to the server
                // TODO: Send a broadcast intent for the service to pick up
                // mTcpSender.sendMessage(message);

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

}
