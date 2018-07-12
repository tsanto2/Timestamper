package com.example.android.timestamper;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class RecordAudioFragment extends Fragment {

    private View view;
    private boolean isRecording;
    private MediaRecorder audioRecorder;
    private File internalDirectory;
    private MainActivityInterface mainActivityInterface;
    private String temporaryAudioFilePath;
    private String tempFilePrefix;
    private ArrayList<Timestamp> timestamps;
    private Handler timeTrackingHandler;
    private Runnable timeTrackingRunnable;
    private int timeMillis;

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

        InitializeAudioRecorder();
        SetRecordAudioButtonListener();
        SetRecordTimestampButtonListener();

        return view;
    }

    //TODO: Fix file types, path, and encoding
    private void InitializeAudioRecorder(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        tempFilePrefix = df.format(Calendar.getInstance().getTime());
        File newFile = new File(internalDirectory, tempFilePrefix + ".ogg");
        temporaryAudioFilePath = newFile.getAbsolutePath();
        /*try {
            File tempFile = File.createTempFile("Recording", ".ogg", internalDirectory);
            temporaryAudioFilePath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setOutputFile(temporaryAudioFilePath);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //Log.d("FILELOCATION", internalDirectory.getAbsolutePath());
        try {
            audioRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void RecordButtonPressed(Button recordAudioBtn){
        if (!isRecording) {
            isRecording = true;
            audioRecorder.start();
            recordAudioBtn.setText(R.string.stop_record_btn_text);

            timeTrackingHandler = new Handler();

            timeTrackingRunnable = new Runnable() {
                @Override
                public void run() {
                    timeMillis += 10;
                    timeTrackingHandler.postDelayed(this, 10);
                }
            };

            timeTrackingHandler.postDelayed(timeTrackingRunnable, 10);

            // Change from hard coded string
            Toast.makeText(view.getContext(), "Recording started.", Toast.LENGTH_SHORT).show();
        }
        else{
            isRecording = false;
            audioRecorder.stop();
            recordAudioBtn.setText(R.string.start_record_btn_text);

            String filename = tempFilePrefix + ".tds";;
            FileOutputStream outputStream;

            // First attempt at saving timestamp array to json file...
            // Potentially able to save timestamps and audio path together in json file
            // TODO: Do above...
            JSONArray jsonArray = new JSONArray();
            int jObjIndex = 0;
            for (Timestamp stamp:
                 timestamps) {
                try {
                    jsonArray.put(jObjIndex, stamp.getCurrTime());
                    jObjIndex++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String json = jsonArray.toString();

            try {
                outputStream = getContext().openFileOutput(filename, Context.MODE_APPEND);
                outputStream.write(json.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Change from hard coded string
            mainActivityInterface.SwitchToFragment(tempFilePrefix);
        }
    }

    private void RecordTimestampButtonPressed(){
        timestamps.add(new Timestamp(timeMillis));
        Toast.makeText(view.getContext(), "Time added: " + Integer.toString(timeMillis)  , Toast.LENGTH_SHORT).show();
    }

    private void SetRecordAudioButtonListener(){
        final Button recordAudioButton = view.findViewById(R.id.record_audio_btn);

        recordAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordButtonPressed(recordAudioButton);
            }
        });
    }
    private void SetRecordTimestampButtonListener(){
        final Button recordTimestampButton = view.findViewById(R.id.record_timestamp_btn);

        recordTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordTimestampButtonPressed();
            }
        });
    }

}
