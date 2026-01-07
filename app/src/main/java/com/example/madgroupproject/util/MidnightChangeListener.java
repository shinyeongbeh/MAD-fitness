package com.example.madgroupproject.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 监听系统日期变化的工具类
 * 支持系统广播、定时检查和自定义测试时间
 */
public class MidnightChangeListener {
    private static final String TAG = "MidnightChangeListener";

    // 自定义测试广播
    public static final String TEST_DATE_CHANGED = "com.example.madgroupproject.TEST_DATE_CHANGED";

    // 定时检查间隔(毫秒)
    private static final long CHECK_INTERVAL = 10000; // 10秒检查一次

    // SharedPreferences keys
    private static final String PREFS_NAME = "MidnightTestPrefs";
    private static final String KEY_TEST_TIME_ENABLED = "test_time_enabled";
    private static final String KEY_TEST_HOUR = "test_hour";
    private static final String KEY_TEST_MINUTE = "test_minute";
    private static final String KEY_LAST_TRIGGER_DATE = "last_trigger_date";

    public interface OnMidnightChangeListener {
        void onMidnightPassed();
    }

    private Context context;
    private List<OnMidnightChangeListener> listeners = new ArrayList<>();
    private BroadcastReceiver dateChangeReceiver;

    // 定时器相关
    private Handler checkHandler;
    private Runnable checkRunnable;
    private String lastKnownDate;
    private int lastCheckHour = -1; // 记录上次检查的小时

    // 🆕 防重复触发机制
    private String lastTriggeredDate = ""; // 记录上次触发的日期
    private boolean hasTriggeredToday = false; // 今天是否已经触发过

    // SharedPreferences
    private android.content.SharedPreferences prefs;

    public MidnightChangeListener(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.lastKnownDate = LocalDate.now().toString();
        this.lastCheckHour = LocalTime.now().getHour();
        this.lastTriggeredDate = lastKnownDate; // 🆕 初始化为当前日期
        this.hasTriggeredToday = true; // 🆕 假设今天已经触发过(避免App启动就触发)

        Log.d(TAG, "🌙 MidnightChangeListener created at " + LocalTime.now());
        Log.d(TAG, "   Initial hour: " + lastCheckHour);
        Log.d(TAG, "   Initial date: " + lastKnownDate);
        Log.d(TAG, "   Last triggered date: " + lastTriggeredDate);

        setupDateChangeReceiver();
        startPeriodicCheck();
    }

    /**
     * 设置测试时间(用于快速测试)
     * @param hour 小时 (0-23)
     * @param minute 分钟 (0-59)
     */
    public void setTestTime(int hour, int minute) {
        prefs.edit()
                .putBoolean(KEY_TEST_TIME_ENABLED, true)
                .putInt(KEY_TEST_HOUR, hour)
                .putInt(KEY_TEST_MINUTE, minute)
                .apply();

        Log.d(TAG, String.format("🧪 Test time set to %02d:%02d", hour, minute));
    }

    /**
     * 禁用测试时间,恢复正常的午夜检测
     */
    public void disableTestTime() {
        prefs.edit()
                .putBoolean(KEY_TEST_TIME_ENABLED, false)
                .apply();

        Log.d(TAG, "🧪 Test time disabled");
    }

    /**
     * 获取当前的测试时间设置
     */
    public String getTestTimeInfo() {
        if (prefs.getBoolean(KEY_TEST_TIME_ENABLED, false)) {
            int hour = prefs.getInt(KEY_TEST_HOUR, 0);
            int minute = prefs.getInt(KEY_TEST_MINUTE, 0);
            return String.format("Test time: %02d:%02d", hour, minute);
        } else {
            return "Test time: Disabled";
        }
    }

    private void setupDateChangeReceiver() {
        dateChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (Intent.ACTION_DATE_CHANGED.equals(action) ||
                        Intent.ACTION_TIME_CHANGED.equals(action) ||
                        TEST_DATE_CHANGED.equals(action)) {

                    Log.d(TAG, "📡 Date/Time changed detected via broadcast! Action: " + action);
                    handleDateChange();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(TEST_DATE_CHANGED);

        // 修复:Android 13+ 需要明确指定 RECEIVER_NOT_EXPORTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            context.registerReceiver(dateChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            Log.d(TAG, "Date change receiver registered (Android 13+ with RECEIVER_NOT_EXPORTED)");
        } else {
            // Android 12 及以下
            context.registerReceiver(dateChangeReceiver, filter);
            Log.d(TAG, "Date change receiver registered (Android 12 and below)");
        }
    }

