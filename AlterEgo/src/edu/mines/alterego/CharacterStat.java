package edu.mines.alterego;

public class CharacterStat {

    private int characterId;
    private int statValue;
    private String statName;
    private int category;

    CharacterStat(int charId, int statVal, String statN, int cat) {
        characterId = charId;
        statValue = statVal;
        statName = statN;
        category = cat;
    }

    @Override
    public String toString() {
        return statName + " " + Integer.toString(statValue);
    }
}