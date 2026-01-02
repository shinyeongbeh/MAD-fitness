package com.example.madgroupproject.main;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.madgroupproject.data.StreakPreferenceManager;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.StreakHistoryDao;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class StreakNotificationReceiver extends BroadcastReceiver {


    public static final int STREAK_NOTIFICATION_ID = 1002;


    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //int streak = StreakPreferenceManager.getStreak(context);

        //get the current Streak
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            StreakHistoryDao dao = db.streakHistoryDao();

            List<StreakHistoryEntity> allHistory = dao.getAll(); // get all streak records
            int currentStreak = getCurrentStreak(allHistory);

            if(currentStreak==0){
                NotificationUtil.showNotification(
                        context,
                        STREAK_NOTIFICATION_ID,
                        "Start Your Journey!",
                        "Start running today to start your journey!"
                );
            }

            if (currentStreak > 0) {
                NotificationUtil.showNotification(

                        context,
                        STREAK_NOTIFICATION_ID,
                        "Keep Your Streak!",
                        "You're on a " + currentStreak + "-day streak. Don’t break it today!"
                );
            }
        });



       // manager.notify(STREAK_NOTIFICATION_ID, builder.build());
    }

    private int getCurrentStreak(List<StreakHistoryEntity> history) {
        int streak = 0;

        // Start from the last entry (most recent day)
        for (int i = history.size() - 1; i >= 0; i--) {
            StreakHistoryEntity day = history.get(i);
            if (day.achieved) {
                streak++;
            } else {
                break; // streak broken
            }
        }

        return streak;
    }



}

