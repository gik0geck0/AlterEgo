package edu.mines.alterego;

import android.util.Pair;

class GameData extends Pair<Integer, String> {

    GameData(Integer gameId, String gameName) {
        super(gameId, gameName);
    }

    @Override
    public String toString() {
        return first + ": " + second;
    }
}
