package com.example.android.timestamper;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.drm.DrmStore;
import android.os.VibrationEffect;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavBar;
    private ActionBar toolBar;

    private Fragment recordFrag, playbackFrag, libraryFrag;

    private final String RECORD_TAG = "record";
    private final String PLAYBACK_TAG = "playback";
    private final String LIBRARY_TAG = "library";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CreateFragments();
        SetupActionBar();
    }

    private void CreateFragments(){
        recordFrag = RecordAudioFragment.newInstance();
        playbackFrag = MediaPlaybackFragment.newInstance();
        libraryFrag = LibraryAccessFragment.newInstance();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.container, recordFrag, RECORD_TAG);
        transaction.add(R.id.container, playbackFrag, PLAYBACK_TAG);
        transaction.add(R.id.container, libraryFrag, LIBRARY_TAG);
        transaction.addToBackStack(PLAYBACK_TAG);
        transaction.addToBackStack(LIBRARY_TAG);
        transaction.hide(libraryFrag);
        transaction.hide(playbackFrag);
        transaction.show(recordFrag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private void SetupActionBar(){
        toolBar = getSupportActionBar();
        bottomNavBar = (BottomNavigationView) findViewById(R.id.navigationView);

        bottomNavBar.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        String hide1 = null;
                        String hide2 = null;

                        switch (item.getItemId()){
                            case R.id.navigation_record_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(RECORD_TAG);
                                hide1 = PLAYBACK_TAG;
                                hide2 = LIBRARY_TAG;
                                Log.d("SELECTION", "Record");
                                break;

                            case R.id.navigation_playback_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(PLAYBACK_TAG);
                                hide1 = RECORD_TAG;
                                hide2 = LIBRARY_TAG;
                                Log.d("SELECTION", "Playback");
                                break;

                            case R.id.navigation_library_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(LIBRARY_TAG);
                                hide1 = RECORD_TAG;
                                hide2 = PLAYBACK_TAG;
                                Log.d("SELECTION", "Library");
                                break;
                        }
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.show(selectedFragment);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.addToBackStack(hide1);
                        transaction.addToBackStack(hide2);
                        transaction.hide(getFragmentManager().findFragmentByTag(hide1));
                        transaction.hide(getFragmentManager().findFragmentByTag(hide2));
                        transaction.commit();
                        return true;
                    }
                }
        );

        /*FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, recordFrag);
        transaction.commit();*/
    }
}
