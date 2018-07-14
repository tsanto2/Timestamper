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
                        // TODO: Add menu item for adding description/comment for timestamp
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

                            case R.id.item_option_rename:
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Enter New Name");

                                // Set up the input
                                final EditText input = new EditText(getContext());
                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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

                                builder.show();
                                return true;

                            case R.id.item_option_toast:
                                // This is just for testing
                                Toast.makeText(getContext(), currentItem.getItemName(), Toast.LENGTH_SHORT).show();
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
