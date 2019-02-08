package com.rissy.android.classlog;

import android.app.Application;

import io.realm.Realm;

public class ImageApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        Realm.init(getApplicationContext());
    }
}
