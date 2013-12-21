package edu.mines.alterego;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Description: This class defines the functionality for the inventory fragment.
 * It handles loading fragment data from the database and displaying it in the
 * list view.
 * 
 * @author Matt Buland, Maria Deslis, Eric Young
 * 
 */

public class InventoryFragment extends Fragment {
	// ArrayAdapter<InventoryItem> mInvAdapter;
	SimpleCursorAdapter mInvAdapter;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (GameActivity.mCharId == -1) {
			// Yes, this is annoying, but it'll make an error VERY obvious. In
			// testing, I have never seen this toast/error message. But ya never
			// know
			Toast.makeText(getActivity(), "No character. Please make one!",
					Toast.LENGTH_SHORT).show();
			Log.e("AlterEgo:InventoryFragment",
					"No valid character. The user needs to make one.");
		}

		// Inflate the layout for this fragment
		View inventoryView = inflater.inflate(R.layout.inventory_view,
				container, false);
		ListView invListView = (ListView) inventoryView
				.findViewById(R.id.inventory_list);

		CharacterDBHelper dbhelper = new CharacterDBHelper(getActivity());

        /* Manual adapter
		ArrayList<InventoryItem> invItems = dbhelper
				.getInventoryItems(GameActivity.mCharId);

		mInvAdapter = new ArrayAdapter<InventoryItem>(getActivity(),
				android.R.layout.simple_list_item_1, invItems);
        */

        /* Cursor Adapter */
        Cursor invCursor = dbhelper.getInventoryItemsCursor(GameActivity.mCharId);
        int[] ctrlIds = new int[] {android.R.id.text1, android.R.id.text2};
        mInvAdapter = new SimpleCursorAdapter(
                this.getActivity(),
                android.R.layout.simple_list_item_2,
                invCursor,
                new String[] {"item_name", "item_description"},
                ctrlIds,
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		invListView.setAdapter(mInvAdapter);

		// Bind the new-character button to it's appropriate action
		Button newInv = (Button) inventoryView.findViewById(R.id.newinv_button);

		if (GameActivity.mCharId == -1)
			newInv.setEnabled(false);

		newInv.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Spawn the create inventory item dialog

				AlertDialog.Builder invBuilder = new AlertDialog.Builder(v
						.getContext());
				LayoutInflater inflater = getActivity().getLayoutInflater();

				invBuilder
						.setView(
								inflater.inflate(R.layout.new_inv_dialog, null))
						.setPositiveButton(R.string.create,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										AlertDialog thisDialog = (AlertDialog) dialog;

										EditText nameInput = (EditText) thisDialog
												.findViewById(R.id.item_name);
										EditText descInput = (EditText) thisDialog
												.findViewById(R.id.item_desc);

										String name = nameInput.getText()
												.toString();
										String desc = descInput.getText()
												.toString();

										CharacterDBHelper dbHelper = new CharacterDBHelper(
												getActivity());
										// InventoryItem newItem = 
										dbHelper.addInventoryItem(GameActivity.mCharId, name, desc);

										// mInvAdapter.add(newItem);
                                        Cursor c = dbHelper.getInventoryItemsCursor(GameActivity.mCharId);
                                        mInvAdapter.changeCursor(c);
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									// Negative button just closes the dialog
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								});
				invBuilder.create().show();
			}
		});
		return inventoryView;
	}
}
