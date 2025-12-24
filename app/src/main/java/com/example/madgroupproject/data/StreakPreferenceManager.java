package com.example.madgroupproject.data;

import android.content.Context;
import android.content.SharedPreferences;

public class StreakPreferenceManager {
    private static final String PREF_NAME = "StreakPrefs";
    private static final String KEY_DAILY_GOAL = "daily_goal";
    private static final int DEFAULT_DAILY_GOAL = 10000;

    private final SharedPreferences prefs;

    public StreakPreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
    }

    public void setDailyGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goal).apply();
    }

    public void resetToDefault() {
        setDailyGoal(DEFAULT_DAILY_GOAL);
    }
}