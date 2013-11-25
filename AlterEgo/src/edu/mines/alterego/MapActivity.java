package edu.mines.alterego;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class MapActivity extends Activity {
	
	TileOverlay overlay;
	GoogleMap map;
	Context context = this ;
	int count = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapactivity);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
        TileOverlayOptions opts = new TileOverlayOptions();
        opts.tileProvider(new CustomMapTileProvider(getAssets()));

        overlay = map.addTileOverlay(opts);
 
        // Creating onLongClickListener for user to add marker to map      
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(final LatLng position) {
				LayoutInflater li = LayoutInflater.from(context);
				final View v = li.inflate(R.layout.new_marker_dialog, null);
				AlertDialog.Builder addMarker = new AlertDialog.Builder(context);
				addMarker.setView(v);
				addMarker.setTitle("Add Marker");
				addMarker.setCancelable(false);
				addMarker.setPositiveButton("Create", new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//grab user input for marker name
						EditText mTitle = (EditText) v.findViewById(R.id.marker_title);
						float mColor;
						
						if (count == 0) {
							mColor = BitmapDescriptorFactory.HUE_RED;
						} else if (count == 1 ) {
							mColor = BitmapDescriptorFactory.HUE_ORANGE;
						} else if (count == 2) {
							mColor = BitmapDescriptorFactory.HUE_YELLOW;
						} else if (count == 3) {
							mColor = BitmapDescriptorFactory.HUE_GREEN;
						} else if (count == 4) {
							mColor = BitmapDescriptorFactory.HUE_BLUE;
						} else if (count == 5) {
							mColor = BitmapDescriptorFactory.HUE_VIOLET;
						} else if (count == 6) {
							mColor = BitmapDescriptorFactory.HUE_AZURE;
						} else if (count == 7) {
							mColor = BitmapDescriptorFactory.HUE_MAGENTA;
						} else if (count == 8) {
							mColor = BitmapDescriptorFactory.HUE_ROSE;
						} else if (count == 9) {
							mColor = BitmapDescriptorFactory.HUE_CYAN;
						} else {
							mColor = BitmapDescriptorFactory.HUE_YELLOW;
						}
					
						map.addMarker(new MarkerOptions()
								.title(mTitle.getText().toString())
								.position(position)
								.icon(BitmapDescriptorFactory.defaultMarker(mColor))
								.draggable(true)
								);
						
						if (mColor > 9) {
							count = 0;
						} else {
							count++;
						}
					}
				
				});
				
				addMarker.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						
					}
				});
				
				AlertDialog alert = addMarker.create();
				alert.show();
				
			}
		});
    }
}

