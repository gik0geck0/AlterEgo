package edu.mines.alterego;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import edu.mines.alterego.CharacterDBHelper;
import edu.mines.alterego.GameData;
import edu.mines.alterego.GameActivity;

/**
 *  Alter Ego
 *  @author: Matt Buland, Maria Deslis, Eric Young
 *
 * ----------------------------------------------------------------------------
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Matt Buland, Maria Deslis, Eric Young
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ----------------------------------------------------------------------------
 *
 * @version: 0.1
 *
 * Release Notes:
 *
 * 0.1:
 *      The basic functionality is *basically* there. Games and characters can
 *      be created. The remaining components will follow the same flow: have a
 *      display; click button to add things to "display" (from database); use
 *      a dialog to get input-parameters.
 */
public class MainActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener {

    ArrayAdapter<GameData> mGameDbAdapter;
    CharacterDBHelper mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//ArrayList<String> gameList = new ArrayList<String>();
		mDbHelper = new CharacterDBHelper(this);
		ArrayList<GameData> gamePairList = mDbHelper.getGames();
        /*
		for( Pair<Integer, String> game : gamePairList) {
			gameList.add(game.second);
		}
        */

		mGameDbAdapter = new ArrayAdapter<GameData>( this, android.R.layout.simple_list_item_1, gamePairList);
		ListView gameListView = (ListView) findViewById(R.id.game_list_view);
		gameListView.setAdapter(mGameDbAdapter);
        gameListView.setOnItemClickListener(this);

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
                    // Perceive this dialog as an AlertDialog
                    AlertDialog thisDialog = (AlertDialog) dialog;

                    EditText nameInput = (EditText) thisDialog.findViewById(R.id.game_name);
                    String gameName = nameInput.getText().toString();
                    // Create a new game
                    Log.i("AlterEgos::MainAct::NewGame", "Creating a game with the name " + gameName);
                    //CharacterDBHelper mDbHelper = new CharacterDBHelper(this);
                    GameData newGame = mDbHelper.addGame(gameName);
                    mGameDbAdapter.add(newGame);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GameData selectedGame = mGameDbAdapter.getItem(position);

        Log.i("AlterEgos::MainAct::SelectGame", "The game with an id " + selectedGame.first + " and a name of " + selectedGame.second + " was selected.");

        Intent launchGame = new Intent(view.getContext(), GameActivity.class);
        launchGame.putExtra((String) getResources().getText(R.string.gameid), selectedGame.first);

        MainActivity.this.startActivity(launchGame);
    }
}
