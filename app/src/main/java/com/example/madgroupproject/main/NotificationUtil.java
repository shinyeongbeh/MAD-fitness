package com.example.madgroupproject.main;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.madgroupproject.R;

public class NotificationUtil {

    public static final String CHANNEL_ID = "default_channel";

    public static void createNotificationChannel(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Notification",
                    NotificationManager.IMPORTANCE_DEFAULT

        );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);


        }
    }

    //Check if user enabled notification
    public static boolean isNotificationEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean("notification_enabled", true);
    }

    //show notification ONLY if enabled
    public static void showNotification(Context context,
                                        String title,
                                        String message) {

        if (!isNotificationEnabled(context)) return;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        //.setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context)
                .notify(1, builder.build());

    }
}
