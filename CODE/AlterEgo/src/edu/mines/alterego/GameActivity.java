package edu.mines.alterego;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import edu.mines.alterego.RefreshInterface;
import edu.mines.alterego.NetworkingService;

/**
 * This activity holds a view-flipper to each of the different components of a
 * game. Right now, there are only 3 parts (staticly defined): Character,
 * Inventory, and Notes. The GameActivity also serves as a unifying
 * data-instructor for the fragments. This activity instructs the fragments
 * which game and character id to use.
 * 
 * @author: Matt Buland, Maria Deslis, Eric Young
 */
public class GameActivity extends FragmentActivity implements RefreshInterface {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	public static int mGameId = -1;
	public static int mCharId = -1;
	
	public final static String GAME_ID = "edu.mines.alterego.GAMEID";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_activity);

		// Grab the gameId from the starting activity (should be MainActivity)
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		if (extras != null) {
			mGameId = extras.getInt(
					(String) getResources().getText(R.string.gameid), -1);
			Log.i("AlterEgo::GameActivity::Init",
					"The gameid passed from the MainActivity was " + mGameId);
		} else {
			mGameId = -1;
			Log.i("AlterEgo::GameActivity::Init",
					"There were no extras passed to the GameActivity. That could be bad.");
		}

		if (mGameId == -1) {
			// Yes, this is annoying, but it'll make an error VERY obvious. In
			// testing, I have never seen this toast/error message. But ya never
			// know
			Toast.makeText(this, "GameID not valid", Toast.LENGTH_SHORT).show();
			Log.e("AlterEgo::CharacterFragment", "Game ID is not valid...?");
		}

		// Grab game name for character
		// Try to find a character for this game
		CharacterDBHelper dbhelper = new CharacterDBHelper(this);
		mCharId = dbhelper.getCharacterIdForGame(mGameId);
		setTitle(dbhelper.getGameNameForCharacterId(mGameId));

		// Try to find a character for this game
		mCharId = dbhelper.getCharacterIdForGame(mGameId);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

        spawnNetworking();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.

            // Create a bundle to hold the gameId and charId for the fragment
            //Bundle args = new Bundle();
            //args.putInt((String) getResources().getText(R.string.gameid), mGameId);
            //args.putInt((String) getResources().getText(R.string.charid), mCharId);
            Log.i("AlterEgo::GameActivity::getItem", "Spawning a new fragment with game=" + mGameId + " and char=" + mCharId);

            // Short-circuit if there's no character yet. CharacterFragment is the only possibility
            if (mCharId < 0 && position > 0) {
                return null;
            }

            // Get a fragment to be used
            Fragment fragment;
            switch(position) {
                case 0:
                    // CharacterFragment needs a call-back to the refresh button (in case a character is added)
                    fragment = new CharacterFragment(GameActivity.this);
                    //fragment.setArguments(args);
                    break;
                case 1:
                    fragment = new InventoryFragment();
                    //fragment.setArguments(args);
                    break;
                case 2:
                	fragment = new NotesFragment();
                	//fragment.setArguments(args);
                	break;
                case 3:
                    fragment = new ChatFragment();
                    break;
                case 4:
                    fragment = new MapFragment();
                    break;
                default:
                    fragment = new DummySectionFragment();
                    //args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                    //fragment.setArguments(args);
            }

			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.

            // If there's no valid character, show only the character fragment
            if (mCharId < 0)
                return 1;

			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_character).toUpperCase(l);
			case 1:
				return getString(R.string.title_inventory).toUpperCase(l);
			case 2:
				return getString(R.string.title_notes).toUpperCase(l);
            case 3:
                return getString(R.string.title_chat).toUpperCase(l);
            case 4:
                return getString(R.string.title_map).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	@Override
	public void refresh() {
		CharacterDBHelper dbhelper = new CharacterDBHelper(this);
		mCharId = dbhelper.getCharacterIdForGame(mGameId);
		Log.i("AlterEgo::GameAct::Refresh",
				"Re-stating the character-ID. It was " + mCharId);
        mSectionsPagerAdapter.notifyDataSetChanged();
	}

	public void startMap(View view) {
		Intent mapIntent = new Intent(this, MapActivity.class);
		mapIntent.putExtra(GAME_ID, mGameId);
		startActivity(mapIntent);
	}

    private void spawnNetworking() {
        // Get Wireless info
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wm.createMulticastLock("mydebuginfo");
        multicastLock.acquire();
        WifiInfo myWifiInfo = wm.getConnectionInfo();
        int myIpAddr = myWifiInfo.getIpAddress();

        Intent netServIntent = new Intent(this, NetworkingService.class);
        netServIntent.putExtra("IP", myIpAddr);

        Log.d("AlterEgo::GameActivity", "Starting the networking service");
        startService(netServIntent);
    }
}
