package edu.mines.alterego;

class MarkerData {
    public int map_id;
    public String marker_name;
    public String marker_description;
    public int marker_type;
    public double marker_lat;
    public double marker_long;

    /**
     * <p>
     * Creates a model object for the marker. Each marker must have a name
     * and description,type, and position (lat and long), and a map that the marker belongs to. These values
     * should come directly from the database.
     * </p>
     *
     * @param mapId :: corresponding mapId to marker 
     * @param name :: marker name
     * @param description :: marker description
     * @param type :: marker type
     * @param lat :: marker latitude
     * @param long :: marker longitude
     * 
     */
    MarkerData(int mapId, String name, String description, int type, double latitude, double longitude) {
       map_id=mapId;
       marker_name=name;
       marker_description=description;
       marker_type=type;
       marker_lat=latitude;
       marker_long=longitude;
    }

    
}
