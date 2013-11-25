package edu.mines.alterego;

import android.content.Context;
import android.content.ContentValues;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import android.util.Log;

import java.util.ArrayList;

import edu.mines.alterego.GameData;

/**
 * <h1>SQLite Database Adapter (helper as Google/Android calls it)</h1>
 * 
 * Offers many static functions that can be used to update or view
 * game-statistics in the database The API follows the general rule of first
 * aquiring the database via 'getWritable/ReadableDatabase', then using the
 * static functions defined in this class to interact with the database.
 * 
 * @author: Matt Buland
 */
public class CharacterDBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "alterego";
	private static final int DB_VERSION = 3;

	public CharacterDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	/**
	 * For an SQLiteOpenHelper, the onCreate method is called if and only if the
	 * database-name in question does not already exist. Theoretically, this
	 * should only happen once ever, and after the one time, updates will be
	 * applied for schema updates.
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {

		database.execSQL("CREATE TABLE IF NOT EXISTS game ( "
				+ "game_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "hosting INTEGER," + "name TEXT" + ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS character ( "
				+ "character_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "name TEXT, " + "description TEXT, " + "game_id INTEGER, "
				+ "FOREIGN KEY(game_id) REFERENCES game(game_id) )");

		database.execSQL("CREATE TABLE IF NOT EXISTS inventory_item ( "
				+ "inventory_item_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "character_id INTEGER,"
				+ "FOREIGN KEY(character_id) REFERENCES character(character_id)"
				+ ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS character_stat ( "
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "character_id INTEGER,"
				+ "stat_value INTEGER,"
				+ "stat_name TEXT,"
				+ "description_usage_etc TEXT,"
				+ "category_id INTEGER,"
				+ "FOREIGN KEY(character_id) REFERENCES character(character_id)"
				+ "FOREIGN KEY(category_id) REFERENCES category(category_id)"
				+ ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS item_stat ( "
				+ "item_stat_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "inventory_item_id INTEGER,"
				+ "stat_value INTEGER,"
				+ "stat_name INTEGER,"
				+ "description_usage_etc INTEGER,"
				+ "category_id INTEGER,"
				+ "FOREIGN KEY(category_id) REFERENCES category(category_id)"
				+ "FOREIGN KEY(inventory_item_id) REFERENCES inventory_item(inventory_item_id)"
				+ ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS category ( "
				+ "category_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "category_name TEXT" + ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS notes_data ( "
				+ "notes_data_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "subject TEXT, "
				+ "description TEXT, "
				+ "character_id INTEGER,"
				+ "FOREIGN KEY(character_id) REFERENCES character(character_id)"
				+ ")");

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		// Do Nothing.
	}

	/**
	 * <p>
	 * Queries the database for all the games. Will return a list of GameData
	 * objects which have an ID, and a description for the game.
	 * </p>
	 * 
	 * @return ArrayList of GameData objects that hold all the games
	 */
	public ArrayList<GameData> getGames() {
		Cursor dbGames = getReadableDatabase().rawQuery(
				"SELECT game_id, name, hosting from game", null);
		dbGames.moveToFirst();
		ArrayList<GameData> games = new ArrayList<GameData>();
		while (!dbGames.isAfterLast()) {
			games.add(new GameData(dbGames.getInt(0), dbGames.getString(1),
					dbGames.getInt(2)));
			dbGames.moveToNext();
		}
		dbGames.close();
		return games;
	}

	/**
	 * <p>
	 * Add a new game to the database with a given description. The returned
	 * GameData object will contain the id that can be used to reference it.
	 * </p>
	 * 
	 * @param name
	 *            Description for the new game
	 * @return GameData object representing the newly created Game.
	 */
	public GameData addGame(String name, int hosting) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues gamevals = new ContentValues();
		gamevals.put("name", name);
		gamevals.put("hosting", hosting);

		long rowid = database.insert("game", null, gamevals);
		String[] args = new String[] { "" + rowid };

		Cursor c = database.rawQuery(
				"SELECT game_id, name, hosting FROM game WHERE game.ROWID =?",
				args);
		c.moveToFirst();

		return new GameData(c.getInt(c.getColumnIndex("game_id")),
				c.getString(c.getColumnIndex("name")), c.getInt(c
						.getColumnIndex("hosting")));
	}

	/**
	 * <p>
	 * Modifies the game (identified by gameId) to have a new game
	 * name/description.
	 * </p>
	 * 
	 * @param gameId
	 *            GameID identifying the game to be changed.
	 * @param gameName
	 *            New name for the game
	 */
	public void updateGame(int gameId, String gameName) {
		SQLiteDatabase database = getWritableDatabase();
		ContentValues cvs = new ContentValues();
		cvs.put("name", gameName);
		String[] args = { Integer.toString(gameId) };
		database.update("game", cvs, "game_id=?", args);
	}

	/**
	 * <p>
	 * Delete a game from the database that's identified by its ID.
	 * </p>
	 * 
	 * @param gameId
	 *            ID for the game to be deleted
	 */
	public void deleteGame(int gameId) {
		SQLiteDatabase database = getWritableDatabase();

		String[] args = new String[] { Integer.toString(gameId) };
		database.delete("game", "game_id=?", args);
	}

	/**
	 * <p>
	 * Add a new note for a character to the database with a subject and
	 * description. The returned NotesData object will contain the
	 * object-representation of the newly created row.
	 * </p>
	 * 
	 * @param subject
	 *            Brief name for the note
	 * @param desc
	 *            Full description/content of the note
	 * @return NotesData object representing the newly created note
	 */
	public NotesData addNote(int charId, String subject, String desc) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues notevals = new ContentValues();
		notevals.put("character_id", charId);
		notevals.put("subject", subject);
		notevals.put("description", desc);

		long rowid = database.insert("notes_data", null, notevals);
		String[] args = new String[] { "" + rowid };

		Cursor c = database.rawQuery(
				"SELECT * FROM notes_data WHERE notes_data.ROWID =?", args);
		c.moveToFirst();

		return new NotesData(c.getInt(c.getColumnIndex("notes_data_id")),
				c.getString(c.getColumnIndex("subject")), c.getString(c
						.getColumnIndex("description")));
	}

	/**
	 * <p>
	 * Looks up the character for a given game, and returns its ID. It's assumed
	 * that there's 1 character per game.
	 * </p>
	 * 
	 * @return character Id for the given game.
	 */
	public int getCharacterIdForGame(int gameId) {
		Cursor cursor = getReadableDatabase()
				.rawQuery(
						"SELECT character_id FROM character WHERE character.game_id = ? LIMIT 1",
						new String[] { "" + gameId });
		cursor.moveToFirst();
		if (cursor.getCount() < 1) {
			return -1;
		} else {
			return cursor.getInt(cursor.getColumnIndex("character_id"));
		}
	}

	/**
	 * <p>
	 * Finds associated game name with character and returns game name. This
	 * will be used as the action bar title for the game activity
	 * </p>
	 * 
	 * @return game name for the given gameid
	 */
	public String getGameNameForCharacterId(int gameId) {
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT name FROM game WHERE game.game_id =? LIMIT 1",
				new String[] { "" + gameId });
		cursor.moveToFirst();
		if (cursor.getCount() < 1) {
			return "Game Not Available";
		} else {
			return cursor.getString(cursor.getColumnIndex("name"));
		}

	}

	/**
	 * <p>
	 * Add a new character to the database for a given game, the name of the
	 * character, and the character's description/backstory.
	 * </p>
	 * 
	 * @param gameId
	 *            ID for the game the character should be added to
	 * @param name
	 *            The character's name
	 * @param desc
	 *            A description of the character
	 * @return CharacterData object representing the newly created character
	 */
	public CharacterData addCharacter(int gameId, String name, String desc) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues gamevals = new ContentValues();
		gamevals.put("name", name);
		gamevals.put("description", desc);
		gamevals.put("game_id", gameId);

		long rowid = database.insert("character", null, gamevals);

		String[] args = new String[] { "" + rowid };
		Cursor c = database.rawQuery(
				"SELECT * FROM character WHERE character.ROWID =?", args);
		c.moveToFirst();

		return new CharacterData(c.getInt(c.getColumnIndex("character_id")),
				c.getString(c.getColumnIndex("name")), c.getString(c
						.getColumnIndex("description")));
	}

	/**
	 * <p>
	 * Get a CharacterData object to represent the character row in the
	 * database.
	 * </p>
	 * 
	 * @param charId
	 *            Character ID to lookup
	 * @return CharacterData object representing the character. Will return null
	 *         if the character does not exist
	 */
	public CharacterData getCharacter(int charId) {
		Cursor c = getReadableDatabase()
				.rawQuery(
						"SELECT * FROM character WHERE character.character_id = ? LIMIT 1",
						new String[] { "" + charId });
		c.moveToFirst();
		if (c.getCount() < 1) {
			// No character with that id available. That's probably bad.
			return null;
		} else {
			return new CharacterData(
					c.getInt(c.getColumnIndex("character_id")), c.getString(c
							.getColumnIndex("name")), c.getString(c
							.getColumnIndex("description")));
		}
	}

	/**
	 * <p>
	 * Lookup all the inventory items that a character has.
	 * </p>
	 * 
	 * @param characterId
	 *            The ID for the character to search
	 * @return A list of InventoryItems representing the character's inventory
	 */
	public ArrayList<InventoryItem> getInventoryItems(int characterId) {

		// Verify that the name and description columns exist
		// This is done here because
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT * FROM inventory_item LIMIT 0", null);
		if (cursor.getColumnIndex("name") < 0
				|| cursor.getColumnIndex("description") < 0) {
			Log.i("AlterEgo::CharacterDBHelper",
					"The name and description columns didn't exist. Dropping the table, and resetting it");
			SQLiteDatabase database = getWritableDatabase();
			database.execSQL("DROP TABLE inventory_item");
			database.execSQL("CREATE TABLE IF NOT EXISTS inventory_item ( "
					+ "inventory_item_id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "name TEXT, "
					+ "description TEXT, "
					+ "character_id INTEGER,"
					+ "FOREIGN KEY(character_id) REFERENCES character(character_id)"
					+ ")");
		}

		Cursor invCursor = getReadableDatabase()
				.rawQuery(
						"SELECT "
								+ "character.character_id,"
								+ "inventory_item.inventory_item_id,"
								+ "inventory_item.name AS 'item_name',"
								+ "inventory_item.description AS 'item_description'"
								+ "FROM character "
								+ "INNER JOIN inventory_item ON inventory_item.character_id = character.character_id "
								+ "WHERE character.character_id = ?",
						new String[] { "" + characterId });
		ArrayList<InventoryItem> invList = new ArrayList<InventoryItem>();
		invCursor.moveToFirst();

		int iidCol = invCursor.getColumnIndex("inventory_item_id");
		int iNameCol = invCursor.getColumnIndex("item_name");
		int iDescCol = invCursor.getColumnIndex("item_description");
		while (!invCursor.isAfterLast()) {
			invList.add(new InventoryItem(invCursor.getInt(iidCol), invCursor
					.getString(iNameCol), invCursor.getString(iDescCol)));
			invCursor.moveToNext();
		}
		return invList;
	}

	/**
	 * <p>
	 * Lookup all the notes a character has taken.
	 * </p>
	 * 
	 * @param characterId
	 *            The ID for the character to search
	 * @return A list of NotesData representing the character's notebook
	 */
	public ArrayList<NotesData> getNotesData(int characterId) {
		Log.i("AlterEgos::CharacterDBHelper::characterId", "characterId "
				+ characterId);
		// Verify that the name and description columns exist
		// This is done here because
		Cursor notesCursor = getReadableDatabase()
				.rawQuery(
						"SELECT "
								+ "character.character_id,"
								+ "notes_data.notes_data_id,"
								+ "notes_data.subject AS 'notes_subject',"
								+ "notes_data.description AS 'notes_description'"
								+ "FROM character "
								+ "INNER JOIN notes_data ON notes_data.character_id = character.character_id "
								+ "WHERE character.character_id = ?",
						new String[] { "" + characterId });
		ArrayList<NotesData> notesList = new ArrayList<NotesData>();
		notesCursor.moveToFirst();
		Log.i("AlterEgos::characterDBHelper::notesCursor", "notesCursor "
				+ notesCursor.getCount());
		int nidCol = notesCursor.getColumnIndex("notes_data_id");
		int nNameCol = notesCursor.getColumnIndex("notes_subject");
		int nDescCol = notesCursor.getColumnIndex("notes_description");
		while (!notesCursor.isAfterLast()) {
			notesList.add(new NotesData(notesCursor.getInt(nidCol), notesCursor
					.getString(nNameCol), notesCursor.getString(nDescCol)));
			notesCursor.moveToNext();
		}
		return notesList;
	}

	/**
	 * <p>
	 * Add a new invetory item to a character's inventory.
	 * </p>
	 * 
	 * @param charId
	 *            ID for the character who will have this item in their
	 *            inventory
	 * @param name
	 *            Name of the item
	 * @param desc
	 *            Description of the item. May include usage instructions
	 * @return Model object representing the newly created inventory item
	 */
	public InventoryItem addInventoryItem(int charId, String name, String desc) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues gamevals = new ContentValues();
		gamevals.put("name", name);
		gamevals.put("description", desc);
		gamevals.put("character_id", charId);

		long rowid = database.insert("inventory_item", null, gamevals);

		String[] args = new String[] { "" + rowid };
		Cursor c = database.rawQuery(
				"SELECT * FROM inventory_item WHERE inventory_item.ROWID =?",
				args);
		c.moveToFirst();

		return new InventoryItem(
				c.getInt(c.getColumnIndex("inventory_item_id")), c.getString(c
						.getColumnIndex("name")), c.getString(c
						.getColumnIndex("description")));
	}

	/**
	 * <p>
	 * Add a new statistic to a character. Ex: charId = 1, statVal = 9, statName
	 * = 'Charisma', category = 0 This will at the Charisma value of 9 to the
	 * character
	 * </p>
	 * 
	 * @param charID
	 *            ID for the character to be 'buffed'
	 * @param statVal
	 *            Numeric value for the stat
	 */
	public void insertCharStat(int charId, int statVal, String statName,
			int category) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues statVals = new ContentValues();
		statVals.put("character_id", charId);
		statVals.put("stat_value", statVal);
		statVals.put("stat_name", statName);
		statVals.put("category_id", category);

		db.insert("character_stat", null, statVals);
	}

	/**
	 * <p>
	 * Lookup all the stats for a given character
	 * </p>
	 * 
	 * @param charId
	 *            ID for the character to be analyzed
	 * @return List of CharacterStats for the character
	 */
	public ArrayList<CharacterStat> getStatsForCharacter(int charId) {
		SQLiteDatabase db = getReadableDatabase();
		String[] whereArgs = new String[1];
		whereArgs[0] = Integer.toString(charId);
		Cursor statCursor = db
				.rawQuery(
						"SELECT stat_name, stat_value FROM character_stat WHERE character_id=?",
						whereArgs);
		statCursor.moveToFirst();
		ArrayList<CharacterStat> stats = new ArrayList<CharacterStat>();
		while (!statCursor.isAfterLast()) {
			String statName = statCursor.getString(0);
			int statVal = statCursor.getInt(1);
			CharacterStat stat = new CharacterStat(charId, statVal, statName, 0);
			stats.add(stat);
		}
		return stats;
	}

	/**
	 * <p>
	 * Cursor-version of getStatsForCharacter: Lookup all the stats for a given
	 * character, and return a Cursor for the results
	 * </p>
	 * 
	 * @param charId
	 *            ID for the character to be analyzed
	 * @return Cursor to the character's stats
	 */
	public Cursor getStatsForCharCursor(int charId) {
		SQLiteDatabase db = getReadableDatabase();
		String[] whereArgs = new String[1];
		whereArgs[0] = Integer.toString(charId);
		Cursor statCursor = db
				.rawQuery(
						"SELECT _id, stat_name, stat_value FROM character_stat WHERE character_id=?",
						whereArgs);
		return statCursor;
	}
}
