package com.example.android.audiohighlighter;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class SettingsFragment extends android.support.v4.app.Fragment {

    private View view;

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
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        SetTimestampCushionSettingListener();

        SetAudioSampleRateSettingListener();

        SetAudioBitRateSettingListener();

        SetPremiumPopupSettingListener();

        SetStorageSpaceSettingListener();

        SetHintsSettingListener();

        return view;
    }

    private void SetAudioBitRateSettingListener(){
        TextView tv = view.findViewById(R.id.bit_rate_setting_text_view);
        tv.setText(Integer.toString(sharedPreferences.getInt("AudioBitRate", 24)) + "-bit");

        RelativeLayout bitrateView = view.findViewById(R.id.bit_rate_setting_parent_view);
        bitrateView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String list[] = {"8-bit", "16-bit", "24-bit"};

                AlertDialog.Builder alt_bld = new AlertDialog.Builder(getContext());
                alt_bld.setTitle("Select New Bit Rate:");
                alt_bld.setSingleChoiceItems(list, -1, new DialogInterface
                        .OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Toast.makeText(getContext(), "New Bit Rate: " + list[item], Toast.LENGTH_SHORT).show();
                        UpdateBitRateSetting(list[item]);
                        dialog.dismiss();

                    }
                });
                AlertDialog alert = alt_bld.create();
                alert.show();
            }
        });
    }

    private void SetAudioSampleRateSettingListener(){
        TextView tv = view.findViewById(R.id.sample_rate_setting_text_view);
        tv.setText(Integer.toString(sharedPreferences.getInt("AudioSampleRate", 16)) + " kHz");

        RelativeLayout sampleRateView = view.findViewById(R.id.sample_rate_setting_parent_view);
        sampleRateView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String list[] = {"48kHz", "44kHz", "32kHz", "22kHz", "16kHz", "8kHz"};

                AlertDialog.Builder alt_bld = new AlertDialog.Builder(getContext());
                alt_bld.setTitle("Select New Sample Rate:");
                alt_bld.setSingleChoiceItems(list, -1, new DialogInterface
                        .OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Toast.makeText(getContext(), "New Sample Rate: " + list[item], Toast.LENGTH_SHORT).show();
                        UpdateSampleRateSetting(list[item]);
                        dialog.dismiss();// dismiss the alertbox after chose option

                    }
                });
                AlertDialog alert = alt_bld.create();
                alert.show();
            }
        });
    }

    private void SetTimestampCushionSettingListener(){

        TextView tv = view.findViewById(R.id.cushion_setting_text_view);
        tv.setText(Integer.toString(sharedPreferences.getInt("TimestampCushion", 0)) + " ms");

        RelativeLayout cushionView = view.findViewById(R.id.cushion_setting_parent_view);
        cushionView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Timestamp Cushion (ms):");

                final View vFinal = v;

                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                builder.setMessage("(Amount of time subtracted from current recording time. Used to account for delay of user pressing button.)");
                // Set up the buttons
                builder.setPositiveButton("Save Cushion", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stampCushion = parseInt(input.getText().toString());

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
    }

    private void SetPremiumPopupSettingListener(){
        RelativeLayout premiumPopupSetting = view.findViewById(R.id.premium_popup_setting);
        if (sharedPreferences.getBoolean("PremiumDialogueDisabled", false)){
            TextView tv = view.findViewById(R.id.premium_popup_setting_text_view);
            tv.setText("Yes");
        }
        else{
            TextView tv = view.findViewById(R.id.premium_popup_setting_text_view);
            tv.setText("No");
        }
        premiumPopupSetting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Disable Premium Popup?")
                    .setMessage("Boobs tits ass boner penis balls dick")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO: Implement purchase flow

                            TextView tv = view.findViewById(R.id.premium_popup_setting_text_view);
                            tv.setText("No");

                            sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("PremiumDialogueDisabled", false);
                            editor.commit();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            TextView tv = view.findViewById(R.id.premium_popup_setting_text_view);
                            tv.setText("Yes");

                            sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("PremiumDialogueDisabled", true);
                            editor.commit();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();
            }
        });
    }

    private void SetStorageSpaceSettingListener(){
        RelativeLayout storageSpaceSetting = view.findViewById(R.id.storage_space_setting);
        if (sharedPreferences.getBoolean("StorageTextDisabled", true)){
            TextView tv = view.findViewById(R.id.storage_space_setting_text_view);
            tv.setText("Yes");
        }
        else{
            TextView tv = view.findViewById(R.id.storage_space_setting_text_view);
            tv.setText("No");
        }
        storageSpaceSetting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Disable Storage Space Text?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TextView tv = view.findViewById(R.id.storage_space_setting_text_view);
                                tv.setText("Yes");

                                sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("StorageTextDisabled", true);
                                editor.commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TextView tv = view.findViewById(R.id.storage_space_setting_text_view);
                                tv.setText("No");

                                sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("StorageTextDisabled", false);
                                editor.commit();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();
            }
        });
    }

    private void SetHintsSettingListener(){
        RelativeLayout hintsSetting = view.findViewById(R.id.hints_setting);
        TextView tv = view.findViewById(R.id.hints_setting_text_view);

        if (sharedPreferences.getBoolean("HintsDisabled", false)){
            tv.setText("Yes");
        }
        else{
            tv.setText("No");
        }

        hintsSetting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Disable Hints?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TextView tv = view.findViewById(R.id.hints_setting_text_view);
                                tv.setText("Yes");

                                sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("HintsDisabled", true);
                                editor.commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TextView tv = view.findViewById(R.id.hints_setting_text_view);
                                tv.setText("No");

                                sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("HintsDisabled", false);
                                editor.commit();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();
            }
        });
    }

    private void UpdateSampleRateSetting(String selection){
        for (int i = 0; i < 3; i++){
            selection = selection.substring(0, selection.length() - 1);
        }

        int newSampleRate = Integer.parseInt(selection);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("AudioSampleRate", newSampleRate);
        editor.commit();
        TextView tv = view.findViewById(R.id.sample_rate_setting_text_view);
        tv.setText(Integer.toString(newSampleRate)+" kHz");
    }

    private void UpdateBitRateSetting(String selection){
        for (int i = 0; i < 4; i++){
            selection = selection.substring(0, selection.length() - 1);
        }

        int newBitRate = Integer.parseInt(selection);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("AudioBitRate", newBitRate);
        editor.commit();
        TextView tv = view.findViewById(R.id.bit_rate_setting_text_view);
        tv.setText(Integer.toString(newBitRate) + "-bit");
    }

}