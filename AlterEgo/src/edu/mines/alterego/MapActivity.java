package edu.mines.alterego;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class MapActivity extends Activity {

	private GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapactivity);

       
        map.setMapType(GoogleMap.MAP_TYPE_NONE);
        TileOverlayOptions opts = new TileOverlayOptions();
        opts.tileProvider(new CustomMapTileProvider(getAssets()));

        TileOverlay overlay = map.addTileOverlay(opts);
        
        //Ground overlay stuff
        
        float t = (float) .5;
        BitmapDescriptor image = BitmapDescriptorFactory.fromResource(R.drawable.kittens);
        GroundOverlay gOverlay = map.addGroundOverlay(new GroundOverlayOptions().image(image).position(map.getCameraPosition().target,500000f, 500000f ) );
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e){
    	int action = e.getAction();
    	float xCord = e.getX();
    	float yCord = e.getY();
    	Point a = new Point();
    	a.set((int) xCord, (int) yCord);
    	LatLng loc = map.getProjection().fromScreenLocation(a);
    	
    	
    	
    	return true;
    }

}

