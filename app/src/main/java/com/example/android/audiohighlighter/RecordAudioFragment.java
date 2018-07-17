package com.example.android.audiohighlighter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class RecordAudioFragment extends Fragment {

    private View view;
    private boolean isRecording, isPaused;
    private MediaRecorder audioRecorder;
    private File internalDirectory;
    private MainActivityInterface mainActivityInterface;
    private String temporaryAudioFilePath;
    private String tempFilePrefix;
    private ArrayList<Timestamp> timestamps;
    private Handler timeTrackingHandler;
    private Runnable timeTrackingRunnable;
    private int timeMillis;
    private View recordAudioButton;
    private long startTime, currTime, pauseTimeMillis;

    public static RecordAudioFragment newInstance(){
        RecordAudioFragment fragment = new RecordAudioFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if(activity instanceof MainActivityInterface) {
            mainActivityInterface = (MainActivityInterface)activity;
        } else {
            // Throw an error!
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_record_audio, container, false);
        internalDirectory = getContext().getFilesDir();
        timestamps = new ArrayList<>();

        SetRecordAudioButtonListener();
        SetRecordTimestampButtonListener();
        SetSaveRecordingButtonListener();

        return view;
    }

    //TODO: Fix file types, path, and encoding
    private void InitializeAudioRecorder(){
        // Use time/date for temporary recording name
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        tempFilePrefix = df.format(Calendar.getInstance().getTime());

        // Create file for output with proper name
        File newFile = new File(internalDirectory, tempFilePrefix + ".ogg");

        temporaryAudioFilePath = newFile.getAbsolutePath();

        audioRecorder = new MediaRecorder();

        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setOutputFile(temporaryAudioFilePath);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            audioRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Runnable for tracking current length of recording
        timeTrackingHandler = new Handler();

        timeTrackingRunnable = new Runnable() {
            @Override
            public void run() {
                timeMillis += 10;
                timeTrackingHandler.postDelayed(this, 10);
            }
        };

        // Pause runnable until recording begins
        timeTrackingHandler.removeCallbacks(timeTrackingRunnable);
        timeMillis = 0;

        //ImageButton btn = (ImageButton)view.findViewById(R.id.record_timestamp_btn);
        //btn.setColorFilter(R.color.colorSlightlyGray);
    }

    private void RecordButtonPressed(){
        if (!isRecording && !isPaused) {
            // Prepare media recorder and initialize/re-initialize variables properly
            InitializeAudioRecorder();
            isRecording = true;
            ImageButton btn = (ImageButton)recordAudioButton;
            btn.setImageResource(R.drawable.ic_baseline_mic_none_24px);
            //btn = (ImageButton)view.findViewById(R.id.record_timestamp_btn);
            //btn.setColorFilter(R.color.colorAccent);
            TextView text = (TextView)view.findViewById(R.id.record_tip_text_view);
            text.setText("Pause");
            timeMillis = 0;
            timestamps.clear();

            // Start recording
            audioRecorder.start();
            startTime = SystemClock.uptimeMillis();

            // Start runnable
            timeTrackingHandler.postDelayed(timeTrackingRunnable, 10);

            // Change from hard coded string
            Toast.makeText(view.getContext(), "Recording started.", Toast.LENGTH_SHORT).show();
        }
        else if (isRecording && isPaused){
            UnpauseRecording();
        }
        else{
            PauseRecording();
            //StopRecording(true);
        }
    }

    private void PauseRecording(){
        isPaused = true;
        ImageButton btn = (ImageButton)recordAudioButton;
        btn.setImageResource(R.drawable.ic_baseline_mic_24px);
        TextView text = view.findViewById(R.id.record_tip_text_view);
        text.setText("Resume");
        //btn = (ImageButton)view.findViewById(R.id.record_timestamp_btn);
        //btn.setColorFilter(R.color.colorSlightlyGray);

        pauseTimeMillis = SystemClock.uptimeMillis();

        audioRecorder.pause();
        timeTrackingHandler.removeCallbacks(timeTrackingRunnable);
    }

    private void UnpauseRecording(){
        isPaused = false;
        ImageButton btn = (ImageButton)recordAudioButton;
        btn.setImageResource(R.drawable.ic_baseline_mic_none_24px);
        TextView text = view.findViewById(R.id.record_tip_text_view);
        text.setText("Pause");
        //btn = (ImageButton)view.findViewById(R.id.record_timestamp_btn);
        //btn.setColorFilter(R.color.colorAccent);

        pauseTimeMillis = SystemClock.uptimeMillis() - pauseTimeMillis;

        audioRecorder.resume();
        timeTrackingHandler.postDelayed(timeTrackingRunnable, 10);
    }

    public void StopRecording(Boolean manuallyEnded){
        if (isRecording) {
            // Set recording variables, pause runnable, stop recording
            isRecording = false;
            isPaused = false;
            ImageButton btn = (ImageButton) recordAudioButton;
            btn.setImageResource(R.drawable.ic_baseline_mic_24px);
            TextView text = (TextView)view.findViewById(R.id.record_tip_text_view);
            text.setText("Record");
            timeTrackingHandler.removeCallbacks(timeTrackingRunnable);
            startTime = 0;
            audioRecorder.stop();

            SaveRecording();

            // Change from hard coded string
            if (manuallyEnded)
                mainActivityInterface.SwitchToFragment(tempFilePrefix);
        }
    }

    private void SaveRecording(){
        // Create filename for timestamp list with same prefix as audio
        String filename = tempFilePrefix + ".tds";
        FileOutputStream outputStream;

        // Create json array for saving array
        JSONArray jsonArray = new JSONArray();
        int jObjIndex = 0;
        for (Timestamp stamp:
                timestamps) {
            try {
                // Add each stamp to json array
                jsonArray.put(jObjIndex, stamp.getCurrTime());
                jObjIndex++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Convert json containing stamps to string so it can be saved
        String json = jsonArray.toString();

        // Save json string to file
        try {
            outputStream = getContext().openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void RecordTimestampButtonPressed(){
        if (isRecording && !isPaused) {
            currTime = SystemClock.uptimeMillis() - pauseTimeMillis - startTime;
            currTime -= getActivity().getPreferences(Context.MODE_PRIVATE).getInt("TimestampCushion", 0);
            if (currTime < 0)
                currTime = 0;
            timestamps.add(new Timestamp((int) currTime));
            Toast.makeText(view.getContext(), "Time added: " + Integer.toString(timeMillis), Toast.LENGTH_SHORT).show();
        }
    }

    private void SetRecordAudioButtonListener(){
        recordAudioButton = view.findViewById(R.id.record_audio_btn);

        recordAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordButtonPressed();
            }
        });
    }
    private void SetRecordTimestampButtonListener(){
        final ImageButton recordTimestampButton = view.findViewById(R.id.record_timestamp_btn);

        recordTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordTimestampButtonPressed();
            }
        });
    }

    private void SetSaveRecordingButtonListener(){
        ImageButton saveRecordingBtn = view.findViewById(R.id.save_recording_btn);
        saveRecordingBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                StopRecording(true);
            }
        });
    }

}
