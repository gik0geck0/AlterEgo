package edu.mines.alterego;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Description: This file defines the fragment showing characters and stats
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */

public class CharacterFragment extends Fragment implements RefreshInterface {

    // Local data object to hold the character name and description
    CharacterData mChar;
    RefreshInterface mActRefresher;
    View mainView;
    // // private SimpleCursorAdapter mCharStatAdapterC;
    private ModelAdapter<CharacterStat> mCharStatAdapterC;
    private Cursor statsCursor;
    private CharacterDBHelper mDbHelper;

    CharacterFragment(RefreshInterface refresher) {
        super();
        mActRefresher = refresher;
    }


    /**
     * This function creates the fragment view and decides if it needs to show new character creation
     * or existing characters.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDbHelper = new CharacterDBHelper(getActivity());
        if (GameActivity.mCharId < 0) {
            GameActivity.mCharId = mDbHelper.getCharacterIdForGame(GameActivity.mGameId);
        }

        // Inflate the layout for this fragment
        View characterView = inflater.inflate(R.layout.character_view, container, false);
        mainView = characterView;


        if (GameActivity.mCharId >= 0) {
            mChar = mDbHelper.getCharacter(GameActivity.mCharId);
            showCharacter();
        } else {
            // Make the no-char layout visible
            LinearLayout nochar11 = (LinearLayout) characterView.findViewById(R.id.nochar_layout);
            nochar11.setVisibility(0);

            // Bind the new-character button to it's appropriate action
            Button newChar = (Button) characterView.findViewById(R.id.nochar_button);

            newChar.setOnClickListener( new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Spawn the create-character dialog

                    AlertDialog.Builder charBuilder = new AlertDialog.Builder(v.getContext());
                    LayoutInflater inflater = getActivity().getLayoutInflater();

                    charBuilder.setView(inflater.inflate(R.layout.new_char_dialog, null))
                        .setTitle("Create New Character")
                        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                AlertDialog thisDialog = (AlertDialog) dialog;

                                EditText nameInput = (EditText) thisDialog.findViewById(R.id.char_name);
                                EditText descInput = (EditText) thisDialog.findViewById(R.id.char_desc);

                                String name = nameInput.getText().toString();
                                String desc = descInput.getText().toString();

                                CharacterData newChar = mDbHelper.addCharacter(GameActivity.mGameId, name, desc);

                                mChar = newChar;
                                GameActivity.mCharId = newChar.id;

                                showCharacter();

                                mActRefresher.refresh();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            // Negative button just closes the dialog
                            @Override
                            public void onClick(DialogInterface dialog, int id) { dialog.dismiss(); }
                        });
                    charBuilder.create().show();
                }
            });
        }

        return characterView;
    }

    /**
     * This method shows character the current, or newly created character and applicable stats.
     */
    public void showCharacter() {
        // Make the no-char layout invisible
        LinearLayout nochar11 = (LinearLayout) mainView.findViewById(R.id.nochar_layout);
        nochar11.setVisibility(View.GONE);

        // Make the character-viewing area visible
        LinearLayout charLayout = (LinearLayout) mainView.findViewById(R.id.haschar_layout);
        charLayout.setVisibility(View.VISIBLE);

        // Show the character name and description
        TextView cName = (TextView) mainView.findViewById(R.id.char_name);
        TextView cDesc = (TextView) mainView.findViewById(R.id.char_desc);

        cName.setText(mChar.name);
        cDesc.setText(mChar.description);

        // Show all the character's attributes/skills/complications
        int[] ctrlIds = new int[] {android.R.id.text1, android.R.id.text2};
        statsCursor = mDbHelper.getStatsForCharacterCursor(mChar.id);

        // Initialize the ModelAdapter
        CursorFetcher cfetcher = new CursorFetcher() {
            public Cursor fetch() { return mDbHelper.getStatsForCharacterCursor(mChar.id); }
        };
        ModelInitializer<CharacterStat> initer = new ModelInitializer<CharacterStat>() {
            public CharacterStat initialize(Cursor c) {
                CharacterStat cs = CharacterDBHelper.createCharacterStatFromCursor(c);
                Log.d("AlterEgo::CharStatInitializer", "Created an object for " + cs);
                return cs;
            }
        };

        ListView statView = (ListView) mainView.findViewById(R.id.char_stats);
        mCharStatAdapterC = new ModelAdapter<CharacterStat>(getActivity(), cfetcher, initer);
        // new SimpleCursorAdapter(this.getActivity(), android.R.layout.simple_list_item_2 , statsCursor, new String[] {"stat_name", "stat_value"} , ctrlIds, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        statView.setAdapter(mCharStatAdapterC);

        // Create context menu (Long-press)
        registerForContextMenu(statView);

        Button newStat = (Button) mainView.findViewById(R.id.new_stat_button);
        newStat.setOnClickListener( new Button.OnClickListener() {

         @Override
            public void onClick(View v) {
                // Spawn the create-character dialog
                DialogFactory.makeDialog(
                        getActivity(),
                        DialogFactory.DialogType.NEW,
                        DialogFactory.ModelType.CHARSTAT,
                        mDbHelper,
                        mChar.id,
                        0,
                        CharacterFragment.this);
            }
        });
    }

    // Long Press Menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // if (v.getId() == R.id.main_game_list_view) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
        // }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.context_edit:
                // Create Edit-item context menu
                DialogFactory.makeDialog(
                        getActivity(),
                        DialogFactory.DialogType.EDIT,
                        DialogFactory.ModelType.CHARSTAT,
                        mDbHelper,
                        mChar.id,
                        (mCharStatAdapterC.getItem(info.position).getStatId()),
                        this);

                return true;
            case R.id.context_delete:

                /*
                Log.d("AlterEgo::CharacterFragment", "mDbHelper: " + mDbHelper);
                Log.d("AlterEgo::CharacterFragment", "mStatAdapter: " + mCharStatAdapterC);
                Log.d("AlterEgo::CharacterFragment", "info: " + info);
                Log.d("AlterEgo::CharacterFragment", "AdapterItem: " + mCharStatAdapterC.getItem(info.position));
                */
                Log.d("AlterEgo::CharacterFragment", "Deleting CharacterStat");
                mDbHelper.deleteCharStat(
                        mCharStatAdapterC.getItem(
                            info.position)
                        .getStatId());
                refresh();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

	public void showToast(String message) {
		Toast toast = Toast.makeText(getActivity(), message,
				Toast.LENGTH_SHORT);
		toast.show();
	}

    public void refresh() {
        mCharStatAdapterC.refreshDB();
    }
}
