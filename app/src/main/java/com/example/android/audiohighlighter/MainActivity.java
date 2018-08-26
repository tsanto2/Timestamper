package com.example.android.audiohighlighter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements MainActivityInterface, BillingProcessor.IBillingHandler{

    private String adMobAppID = "ca-app-pub-9485517543167139~7756344909";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private SharedPreferences sharedPreferences;
    private int runCount;

    private BottomNavigationView bottomNavBar;
    private ActionBar toolBar;

    private Fragment recordFrag, playbackFrag, libraryFrag, settingsFrag, prevFrag;

    private final String RECORD_TAG = "record";
    private final String PLAYBACK_TAG = "playback";
    private final String LIBRARY_TAG = "library";
    private final String SETTINGS_TAG = "settings";
    private boolean isPlaybackFrag = false;

    private BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, adMobAppID);

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);

        CreateFragments();
        SetupActionBar();
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        bp = new BillingProcessor(this, null, this);

        runCount = sharedPreferences.getInt("RunCount", 0);

        if (runCount > 1 && !sharedPreferences.getBoolean("PremiumDialogueDisabled", false)){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Premium Upgrade")
                    .setMessage("Premium upgrade allows you to record for as long as you want, create as many recordings as you want, and allows access to several customization settings. There are also many additional premium features planned for future updates.")
                    .setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            RecordAudioFragment tempFrag = (RecordAudioFragment)getFragmentManager().findFragmentByTag(RECORD_TAG);
                            tempFrag.PurchasePremium();
                        }
                    })
                    .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else{
            runCount = sharedPreferences.getInt("RunCount", 0) + 1;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("RunCount", runCount);
            editor.commit();
        }
        // TODO: Set first run false after showing this. Maybe counter for first 5 times?
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

    // Implementation of interface function
    // Used for switching to playback fragment and passing filename to play
    @Override
    public void SwitchToFragment(String tempAudioFilePath){
        MediaPlaybackFragment tempFrag = (MediaPlaybackFragment)getFragmentManager().findFragmentByTag(PLAYBACK_TAG);
        tempFrag.SetPlaybackInfo(tempAudioFilePath);
        LibraryAccessFragment tempLibFrag = (LibraryAccessFragment)getFragmentManager().findFragmentByTag(LIBRARY_TAG);
        tempLibFrag.RefreashLibraryItemAdapter();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.addToBackStack(RECORD_TAG);
        transaction.addToBackStack(LIBRARY_TAG);
        transaction.addToBackStack(SETTINGS_TAG);

        transaction.hide(libraryFrag);
        transaction.hide(recordFrag);
        transaction.hide(settingsFrag);

        isPlaybackFrag = true;
        getSupportActionBar().hide();
        transaction.show(playbackFrag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();

        bottomNavBar.setVisibility(View.GONE);
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
        bottomNavBar = (BottomNavigationView) findViewById(R.id.navigationView);

        bottomNavBar.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        String hide1 = null;
                        String hide2 = null;
                        String hide3 = null;

                        if (prevFrag == recordFrag){
                            RecordAudioFragment tempFrag = (RecordAudioFragment)getFragmentManager().findFragmentByTag(RECORD_TAG);
                            tempFrag.StopRecording(false);
                        }

                        switch (item.getItemId()){
                            case R.id.navigation_record_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(RECORD_TAG);
                                prevFrag = selectedFragment;
                                getSupportActionBar().setTitle("Record");
                                getSupportActionBar().show();
                                hide1 = PLAYBACK_TAG;
                                hide2 = LIBRARY_TAG;
                                hide3 = SETTINGS_TAG;

                                CheckRecordingScreenSettings();

                                Log.d("SELECTION", "Record");
                                break;

                            case R.id.navigation_library_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(LIBRARY_TAG);
                                LibraryAccessFragment tempLibFrag = (LibraryAccessFragment)selectedFragment;
                                tempLibFrag.GetLibraryItems();
                                prevFrag = selectedFragment;
                                getSupportActionBar().setTitle("Library");
                                getSupportActionBar().show();
                                hide1 = RECORD_TAG;
                                hide2 = PLAYBACK_TAG;
                                hide3 = SETTINGS_TAG;
                                Log.d("SELECTION", "Library");
                                break;

                            case R.id.navigation_settings_frag:
                                selectedFragment = getFragmentManager().findFragmentByTag(SETTINGS_TAG);
                                prevFrag = selectedFragment;
                                getSupportActionBar().setTitle("Settings");
                                getSupportActionBar().show();
                                hide1 = RECORD_TAG;
                                hide2 = PLAYBACK_TAG;
                                hide3 = LIBRARY_TAG;
                                Log.d("SELECTION", "Library");
                                break;
                        }
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        //transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
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
    }

    private void CheckRecordingScreenSettings(){
        boolean storageTextDisabled = getPreferences(Context.MODE_PRIVATE).getBoolean("StorageTextDisabled", true);
        LinearLayout storageLayout = findViewById(R.id.storage_space_layout);

        if (storageTextDisabled){
            storageLayout.setVisibility(View.GONE);
        }
        else{
            storageLayout.setVisibility(View.VISIBLE);
        }

        boolean hintsTextDisabled = getPreferences(Context.MODE_PRIVATE).getBoolean("HintsDisabled", false);
        if (hintsTextDisabled) {
            TextView tv = findViewById(R.id.title_hint_text);
            tv.setVisibility(View.GONE);

            tv = findViewById(R.id.record_tip_text_view);
            tv.setVisibility(View.GONE);

            tv = findViewById(R.id.save_tip_text_view);
            tv.setVisibility(View.GONE);

            tv = findViewById(R.id.timestamp_tip_text_view);
            tv.setVisibility(View.GONE);
        }
        else{
            TextView tv = findViewById(R.id.title_hint_text);
            tv.setVisibility(View.VISIBLE);

            tv = findViewById(R.id.record_tip_text_view);
            tv.setVisibility(View.VISIBLE);

            tv = findViewById(R.id.save_tip_text_view);
            tv.setVisibility(View.VISIBLE);

            tv = findViewById(R.id.timestamp_tip_text_view);
            tv.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed(){
        if (isPlaybackFrag){
            MediaPlaybackFragment pFrag = (MediaPlaybackFragment)playbackFrag;
            pFrag.playButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24px);
            MediaPlayer mPlayer = pFrag.GetMediaPlayer();
            mPlayer.stop();
            getSupportActionBar().show();
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
                bottomNavBar.getMenu().getItem(1).setChecked(true);
                getSupportActionBar().setTitle("Library");
            }
            else if (prevFrag == settingsFrag) {
                bottomNavBar.getMenu().getItem(2).setChecked(true);
                getSupportActionBar().setTitle("Settings");
                getSupportActionBar().setSubtitle("bitch");
            }

            isPlaybackFrag = false;
        }
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (!bp.handleActivityResult(requestCode, resultCode, data)){
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy(){
        if (bp != null){
            bp.release();
        }
        super.onDestroy();
    }
}
