package com.example.android.timestamper;

public class LibraryItem {
    private String mItemName;

    public LibraryItem(String itemName){mItemName = itemName;}

    public String getItemName(){return mItemName;}

    public void setNewItemName(String newName){
        mItemName = newName;
    }
}
