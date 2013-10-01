package edu.mines.alterego;

import android.app.Fragment;
import android.widget.ListView;

class InventoryFragment extends Fragment {
    int mGameId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mGameId = getArguments().getInt(R.string.gameid, -1);

        if (mGameId == -1) {
            // Yes, this is annoying, but it'll make an error VERY obvious. In testing, I have never seen this toast/error message. But ya never know
            Toast.makeText(this, "GameID not valid", 400).show();
            Log.e("QuidditchScoring:ScoringScreen", "GAME ID IS NOT VALID!!!!!");
        }

        // Inflate the layout for this fragment
        View inventory_view = inflater.inflate(R.layout.inventory_view, container, false);

        CharacterDBHelper dbhelper = new CharacterDBHelper(this);
        ArrayList<InventoryItem> invItems = dbhelper.getInventoryItems(mGameId);

        return inventory_view;
    }
}
