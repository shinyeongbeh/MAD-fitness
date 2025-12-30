package com.example.madgroupproject.main;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.madgroupproject.data.StreakPreferenceManager;

import java.util.stream.Stream;

public class StreakNotificationReceiver extends BroadcastReceiver {
    public static final int STREAK_NOTIFICATION_ID = 1002;


    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int streak = StreakPreferenceManager.getStreak(context);
        if(streak==0){
            NotificationUtil.showNotification(
                    context,
                    STREAK_NOTIFICATION_ID,
                    "Start Your Journey!",
                    "Start running today to start your journey!"
            );
        }

        if (streak > 0) {
            NotificationUtil.showNotification(

                    context,
                    STREAK_NOTIFICATION_ID,
                    "Keep Your Streak!",
                    "You're on a " + streak + "-day streak. Don’t break it today!"
            );
        }

       // manager.notify(STREAK_NOTIFICATION_ID, builder.build());
    }

}
