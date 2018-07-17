package com.example.android.audiohighlighter;

public class Timestamp {

    private String mTimestampComment = "";
    private int mCurrTime = 0;

    public Timestamp(int currTime){
        mCurrTime = currTime;
    }
    public Timestamp(int currTime, String comment){
        mCurrTime = currTime;
        mTimestampComment = comment;
    }

    public void SetTimestampComment(String comment){
        mTimestampComment = comment;
    }
    public String getTimestampComment(){
        return mTimestampComment;
    }

    public int getCurrTime(){
        return mCurrTime;
    }

}
