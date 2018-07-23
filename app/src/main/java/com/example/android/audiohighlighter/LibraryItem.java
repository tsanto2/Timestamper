package com.example.android.audiohighlighter;

public class LibraryItem {
    private String mItemName;
    private int mItemDuration;

    public LibraryItem(String itemName, int itemDuration){
        mItemName = itemName;
        mItemDuration = itemDuration;
    }

    public String getItemName(){return mItemName;}

    public int GetItemDuration(){return mItemDuration;}

    public void setNewItemName(String newName){
        mItemName = newName;
    }
}
