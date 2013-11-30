package edu.mines.alterego;


/**
 * Description: This class is an abstraction layer to represent game data
 * @author Matt Buland, Maria Deslis, Eric Young
 *
 */


class MapData {
    int mapId;

	/**
	 * 
	 * @param mapId database id for the map
	 * 
	 */
    MapData(Integer mapId) {
        this.mapId = mapId;
    }
}
