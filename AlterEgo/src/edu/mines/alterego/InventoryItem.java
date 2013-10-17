package edu.mines.alterego;


/**
 * Description: This class defines the inventory abstraction
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */

class InventoryItem {
    int mItemId;
    String mName;
    String mDescription;
/**
 * 
 * @param item_id  Item id in database
 * @param name  Item name
 * @param description  Item description
 */
    InventoryItem(int item_id, String name, String description) {
        mItemId = item_id;
        mName = name;
        mDescription = description;
    }

    /**
     * Getter for GameID
     * @return returns int gameId
     */
    public int getItemId() { return mItemId; }
    
    /**
     * Getter for Name
     * @return Returns string name
     */
    public String getName() { return mName; }
    
    /**
     * Getter for description
     * @return Returns string of description
     */
    public String getDescription() { return mDescription; }

    public static int showableDescLength = 25;
    
    public String toString() {
        int cutIndex = showableDescLength;
        if (mDescription.length() <= cutIndex)
            cutIndex = mDescription.length();
        return mName + ": " + mDescription.substring(0,cutIndex);
    }
}
