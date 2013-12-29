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

public class ModelAdapter<T> extends BaseAdapter {
    private ArrayList<T> mListItems;
    private CursorFetcher mCFetcher;
    private LayoutInflater mLayoutInflater;
    private ModelInitializer<T> mInitializer;

    ModelAdapter(Context context, CursorFetcher cfetcher, ModelInitializer<T> initer) {
        mListItems = new ArrayList<T>();
        mCFetcher = cfetcher;
        mInitializer = initer;

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
    public T getItem(int i) {
        return mListItems.get(i);
    }

    @Override
    //get the position id of the item from the list
    public long getItemId(int i) {
        return 0;
    }

    @Override

    public View getView(int position, View view, ViewGroup viewGroup) {
        Log.d("AlterEgo::ModelAdapter", "Getting the Adapter View at position " + position);

        //check to see if the reused view is null or not, if is not null then reuse it
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item, null);
        }

        //get the string item from the position "position" from array list to put it on the TextView
        String stringItem = mListItems.get(position).toString();
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
        Log.d("AlterEgo::ModelAdapter", "Refreshing the database");
        Cursor mCursor = mCFetcher.fetch();

        if (mListItems.size() > mCursor.getCount()) {
            mListItems.clear();
            mCursor.moveToFirst();
        } else {
            mCursor.moveToPosition(mListItems.size());
        }

        while (!mCursor.isAfterLast()) {
            T dto = mInitializer.initialize(mCursor);
            /*
            MessageData md = new MessageData(
                mCursor.getInt(0), mCursor.getString(1),
                mCursor.getLong(2), mCursor.getInt(3));
            */
            // mListItems.add(md.toString(MessageData.StringFormat.MESSAGE));
            mListItems.add(dto);
            mCursor.moveToNext();
        }

        Log.d("AlterEgo::ModelAdapter", "Notifying Data Set Changing");
        // Invalidate current set
        notifyDataSetChanged();
    }
}
