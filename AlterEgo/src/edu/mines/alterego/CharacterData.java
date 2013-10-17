package edu.mines.alterego;

class CharacterData {
    public int id;
    public String name;
    public String description;

    /**
     * <p>
     * Creates a model object for a Character. Each character must have a name
     * and description, and a game that the character belongs to. These values
     * should come directly from the database.
     * </p>
     *
     * @param gameId    ID to the game to belong to
     * @param name      Name of the character
     * @param description   Description of the character. Could be physical, behavioral, or even a full backstory
     */
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
