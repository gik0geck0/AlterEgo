package edu.mines.alterego;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Description: This class defines the functionality for the notes fragment.
 * It handles loading fragment data from the database and displaying it in the list view.
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */

public class NotesFragment extends Fragment implements View.OnClickListener {
	int mCharId;
	ArrayAdapter<NotesData> mNotesAdapter;
	CharacterDBHelper mDbHelper;
	Button addNoteB;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// TODO: The default should be -1, so that errors are generated. BUT for
		// testing, this was set to 0, for game 0
		mCharId = getArguments().getInt(
				(String) getResources().getText(R.string.charid), 0);

		if (mCharId == -1) {
			// Yes, this is annoying, but it'll make an error VERY obvious. In
			// testing, I have never seen this toast/error message. But ya never
			// know
			Toast.makeText(getActivity(), "GameID not valid", Toast.LENGTH_LONG)
					.show();
			Log.e("AlterEgo::NotesFragment", "GAME ID IS NOT VALID!!!!!");
		}

		// Inflate the layout for this fragment
		View notes_view = inflater.inflate(R.layout.notes_view, container,
				false);
		ListView nView = (ListView) notes_view.findViewById(R.id.notes_list);

        // Lookup the character in the database
		mDbHelper = new CharacterDBHelper(getActivity());
		ArrayList<NotesData> nItems = mDbHelper.getNotesData(mCharId);
		mNotesAdapter = new ArrayAdapter<NotesData>(getActivity(),
				android.R.layout.simple_list_item_1, nItems);
		nView.setAdapter(mNotesAdapter);

		// Create Add Notes Button
		addNoteB = (Button) notes_view.findViewById(R.id.notes_add_notes);
		addNoteB.setOnClickListener(this);

		return notes_view;
	}

	@Override
	public void onClick(View v) {
		if (v == addNoteB) {
			newNoteDialogue();
		}
	}

    /**
     * <p>
     * Spawns a dialog for creating a new note
     * </p>
     */
	public void newNoteDialogue() {
		AlertDialog.Builder newNotesDialog = new AlertDialog.Builder(
				getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate the view
		newNotesDialog
				.setView(inflater.inflate(R.layout.new_notes_dialog, null))
				.setPositiveButton(R.string.create,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// Perceive this dialog as an AlertDialog
								AlertDialog thisDialog = (AlertDialog) dialog;

								EditText subjectInput = (EditText) thisDialog
										.findViewById(R.id.notes_subject);
								EditText descriptionInput = (EditText) thisDialog
										.findViewById(R.id.notes_desc);

								String noteSubject = subjectInput.getText()
										.toString();
								String noteDescription = descriptionInput
										.getText().toString();

								// Create a new note
								NotesData newNote = mDbHelper.addNote(mCharId,
										noteSubject, noteDescription);
								mNotesAdapter.add(newNote);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// Cancel: Just close the dialog
								dialog.dismiss();
							}
						});

		newNotesDialog.create().show();
	}

}
