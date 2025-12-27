package com.example.madgroupproject.ui.settingpage;

import androidx.appcompat.app.AppCompatDelegate;

public class DarkMode {
    public static void setDarkMode(boolean enabled) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
