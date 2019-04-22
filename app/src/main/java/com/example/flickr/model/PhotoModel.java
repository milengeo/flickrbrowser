package com.example.flickr.model;


import android.graphics.Bitmap;


/**
 * The model of a single photo object
 */
public class PhotoModel {
    private String mId;
    private String mTitle;
    private String mOwner;
    private String mSecret;
    private String mServer;
    private String mFarm;
    private Bitmap mBitmap;


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }


    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }


    public String getSecret() {
        return mSecret;
    }

    public void setSecret(String secret) {
        mSecret = secret;
    }


    public String getServer() {
        return mServer;
    }

    public void setServer(String server) {
        mServer = server;
    }


    public String getFarm() {
        return mFarm;
    }

    public void setFarm(String farm) {
        mFarm = farm;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }
}
