package edu.mines.alterego;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.mines.alterego.CharacterDBHelper;

 class InventoryFragment extends Fragment {
    int mCharId;
    ArrayAdapter<InventoryItem> mInvAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // TODO: The default should be -1, so that errors are generated. BUT for testing, this was set to 0, for game 0
        mCharId = getArguments().getInt((String) getResources().getText(R.string.gameid), 0);

        if (mCharId == -1) {
            // Yes, this is annoying, but it'll make an error VERY obvious. In testing, I have never seen this toast/error message. But ya never know
            Toast.makeText(getActivity(), "GameID not valid", 400).show();
            Log.e("QuidditchScoring:ScoringScreen", "GAME ID IS NOT VALID!!!!!");
        }

        // Inflate the layout for this fragment
        View inventory_view = inflater.inflate(R.layout.inventory_view, container, false);
        ListView invView = (ListView) inventory_view.findViewById(R.id.inventory_list);

        CharacterDBHelper dbhelper = new CharacterDBHelper(getActivity());
        ArrayList<InventoryItem> invItems = dbhelper.getInventoryItems(mCharId);

        mInvAdapter = new ArrayAdapter<InventoryItem>(getActivity(), android.R.layout.simple_list_item_1, invItems);
        invView.setAdapter(mInvAdapter);


        return inventory_view;
    }
}
