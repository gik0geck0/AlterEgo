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

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

class DialogFactory {
    public static enum DialogType {
        EDIT,
        NEW
    };
    public static enum ModelType {
        GAME,
        CHARSTAT
    };

    // There will ALWAYS need to be a gameCharId, which can be EITHER GameId OR CharId, and there MAY be a special ID. This could be something like Character Stat ID, Inventory Item ID, etc...
    public static void makeDialog(
            Activity act,
            DialogType dialogtype,
            ModelType modeltype,
            final CharacterDBHelper dbh,
            final int gameCharId,
            int maybeSpecialId,
            RefreshInterface refresher) {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(act);
        LayoutInflater inflater = act.getLayoutInflater();

        // Create our own custom layout for the dialog, built dynamically
        LinearLayout diagLayout = new LinearLayout(act);
        diagLayout.setOrientation(LinearLayout.VERTICAL);

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
                        DBUpdater dbup = new DBUpdater() {public void update(HashMap<String, Object> valMap) {
                            // TODO: There's currently no category selection. When that's added, the last part of this will need to change
                            dbh.insertCharStat(gameCharId, (Integer) valMap.get("stat_value"), (String) valMap.get("stat_name"), 0); }};
                        populateEditDialogFromCursor(newDialog, act, dbh.getStatsForCharacterCursor(gameCharId), diagLayout, dbup, refresher);
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
        newDialog.setView(diagLayout);

        // TODO: Show the dialog
        newDialog.create().show();
    }

    public static void populateEditDialogFromCursor(
            AlertDialog.Builder builder,
            final Context context,
            final Cursor c,
            LinearLayout diagLayout,
            final DBUpdater dbup,
            final RefreshInterface refresher) {
        // ColumnName -> EditText
        final HashMap<String, EditText> editableColumns = new HashMap<String, EditText>();

        // We have a blind cursor, and a dialog to populate with edit-fields
        for (String s : c.getColumnNames()) {
            if (!s.matches(".*_id$")) {
                // This column is not an ID-column. Must be the name of an essential element
                String shownName = CharacterDBHelper.getNameOfColumn(s);

                // Create a label with shownName
                // Create an edit-text that will be used to edit the column
                TextView lbl = new TextView(context);
                lbl.setText(shownName);

                LinearLayout.LayoutParams labelparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                // labelparams.weight = 1.0f;
                labelparams.gravity=Gravity.LEFT;
                lbl.setLayoutParams(labelparams);


                EditText editBox = new EditText(context);

                LinearLayout.LayoutParams editparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                editparams.weight = 1.0f;
                editparams.gravity=Gravity.RIGHT;
                editBox.setLayoutParams(editparams);
                // TODO: Set a type for the editBox

                // Place a reference to the edit-text in the map
                editableColumns.put(s, editBox);

                LinearLayout horizLayout = new LinearLayout(context);
                horizLayout.setOrientation(LinearLayout.HORIZONTAL);
                horizLayout.addView(lbl);
                horizLayout.addView(editBox);

                diagLayout.addView(horizLayout);
            }
        }

        builder.setPositiveButton(R.string.create,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Perceive this dialog as an AlertDialog
                    AlertDialog thisDialog = (AlertDialog) dialog;

                    // editableColumns is ColName -> EditText
                    // Turn it into ColName -> NewValue
                    HashMap<String, Object> valMap = new HashMap<String, Object>();
                    boolean valid = false;
                    for (String key : editableColumns.keySet()) {
                        if (editableColumns.get(key).getText().toString().equals("")) {
                            Toast createGame = Toast.makeText(context, "Required: " + key, Toast.LENGTH_SHORT);
                            createGame.show();
                        } else {
                            valid = true;
                            int idx = c.getColumnIndex(key);
                            Log.d("AlterEgo::DialogFactory", "What is the type of " + key + "? Index " + idx);
                            Log.d("AlterEgo::DialogFactory", "Type is " + c.getType(idx));
                            switch(c.getType(c.getColumnIndex(key))) {
                                case Cursor.FIELD_TYPE_STRING:
                                    valMap.put(key, editableColumns.get(key).getText().toString());
                                    break;
                                case Cursor.FIELD_TYPE_INTEGER:
                                    // TODO: Long vs Int vs Bool : Assume the user never enters longs?
                                    // TODO: Bool will somehow need to come from a checkbox
                                    valMap.put(key, Integer.parseInt(editableColumns.get(key).getText().toString()));
                                    break;
                                default:
                                    Log.e("AlterEgo::DialogFactory", "Unsupported column type for dynamic edits: " + c.getType(c.getColumnIndex(key)));
                                    break;
                            }
                        }
                    }

                    if (valid) {
                        // Perform Update!
                        // If new, use new functions for database
                        // If edit, use edit functions for database
                        Log.d("AlterEgo::DialogFactory", "Updating the database with the values: " + valMap);
                        dbup.update(valMap);

                        // Close the dialog
                        //
                        // Notify the adapter/s to update
                        refresher.refresh();
                    }
                }
            });
    }
}
