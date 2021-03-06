package com.example.android.audiohighlighter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class LibraryAccessFragment extends android.support.v4.app.Fragment{

    private LibraryItemAdapter libraryItemAdapter;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<LibraryItem> libraryItems;
    private ListView listView;
    private ViewGroup view;
    private File dataDir;

    private MainActivity mi;

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
        view = (ViewGroup)inflater.inflate(R.layout.fragment_access_library, container, false);
        mi = (MainActivity)getActivity();

        // List of saved audio+timestamp items in library
        libraryItems = new ArrayList<>();

        // Data directory of this app
        dataDir = getContext().getFilesDir();

        GetLibraryItems();

        SetUpArrayAdapter();

        if (libraryItems.size() > 0){
            TextView tv = view.findViewById(R.id.empty_library_text);
            tv.setVisibility(View.INVISIBLE);
        }

        AdView bannerAdView = view.findViewById(R.id.adView);

        if (!mi.IsPremium()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            bannerAdView.loadAd(adRequest);
        }
        else{
            bannerAdView.setVisibility(View.GONE);
        }

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

        TextView tv = view.findViewById(R.id.empty_library_text);

        if (libraryItems.size() > 0){
            tv.setVisibility(View.INVISIBLE);
        }
        else{
            tv.setVisibility(View.VISIBLE);
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

        TextView tv = view.findViewById(R.id.empty_library_text);

        AdView bannerAdView = view.findViewById(R.id.adView);
        if (!mi.IsPremium()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            bannerAdView.loadAd(adRequest);
        }
        else{
            bannerAdView.setVisibility(View.GONE);
        }

        if (libraryItems.size() > 0){
            tv.setVisibility(View.INVISIBLE);
        }
        else{
            tv.setVisibility(View.VISIBLE);
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