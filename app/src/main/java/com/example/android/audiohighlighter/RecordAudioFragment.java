package com.example.android.audiohighlighter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class RecordAudioFragment extends android.support.v4.app.Fragment{

    private View view;
    private boolean isRecording, isPaused;
    private MediaRecorder audioRecorder;
    private File internalDirectory;
    private MainActivityInterface mainActivityInterface;
    private MediaPlaybackFragment playbackFrag;
    private String temporaryAudioFilePath;
    private String tempFilePrefix;
    private ArrayList<Timestamp> timestamps;
    private int recordingCount;
    private Handler timeTrackingHandler, visualizerHandler;
    private Runnable timeTrackingRunnable, visualizerRunnable;
    private int timeMillis;
    private View recordAudioButton;
    private long startTime, currTime, pauseStartTime, pauseTimeMillis;
    private String newTitle;
    private long recordingLimit;
    public MainActivity mi;

    private float timestampElevation, recordElevation, saveElevation;

    private int sampleRate;

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
        mi = (MainActivity)getActivity();

        internalDirectory = getContext().getFilesDir();
        timestamps = new ArrayList<>();

        playbackFrag = (MediaPlaybackFragment)getActivity().getSupportFragmentManager().findFragmentByTag("playback");

        TextView tv2 = view.findViewById(R.id.storage_space_text);
        long freeSpace = internalDirectory.getFreeSpace();
        float freeSpaceMB = freeSpace / 1048576;
        tv2.setText(Float.toString(freeSpaceMB));

        SetRecordAudioButtonListener();
        SetRecordTimestampButtonListener();
        SetSaveRecordingButtonListener();
        SetTitleTextTouchedListener();

        Button btn = view.findViewById(R.id.purchase_btn);
        btn.setVisibility(View.GONE);

        boolean storageTextDisabled = getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean("StorageTextDisabled", true);
        LinearLayout storageLayout = view.findViewById(R.id.storage_space_layout);

        if (storageTextDisabled){
            storageLayout.setVisibility(View.GONE);
        }

        boolean hintsTextDisabled = getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean("HintsDisabled", false);
        if (hintsTextDisabled) {
            TextView tv = view.findViewById(R.id.title_hint_text);
            tv.setVisibility(View.GONE);

            tv = view.findViewById(R.id.record_tip_text_view);
            tv.setVisibility(View.GONE);

            tv = view.findViewById(R.id.save_tip_text_view);
            tv.setVisibility(View.GONE);

            tv = view.findViewById(R.id.timestamp_tip_text_view);
            tv.setVisibility(View.GONE);
        }

        return view;
    }

    //TODO: Fix file types, path, and encoding
    private void InitializeAudioRecorder(){
        newTitle = null;

        // Use time/date for temporary recording name
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy' T 'HH-mm-ss");
        tempFilePrefix = df.format(Calendar.getInstance().getTime());

        TextView tv = view.findViewById(R.id.record_screen_title_text);
        tv.setText(tempFilePrefix);

        TextView tv2 = view.findViewById(R.id.storage_space_text);
        long freeSpace = internalDirectory.getFreeSpace();
        float freeSpaceMB = freeSpace / 1048576;
        tv2.setText(Float.toString(freeSpaceMB));

        // Create file for output with proper name
        File newFile = new File(internalDirectory, tempFilePrefix + ".ogg");
        temporaryAudioFilePath = newFile.getAbsolutePath();

        audioRecorder = new MediaRecorder();

        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setOutputFile(temporaryAudioFilePath);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        sampleRate = getActivity().getPreferences(Context.MODE_PRIVATE).getInt("AudioSampleRate", 48);
        audioRecorder.setAudioSamplingRate(sampleRate * 1000);
        int bitRate = getActivity().getPreferences(Context.MODE_PRIVATE).getInt("AudioBitRate", 24);
        audioRecorder.setAudioEncodingBitRate(bitRate * 1000);

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

                TextView tv2 = view.findViewById(R.id.storage_space_text);
                long freeSpace = internalDirectory.getFreeSpace();
                float freeSpaceMB = freeSpace / 1048576;
                tv2.setText(Float.toString(freeSpaceMB));

                TextView recLength = view.findViewById(R.id.recording_length_text);
                int time = (int)(SystemClock.uptimeMillis() - pauseTimeMillis - startTime);
                recLength.setText(playbackFrag.getTime(time));

                if (time >= recordingLimit && !mi.IsPremium()){
                    PauseRecording();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Free Recording Limit Reached")
                            .setMessage("Premium unlock allows you to record for an unlimited amount of time! Please purchase premium if you would like to record for a longer duration.")
                            .setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mi.PurchasePremium();
                                    UnpauseRecording();
                                    StopRecording(true);
                                }
                            })
                            .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    UnpauseRecording();
                                    StopRecording(true);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    timeMillis = 0;
                }
                else {
                    timeTrackingHandler.postDelayed(this, 10);
                }
            }
        };

        // Pause runnable until recording begins
        timeTrackingHandler.removeCallbacks(timeTrackingRunnable);
        timeMillis = 0;
    }

    private void RecordButtonPressed(){
        MainActivity mi = (MainActivity)getActivity();
        if (!mi.RecordAudioPermissionGranted()){
            Toast.makeText(view.getContext(), "Please grant recording permission.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mi.IsPremium()) {
            recordingLimit = 1200000;
            //recordingLimit = 10000;
        }

        if (!isRecording && !isPaused) {
            // Prepare media recorder and initialize/re-initialize variables properly
            InitializeAudioRecorder();
            isRecording = true;
            ImageButton btn = (ImageButton)recordAudioButton;
            btn.setImageResource(R.drawable.ic_baseline_mic_none_24px);

            TextView text = (TextView)view.findViewById(R.id.record_tip_text_view);
            text.setText("Pause");
            timeMillis = 0;
            pauseTimeMillis = 0;
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

        pauseStartTime = SystemClock.uptimeMillis();

        audioRecorder.pause();
        timeTrackingHandler.removeCallbacks(timeTrackingRunnable);
    }

    private void UnpauseRecording(){
        isPaused = false;
        ImageButton btn = (ImageButton)recordAudioButton;
        btn.setImageResource(R.drawable.ic_baseline_mic_none_24px);
        TextView text = view.findViewById(R.id.record_tip_text_view);
        text.setText("Pause");

        pauseTimeMillis = SystemClock.uptimeMillis() - pauseStartTime + pauseTimeMillis;

        audioRecorder.resume();
        timeTrackingHandler.postDelayed(timeTrackingRunnable, 10);
    }

    public void StopRecording(Boolean manuallyEnded){
        if (isRecording) {
            if (isPaused)
                UnpauseRecording();
            TextView tv = view.findViewById(R.id.record_screen_title_text);
            tv.setText("Start Recording");
            // Set recording variables, pause runnable, stop recording
            isRecording = false;
            isPaused = false;
            ImageButton btn = (ImageButton) recordAudioButton;
            btn.setImageResource(R.drawable.ic_baseline_mic_24px);
            TextView text = (TextView)view.findViewById(R.id.record_tip_text_view);
            text.setText("Record");
            timeTrackingHandler.removeCallbacks(timeTrackingRunnable);
            audioRecorder.stop();

            SaveRecording();

            startTime = 0;

            text = (TextView)view.findViewById(R.id.recording_length_text);
            text.setText("00:00:00");

            // Change from hard coded string
            if (manuallyEnded)
                mainActivityInterface.SwitchToFragment(tempFilePrefix);

            else{
                LibraryAccessFragment tempLibFrag = (LibraryAccessFragment)getActivity().getSupportFragmentManager().findFragmentByTag("library");
                tempLibFrag.RefreashLibraryItemAdapter();
            }
        }
    }

    private void SaveRecording(){
        // Create filename for timestamp list with same prefix as audio
        if (newTitle != null){
            File file = new File(getContext().getFilesDir(), tempFilePrefix + ".ogg");
            File newFile = new File(getContext().getFilesDir(), newTitle + ".ogg");
            file.renameTo(newFile);

            tempFilePrefix = newTitle;
        }

        String filename = tempFilePrefix + ".tds";

        FileOutputStream outputStream;

        // Create json array for saving array
        JSONArray jsonArray = new JSONArray();

        /*if (isPaused){
            pauseTimeMillis = SystemClock.uptimeMillis() - pauseStartTime + pauseTimeMillis;
        }*/

        int time = (int)(SystemClock.uptimeMillis() - pauseTimeMillis - startTime);
        try {
            jsonArray.put(0, time);
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
                jsonArray.put(jObjIndex, "(Add comment...)");
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
            timestamps.add(new Timestamp((int)currTime));
            Toast.makeText(view.getContext(), "Time added: " + Integer.toString(timeMillis), Toast.LENGTH_SHORT).show();
        }
    }

    private void SetRecordAudioButtonListener(){
        recordAudioButton = view.findViewById(R.id.record_audio_btn);
        recordElevation = recordAudioButton.getElevation();

        recordAudioButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    recordAudioButton.setElevation(0);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    recordAudioButton.setElevation(recordElevation);
                    recordingCount = 0;
                    for (File file : getContext().getFilesDir().listFiles()) {
                        recordingCount++;
                    }

                    if ((!mi.IsPremium() && recordingCount < 12) || mi.IsPremium())
                        RecordButtonPressed();
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Free Recording Limit Reached")
                                .setMessage("Premium unlock allows you to create an unlimited amount of recordings! Please purchase premium or delete an existing recording to create more.")
                                .setPositiveButton("Purchase", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        mi.PurchasePremium();
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
                }
                return true;
            }
        });
    }

    private void SetRecordTimestampButtonListener(){
        final ImageButton recordTimestampButton = view.findViewById(R.id.record_timestamp_btn);
        timestampElevation = recordTimestampButton.getElevation();

        recordTimestampButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    recordTimestampButton.setElevation(0);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    recordTimestampButton.setElevation(timestampElevation);
                    RecordTimestampButtonPressed();
                }

                return true;
            }
        });
    }

    private void SetSaveRecordingButtonListener(){
        final ImageButton saveRecordingBtn = view.findViewById(R.id.save_recording_btn);
        saveElevation = saveRecordingBtn.getElevation();

        saveRecordingBtn.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent event){
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    saveRecordingBtn.setElevation(0);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    saveRecordingBtn.setElevation(saveElevation);
                    StopRecording(true);
                }

                return true;
            }
        });
    }

    private void SetTitleTextTouchedListener(){
        TextView titleTextView = view.findViewById(R.id.record_screen_title_text);
        titleTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (isRecording) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Enter New Title:");

                    // Set up the input
                    final EditText input = new EditText(getContext());

                    input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("Confirm Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String m_Text = input.getText().toString();
                            if (mi.ContainsIllegalCharacter(m_Text)){
                                Toast.makeText(getContext(), "Illegal character in file name.", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                return;
                            }
                            newTitle = m_Text;

                            TextView tv = getView().findViewById(R.id.record_screen_title_text);
                            tv.setText(m_Text);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            }
        });
    }

}
