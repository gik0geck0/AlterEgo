package edu.mines.alterego;

class NotesData {

    int mNoteId;
    String mSubject;
    String mDescription;

    /**
     * Create a new note. Each note needs a subject (like a title), and a
     * description. This info should come straight from the database. This
     * is basically a model object.
     *
     * @param notesId       Id for the note
     * @param subject       Brief title for the note
     * @param description   Full description/content for the note
     */
    NotesData(int notesId, String subject, String description) {
        mNoteId = notesId;
        mSubject = subject;
        mDescription = description;
    }

    // Getters
    public int getNoteId() { return mNoteId; }
    public String getSubject() { return mSubject; }
    public String getDescription() { return mDescription; }

    // Cutoff length for displaying the description
    public static int showableDescLength = 25;
    public String toString() {
    	int cutIndex = showableDescLength;
    	if (mDescription.length() <= cutIndex) {
    		cutIndex = mDescription.length();
    	}
    	return mSubject + ": " + mDescription.substring(0, cutIndex);
    }
}
