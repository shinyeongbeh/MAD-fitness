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
        //NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //this method fail so we calculate manually
        //int streak = StreakPreferenceManager.getStreak(context);


        //using history to calculate

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

    //calculate manually
    private int getCurrentStreak(List<StreakHistoryEntity> history) {
        if (history == null || history.isEmpty()) return 0;

        // get from the lastest
        history.sort((a, b) -> b.date.compareTo(a.date));

        int streak = 0;
        LocalDate expectedDate = LocalDate.now();

        for (StreakHistoryEntity day : history) {
            LocalDate recordDate = LocalDate.parse(day.date);

            // let streak to start from yesterday if today not achieved yet
            if (streak == 0 && recordDate.equals(expectedDate.minusDays(1))) {
                expectedDate = expectedDate.minusDays(1);
            }

            if (recordDate.equals(expectedDate) && day.achieved) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }



}

