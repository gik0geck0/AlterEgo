package edu.mines.alterego;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyCustomAdapter extends BaseAdapter {
    private ArrayList<String> mListItems;
    private LayoutInflater mLayoutInflater;
    private CharacterDBHelper mDbHelper;
    private int mGameId;

    public MyCustomAdapter(Context context, ArrayList<String> arrayList, CharacterDBHelper dbh, int gameid) {
        mDbHelper = dbh;
        mListItems = arrayList;
        mGameId = gameid;

        //get the layout inflater
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Check the database
        refreshDB();
    }

    @Override
    public int getCount() {
        //getCount() represents how many items are in the list
        return mListItems.size();
    }

    @Override
    //get the data of an item from a specific position
    //i represents the position of the item in the list
    public Object getItem(int i) {
        return null;
    }

    @Override
    //get the position id of the item from the list
    public long getItemId(int i) {
        return 0;
    }

    @Override

    public View getView(int position, View view, ViewGroup viewGroup) {

        //check to see if the reused view is null or not, if is not null then reuse it
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item, null);
        }

        //get the string item from the position "position" from array list to put it on the TextView
        String stringItem = mListItems.get(position);
        if (stringItem != null) {

            TextView itemName = (TextView) view.findViewById(R.id.list_item_text_view);

            if (itemName != null) {
                //set the item name on the TextView
                itemName.setText(stringItem);
            }
        }

        //this method must return the view corresponding to the data at the specified position.
        return view;

    }

    public void refreshDB() {
        Log.d("AlterEgo::MessageAdapter", "Refreshing the database");
        Cursor mCursor = mDbHelper.getAllMessagesCursor(mGameId);
        mCursor.moveToPosition(mListItems.size());

        while (!mCursor.isAfterLast()) {
            MessageData md = new MessageData(
                mCursor.getInt(0), mCursor.getString(1),
                mCursor.getLong(2), mCursor.getInt(3));
            mListItems.add(md.toString(MessageData.StringFormat.MESSAGE));
            mCursor.moveToNext();
        }

        // Invalidate current set
        notifyDataSetChanged();
    }
}
