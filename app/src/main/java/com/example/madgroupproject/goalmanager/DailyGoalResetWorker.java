package com.example.madgroupproject.goalmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.GoalDao;
import com.example.madgroupproject.main.GoalNotificationManager;

/**
 * 每日目标重置Worker
 * 在每天0点自动执行，重置所有目标的完成状态
 */
public class DailyGoalResetWorker extends Worker {

    private static final String TAG = "DailyGoalResetWorker";

    public DailyGoalResetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting daily goal reset...");

            // 获取数据库DAO
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            GoalDao goalDao = db.goalDao();

            // 重置所有目标的完成状态
            goalDao.resetAllCompletionStatus();

            Log.d(TAG, "All goals reset successfully");

            // 重置后立即更新通知，显示新一天的目标
            GoalNotificationManager.updateGoalNotification(getApplicationContext());

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error resetting goals", e);
            return Result.retry();
        }
    }
}