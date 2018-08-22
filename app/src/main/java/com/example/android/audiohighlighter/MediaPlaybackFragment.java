package com.example.android.audiohighlighter;

import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
    public ImageButton playButton;
    private ArrayList<Timestamp> timestamps;
    public TimestampAdapter timestampAdapter;
    private int playBtnImage = R.drawable.ic_baseline_play_circle_filled_24px;
    private int pauseBtnImage = R.drawable.ic_baseline_pause_circle_filled_24px;
    private static MediaPlaybackFragment fragment;
    private ListView listView;
    private String tempFilePath;
    private int recordingDuration;

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

    public MediaPlayer GetMediaPlayer(){
        return mMediaPlayer;
    }

    // Load string using prefix and parse to json, then extract timestamps
    private void LoadTimestamps(String filePath){
        tempFilePath = filePath;
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

                recordingDuration = tempJson.getInt(0);
                TextView durationText = view.findViewById(R.id.duration_text_view);
                durationText.setText(getTime(recordingDuration));

                for (int i = 1; i < tempJson.length(); i++){
                    timestamps.add(new Timestamp(tempJson.getInt(i), tempJson.getString(i + 1)));
                    i++;
                }

                if (timestamps.size() < 1){
                    TextView tv = view.findViewById(R.id.empty_stamp_list_text);
                    tv.setVisibility(View.VISIBLE);
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
                int stampTime = mMediaPlayer.getCurrentPosition();

                listView.findViewById(R.id.timestamp_list).setVisibility(View.VISIBLE);

                // Add timestamp to arraylist
                timestamps.add(new Timestamp(stampTime, "(Add comment...)"));
                sortTimestamps();
                timestampAdapter.notifyDataSetChanged();
                SaveTimestamps();
            }
        });
    }

    public ArrayList<Timestamp> GetTimestamps(){
        return timestamps;
    }

    public void SetTimestamps(ArrayList<Timestamp> stamps){
        if(stamps != null)
            timestamps = stamps;
        if (timestamps.size() > 1)
            sortTimestamps();

        TextView tv = view.findViewById(R.id.empty_stamp_list_text);

        if (timestamps.size() < 1){
            tv.setVisibility(View.INVISIBLE);
        }
        else {
            tv.setVisibility(View.VISIBLE);
        }
    }

    public void EditComment(int pos, String newComment){
        timestamps.get(pos).SetTimestampComment(newComment);
    }

    public void SaveTimestamps(){
        FileOutputStream outputStream;

        File oldFile = new File(getContext().getFilesDir(), tempFilePath + ".tds");
        oldFile.delete();

        // Create json array for saving array
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(0, recordingDuration);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int jObjIndex = 1;
        for (Timestamp stamp:
                timestamps) {
            try {
                // Add each stamp to json array
                jsonArray.put(jObjIndex, stamp.getCurrTime());
                jObjIndex++;
                jsonArray.put(jObjIndex, stamp.getTimestampComment());
                jObjIndex++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Convert json containing stamps to string so it can be saved
        String json = jsonArray.toString();

        // Save json string to file
        try {
            outputStream = getContext().openFileOutput(tempFilePath + ".tds", Context.MODE_APPEND);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sortTimestamps(){
        Collections.sort(timestamps, new Comparator<Timestamp>() {
            @Override
            public int compare(Timestamp lhs, Timestamp rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getCurrTime() < rhs.getCurrTime() ? -1 : (lhs.getCurrTime() > rhs.getCurrTime()) ? 1 : 0;
            }
        });

        TextView tv = view.findViewById(R.id.empty_stamp_list_text);

        if (timestamps.size() > 0){
            tv.setVisibility(View.INVISIBLE);
        }
        else {
            tv.setVisibility(View.VISIBLE);
        }
    }

    private void setNextTimestampNavButtonListener(){
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
