package edu.mines.alterego;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.util.Pair;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity implements View.OnClickListener {

    ArrayAdapter<Pair<Integer, String>> mGameDbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//ArrayList<String> gameList = new ArrayList<String>();
		CharacterDBHelper db = new CharacterDBHelper(this);
		ArrayList< Pair<Integer, String> > gamePairList = db.getGames();
        /*
		for( Pair<Integer, String> game : gamePairList) {
			gameList.add(game.second);
		}
        */

		mGameDbAdapter = new ArrayAdapter<Pair<Integer, String>>( this, android.R.layout.simple_list_item_1, gamePairList);
		ListView gameListView = (ListView) findViewById(R.id.game_list_view);
		gameListView.setAdapter(mGameDbAdapter);

        Button newGameB = (Button) findViewById(R.id.new_game);
        newGameB.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public void onClick(View v) {

        AlertDialog.Builder newGameDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        // Inflate the view
        newGameDialog.setView(inflater.inflate(R.layout.new_game_dialog, null))
            .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Create a new game
                    //CharacterDBHelper db = new CharacterDBHelper(this);
                    //Pair<Integer, String> newGame = addGame(name);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Cancel: Just close the dialog
                    dialog.dismiss();
                }
            });

        newGameDialog.create().show();
    }
}
