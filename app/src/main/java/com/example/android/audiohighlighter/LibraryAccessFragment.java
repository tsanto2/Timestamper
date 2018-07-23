package com.example.android.audiohighlighter;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class LibraryAccessFragment extends Fragment{

    private LibraryItemAdapter libraryItemAdapter;
    private MainActivityInterface mainActivityInterface;
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

        // List of saved audio+timestamp items in library
        libraryItems = new ArrayList<>();

        // Data directory of this app
        dataDir = getContext().getFilesDir();

        GetLibraryItems();

        SetUpArrayAdapter();

        return view;
    }

    // Needed to use interface (I think...)
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if(activity instanceof MainActivityInterface) {
            mainActivityInterface = (MainActivityInterface)activity;
        } else {
            // Throw an error!
        }
    }

    public void GetLibraryItems(){
        //Clear list to avoid duplicates when switching to frag multiple times
        libraryItems.clear();

        ArrayList<String> itemNames = new ArrayList<>();

        // Remove file extension for listing names of saved recordings
        for (File file : dataDir.listFiles()){
            itemNames.add(RemoveFileExtension(file.getName()));
        }

        // Following for removing duplicates...
        // Record encountered Strings in HashSet.
        HashSet<String> set = new HashSet<>();

        // Loop over argument list.
        for (String name : itemNames) {

            // If String is not in set, add it to the list and the set.
            if (!set.contains(name)) {
                libraryItems.add(new LibraryItem(name, GetItemDuration(name)));
                set.add(name);
            }
        }
    }

    private int GetItemDuration(String fileName){
        StringBuffer stringBuffer = new StringBuffer();

        try {
            BufferedReader inputReader = new BufferedReader(new
                    InputStreamReader(getContext().openFileInput(fileName + ".tds")));
            String inputString;

            while ((inputString = inputReader.readLine()) != null) {
                stringBuffer.append(inputString);
            }

            String jsonString = stringBuffer.toString();

            try {
                JSONArray tempJson = new JSONArray(jsonString);

                return(tempJson.getInt(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void RefreashLibraryItemAdapter(){
        libraryItemAdapter.notifyDataSetChanged();
        if (libraryItems.size() == 0){
            //TODO: Add TextView stating no recordings exist
        }
    }

    private void SetUpArrayAdapter(){
        libraryItemAdapter = new LibraryItemAdapter(getActivity(), libraryItems, mainActivityInterface);
        listView = view.findViewById(R.id.library_list);
        listView.setAdapter(libraryItemAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(view.getContext(), libraryItems.get(i).getItemName(), Toast.LENGTH_SHORT).show();

                mainActivityInterface.SwitchToFragment(libraryItems.get(i).getItemName());
            }
        });
    }

    private String RemoveFileExtension(String name){
        for (int i = 0; i < 4; i++){
            name = name.substring(0, name.length() - 1);
        }

        return name;
    }

}