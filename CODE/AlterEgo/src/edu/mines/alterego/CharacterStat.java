package edu.mines.alterego;

import android.database.Cursor;

/**
 * Description: This class was originally designed to be an abstraction layer for character stats
 * This class is currently unused but is retained for future use.
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */
public class CharacterStat {

    private int characterId;
    private int statId;
    private int statValue;
    private String statName;
    private int category;

    /**
     * 
     * @param charId The database primary key for character
     * @param statVal The numeric value for the current stat
     * @param statN The name for the current stat
     * @param cat The category for the current stat
     */
    CharacterStat(int charId, int statId, int statVal, String statN, int cat) {
        characterId = charId;
        this.statId = statId;
        statValue = statVal;
        statName = statN;
    }

    /**
     * @param cursor Cursor that's currently pointing to a row that will be used to populate this object
     */
    CharacterStat(Cursor cursor) {
        CharacterDBHelper.createCharacterStatFromCursor(cursor);
    }

    @Override
    public String toString() {
        return statName + " " + Integer.toString(statValue);
    }

	public int getCharacterId() {
		return characterId;
	}


	public int getCategory() {
		return category;
	}

    public int getStatId() {
        return statId;
    }
}
