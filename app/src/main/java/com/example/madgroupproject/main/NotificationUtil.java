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

    // Check whether the user turn on the notification
    public static boolean isNotificationEnabled(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        return prefs.getBoolean("notifications_enabled", true);
    }


    // Show a dummy notification
    /*public static void showNotification(Context context) {

        if (!isNotificationEnabled(context)) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // use a built-in dummy icon
                .setContentTitle("Dummy Title")
                .setContentText("This is a dummy notification message.")
                .setAutoCancel(true);

        // Check for POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, just return for now
            return;
        }

        NotificationManagerCompat.from(context).notify(1, builder.build());
    }*/

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
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message)) // 支持多行文本
                        .setAutoCancel(true)
                        .setOngoing(false) // 可以滑动删除
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build();

        NotificationManagerCompat.from(context).notify(notificationId, notification);
    }

    /**
     * 取消指定的通知
     */
    public static void cancelNotification(Context context, int notificationId) {
        NotificationManagerCompat.from(context).cancel(notificationId);
    }

    /**
     * 取消所有通知
     */
    public static void cancelAllNotifications(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }
}
