package edu.mines.alterego;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class MapActivity extends Activity implements GoogleMap.OnMarkerDragListener {

	GoogleMap map;
	Context context = this;
	int count = 0;

	RadioGroup markerGroup;
	RadioButton markerButton;
	int markerId = -1;
	private int gameID = -1;

    // Link between Markers used by the GoogleMap, and its MarkerData for the database
    // Any time a Marker is moved, lookup its MarkerData, and update it in the database
    // Any time a Marker is added to the Map, add it to the database, and put its corresponding MarkerData in here
    private HashMap<Marker, MarkerData> markerDataStore = new HashMap<Marker, MarkerData>();

	// enum for marker types
	public enum MARKERTYPE {
		PLAYER(0), TREASURE(1), ENEMY(2);

		private final int value;

		private MARKERTYPE(final int newValue) {
			value = newValue;
		}

		public int getValue() {
			return value;
		}
	}

	CharacterDBHelper mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapactivity);

		// Intent intent = getIntent();
		gameID = GameActivity.mGameId; //intent.getIntExtra(GameActivity.GAME_ID, -1);

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NONE);
        map.setOnMarkerDragListener(this);

		TileOverlayOptions opts = new TileOverlayOptions();
		opts.tileProvider(new CustomMapTileProvider(getAssets()));
        map.addTileOverlay(opts);

		// Creating onLongClickListener for user to add marker to map
		map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

			@Override
			public void onMapLongClick(final LatLng position) {
				Log.i("AlterEgos::MapAct::onMapLogClick",
						"Long Click Occurred on Map");

				LayoutInflater mapLayI = LayoutInflater.from(context);
				final View mapV = mapLayI.inflate(R.layout.new_marker_dialog,
						null);
				AlertDialog.Builder addMarker = new AlertDialog.Builder(context);

				addMarker.setView(mapV);
				addMarker.setTitle("Add Marker");
				addMarker.setCancelable(false);
				addMarker.setPositiveButton("Create",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								AlertDialog groupDialog = (AlertDialog) dialog;

								markerGroup = (RadioGroup) groupDialog
										.findViewById(R.id.add_marker_group);
								markerId = markerGroup
										.getCheckedRadioButtonId();
								markerButton = (RadioButton) groupDialog
										.findViewById(markerId);

								switch (markerId) {
								case R.id.add_marker_player:
									addPlayer(position);
									break;
								case R.id.add_marker_treasure:
									addTreasure(position);
									break;
								case R.id.add_marker_enemy:
									addEnemy(position);
									break;
								default:
									Toast.makeText(MapActivity.this,
											"Nothing Selected",
											Toast.LENGTH_SHORT).show();
									break;
								}
							}
						});

				addMarker.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();

							}
						});

				AlertDialog markerAlert = addMarker.create();
				markerAlert.show();

			}
		});

		mDbHelper = new CharacterDBHelper(this);
		Log.i("AlterEgo::MapAct::gameId", "gameId " + gameID);
		ArrayList<MarkerData> markerList = mDbHelper.loadMarkers(gameID);
		Log.i("ALTEREGO::MAPACT::markerList", Integer.toString(markerList.size() ));
		for (MarkerData m : markerList) {
			BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            switch(m.getMarkerType()) {
                case PLAYER:
                    icon = BitmapDescriptorFactory.defaultMarker(m.getColor());
                    break;
                case TREASURE:
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_treasure);
                    break;
                default:
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_skull);
            }
            Marker newMarker = map.addMarker(new MarkerOptions()
					.title(m.getName())
					.snippet(m.getDescription())
					.position(new LatLng(m.getLat(), m.getLong()))
					.icon(icon)
					.draggable(true));

            // Create a link to the DB Object in the hashmap
            markerDataStore.put(newMarker, m);
		}

	}

	public void addPlayer(final LatLng position) {

		LayoutInflater mLayI = LayoutInflater.from(context);
		final View mV = mLayI.inflate(R.layout.new_player_marker_dialog, null);
		AlertDialog.Builder addPlayer = new AlertDialog.Builder(context);
		addPlayer.setView(mV);
		addPlayer.setTitle("Create Player");
		addPlayer.setCancelable(false);
		addPlayer.setPositiveButton("Create",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// grab user input for marker name
						EditText mTitle = (EditText) mV
								.findViewById(R.id.marker_player_title);
						EditText mSnippet = (EditText) mV
								.findViewById(R.id.marker_player_snippet);

						float mColor;

						switch (count) {
						case 0:
							mColor = BitmapDescriptorFactory.HUE_RED;
							break;
						case 1:
							mColor = BitmapDescriptorFactory.HUE_ORANGE;
							break;
						case 2:
							mColor = BitmapDescriptorFactory.HUE_YELLOW;
							break;
						case 3:
							mColor = BitmapDescriptorFactory.HUE_GREEN;
							break;
						case 4:
							mColor = BitmapDescriptorFactory.HUE_BLUE;
							break;
						case 5:
							mColor = BitmapDescriptorFactory.HUE_CYAN;
							break;
						case 6:
							mColor = BitmapDescriptorFactory.HUE_VIOLET;
							break;
						case 7:
							mColor = BitmapDescriptorFactory.HUE_MAGENTA;
							break;
						case 8:
							mColor = BitmapDescriptorFactory.HUE_AZURE;
							break;
						case 9:
							mColor = BitmapDescriptorFactory.HUE_ROSE;
							count = -1;
							break;
						default:
							mColor = BitmapDescriptorFactory.HUE_RED;
							break;
						}

						String mName = mTitle.getText().toString();
						String mDesc = mSnippet.getText().toString();

						if (mName.equals("")) {
							Toast name = Toast
									.makeText(MapActivity.this,
											"Required: Player Name",
											Toast.LENGTH_SHORT);
							name.show();
						} else {
                            Marker newMarker = map.addMarker(new MarkerOptions()
									.title(mName)
									.snippet(mDesc)
									.position(position)
									.icon(BitmapDescriptorFactory
											.defaultMarker(mColor))
									.draggable(true));
							MarkerData md = mDbHelper.addDBMarker(mName, mDesc,
									position.latitude, position.longitude,
									MARKERTYPE.PLAYER, gameID, mColor);

                            // Create a link to the DB Object in the hashmap
                            markerDataStore.put(newMarker, md);

							count++;
						}
					}

				});

		addPlayer.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();

					}
				});

		AlertDialog alert = addPlayer.create();
		alert.show();

	}

	public void addTreasure(final LatLng position) {
		LayoutInflater tLayI = LayoutInflater.from(context);
		final View tV = tLayI
				.inflate(R.layout.new_treasure_marker_dialog, null);
		AlertDialog.Builder addTreasure = new AlertDialog.Builder(context);
		addTreasure.setView(tV);
		addTreasure.setTitle("Create Treasure");
		addTreasure.setCancelable(false);
		addTreasure.setPositiveButton("Create",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// grab user input for marker name
						EditText mTitle = (EditText) tV
								.findViewById(R.id.marker_treasure_title);
						EditText mSnippet = (EditText) tV
								.findViewById(R.id.marker_treasure_snippet);

						String mName = mTitle.getText().toString();
						String mDesc = mSnippet.getText().toString();

						if (mName.equals("")) {
							Toast name = Toast.makeText(MapActivity.this,
									"Required: Treasure Title",
									Toast.LENGTH_SHORT);
							name.show();
						} else {
                            Marker newMarker = map.addMarker(new MarkerOptions()
									.title(mName)
									.snippet(mDesc)
									.position(position)
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.marker_treasure))
									.draggable(true));
							MarkerData md = mDbHelper.addDBMarker(mName, mDesc,
									position.latitude, position.longitude,
									MARKERTYPE.TREASURE, gameID, -1);

                            // Create a link to the DB Object in the hashmap
                            markerDataStore.put(newMarker, md);

						}
					}

				});

		addTreasure.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();

					}
				});

		AlertDialog alert = addTreasure.create();
		alert.show();
	}

	public void addEnemy(final LatLng position) {
		LayoutInflater eLayI = LayoutInflater.from(context);
		final View eV = eLayI.inflate(R.layout.new_enemy_marker_dialog, null);
		AlertDialog.Builder addEnemy = new AlertDialog.Builder(context);
		addEnemy.setView(eV);
		addEnemy.setTitle("Create Enemy");
		addEnemy.setCancelable(false);
		addEnemy.setPositiveButton("Create",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// grab user input for marker name
						EditText mTitle = (EditText) eV
								.findViewById(R.id.marker_enemy_title);
						EditText mSnippet = (EditText) eV
								.findViewById(R.id.marker_enemy_snippet);

						String mName = mTitle.getText().toString();
						String mDesc = mSnippet.getText().toString();

						if (mName.equals("")) {
							Toast name = Toast.makeText(MapActivity.this,
									"Required: Enemy Type/Name",
									Toast.LENGTH_SHORT);
							name.show();
						} else {
                            Marker newMarker = map.addMarker(new MarkerOptions()
									.title(mName)
									.snippet(mDesc)
									.position(position)
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.marker_skull))
									.draggable(true));
							MarkerData md = mDbHelper.addDBMarker(mName, mDesc,
									position.latitude, position.longitude,
									MARKERTYPE.ENEMY, gameID, -1);

                            // Create a link to the DB Object in the hashmap
                            markerDataStore.put(newMarker, md);
						}
					}

				});

		addEnemy.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();

					}
				});

		AlertDialog alert = addEnemy.create();
		alert.show();
	}

    // Called whenever a marker has finished moving
    public void onMarkerDragEnd(Marker marker) {
        // A marker has just been dragged to a new location
        LatLng p = marker.getPosition();

        // Depend on markerDataStore.get to return a REFERENCE
        MarkerData md = markerDataStore.get(marker);
        md.move(p);

        // Notify the database of this change
        mDbHelper.updateDBMarker(md);
    }

    // MarkerDrag Stubs
    public void onMarkerDrag(Marker marker) {}
    public void onMarkerDragStart(Marker marker) {}
}
