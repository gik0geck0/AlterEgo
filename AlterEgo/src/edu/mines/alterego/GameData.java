package edu.mines.alterego;

import android.util.Pair;

/**
 * Description: This class is an abstraction layer to represent game data
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */


class GameData extends Pair<Integer, String> {

	/**
	 * 
	 * @param gameId database id for the game
	 * @param gameName name of the game
	 */
    GameData(Integer gameId, String gameName) {
        super(gameId, gameName);
    }

    @Override
    public String toString() {
        return second;
    }
}
