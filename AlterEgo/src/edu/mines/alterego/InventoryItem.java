package edu.mines.alterego;

class InventoryItem {
    int mItemId;
    String mName;
    String mDescription;

    InventoryItem(int item_id, String name, String description) {
        mItemId = item_id;
        mName = name;
        mDescription = description;
    }

    public int getItemId() { return mItemId; }
    public String getName() { return mName; }
    public String getDescription() { return mDescription; }

    public static int showableDescLength = 25;
    
    public String toString() {
        int cutIndex = showableDescLength;
        if (mDescription.length() <= cutIndex)
            cutIndex = mDescription.length();
        return mName + ": " + mDescription.substring(0,cutIndex);
    }
}
