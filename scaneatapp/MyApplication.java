package com.app.project.scaneatapp;

import android.app.Application;
import android.content.Context;
import com.firebase.client.Firebase;


public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(getApplicationContext());
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
