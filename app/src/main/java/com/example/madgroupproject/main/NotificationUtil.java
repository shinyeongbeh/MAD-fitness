package com.example.madgroupproject.main;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.madgroupproject.R;

public class NotificationUtil {

    public static final String CHANNEL_ID = "default_channel";

    // Create notification channel
    public static void createNotificationChannel(Context context) {
        //for daily goals
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Goals",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context,int notificationId,String title, String message) {

        SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        boolean isEnabled = true;

        // Check the specific key based on the ID we assigned earlier
        if (notificationId == 1001) { // Goal ID
            isEnabled = prefs.getBoolean("goal_notifications_enabled", true);
        } else if (notificationId == 1002) { // Streak ID
            isEnabled = prefs.getBoolean("streak_notifications_enabled", true);
        } else if (notificationId == 2001) { // Step Milestone ID
            isEnabled = prefs.getBoolean("step_notifications_enabled", true);
        }

        if (!isEnabled) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat.from(context).notify(notificationId, notification);
    }



}
