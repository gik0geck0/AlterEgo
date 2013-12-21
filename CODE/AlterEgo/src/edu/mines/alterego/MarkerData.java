package edu.mines.alterego;

import edu.mines.alterego.MapActivity.MARKERTYPE;
import com.google.android.gms.maps.model.LatLng;

class MarkerData {
    private int map_id;
    private int marker_id;
    private String marker_name;
    private String marker_description;
    private MARKERTYPE marker_type;
    private double marker_lat;
    private double marker_long;
    private float marker_color;

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
    MarkerData(int mapId, int markerId, String name, String description, MARKERTYPE type, double latitude, double longitude, float color) {
       map_id=mapId;
       marker_id=markerId;
       marker_name=name;
       marker_description=description;
       marker_type=type;
       marker_lat=latitude;
       marker_long=longitude;
       marker_color = color;
    }

    public int getMapId() { return map_id; }
    public int getMarkerId() { return marker_id; }
    public String getName() { return marker_name; }
    public String getDescription() { return marker_description; }
    public MARKERTYPE getMarkerType() { return marker_type; }
    public double getLat() { return marker_lat; }
    public double getLong() { return marker_long; }
    public float getColor() { return marker_color; }

    /**
     * <p>Move a marker to a new location</p>
     *
     * @param pos New position
     */
    public void move(LatLng pos) {
        marker_lat = pos.latitude;
        marker_long = pos.longitude;
    }
}
