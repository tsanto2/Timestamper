package com.example.android.timestamper;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private boolean mpPrepared = false;
    private ArrayList<Integer> timestamps;
    private int stampArrayPos = -1;
    private int stampTimeCushion = 1000;
    private SeekBar seekBar;
    private Handler seekBarHandler;
    private Runnable seekBarRunnable;
    private int seekTime;
    private boolean seekBarTouched;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timestamps = new ArrayList<Integer>();

        prepareMediaPlayer();

        setupClickListeners();

        setupSeekBar();
    }

    private String getTime(int ms){
        Date time = new Date(ms);
        DateFormat formatter = new SimpleDateFormat("mm:ss:SS");
        String timeFormatted = formatter.format(time);

        return timeFormatted;
    }

    // Setting up seekbar
    private void setupSeekBar(){
        seekBar = findViewById(R.id.seekbar_view);
        final TextView currTime = findViewById(R.id.curr_time_text_view);
        seekBarHandler = new Handler();
        seekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (!seekBarTouched) {
                    currTime.setText(getTime(mMediaPlayer.getCurrentPosition()));
                    seekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    seekBarHandler.postDelayed(this, 10);
                }
            }
        };

        seekBar.setMax(mMediaPlayer.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekTime = i;
                currTime.setText(getTime(seekTime));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarTouched = true;
                seekBarHandler.removeCallbacks(seekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarTouched = false;
                seekBarHandler.postDelayed(seekBarRunnable, 0);
                mMediaPlayer.seekTo(seekTime);
            }
        });
    }

    private void setupClickListeners(){
        // Setting up play/pause button listener
        setPlayButtonListener();

        // Setting up button to restart clip
        setRestartButtonListener();

        // Setting up timestamp creation button
        setTimestamperButtonListener();

        // Setup next timestamp navigation button
        setNextTimestampNavButtonListener();

        // Setup previous timestamp navigation button
        setPrevTimestampNavButtonListener();

        // Setup swap to record activity button
        setRecordActivityButtonListener();
    }

    private void setPlayButtonListener(){
        playButton = findViewById(R.id.play_button_view);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mpPrepared) {
                    // Play if audio is not playing
                    if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                        playButton.setText(R.string.pause_btn_text);
                        seekBarHandler.postDelayed(seekBarRunnable, 0);
                    }
                    // Pause if audio is playing
                    else if (mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();
                        playButton.setText(R.string.play_btn_text);
                        seekBarHandler.removeCallbacks(seekBarRunnable);
                    }
                }
            }
        });
    }

    private void setRestartButtonListener(){
        Button restartButton = findViewById(R.id.restart_button_view);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If the media player is prepared and the button is pressed, seek to 0 ms and play
                if (mpPrepared){
                    mMediaPlayer.seekTo(0);
                    seekBarHandler.removeCallbacks(seekBarRunnable);
                    if (!mMediaPlayer.isPlaying())
                        playButton.setText(R.string.pause_btn_text);
                    stampArrayPos = -1;
                    mMediaPlayer.start();
                    seekBarHandler.postDelayed(seekBarRunnable, 0);
                }
            }
        });
    }

    private void setTimestamperButtonListener(){
        Button setTimestampButton = findViewById(R.id.mark_button_view);

        setTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Adjust stampTimeCushion depending on what feels "right"
                // This variable is used to compensate for user pressing button after intended mark
                // location.
                // TODO: Make cushion user defined setting
                int stampTime = mMediaPlayer.getCurrentPosition() - stampTimeCushion;

                // Add timestamp to arraylist
                timestamps.add(stampTime);

                String toastText = getResources().getString(R.string.mark_toast_text) + " " + getTime(stampTime);

                Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setNextTimestampNavButtonListener(){
        // TODO: Make this easier to use. Buttons for each stamp?
        Button nextTimestampButton = findViewById(R.id.nextstamp_button_view);

        nextTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mpPrepared && (timestamps.size() > 0)){
                    stampArrayPos++;

                    if( stampArrayPos > (timestamps.size() - 1) )
                        stampArrayPos = 0;

                    int stampTime = timestamps.get(stampArrayPos);
                    mMediaPlayer.seekTo(stampTime);

                    if ( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                        seekBarHandler.postDelayed(seekBarRunnable, 0);
                        playButton.setText(R.string.pause_btn_text);
                    }
                }
            }
        });
    }

    private void setPrevTimestampNavButtonListener(){
        Button prevTimestampButton = findViewById(R.id.prevstamp_button_view);

        prevTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mpPrepared && (timestamps.size() > 0)){
                    stampArrayPos--;

                    if (stampArrayPos < 0){
                        stampArrayPos = (timestamps.size() - 1);
                    }

                    int stampTime = timestamps.get(stampArrayPos);
                    mMediaPlayer.seekTo(stampTime);

                    if ( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                        seekBarHandler.postDelayed(seekBarRunnable, 0);
                        playButton.setText(R.string.pause_btn_text);
                    }
                }
            }
        });
    }

    // TODO: Clean up media player, shut down audio
    private void setRecordActivityButtonListener(){
        Button recordActivityButton = findViewById(R.id.record_activity_button_view);

        recordActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LiveRecordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void prepareMediaPlayer(){
        mMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.fart);

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mpPrepared = true;
            }
        });

    }
}
