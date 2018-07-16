package com.example.android.timestamper;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class SettingsFragment extends Fragment {

    private int stampCushion;
    private SharedPreferences sharedPreferences;

    public static SettingsFragment newInstance(){
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        TextView tv = view.findViewById(R.id.cushion_setting_text_view);
        tv.setText(Integer.toString(sharedPreferences.getInt("TimestampCushion", 0)) + " ms");

        RelativeLayout cushionView = view.findViewById(R.id.cushion_setting_parent_view);
        cushionView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO: Implement function to set cushion for timestamping
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter New Cushion Value:");

                final View vFinal = v;

                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Save Cushion", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stampCushion = Integer.parseInt(input.getText().toString());

                        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("TimestampCushion", stampCushion);
                        editor.commit();
                        TextView tv = vFinal.findViewById(R.id.cushion_setting_text_view);
                        tv.setText(Integer.toString(stampCushion)+" ms");
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
        });

        return view;
    }

}