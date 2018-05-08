package com.example.android.timestamper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class LibraryAccessFragment extends Fragment {

    public static LibraryAccessFragment newInstance(){
        LibraryAccessFragment fragment = new LibraryAccessFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_access_library, container, false);

        return view;
    }

}