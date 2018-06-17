package com.example.android.timestamper;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.drm.DrmStore;
import android.os.VibrationEffect;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private BottomNavigationView bottomNavBar;
    private ActionBar toolBar;

    private Fragment recordFrag, playbackFrag, libraryFrag, settingsFrag, prevFrag;

    private final String RECORD_TAG = "record";
    private final String PLAYBACK_TAG = "playback";
    private final String LIBRARY_TAG = "library";
    private final String SETTINGS_TAG = "settings";
    private boolean isPlaybackFrag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CreateFragments();
        SetupActionBar();
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void RequestPermissions(){
    }

    private void CreateFragments(){
        recordFrag = RecordAudioFragment.newInstance();
        playbackFrag = MediaPlaybackFragment.newInstance();
        libraryFrag = LibraryAccessFragment.newInstance();
        settingsFrag = SettingsFragment.newInstance();
        prevFrag = recordFrag;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.container, recordFrag, RECORD_TAG);
        transaction.add(R.id.container, playbackFrag, PLAYBACK_TAG);
        transaction.add(R.id.container, libraryFrag, LIBRARY_TAG);
        transaction.add(R.id.container, settingsFrag, SETTINGS_TAG);
        transaction.addToBackStack(PLAYBACK_TAG);
        transaction.addToBackStack(LIBRARY_TAG);
        transaction.addToBackStack(SETTINGS_TAG);
        transaction.hide(libraryFrag);
        transaction.hide(playbackFrag);
        transaction.hide(settingsFrag);
        transaction.show(recordFrag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private void SetupActionBar(){
        toolBar = getSupportActionBar();
        getSupportActionBar().hide();
        bottomNavBar = (BottomNavigationView) findViewById(R.id.navigationView);

        bottomNavBar.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        String hide1 = null;
                        String hide2 = null;
                        String hide3 = null;

                        switch (item.getItemId()){
                            case R.id.navigation_record_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(RECORD_TAG);
                                prevFrag = selectedFragment;
                                getSupportActionBar().setTitle("Record");
                                hide1 = PLAYBACK_TAG;
                                hide2 = LIBRARY_TAG;
                                hide3 = SETTINGS_TAG;
                                Log.d("SELECTION", "Record");
                                break;

                            /*case R.id.navigation_playback_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(PLAYBACK_TAG);
                                isPlaybackFrag = true;
                                getSupportActionBar().setTitle("Playback");
                                hide1 = RECORD_TAG;
                                hide2 = LIBRARY_TAG;
                                hide3 = SETTINGS_TAG;
                                Log.d("SELECTION", "Playback");
                                break;*/

                            case R.id.navigation_library_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(LIBRARY_TAG);
                                prevFrag = selectedFragment;
                                getSupportActionBar().setTitle("Library");
                                hide1 = RECORD_TAG;
                                hide2 = PLAYBACK_TAG;
                                hide3 = SETTINGS_TAG;
                                Log.d("SELECTION", "Library");
                                break;

                            case R.id.navigation_settings_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(SETTINGS_TAG);
                                prevFrag = selectedFragment;
                                getSupportActionBar().setTitle("Settings");
                                hide1 = RECORD_TAG;
                                hide2 = PLAYBACK_TAG;
                                hide3 = LIBRARY_TAG;
                                Log.d("SELECTION", "Library");
                                break;
                        }
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                        transaction.show(selectedFragment);
                        transaction.addToBackStack(hide1);
                        transaction.addToBackStack(hide2);
                        transaction.addToBackStack(hide3);
                        transaction.hide(getFragmentManager().findFragmentByTag(hide1));
                        transaction.hide(getFragmentManager().findFragmentByTag(hide2));
                        transaction.hide(getFragmentManager().findFragmentByTag(hide3));
                        transaction.commit();
                        if (isPlaybackFrag) {
                            bottomNavBar.setVisibility(View.GONE);
                        }

                        return true;
                    }
                }
        );

        /*FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, recordFrag);
        transaction.commit();*/
    }

    @Override
    public void onBackPressed(){
        if (isPlaybackFrag){
            bottomNavBar.setVisibility(View.VISIBLE);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.addToBackStack(PLAYBACK_TAG);
            transaction.hide(playbackFrag);
            transaction.show(prevFrag);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.commit();

            if (prevFrag == recordFrag) {
                bottomNavBar.getMenu().getItem(0).setChecked(true);
                getSupportActionBar().setTitle("Record");
            }
            else if (prevFrag == libraryFrag) {
                bottomNavBar.getMenu().getItem(2).setChecked(true);
                getSupportActionBar().setTitle("Library");
            }
            else if (prevFrag == settingsFrag) {
                bottomNavBar.getMenu().getItem(3).setChecked(true);
                getSupportActionBar().setTitle("Settings");
                getSupportActionBar().setSubtitle("bitch");
            }

            isPlaybackFrag = false;
        }
    }
}
