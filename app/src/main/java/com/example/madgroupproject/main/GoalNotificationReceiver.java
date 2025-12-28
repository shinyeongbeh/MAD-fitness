package com.example.madgroupproject.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.madgroupproject.data.GoalPreferenceManager;
import com.example.madgroupproject.ui.goalpage.GoalFragment;


import java.util.List;

public class GoalNotificationReceiver extends BroadcastReceiver {
    public static final int GOAL_NOTIFICATION_ID = 101;

    @Override
    public void onReceive(Context context, Intent intent) {
        GoalPreferenceManager manager =
                new GoalPreferenceManager(context);

        List<GoalFragment.Goal> goals = manager.loadGoals();

        if (goals == null || goals.isEmpty()) {
            NotificationUtil.showNotification(
                    context,
                    GOAL_NOTIFICATION_ID,
                    "Daily Goal",
                    "You have no goals set today"
            );
            return;
        }

        //Multiple goals set
        StringBuilder message = new StringBuilder();
        int count = 0;

        for (GoalFragment.Goal g : goals) {
            if (!g.isCompleted()) {
                message.append("• ").append(g.getName()).append("\n");
                count++;
            }
        }

        if (count == 0) {
            NotificationUtil.showNotification(
                    context,
                    GOAL_NOTIFICATION_ID,
                    "Great job 🎉",
                    "All goals completed today!"
            );
        } else {
            NotificationUtil.showNotification(
                    context,
                    GOAL_NOTIFICATION_ID,
                    "Today's Goals",
                    message.toString().trim()
            );
        }
    }


}

