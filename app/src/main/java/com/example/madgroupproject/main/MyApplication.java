package com.example.madgroupproject.main;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        int mode = prefs.getInt(
                "theme_mode",
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );

        AppCompatDelegate.setDefaultNightMode(mode);
    }
}