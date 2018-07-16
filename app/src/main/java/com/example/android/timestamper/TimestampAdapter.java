package com.example.android.timestamper;

import android.app.Activity;
import android.app.AlertDialog;
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
import java.util.ArrayList;

public class TimestampAdapter extends ArrayAdapter<Timestamp> {

    public TimestampAdapter(Activity context, ArrayList<Timestamp> timestamps){
        super(context, 0, timestamps);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View listItemView = convertView;

        if (listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.timestamp_list_item, parent, false);
        }

        final Timestamp currentStamp = getItem(position);

        TextView stampTimeTextView = (TextView)listItemView.findViewById(R.id.timestamp_item_time_text_view);
        stampTimeTextView.setText(MediaPlaybackFragment.getTime(currentStamp.getCurrTime()));

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
                        // TODO: Add menu item for adding description/comment for timestamp
                        switch (item.getItemId()) {
                            case R.id.timestamp_option_delete:
                                // TODO: Implement this
                                Toast.makeText(getContext(), "DELETING TIMESTAMP.", Toast.LENGTH_SHORT).show();

                                return true;

                            case R.id.timestamp_option_comment:
                                Toast.makeText(getContext(), "EDITING COMMENT.", Toast.LENGTH_SHORT).show();
                                /*AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Enter New Name");

                                // Set up the input
                                final EditText input = new EditText(getContext());
                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                builder.setView(input);

                                // Set up the buttons
                                builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        m_Text = input.getText().toString();

                                        // TODO: Add check for already existing duplicate name
                                        File file = new File(getContext().getFilesDir(), currentItem.getItemName()+".ogg");
                                        File newFile = new File(getContext().getFilesDir(), m_Text + ".ogg");
                                        file.renameTo(newFile);

                                        file = new File(getContext().getFilesDir(), currentItem.getItemName()+".tds");
                                        newFile = new File(getContext().getFilesDir(), m_Text + ".tds");
                                        file.renameTo(newFile);

                                        libraryItemNameTextView.setText(m_Text);
                                        currentItem.setNewItemName(m_Text);
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();*/
                                return true;

                            case R.id.timestamp_option_toast:
                                // This is just for testing
                                Toast.makeText(getContext(), Integer.toString(currentStamp.getCurrTime()), Toast.LENGTH_SHORT).show();
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
