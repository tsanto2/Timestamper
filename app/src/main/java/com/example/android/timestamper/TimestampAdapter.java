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
            //stampTimeTextView.setText("Titty.");

        return listItemView;
    }

}
