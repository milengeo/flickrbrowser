package com.example.flickr;


import android.app.Application;

import com.example.flickr.engine.PhotoDepot;

/**
 * The application class. Initializes the components which have life cycle tied to it.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PhotoDepot.getInstance().initialize(getApplicationContext());
    }

}
