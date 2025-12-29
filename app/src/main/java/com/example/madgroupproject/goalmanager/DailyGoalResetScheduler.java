package com.example.madgroupproject.goalmanager;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * 每日目标重置调度器
 * 负责安排在每天0点重置目标
 */
public class DailyGoalResetScheduler {

    private static final String TAG = "DailyGoalResetScheduler";
    private static final String WORK_NAME = "daily_goal_reset_work";

    /**
     * 开始调度每日重置任务
     * 在每天0点执行
     */
    public static void scheduleDailyReset(Context context) {
        // 计算距离下一个0点的延迟时间
        long initialDelay = calculateInitialDelay();

        Log.d(TAG, "Scheduling daily goal reset with initial delay: " + initialDelay + " ms");

        // 创建约束条件（可选）
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(false) // 即使电量低也要执行
                .build();

        // 创建周期性工作请求（每24小时执行一次）
        PeriodicWorkRequest resetWork = new PeriodicWorkRequest.Builder(
                DailyGoalResetWorker.class,
                24, // 重复间隔
                TimeUnit.HOURS
        )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        // 使用 REPLACE 策略，确保只有一个重置任务在运行
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                resetWork
        );

        Log.d(TAG, "Daily goal reset scheduled successfully");
    }

    /**
     * 取消每日重置任务
     */
    public static void cancelDailyReset(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
        Log.d(TAG, "Daily goal reset cancelled");
    }

    /**
     * 计算距离下一个0点（午夜）的毫秒数
     */
    private static long calculateInitialDelay() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();

        // 设置为今天的0点
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        // 加一天，得到明天的0点
        midnight.add(Calendar.DAY_OF_MONTH, 1);

        // 计算时间差
        long delay = midnight.getTimeInMillis() - now.getTimeInMillis();

        return delay;
    }

    /**
     * 获取下一次重置的时间（用于调试）
     */
    public static String getNextResetTime() {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_MONTH, 1);

        return midnight.getTime().toString();
    }
}