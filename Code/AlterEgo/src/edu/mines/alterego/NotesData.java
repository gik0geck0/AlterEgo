package edu.mines.alterego;

class NotesData {
    int mNoteId;
    String mSubject;
    String mDescription;

    NotesData(int item_id, String name, String description) {
        mNoteId = item_id;
        mSubject = name;
        mDescription = description;
    }

    public int getItemId() { return mNoteId; }
    public String getName() { return mSubject; }
    public String getDescription() { return mDescription; }
}
