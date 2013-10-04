package edu.mines.alterego;

class CharacterData {
    public int id;
    public String name;
    public String description;

    CharacterData(int gameId, String name, String description) {
        id = gameId;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return name + ": " + description;
    }
}
