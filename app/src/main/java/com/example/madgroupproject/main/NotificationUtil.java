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

    // Create notification channel (Android 8+)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // Check if user enabled notification
    public static boolean isNotificationEnabled(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        return prefs.getBoolean("notifications_enabled", true);
    }

    // Show notification ONLY if enabled
   /* public static void showNotification(Context context,
                                        String title,
                                        String message) {

        if (!isNotificationEnabled(context)) return;

        // Android 13+ permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification) // REQUIRED
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat.from(context).notify(1, notification);
    }*/

    // Show a dummy notification
    public static void showNotification(Context context) {

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
    }
}
