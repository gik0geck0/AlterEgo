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
import android.text.InputType;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
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
            final int maybeSpecialId,
            RefreshInterface refresher) {
        Log.i("AlterEgo::DialogFactor", "Creating an " + dialogtype + " for ID " + gameCharId + "-" + maybeSpecialId);
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
                            dbh.insertCharStat(gameCharId, (Integer) valMap.get("stat_value"), (String) valMap.get("stat_name"), (String) valMap.get("description_usage_etc"), 0); }};
                        populateEditDialogFromCursor(newDialog, act, dbh.getStatsForCharacterCursor(gameCharId), diagLayout, dbup, refresher, dialogtype, dbh);
                        break;
                    case GAME:
                        title += " Game";
                        newDialog.setTitle(title);
                        break;
                }
                break;
            case EDIT:
                title = "Edit";
                switch (modeltype) {
                    case CHARSTAT:
                        Log.d("AlterEgo::CharacterFragment", "Opening Edit Character Stat dialog");
                        title += " Character Stat";
                        newDialog.setTitle(title);
                        DBUpdater dbup = new DBUpdater() {public void update(HashMap<String, Object> valMap) {
                            // TODO: There's currently no category selection. When that's added, the last part of this will need to change
                            dbh.updateCharStat(maybeSpecialId, (Integer) valMap.get("stat_value"), (String) valMap.get("stat_name"), (String) valMap.get("description_usage_etc"), 0); }};
                        // maybeSpecialId must be the statistic
                        populateEditDialogFromCursor(newDialog, act, dbh.getSpecificStatForCharacterCursor(gameCharId, maybeSpecialId), diagLayout, dbup, refresher, dialogtype, dbh);
                        break;
                    case GAME:
                        title += " Game";
                        newDialog.setTitle(title);
                        break;
                }
                break;
        }
        newDialog.setView(diagLayout);

        // TODO: Show the dialog
        newDialog.create().show();
    }

    /**
     * <p>
     * Populate the dialog with an editable interface for the contents of the cursor.
     * </p>
     * @param builder DialogBuilder that's being built
     * @param context App/Activity context
     * @param c Cursor that points to the current values in the database. The columns
     *          of this cursor that don't end in '_id' are used to make EditTexts. 
     *          Cursor MUST be already pointing to the correct row.
     * @param diagLayout LinearLayout to populate with the buttons and edit texts
     * @param dbup Updates the database using a column-value map
     * @param refresher Notifies listviews/adapters that the database has changed
     * @param dt EDIT vs NEW
     */
    public static void populateEditDialogFromCursor(
            AlertDialog.Builder builder,
            final Context context,
            final Cursor c,
            LinearLayout diagLayout,
            final DBUpdater dbup,
            final RefreshInterface refresher,
            final DialogType dt,
            CharacterDBHelper dbh) {
        // ColumnName -> EditText
        final HashMap<String, Object> editableColumns = new HashMap<String, Object>();
        // c.moveToFirst();

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


                LinearLayout.LayoutParams editparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                editparams.weight = 1.0f;
                editparams.gravity=Gravity.RIGHT;

                LinearLayout horizLayout = new LinearLayout(context);
                horizLayout.setOrientation(LinearLayout.HORIZONTAL);
                horizLayout.addView(lbl);

                int type = CharacterDBHelper.getType(s, c);
                if (type == 5) {
                    // Make a checkbox
                    CheckBox cb = new CheckBox(context);
                    if (c.getInt(c.getColumnIndex(s)) == 1)
                        cb.setChecked(true);
                    else
                        cb.setChecked(false);
                    cb.setLayoutParams(editparams);
                    horizLayout.addView(cb);
                    editableColumns.put(s, cb);
                } else {
                    EditText editBox = new EditText(context);
                    editBox.setLayoutParams(editparams);
                    horizLayout.addView(editBox);

                    switch(type) {
                        case Cursor.FIELD_TYPE_FLOAT:
                        case Cursor.FIELD_TYPE_INTEGER:
                            editBox.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                            break;
                        default:
                            // Assume String
                            editBox.setRawInputType(InputType.TYPE_CLASS_TEXT);
                            break;
                    }

                    // Only set the text if we're editing a pre-existing value
                    if (dt == DialogType.EDIT) {
                        editBox.setText(c.getString(c.getColumnIndex(s)), TextView.BufferType.EDITABLE);
                    }

                    // Place a reference to the edit-text in the map
                    editableColumns.put(s, editBox);
                }


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
                    c.moveToFirst();
                    for (String key : editableColumns.keySet()) {
                        String newVal = ((EditText) editableColumns.get(key)).getText().toString();
                        if (dt == DialogType.NEW && newVal.equals("")) {
                            Toast createGame = Toast.makeText(context, "Required: " + key, Toast.LENGTH_SHORT);
                            createGame.show();
                            break;
                        }

                        valid = true;
                        int idx = c.getColumnIndex(key);
                        switch(CharacterDBHelper.getType(key, c)) {
                            case Cursor.FIELD_TYPE_STRING:
                                valMap.put(key, ((EditText) editableColumns.get(key)).getText().toString());
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                // TODO: Long vs Int: Assume the user never enters longs?
                                valMap.put(key, Integer.parseInt(((EditText) editableColumns.get(key)).getText().toString()));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                valMap.put(key, Float.parseFloat(((EditText) editableColumns.get(key)).getText().toString()));
                                break;
                            case 5:
                                // Bool will need to come from a checkbox
                                valMap.put(key, (Boolean) ((CheckBox) editableColumns.get(key)).isChecked());
                                break;
                            default:
                                Log.e("AlterEgo::DialogFactory", "Unsupported column type for dynamic edits: " + c.getType(c.getColumnIndex(key)));
                                break;
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
