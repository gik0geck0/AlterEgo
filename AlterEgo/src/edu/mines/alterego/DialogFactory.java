package edu.mines.alterego;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

class DialogFactory {
    public static enum DialogType {
        EDIT,
        NEW
    };
    public static enum ModelType {
        GAME,
        CHARSTAT
    };

    // There will ALWAYS need to be a gameId, and there MAY be a special ID. This could be something like Character ID, Character Stat ID, Inventory Item ID, etc...
    public static void makeDialog(Activity act, DialogType dialogtype, ModelType modeltype, CharacterDBHelper dbh, int gameId, int maybeSpecialId) {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(act);
        LayoutInflater inflater = act.getLayoutInflater();
        // All dialogs will do nothing on cancel
        newDialog.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Cancel: Just close the dialog
                            dialog.dismiss(); } });

        String title = "";
        switch (dialogtype) {
            case NEW:
                title = "Create New";
                switch (modeltype) {
                    case CHARSTAT:
                        title += " Character Stat";
                        newDialog.setTitle(title);
                        populateEditDialogFromCursor(newDialog, dbh.getStatsForCharacterCursor(maybeSpecialId));
                        break;
                    case GAME:
                        title += " Game";
                        newDialog.setTitle(title);
                        break;
                }
                break;
            case EDIT:
                title = "Edit";
        }
    }

    public static void populateEditDialogFromCursor(AlertDialog.Builder builder, Cursor c) {
        // We have a blind cursor, and a dialog to populate with edit-fields
        for (String s : c.getColumnNames()) {
            if (!s.matches("_id$")) {
                // This column is not an ID-column. Must be the name of an essential element
                String shownName = CharacterDBHelper.getNameOfColumn(s);

                // Create a label with shownName
                // Create an edit-text that will be used to edit the column
            }
        }
    }

    /*
     *  What's needed for an New AND Edit Dialog?
     *  table name
     *
     *  Attn:
     *  Edit / Delete option only. Context dialog
     */

    /*
    // Opens up dialogue for user to input new game
    public void newGameDialogue() {
        AlertDialog.Builder newGameDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // Inflate the view
        newGameDialog
                .setTitle("Create New Game")
                .setView(inflater.inflate(R.layout.new_game_dialog, null))
                .setPositiveButton(R.string.create,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Perceive this dialog as an AlertDialog
                                AlertDialog thisDialog = (AlertDialog) dialog;

                                EditText nameInput = (EditText) thisDialog
                                        .findViewById(R.id.game_name);
                                String gameName = nameInput.getText()
                                        .toString();
                                // Create a new game
                                Log.i("AlterEgos::MainAct::NewGame",
                                        "Creating a game with the name "
                                                + gameName);

                                CheckBox hostingCheck = (CheckBox) thisDialog
                                        .findViewById(R.id.hosting);
                                int hosting = hostingCheck.isChecked() ? 1 : 0;

                                // CharacterDBHelper mDbHelper = new
                                // CharacterDBHelper(this);
                                if (gameName.equals("")) {
                                    Toast createGame = Toast.makeText(MainActivity.this, "Required: Game Name", Toast.LENGTH_SHORT);
                                    createGame.show();
                                } else {
                                    GameData newGame = mDbHelper.addGame(gameName,
                                            hosting);
                                    mGameDbAdapter.add(newGame);
                                    hideCreateNewGameButton();
                                }
                            }
                        });

        newGameDialog.create().show();
    }

    public void editGameDialogue(final int game_id) {
        AlertDialog.Builder editGameDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        // Inflate the view
        editGameDialog
                .setTitle("Edit Game")
                .setView(inflater.inflate(R.layout.edit_game_dialog, null))
                .setPositiveButton(R.string.new_edit,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Perceive this dialog as an AlertDialog
                                AlertDialog thisDialog = (AlertDialog) dialog;

                                EditText nameInput = (EditText) thisDialog
                                        .findViewById(R.id.new_game_name);
                                String name = nameInput.getText().toString();
                                if (name.equals("")) {
                                    Toast editGame = Toast.makeText(MainActivity.this, "Required: Game Name", Toast.LENGTH_SHORT);
                                    editGame.show();
                                } else {
                                    mDbHelper.updateGame(game_id, name);
                                    mGameDbAdapter.clear();
                                    mGameDbAdapter.addAll(mDbHelper.getGames());
                                    Toast editGame = Toast.makeText(MainActivity.this, "Game Edited", Toast.LENGTH_SHORT);
                                    editGame.show();
                                }
                            }
                        })
                .setNegativeButton(R.string.new_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Cancel: Just close the dialog
                                dialog.dismiss();
                            }
                        });

        editGameDialog.create().show();
    }
    */

}
