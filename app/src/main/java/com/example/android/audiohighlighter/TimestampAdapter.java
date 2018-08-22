package com.example.android.audiohighlighter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;

public class TimestampAdapter extends ArrayAdapter<Timestamp> {

    private View listItemView;
    private ArrayList<Timestamp> stamps;
    private int pos;
    private TimestampAdapter thisAdapter;

    public TimestampAdapter(Activity context, ArrayList<Timestamp> timestamps){
        super(context, 0, timestamps);
        thisAdapter = this;
        // Commenting out for now...
        // Seems to cause crash upon timestamp deletion when stamps is not initialized.
        //if (timestamps.size() > 0)
        stamps = timestamps;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent){
        pos = position;
        listItemView = convertView;

        if (listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.timestamp_list_item, parent, false);
        }

        final Timestamp currentStamp = getItem(position);

        TextView stampTimeTextView = (TextView)listItemView.findViewById(R.id.timestamp_item_time_text_view);
        stampTimeTextView.setText(MediaPlaybackFragment.getTime(currentStamp.getCurrTime()));
        TextView stampCommentTextView = listItemView.findViewById(R.id.timestamp_comment_text);
        stampCommentTextView.setText(currentStamp.getTimestampComment());

        ImageView timestampImageView = listItemView.findViewById(R.id.timestamp_options_button_view);
        timestampImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), v);
                // Inflate the menu from xml
                popup.inflate(R.menu.timestamp_options);
                // Setup menu item selection
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.timestamp_option_delete:
                                Toast.makeText(getContext(), "DELETING TIMESTAMP.", Toast.LENGTH_SHORT).show();

                                if (getItem(position) != null && stamps != null)
                                    stamps.remove(getItem(position));

                                MainActivity activity = (MainActivity)getContext();
                                MediaPlaybackFragment mpFrag = (MediaPlaybackFragment)activity.getFragmentManager().findFragmentByTag("playback");
                                mpFrag.SetTimestamps(stamps);
                                mpFrag.SaveTimestamps();
                                mpFrag.timestampAdapter.notifyDataSetChanged();


                                return true;

                            case R.id.timestamp_option_comment:
                                Toast.makeText(getContext(), "EDITING COMMENT.", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Enter New Comment:");

                                // Set up the input
                                final EditText input = new EditText(getContext());
                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                                builder.setView(input);

                                // Set up the buttons
                                builder.setPositiveButton("Save Comment", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String m_Text = input.getText().toString();
                                        currentStamp.SetTimestampComment(m_Text);

                                        MainActivity activity = (MainActivity)getContext();
                                        MediaPlaybackFragment mpFrag = (MediaPlaybackFragment)activity.getFragmentManager().findFragmentByTag("playback");
                                        mpFrag.SetTimestamps(stamps);
                                        mpFrag.SaveTimestamps();
                                        mpFrag.timestampAdapter.notifyDataSetChanged();
                                        //mpFrag.EditComment(pos, m_Text);
                                        //mpFrag.SaveTimestamps();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                // Show the menu
                popup.show();
            }
        });

        return listItemView;
    }

}
