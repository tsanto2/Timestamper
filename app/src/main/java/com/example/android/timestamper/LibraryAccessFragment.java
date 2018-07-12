package com.example.android.timestamper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class LibraryAccessFragment extends Fragment {

    private LibraryItemAdapter libraryItemAdapter;
    private ArrayList<LibraryItem> libraryItems;
    private ListView listView;
    private View view;
    private File dataDir;

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
        view = inflater.inflate(R.layout.fragment_access_library, container, false);

        libraryItems = new ArrayList<LibraryItem>();

        dataDir = getContext().getFilesDir();

        GetLibraryItems();

        SetUpArrayAdapter();

        return view;
    }

    private void GetLibraryItems(){
        for (File file : dataDir.listFiles()){
            libraryItems.add(new LibraryItem(file.getName()));
        }
    }

    private void SetUpArrayAdapter(){
        libraryItemAdapter = new LibraryItemAdapter(getActivity(), libraryItems);
        listView = (ListView)view.findViewById(R.id.library_list);
        listView.setAdapter(libraryItemAdapter);
    }

}