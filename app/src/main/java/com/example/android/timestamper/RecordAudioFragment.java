package com.example.android.timestamper;

import android.app.Activity;
import android.app.Fragment;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordAudioFragment extends Fragment {

    private View view;
    private boolean isRecording;
    private MediaRecorder audioRecorder;
    private File internalDirectory;
    private MainActivityInterface mainActivityInterface;

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

        initializeAudioRecorder();
        setRecordAudioButtonListener();

        return view;
    }

    //TODO: Fix file types, path, and encoding
    private void initializeAudioRecorder(){
        File tempFile = null;
        try {
            tempFile = File.createTempFile("Recording", ".ogg", internalDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setOutputFile(tempFile.getAbsolutePath());
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //Log.d("FILELOCATION", internalDirectory.getAbsolutePath());
        try {
            audioRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordButtonPressed(Button recordAudioBtn){
        if (!isRecording) {
            isRecording = true;
            audioRecorder.start();
            recordAudioBtn.setText(R.string.stop_record_btn_text);

            // Change from hard coded string
            Toast.makeText(view.getContext(), "Recording started.", Toast.LENGTH_SHORT).show();
        }
        else{
            isRecording = false;
            audioRecorder.stop();
            recordAudioBtn.setText(R.string.start_record_btn_text);
            /*MediaPlaybackFragment nextFrag= new MediaPlaybackFragment();
            getActivity().getFragmentManager().beginTransaction()
                    .replace(R.id.container, nextFrag,"findThisFragment")
                    .addToBackStack(null)
                    .commit();*/
            // Change from hard coded string
            mainActivityInterface.SwitchToFragment("Playback");
        }
    }

    private void setRecordAudioButtonListener(){
        final Button recordAudioButton = view.findViewById(R.id.record_audio_btn);

        recordAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButtonPressed(recordAudioButton);
            }
        });
    }

}
