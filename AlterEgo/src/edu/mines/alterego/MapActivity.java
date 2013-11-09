package edu.mines.alterego;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import edu.mines.alterego.CustomMapTileProvider;

public class MapActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapactivity);

        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        TileOverlayOptions opts = new TileOverlayOptions();
        opts.tileProvider(new CustomMapTileProvider(getAssets()));

        TileOverlay overlay = map.addTileOverlay(opts);
    }
}

