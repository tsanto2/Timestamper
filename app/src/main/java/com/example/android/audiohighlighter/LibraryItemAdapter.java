package com.example.android.audiohighlighter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LibraryItemAdapter extends ArrayAdapter<LibraryItem>{
    private LibraryItemAdapter thisAdapter;
    private ArrayList<LibraryItem> items;
    private MainActivityInterface mainActivityInterface;
    private String m_Text;

    public LibraryItemAdapter(Activity context, ArrayList<LibraryItem> libraryItems, MainActivityInterface mai){
        super(context, 0, libraryItems);
        items = libraryItems;
        mainActivityInterface = mai;
        thisAdapter = this;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent){
        View listItemView = convertView;

        if (listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.library_list_item, parent, false);
        }

        final LibraryItem currentItem = getItem(position);

        final TextView libraryItemNameTextView = listItemView.findViewById(R.id.library_item_name_text_view);
        libraryItemNameTextView.setText(currentItem.getItemName());

        TextView itemDurationTextView = listItemView.findViewById(R.id.library_item_duration_text_view);
        int temp = currentItem.GetItemDuration();
        MainActivity activity = (MainActivity)getContext();
        MediaPlaybackFragment playbackFrag = (MediaPlaybackFragment)activity.getSupportFragmentManager().findFragmentByTag("playback");
        String time = playbackFrag.getTime(temp);
        itemDurationTextView.setText(time);

        ImageView libraryItemImageView = listItemView.findViewById(R.id.library_item_options_button_view);
        libraryItemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), v);
                // Inflate the menu from xml
                popup.inflate(R.menu.library_item_options);
                // Setup menu item selection
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_option_delete:
                                // TODO: Create pop-up dialogue to check if user actually wants to delete
                                Toast.makeText(getContext(), "DELETING FILES.", Toast.LENGTH_SHORT).show();

                                // Delete associated files from directory
                                File file = new File(getContext().getFilesDir(), currentItem.getItemName()+".ogg");
                                file.delete();
                                file = new File(getContext().getFilesDir(), currentItem.getItemName() + ".tds");
                                file.delete();

                                // Remove item from ArrayList and update adaptor
                                items.remove(getItem(position));
                                thisAdapter.notifyDataSetChanged();

                                return true;

                            case R.id.item_option_share:
                                MainActivity mi = (MainActivity)((MainActivity) getContext()).getSupportFragmentManager().findFragmentByTag("library").getActivity();
                                if (!mi.ReadWritePermissionGranted()){
                                    Toast.makeText(getContext(), "Please grant file access permissions.", Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                String sharePath = copyFiletoExternalStorage(getContext().getFilesDir() + "/"
                                    + currentItem.getItemName()+".ogg", currentItem.getItemName()+".wav");
                                Uri uri = Uri.parse(sharePath);
                                File newFile = new File(uri.getPath());
                                Uri fileUri = FileProvider.getUriForFile(getContext(), "com.example.android.audiohighlighter", newFile);
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                share.setType("audio/*");
                                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                                ((MainActivity) getContext()).getApplication().startActivity(Intent.createChooser(share, "Share Sound File").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));

                                return true;

                            case R.id.item_option_rename:
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Enter New Name");

                                // Set up the input
                                final EditText input = new EditText(getContext());
                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                                builder.setView(input);

                                // Set up the buttons
                                builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        m_Text = input.getText().toString();

                                        if(m_Text.length() > 0 && m_Text != null) {
                                            // TODO: Add check for already existing duplicate name
                                            File file = new File(getContext().getFilesDir(), currentItem.getItemName() + ".ogg");
                                            File newFile = new File(getContext().getFilesDir(), m_Text + ".ogg");
                                            file.renameTo(newFile);

                                            file = new File(getContext().getFilesDir(), currentItem.getItemName() + ".tds");
                                            newFile = new File(getContext().getFilesDir(), m_Text + ".tds");
                                            file.renameTo(newFile);

                                            libraryItemNameTextView.setText(m_Text);
                                            currentItem.setNewItemName(m_Text);
                                        }
                                        else{
                                            Toast.makeText(getContext(), "ILLEGAL FILE NAME.", Toast.LENGTH_SHORT).show();
                                        }
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

    private String copyFiletoExternalStorage(String resourceId, String resourceName){
        String pathSDCard = Environment.getExternalStorageDirectory() + "/Android/data/" + resourceName;
        try{
            InputStream in = new FileInputStream(resourceId);
            FileOutputStream out = null;
            out = new FileOutputStream(pathSDCard);
            byte[] buff = new byte[1024];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  pathSDCard;
    }
}
