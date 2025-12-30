package com.example.madgroupproject.main;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.StreakPreferenceManager;
import com.example.madgroupproject.fitnessmanager.RecordingAPIManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StepTrackingService extends Service {

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private static final String CHANNEL_ID = "step_tracking_channel";
    private static final int NOTIFICATION_ID = 2001;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        //Pinned notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Tracking Active")
                .setContentText("Tracking your steps in background")
                .setSmallIcon(R.drawable.ic_exercise) // MUST exist
                .setOngoing(true)                  // 🔒 pin
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        // REQUIRED for foreground service
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startStepLoop();
        return START_STICKY; // keep pinned
    }

    private void startStepLoop() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                executor.execute(() -> {
                    int steps = readStepsSafely(); // ✅ background thread

                    mainHandler.post(() -> {
                        updateNotification(steps); // ✅ UI thread
                    });
                });

                mainHandler.postDelayed(this, 5000);
            }
        }, 1000);
    }


    private int readStepsSafely() {
        RecordingAPIManager recordingAPIManager = new RecordingAPIManager(this);

        int steps = recordingAPIManager.readDailyTotals().steps;
        return Math.max(0, steps);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Tracking",
                    NotificationManager.IMPORTANCE_LOW // important
            );
            channel.setDescription("Pinned step tracking notification");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void updateNotification(int steps) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Daily Steps")
                    .setContentText("Steps: " + steps)
                    .setSmallIcon(R.drawable.ic_exercise)
                    .setOngoing(true) // STILL pinned
                    .setOnlyAlertOnce(true)
                    .build();

            NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID, notification);

            //Check daily goal if reach then notify
             StreakPreferenceManager.checkAndNotifyDailyGoal(this, steps);
        }

}
