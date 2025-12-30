package com.example.madgroupproject.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.madgroupproject.main.NotificationUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StreakPreferenceManager {
    private static final String PREF_NAME = "StreakPrefs";
    private static final String KEY_DAILY_GOAL = "daily_goal";
    private static final int DEFAULT_DAILY_GOAL = 10000;

    private final SharedPreferences prefs;

    public StreakPreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    //for notification use
    public static int getStreak(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_DAILY_GOAL, 0);
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

    private static final String KEY_GOAL_REACHED_DATE = "goal_reached_date";


    public static void checkAndNotifyDailyGoal(
            Context context,
            int currentSteps
    ) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        int dailyGoal = prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);

        // Not reached yet
        if (currentSteps < dailyGoal) return;


        //Show notification
        NotificationUtil.showNotification(
                context,
                2002, // unique ID for daily goal
                "Daily Step Goal Achieved 🎉",
                "You reached " + dailyGoal + " steps today. Great job!"
        );

    }

}