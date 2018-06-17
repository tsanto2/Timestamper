package com.example.android.timestamper;

public class Timestamp {

    private String mTimeText;
    private int mCurrTime = 0;

    public Timestamp(int currTime){
        mCurrTime = currTime;
    }

    public String getTimeText(){
        return mTimeText;
    }

    public int getCurrTime(){
        return mCurrTime;
    }

}
