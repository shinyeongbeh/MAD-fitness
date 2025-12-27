package com.example.madgroupproject.main;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//Use Dummy before the goal database available
public class DummyNotificationReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {
        // Show dummy notification
        NotificationUtil.showNotification(context);
    }

}
