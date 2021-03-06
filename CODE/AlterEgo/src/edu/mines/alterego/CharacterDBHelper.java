package edu.mines.alterego;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import edu.mines.alterego.GameData;
import edu.mines.alterego.MessageData;
import edu.mines.alterego.MapActivity.MARKERTYPE;

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
	private static final int DB_VERSION = 4;

	public CharacterDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

    /**
     *  Converts the name of a column into a name that can be shown onscreen.
     *  @param colname Snake-case column name
     *  @return Spaced Capitalized phrase
     */
    public static String getNameOfColumn(String colname) {
        // Make words defined by _ split
        String[] words =  colname.split("_");
        String name = "";
        for (int i=0; i < words.length; i++) {
            // Capitalize the first letter of each word
            words[i] = Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);

            // Join it to the name with spaces. This does words.join(' '), which isn't a Java function
            name += words[i];
            if (i < words.length-1)
                name += " ";
        }

        return name;
    }

	/**
	 * For an SQLiteOpenHelper, the onCreate method is called if and only if the
	 * database-name in question does not already exist. Theoretically, this
	 * should only happen once ever, and after the one time, updates will be
	 * applied for schema updates.
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {

		database.execSQL("CREATE TABLE IF NOT EXISTS map ("
				+ "map_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "game_id INTEGER, "
				+ "FOREIGN KEY(game_id) REFERENCES game(game_id)" + ")");

		// marker_type 1 = player
		// marker_type 2 = treasure
		// marker_type 3 = enemy
		database.execSQL("CREATE TABLE IF NOT EXISTS marker ("
				+ "marker_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "marker_name TEXT, " + "marker_description TEXT, "
				+ "marker_type INTEGER, " + "marker_lat FLOAT, "
				+ "marker_long FLOAT, " + "map_id INTEGER, "
				+ "marker_color FLOAT, "
				+ "FOREIGN KEY(map_id) REFERENCES map(map_id)" + ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS game ( "
				+ "game_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "hosting INTEGER," + "name TEXT" + ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS character ( "
				+ "character_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "name TEXT, " + "description TEXT, " + "game_id INTEGER, "
				+ "FOREIGN KEY(game_id) REFERENCES game(game_id) )");

		database.execSQL("CREATE TABLE IF NOT EXISTS inventory_item ( "
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "description TEXT,"
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
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "inventory_item_id INTEGER,"
				+ "stat_value INTEGER,"
				+ "stat_name INTEGER,"
				+ "description_usage_etc INTEGER,"
				+ "category_id INTEGER,"
				+ "FOREIGN KEY(category_id) REFERENCES category(category_id)"
				+ "FOREIGN KEY(inventory_item_id) REFERENCES inventory_item(_id)"
				+ ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS category ( "
				+ "category_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "category_name TEXT" + ")");

		database.execSQL("CREATE TABLE IF NOT EXISTS notes_data ( "
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "subject TEXT, "
				+ "description TEXT, "
				+ "character_id INTEGER,"
				+ "FOREIGN KEY(character_id) REFERENCES character(character_id)"
				+ ")");

        database.execSQL("CREATE TABLE IF NOT EXISTS messages ( " +
                "message_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "json_message TEXT," +
                "game_id INTEGER," +
                "timestamp INTEGER," +
                "FOREIGN KEY(game_id) REFERENCES game(game_id)" +
                ")");

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

		createMap(c.getInt(c.getColumnIndex("game_id")));

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
	 * 
	 * <p>
	 * When game is created, fill map database with corresponding game
	 * </p>
	 * 
	 */
	public void createMap(int game_id) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues mapVals = new ContentValues();
		mapVals.put("game_id", game_id);

		database.insert("map", null, mapVals);
	}

	/**
	 * 
	 * <p>
	 * Grab marker information when first created and save to the database.
	 * Applies to players, treasures, enemies, etc. Thanks to enum in
	 * MapActivity
	 * </p>
	 * 
	 */
	public MarkerData addDBMarker(String marker_name,
			String marker_description, double marker_lat, double marker_long,
			MARKERTYPE marker_type, int map_id, float color) {
        // Log.d("AlterEgo::Database", "Adding marker " + marker_name + " at (" + marker_lat + "," + marker_long + ")");

		SQLiteDatabase database = getWritableDatabase();

		ContentValues markerVals = new ContentValues();
		markerVals.put("marker_name", marker_name);
		markerVals.put("marker_description", marker_description);
		markerVals.put("marker_lat", marker_lat);
		markerVals.put("marker_long", marker_long);
		markerVals.put("marker_type", marker_type.getValue());
		markerVals.put("map_id", map_id);
		markerVals.put("marker_color", color);

		long rowid = database.insert("marker", null, markerVals);
		String[] args = new String[] { "" + rowid };

		Cursor c = database
				.rawQuery(
						"SELECT marker_id, marker_name, marker_description, marker_lat, marker_long, marker_type, marker_color, map_id"
								+ " FROM marker" + " WHERE ROWID=?", args);
		c.moveToFirst();

        return new MarkerData(
            c.getInt(c.getColumnIndex("map_id")),
            c.getInt(c.getColumnIndex("marker_id")),
            c.getString(c.getColumnIndex("marker_name")),
            c.getString(c.getColumnIndex("marker_description")),
            MARKERTYPE.values()[c.getInt(c.getColumnIndex("marker_type"))],
            c.getDouble(c.getColumnIndex("marker_lat")),
            c.getDouble(c.getColumnIndex("marker_long")),
            c.getFloat(c.getColumnIndex("marker_color"))
            );
	}

    /**
     * <p>
     *      Update a marker in the database with a refreshed MarkerData object
     * </p>
     * @param updatedObj MarkerData object that contains modified attributes. This object will takeover as the object in the database
     * @return The provided Marker upon success. NULL upon failure
     */
    public MarkerData updateDBMarker(MarkerData newObj) {
		SQLiteDatabase database = getWritableDatabase();

        MarkerData oldObj = getMarker(newObj.getMarkerId());

		ContentValues markerVals = new ContentValues();

        // Fill the markerVals with only updated entities
        if (oldObj.getName().equals(newObj.getName())) {
            markerVals.put("marker_name", newObj.getName());
        }
        if (oldObj.getDescription().equals(newObj.getDescription())) {
            markerVals.put("marker_description", newObj.getDescription());
        }
        if (oldObj.getLat() != newObj.getLat()) {
            markerVals.put("marker_lat", newObj.getLat());
        }
        if (oldObj.getLong() != newObj.getLong()) {
            markerVals.put("marker_long", newObj.getLong());
        }
        if (oldObj.getMarkerType() != newObj.getMarkerType()) {
            markerVals.put("marker_type", newObj.getMarkerType().getValue());
        }
        if (oldObj.getMapId() != newObj.getMapId()) {
            markerVals.put("map_id", newObj.getMapId());
        }
        if (oldObj.getColor() != newObj.getColor()) {
            markerVals.put("marker_color", newObj.getColor());
        }

        String[] args = { ""+newObj.getMarkerId() };
		int rowsAffected = database.update("marker", markerVals, "marker_id=?", args);

		Cursor c = database
				.rawQuery(
						"SELECT marker_id, marker_name, marker_description, marker_lat, marker_long, marker_type, marker_color, map_id"
								+ " FROM marker" + " WHERE marker_id=?", args);
		c.moveToFirst();

        if (rowsAffected > 1) {
            Log.e("AlterEgo::Database::MarkerUpdate", "There are many rows with the same marker id. This is a critical error; The database is BONED.");
        }

        return new MarkerData(
            c.getInt(c.getColumnIndex("map_id")),
            c.getInt(c.getColumnIndex("marker_id")),
            c.getString(c.getColumnIndex("marker_name")),
            c.getString(c.getColumnIndex("marker_description")),
            MARKERTYPE.values()[c.getInt(c.getColumnIndex("marker_type"))],
            c.getDouble(c.getColumnIndex("marker_lat")),
            c.getDouble(c.getColumnIndex("marker_long")),
            c.getFloat(c.getColumnIndex("marker_color"))
            );
    }

    public MarkerData getMarker(int markerId) {
		SQLiteDatabase database = getReadableDatabase();

		String[] args = { Integer.toString(markerId) };
		Cursor c = database.rawQuery("SELECT * FROM marker INNER JOIN map on map.map_id = marker.map_id WHERE marker_id=?", args);
		//Cursor c = database.rawQuery("SELECT * FROM marker", null);
		c.moveToFirst();
		
        MarkerData marker = null;

        if (!c.isAfterLast()) {
            marker = new MarkerData(
                c.getInt(c.getColumnIndex("map_id")),
                c.getInt(c.getColumnIndex("marker_id")),
                c.getString(c.getColumnIndex("marker_name")),
                c.getString(c.getColumnIndex("marker_description")),
                MARKERTYPE.values()[c.getInt(c.getColumnIndex("marker_type"))],
                c.getDouble(c.getColumnIndex("marker_lat")),
                c.getDouble(c.getColumnIndex("marker_long")),
                c.getFloat(c.getColumnIndex("marker_color"))
                );
        }

		return marker;
    }

	/**
	 * 
	 */
	public ArrayList<MarkerData> loadMarkers(int mapId) {
		SQLiteDatabase database = getReadableDatabase();

		String[] args = { Integer.toString(mapId) };
		Cursor c = database.rawQuery("SELECT * FROM marker INNER JOIN map on map.map_id = marker.map_id WHERE game_id=?", args);
		//Cursor c = database.rawQuery("SELECT * FROM marker", null);
		c.moveToFirst();
		// Log.i("AlterEgo::CharacterDBHelper", "loadMarkers: mapId: " + Integer.toString(mapId));
		ArrayList<MarkerData> markers = new ArrayList<MarkerData>();
		
		while(!c.isAfterLast()) {
			// Log.i("AlterEgo::CharacterDBHelper", "Inside while loop");
			markers.add(new MarkerData(
                c.getInt(c.getColumnIndex("map_id")),
                c.getInt(c.getColumnIndex("marker_id")),
				c.getString(c.getColumnIndex("marker_name")),
                c.getString(c.getColumnIndex("marker_description")),
				MARKERTYPE.values()[c.getInt(c.getColumnIndex("marker_type"))],
				c.getDouble(c.getColumnIndex("marker_lat")),
                c.getDouble(c.getColumnIndex("marker_long")),
                c.getFloat(c.getColumnIndex("marker_color"))
                ));
			c.moveToNext();
		}
		return markers;
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

		return new NotesData(c.getInt(c.getColumnIndex("_id")),
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
	 * Finds associated game with map
	 * </p>
	 * 
	 * @return map_id for the given gameid
	 */
	public String getGameIdForMap(int gameId) {
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT map_id FROM maps WHERE game.game_id =? LIMIT 1",
				new String[] { "" + gameId });
		cursor.moveToFirst();
		if (cursor.getCount() < 1) {
			return "Game/Map Not Available";
		} else {
			return cursor.getString(cursor.getColumnIndex("map_id"));
		}
	}

	/**
	 * <p>
	 * Finds associate map with markers
	 * </p>
	 * 
	 * @return marker_id for the given map_id
	 */
	public String getMarkersForMap(int mapId) {
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT * FROM marker WHERE map.map_id =?",
				new String[] { "" + mapId });
		cursor.moveToFirst();
		if (cursor.getCount() < 1) {
			return "Map/Marker Not Available";
		} else {
			return cursor.getString(cursor.getColumnIndex("marker_id"));
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

		Cursor invCursor = getReadableDatabase()
				.rawQuery(
						"SELECT "
								+ "character.character_id,"
								+ "inventory_item._id,"
								+ "inventory_item.name AS 'item_name',"
								+ "inventory_item.description AS 'item_description'"
								+ "FROM character "
								+ "INNER JOIN inventory_item ON inventory_item.character_id = character.character_id "
								+ "WHERE character.character_id = ?",
						new String[] { "" + characterId });
		ArrayList<InventoryItem> invList = new ArrayList<InventoryItem>();
		invCursor.moveToFirst();

		int iidCol = invCursor.getColumnIndex("_id");
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
	 * Lookup all the inventory items that a character has.
	 * </p>
	 * 
	 * @param characterId
	 *            The ID for the character to search
	 * @return A cursor to the character's inventory
	 */
	public Cursor getInventoryItemsCursor(int characterId) {

		Cursor invCursor = getReadableDatabase()
				.rawQuery(
						"SELECT "
								+ "character.character_id,"
								+ "inventory_item._id,"
								+ "inventory_item.name AS 'item_name',"
								+ "inventory_item.description AS 'item_description'"
								+ "FROM character "
								+ "INNER JOIN inventory_item ON inventory_item.character_id = character.character_id "
								+ "WHERE character.character_id = ?",
						new String[] { "" + characterId });
		invCursor.moveToFirst();

		return invCursor;
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
		// Log.i("AlterEgos::CharacterDBHelper::characterId", "characterId " + characterId);
		// Verify that the name and description columns exist
		// This is done here because
		Cursor notesCursor = getNotesDataCursor(characterId);

		ArrayList<NotesData> notesList = new ArrayList<NotesData>();
		notesCursor.moveToFirst();
		// Log.i("AlterEgos::characterDBHelper::notesCursor", "notesCursor " + notesCursor.getCount());
		int nidCol = notesCursor.getColumnIndex("_id");
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
	 * Lookup all the notes a character has taken.
	 * </p>
	 * 
	 * @param characterId
	 *            The ID for the character to search
	 * @return A list of NotesData representing the character's notebook
	 */
	public Cursor getNotesDataCursor(int characterId) {
		// Log.i("AlterEgos::CharacterDBHelper::characterId", "characterId " + characterId);
		// Verify that the name and description columns exist
		// This is done here because
		Cursor notesCursor = getReadableDatabase()
				.rawQuery(
						"SELECT "
								+ "character.character_id,"
								+ "notes_data._id,"
								+ "notes_data.subject AS 'notes_subject',"
								+ "notes_data.description AS 'notes_description'"
								+ "FROM character "
								+ "INNER JOIN notes_data ON notes_data.character_id = character.character_id "
								+ "WHERE character.character_id = ?",
						new String[] { "" + characterId });
		notesCursor.moveToFirst();

		return notesCursor;
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
				c.getInt(c.getColumnIndex("_id")), c.getString(c
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
	public void insertCharStat(int charId, int statVal, String statName, String desc, int category) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues statVals = new ContentValues();
		statVals.put("character_id", charId);
		statVals.put("stat_value", statVal);
		statVals.put("stat_name", statName);
		statVals.put("category_id", category);
        statVals.put("description_usage_etc", desc);

		db.insert("character_stat", null, statVals);
	}

	/**
	 * <p>
	 * Update statistic for a character. Ex: charId = 1, statVal = 9, statName
	 * = 'Charisma', category = 0 This will at the Charisma value of 9 to the
	 * character
	 * </p>
	 * 
	 * @param charID
	 *            ID for the character to be 'buffed'
	 * @param statVal
	 *            Numeric value for the stat
	 */
	public void updateCharStat(int statId, int statVal, String statName,
			String desc, int category) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues statVals = new ContentValues();
		statVals.put("stat_value", statVal);
		statVals.put("stat_name", statName);
        statVals.put("description_usage_etc", desc);
		statVals.put("category_id", category);

        String[] args = { Integer.toString(statId) };
		db.update("character_stat", statVals, "_id=?", args);
	}

    /**
     * <p>
     * Remove a specific character stat
     * </p>
     *
     * @param charStatId ID that references the character stat
     */
	public void deleteCharStat(int charStatId) {
		SQLiteDatabase database = getWritableDatabase();

		String[] args = new String[] { Integer.toString(charStatId) };
		database.delete("character_stat", "_id=?", args);
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
						"SELECT stat_name, stat_value, _id, character_id FROM character_stat WHERE character_id=?",
						whereArgs);
		statCursor.moveToFirst();
		ArrayList<CharacterStat> stats = new ArrayList<CharacterStat>();
		while (!statCursor.isAfterLast()) {
			stats.add(createCharacterStatFromCursor(statCursor));
		}
		return stats;
	}

    public static CharacterStat createCharacterStatFromCursor(Cursor statCursor) {
        String statName = statCursor.getString(statCursor.getColumnIndex("stat_name"));
        int statVal = statCursor.getInt(statCursor.getColumnIndex("stat_value"));
        int statId = statCursor.getInt(statCursor.getColumnIndex("_id"));
        int charId = statCursor.getInt(statCursor.getColumnIndex("character_id"));
        return new CharacterStat(charId, statId, statVal, statName, 0);
    }

	/**
	 * <p>
	 * Lookup all the stats for a given character
	 * </p>
	 * 
	 * @param charId
	 *            ID for the character to be analyzed
	 * @return Cursor pointing to the character stats
	 */
	public Cursor getStatsForCharacterCursor(int charId) {
		SQLiteDatabase db = getReadableDatabase();
		String[] whereArgs = new String[1];
		whereArgs[0] = Integer.toString(charId);
		Cursor statCursor = db
				.rawQuery(
						"SELECT * FROM character_stat WHERE character_id=?",
						whereArgs);
        statCursor.moveToFirst();
        return statCursor;
	}

	/**
	 * <p>
	 * Lookup one specific stat for a given character
	 * </p>
	 * 
	 * @param charId
	 *            ID for the character to be analyzed
	 * @return Cursor pointing to the character stats
	 */
	public Cursor getSpecificStatForCharacterCursor(int charId, int statId) {
		SQLiteDatabase db = getReadableDatabase();
		String[] whereArgs = new String[2];
		whereArgs[0] = Integer.toString(charId);
		whereArgs[1] = Integer.toString(statId);
		Cursor statCursor = db.rawQuery(
            "SELECT * FROM character_stat WHERE character_id=? and _id=?",
            whereArgs);
        statCursor.moveToFirst();
        return statCursor;
	}

    public ArrayList<MessageData> getAllMessages(int gameId) {
    	SQLiteDatabase db = getReadableDatabase();
    	String[] whereArgs = new String[1];
    	whereArgs[0] = Integer.toString(gameId);
        Cursor c = db.rawQuery("SELECT message_id, json_message, timestamp, game_id FROM messages WHERE game_id=?", whereArgs);
        c.moveToFirst();

        ArrayList<MessageData> messages = new ArrayList<MessageData>();
        while(!c.isAfterLast()) {
        	messages.add(new MessageData(
                        c.getInt(0),
                        c.getString(1),
                        c.getLong(2),
                        c.getInt(3)
                        ));
        }
        return messages;
    }

    public Cursor getAllMessagesCursor(int gameId) {
    	SQLiteDatabase db = getReadableDatabase();
    	String[] whereArgs = new String[1];
    	whereArgs[0] = Integer.toString(gameId);
        Cursor c = db.rawQuery("SELECT message_id, json_message, timestamp, game_id FROM messages WHERE game_id=?", whereArgs);
        c.moveToFirst();
        return c;
    }

    public MessageData insertMessage(int gameId, String message) {
        // Log.d("AlterEgo::Database::Message", "Inserting message: " + message);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues msgVals = new ContentValues();
        msgVals.put("game_id", gameId);
        msgVals.put("json_message", message);

        // Place the current timestamp
        msgVals.put("timestamp", now());

        long rowid = db.insert("messages", null, msgVals);

        String[] args = new String[]{ ""+rowid };
        Cursor c = db.rawQuery("SELECT message_id, json_message, timestamp, game_id  FROM messages WHERE messages.ROWID =?", args);
        c.moveToFirst();

        return new MessageData(
                        c.getInt(0),
                        c.getString(1),
                        c.getLong(2),
                        c.getInt(3)
                        );
    }

    public static long now() {
        return (new Date()).getTime();
    }

    public static int getType(String column, Cursor c) {
        int type;
        if (
            column.equals("hosting")
            ) {
            // Boolean
            type = 5;
        } else if (
            column.equals("timestamp") ||
            column.equals("stat_value") ||
            column.equals("marker_type")
            ) {
            // int
            type = Cursor.FIELD_TYPE_INTEGER;
        } else if (
            column.equals("category_name") ||
            column.equals("subject") ||
            column.equals("json_message") ||
            column.equals("stat_name") ||
            column.equals("description_usage_etc") ||
            column.equals("marker_name") ||
            column.equals("name") ||
            column.equals("description") ||
            column.equals("marker_description")
            ) {
            // string
            type = Cursor.FIELD_TYPE_STRING;
        } else if (
            column.equals("marker_color") ||
            column.equals("marker_lat") ||
            column.equals("marker_long")
            ) {
            type = Cursor.FIELD_TYPE_FLOAT;
        } else {
            type = Cursor.FIELD_TYPE_NULL;
        }
        Log.d("AlterEgo::DB::TypeLookup", "Type of " + column + " is " + type);
        return type;
    }
}
