package com.example.madgroupproject.ui.settingpage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.example.madgroupproject.R;
//import com.example.madgroupproject.main.GoalNotificationReceiver;


public class NotificationSettingsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.setting_fragment_notification, container, false);

        Switch switchNotificationGoal = view.findViewById(R.id.switchGoal);
        Switch switchNotificationStreak = view.findViewById(R.id.switchStreak);
        Switch switchNotificationStep = view.findViewById(R.id.switchStep);



        SharedPreferences prefs =
                requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // Load saved value
        boolean enabled = prefs.getBoolean("notifications_enabled", true);
        switchNotificationGoal.setChecked(enabled);

        // 1. Load saved values for each individual setting
        switchNotificationGoal.setChecked(prefs.getBoolean("goal_notifications_enabled", true));
        switchNotificationStreak.setChecked(prefs.getBoolean("streak_notifications_enabled", true));
        switchNotificationStep.setChecked(prefs.getBoolean("step_notifications_enabled", true));

        // 2. Set individual listeners for each switch
        switchNotificationGoal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("goal_notifications_enabled", isChecked).apply();
            // Optional: call scheduleDailyGoalNotification(requireContext()) here if needed
        });

        switchNotificationStreak.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("streak_notifications_enabled", isChecked).apply();
            // Optional: call scheduleStreakReminder(requireContext()) here if needed
        });

        switchNotificationStep.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("step_notifications_enabled", isChecked).apply();
        });

        return view;
    }





            //if (isChecked) {
            //    scheduleDailyGoalNotification(requireContext());
            //} else {
            //    cancelDailyGoalNotification(requireContext());
            //}


    /*
    // Schedule 8AM notification
    private void scheduleDailyGoalNotification(Context context) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, GoalNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    // Cancel notification
    private void cancelDailyGoalNotification(Context context) {
        Intent intent = new Intent(context, GoalNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void scheduleStreakReminder(Context context) {

        Intent intent = new Intent(context, StreakNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21); // 9pm
        calendar.set(Calendar.MINUTE, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }


    */



}