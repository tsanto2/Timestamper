package com.example.android.timestamper;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MediaPlaybackFragment extends Fragment {

    private View view;

    private MediaPlayer mMediaPlayer;
    private boolean mpPrepared = false;
    //private ArrayList<Integer> timestamps;
    private int stampArrayPos = -1;
    //private int stampTimeCushion = 1000;
    private SeekBar seekBar;
    private Handler seekBarHandler;
    private Runnable seekBarRunnable;
    private int seekTime;
    private boolean seekBarTouched;
    private ImageButton playButton;
    private ArrayList<Timestamp> timestamps;
    private TimestampAdapter timestampAdapter;
    private int playBtnImage = R.drawable.ic_baseline_play_circle_filled_24px;
    private int pauseBtnImage = R.drawable.ic_baseline_pause_circle_filled_24px;
    private static MediaPlaybackFragment fragment;
    private ListView listView;
    private String tempFilePath;

    public static MediaPlaybackFragment newInstance(){
        fragment = new MediaPlaybackFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_media_playback, container, false);

        timestamps = new ArrayList<>();

        createTimestampArrayList();

        prepareMediaPlayer(null);

        setupClickListeners();

        setupSeekBar();

        return view;
    }

    // Load string using prefix and parse to json, then extract timestamps
    private void LoadTimestamps(String filePath){
        StringBuffer stringBuffer = new StringBuffer();

        try {
            BufferedReader inputReader = new BufferedReader(new
                    InputStreamReader(getContext().openFileInput(filePath + ".tds")));
            String inputString;

            while ((inputString = inputReader.readLine()) != null) {
                stringBuffer.append(inputString);
            }

            String jsonString = stringBuffer.toString();

            timestamps.clear();

            try {
                JSONArray tempJson = new JSONArray(jsonString);

                for (int i = 0; i < tempJson.length(); i++){
                    timestamps.add(new Timestamp(tempJson.getInt(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SetPlaybackInfo(String filePath){
        // Get full path from prefix of recording sent here to be played
        String actualFilePath = getContext().getFilesDir() + "/" + filePath + ".ogg";

        TextView titleTextView = view.findViewById(R.id.recording_title_text_view);
        titleTextView.setText(filePath);

        // Re-initialize media player and other views with requested prefix name
        prepareMediaPlayer(actualFilePath);

        setupClickListeners();

        setupSeekBar();

        LoadTimestamps(filePath);

        sortTimestamps();

        createTimestampArrayList();
        timestampAdapter.notifyDataSetChanged();
        listView.setVisibility(View.VISIBLE);
        listView.findViewById(R.id.timestamp_list).setVisibility(View.VISIBLE);
    }

    private void createTimestampArrayList(){
        // Load timestamps into view
        timestampAdapter = new TimestampAdapter(getActivity(), timestamps);
        listView = (ListView)view.findViewById(R.id.timestamp_list);
        listView.setAdapter(timestampAdapter);
        //listView.setVisibility(View.INVISIBLE);

        // Execute when timestamp is tapped
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int stampTime = timestamps.get(i).getCurrTime();
                mMediaPlayer.seekTo(stampTime);

                if ( !mMediaPlayer.isPlaying() ) {
                    mMediaPlayer.start();
                    seekBarHandler.postDelayed(seekBarRunnable, 0);
                    playButton.setImageResource(pauseBtnImage);
                    //playButton.setText(R.string.pause_btn_text);
                }
            }
        });
    }

    public static String getTime(int ms){
        Date time = new Date(ms);
        DateFormat formatter = new SimpleDateFormat("mm:ss:SS");
        String timeFormatted = formatter.format(time);

        return timeFormatted;
    }

    // Setting up seekbar
    private void setupSeekBar(){
        seekBar = view.findViewById(R.id.seekbar_view);
        final TextView currTime = view.findViewById(R.id.curr_time_text_view);
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
    }

    private void setPlayButtonListener(){
        playButton = view.findViewById(R.id.play_button_view);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mpPrepared) {
                    // Play if audio is not playing
                    if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                        playButton.setImageResource(pauseBtnImage);
                        //playButton.setText(R.string.pause_btn_text);
                        seekBarHandler.postDelayed(seekBarRunnable, 0);
                    }
                    // Pause if audio is playing
                    else if (mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();
                        playButton.setImageResource(playBtnImage);
                        //playButton.setText(R.string.play_btn_text);
                        seekBarHandler.removeCallbacks(seekBarRunnable);
                    }
                }
            }
        });
    }

    private void setRestartButtonListener(){
        ImageButton restartButton = view.findViewById(R.id.restart_button_view);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If the media player is prepared and the button is pressed, seek to 0 ms and play
                if (mpPrepared){
                    mMediaPlayer.seekTo(0);
                    seekBarHandler.removeCallbacks(seekBarRunnable);
                    //if (!mMediaPlayer.isPlaying())
                    playButton.setImageResource(playBtnImage);
                        //playButton.setText(R.string.pause_btn_text);
                    stampArrayPos = -1;
                    mMediaPlayer.pause();
                    seekBarHandler.postDelayed(seekBarRunnable, 0);
                }
            }
        });
    }

    private void setTimestamperButtonListener(){
        ImageButton setTimestampButton = view.findViewById(R.id.mark_button_view);

        setTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Adjust stampTimeCushion depending on what feels "right"
                // This variable is used to compensate for user pressing button after intended mark
                // location.
                // TODO: Make cushion user defined setting
                //int stampTime = mMediaPlayer.getCurrentPosition() - stampTimeCushion;
                int stampTime = mMediaPlayer.getCurrentPosition();

                listView.findViewById(R.id.timestamp_list).setVisibility(View.VISIBLE);

                // Add timestamp to arraylist
                timestamps.add(new Timestamp(stampTime));
                sortTimestamps();
                timestampAdapter.notifyDataSetChanged();

                //String toastText = getResources().getString(R.string.mark_toast_text) + " " + getTime(stampTime);

                //Toast.makeText(view.getContext(), toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortTimestamps(){
        Collections.sort(timestamps, new Comparator<Timestamp>() {
            @Override
            public int compare(Timestamp lhs, Timestamp rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getCurrTime() < rhs.getCurrTime() ? -1 : (lhs.getCurrTime() > rhs.getCurrTime()) ? 1 : 0;
            }
        });
    }

    private void setNextTimestampNavButtonListener(){
        // TODO: Make this easier to use. Buttons for each stamp?
        ImageButton nextTimestampButton = view.findViewById(R.id.nextstamp_button_view);

        nextTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mpPrepared && (timestamps.size() > 0)){
                    stampArrayPos++;

                    if( stampArrayPos > (timestamps.size() - 1) )
                        stampArrayPos = 0;

                    int stampTime = timestamps.get(stampArrayPos).getCurrTime();
                    mMediaPlayer.seekTo(stampTime);

                    if ( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                        seekBarHandler.postDelayed(seekBarRunnable, 0);
                        playButton.setImageResource(pauseBtnImage);
                        //playButton.setText(R.string.pause_btn_text);
                    }
                }
            }
        });
    }

    private void setPrevTimestampNavButtonListener(){
        ImageButton prevTimestampButton = view.findViewById(R.id.prevstamp_button_view);

        prevTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mpPrepared && (timestamps.size() > 0)){
                    stampArrayPos--;

                    if (stampArrayPos < 0){
                        stampArrayPos = (timestamps.size() - 1);
                    }

                    int stampTime = timestamps.get(stampArrayPos).getCurrTime();
                    mMediaPlayer.seekTo(stampTime);

                    if ( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                        seekBarHandler.postDelayed(seekBarRunnable, 0);
                        playButton.setImageResource(pauseBtnImage);
                        //playButton.setText(R.string.pause_btn_text);
                    }
                }
            }
        });
    }

    private void prepareMediaPlayer(String dataPath){
        if (dataPath == null)
            mMediaPlayer = MediaPlayer.create(view.getContext(), R.raw.fart);
        else {
            Uri dataUri = Uri.parse(dataPath);
            mMediaPlayer = MediaPlayer.create(view.getContext(), dataUri);
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mpPrepared = true;
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mpPrepared){
                    mMediaPlayer.seekTo(0);
                    seekBarHandler.removeCallbacks(seekBarRunnable);
                    //if (!mMediaPlayer.isPlaying())
                    playButton.setImageResource(playBtnImage);
                    //playButton.setText(R.string.pause_btn_text);
                    stampArrayPos = -1;
                    mMediaPlayer.pause();
                    seekBarHandler.postDelayed(seekBarRunnable, 0);
                }
            }
        });

    }

}