    private void startPeriodicCheck() {
        checkHandler = new Handler(Looper.getMainLooper());
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                checkForDateOrTimeChange();
                checkHandler.postDelayed(this, CHECK_INTERVAL);
            }
        };

        Log.d(TAG, "⏰ Periodic check started (every " + (CHECK_INTERVAL/1000) + " seconds)");

        // 重要:立即执行一次检查(不要等10秒)
        checkForDateOrTimeChange();

        // 然后开始定时循环
        checkHandler.postDelayed(checkRunnable, CHECK_INTERVAL);
    }

    private void checkForDateOrTimeChange() {
        boolean shouldTrigger = false;
        String triggerReason = "";

        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();
        String currentDate = LocalDate.now().toString();

        // 详细日志
        Log.d(TAG, String.format("⏰ Checking... Time: %02d:%02d, Date: %s, Last hour: %d, HasTriggeredToday: %b",
                currentHour, currentMinute, currentDate, lastCheckHour, hasTriggeredToday));

        // 🆕 关键修复:检查日期是否变化,如果变了就重置标志
        if (!currentDate.equals(lastTriggeredDate)) {
            Log.d(TAG, "📅 New day detected! Resetting trigger flag.");
            Log.d(TAG, "   Last triggered: " + lastTriggeredDate);
            Log.d(TAG, "   Current date: " + currentDate);
            hasTriggeredToday = false;
            lastTriggeredDate = currentDate;
        }

        // 🆕 如果今天已经触发过,直接跳过所有检查
        if (hasTriggeredToday) {
            Log.d(TAG, "   Already triggered today, skipping...");
            lastCheckHour = currentHour; // 更新小时以便下次检查
            return;
        }

        // 检测是否跨越午夜(从23点到0点)
        if (lastCheckHour == 23 && currentHour == 0) {
            shouldTrigger = true;
            triggerReason = "Time crossed midnight (23:xx → 00:xx)";
            Log.d(TAG, "🌙 Midnight crossing detected! (Hour changed: 23 → 0)");
        }

        // 检查1:日期是否变化
        if (!lastKnownDate.equals(currentDate)) {
            shouldTrigger = true;
            triggerReason = "Date changed from " + lastKnownDate + " to " + currentDate;
            Log.d(TAG, "📅 Date change detected: " + lastKnownDate + " → " + currentDate);
            lastKnownDate = currentDate;
        }

        // 检查2:是否到达测试时间
        if (prefs.getBoolean(KEY_TEST_TIME_ENABLED, false)) {
            int testHour = prefs.getInt(KEY_TEST_HOUR, 0);
            int testMinute = prefs.getInt(KEY_TEST_MINUTE, 0);

            if (currentHour == testHour && currentMinute == testMinute) {
                String lastTriggerDate = prefs.getString(KEY_LAST_TRIGGER_DATE, "");
                String today = LocalDate.now().toString();

                if (!today.equals(lastTriggerDate)) {
                    shouldTrigger = true;
                    triggerReason = String.format("Test time reached: %02d:%02d", testHour, testMinute);
                    prefs.edit().putString(KEY_LAST_TRIGGER_DATE, today).apply();
                    Log.d(TAG, "🧪 Test time trigger: " + testHour + ":" + testMinute);
                }
            }
        }

        // 更新上次检查的小时
        lastCheckHour = currentHour;

        if (shouldTrigger) {
            Log.d(TAG, "🔔🔔🔔 TRIGGER DETECTED! 🔔🔔🔔");
            Log.d(TAG, "   Reason: " + triggerReason);
            Log.d(TAG, "   Current time: " + String.format("%02d:%02d", currentHour, currentMinute));

            // 🆕 标记今天已经触发过
            hasTriggeredToday = true;
            lastTriggeredDate = currentDate;
            Log.d(TAG, "   ✅ Marked as triggered for date: " + currentDate);

            handleDateChange();
        } else {
            Log.d(TAG, "   No trigger. Continuing...");
        }
    }

    private void handleDateChange() {
        lastKnownDate = LocalDate.now().toString();
        notifyListeners();
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
        Log.d(TAG, "🔔 Notifying " + listeners.size() + " listeners");
        for (OnMidnightChangeListener listener : listeners) {
            try {
                listener.onMidnightPassed();
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    public void destroy() {
        if (checkHandler != null && checkRunnable != null) {
            checkHandler.removeCallbacks(checkRunnable);
            Log.d(TAG, "⏰ Periodic check stopped");
        }

        try {
            context.unregisterReceiver(dateChangeReceiver);
            Log.d(TAG, "Date change receiver unregistered");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver", e);
        }

        listeners.clear();
    }
}
