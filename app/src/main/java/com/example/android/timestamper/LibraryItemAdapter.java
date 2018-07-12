package com.example.android.timestamper;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LibraryItemAdapter extends ArrayAdapter<LibraryItem> {
    public LibraryItemAdapter(Activity context, ArrayList<LibraryItem> libraryItems){
        super(context, 0, libraryItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View listItemView = convertView;

        if (listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.library_list_item, parent, false);
        }

        final LibraryItem currentItem = getItem(position);

        TextView libraryItemNameTextView = (TextView)listItemView.findViewById(R.id.library_item_name_text_view);
        libraryItemNameTextView.setText(currentItem.getItemName());

        return listItemView;
    }
}
