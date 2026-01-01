package com.example.madgroupproject.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听系统日期变化的工具类
 * 当日期改变时（如凌晨0点），会通知所有注册的监听器
 */
public class MidnightChangeListener {
    private static final String TAG = "MidnightChangeListener";

    public interface OnMidnightChangeListener {
        void onMidnightPassed();
    }

    private Context context;
    private List<OnMidnightChangeListener> listeners = new ArrayList<>();
    private BroadcastReceiver dateChangeReceiver;

    public MidnightChangeListener(Context context) {
        this.context = context.getApplicationContext();
        setupDateChangeReceiver();
    }

    private void setupDateChangeReceiver() {
        dateChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_DATE_CHANGED.equals(action) ||
                        Intent.ACTION_TIME_CHANGED.equals(action)) {

                    Log.d(TAG, "Date/Time changed detected! Action: " + action);
                    notifyListeners();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        context.registerReceiver(dateChangeReceiver, filter);
        Log.d(TAG, "Date change receiver registered");
    }

    public void addListener(OnMidnightChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Log.d(TAG, "Listener added. Total listeners: " + listeners.size());
        }
    }

    public void removeListener(OnMidnightChangeListener listener) {
        listeners.remove(listener);
        Log.d(TAG, "Listener removed. Total listeners: " + listeners.size());
    }

    private void notifyListeners() {
        Log.d(TAG, "Notifying " + listeners.size() + " listeners");
        for (OnMidnightChangeListener listener : listeners) {
            try {
                listener.onMidnightPassed();
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    public void destroy() {
        try {
            context.unregisterReceiver(dateChangeReceiver);
            Log.d(TAG, "Date change receiver unregistered");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver", e);
        }
        listeners.clear();
    }
}
