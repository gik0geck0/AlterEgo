package edu.mines.alterego;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.Toast;

//import java.util.ArrayList;

import edu.mines.alterego.CharacterDBHelper;

public class CharacterFragment extends Fragment {

    int mCharId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mCharId = getArguments().getInt((String) getResources().getText(R.string.charid), -1);

        // Inflate the layout for this fragment
        View character_view = inflater.inflate(R.layout.character_view, container, false);

        if (mCharId < 0) {
            // Make the no-char layout visible
            LinearLayout nochar_ll = (LinearLayout) character_view.findViewById(R.id.nochar_layout);
            nochar_ll.setVisibility(0);
        }

        return character_view;
    }

}
