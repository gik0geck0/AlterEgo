package edu.mines.alterego;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.mines.alterego.CharacterDBHelper;


/**
 * Description: This class defines the functionality for the inventory fragment.
 * It handles loading fragment data from the database and displaying it in the list view.
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */

public class InventoryFragment extends Fragment {
	int mCharId;
	ArrayAdapter<InventoryItem> mInvAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// TODO: The default should be -1, so that errors are generated. BUT for
		// testing, this was set to 0, for game 0
		mCharId = getArguments().getInt(
				(String) getResources().getText(R.string.gameid), 0);

		if (mCharId == -1) {
			// Yes, this is annoying, but it'll make an error VERY obvious. In
			// testing, I have never seen this toast/error message. But ya never
			// know
			Toast.makeText(getActivity(), "No character. Please make one!", Toast.LENGTH_LONG)
					.show();
			Log.e("AlterEgo:InventoryFragment", "No valid character. The user needs to make one.");
		}

		// Inflate the layout for this fragment
		View inventoryView = inflater.inflate(R.layout.inventory_view,
				container, false);
		ListView invListView = (ListView) inventoryView
				.findViewById(R.id.inventory_list);

		CharacterDBHelper dbhelper = new CharacterDBHelper(getActivity());
		ArrayList<InventoryItem> invItems = dbhelper.getInventoryItems(mCharId);

		mInvAdapter = new ArrayAdapter<InventoryItem>(getActivity(),
				android.R.layout.simple_list_item_1, invItems);
		invListView.setAdapter(mInvAdapter);

		// Bind the new-character button to it's appropriate action
		Button newInv = (Button) inventoryView
				.findViewById(R.id.newinv_button);
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
										InventoryItem newItem = dbHelper
												.addInventoryItem(mCharId,
														name, desc);

										mInvAdapter.add(newItem);
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
