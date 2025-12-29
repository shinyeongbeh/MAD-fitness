/*package com.example.madgroupproject.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class GoalNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if user enabled notifications
        if (!NotificationUtil.isNotificationEnabled(context)) return;

        // Get user's goal from SharedPreferences
        SharedPreferences prefs =
                context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String goal = prefs.getString("daily_goal", "No goal set");

        // Show notification
        NotificationUtil.showNotification(
                context,
                "Today's Goal",
                goal
        );
    }
}
*/