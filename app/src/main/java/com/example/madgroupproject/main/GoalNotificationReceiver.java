package com.example.madgroupproject.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 定时通知接收器
 * 用于定时触发目标通知（例如每天早上）
 */
public class GoalNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 直接调用GoalNotificationManager来更新通知
        GoalNotificationManager.updateGoalNotification(context);
    }
}
