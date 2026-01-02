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
 * 在每天0点自动执行，删除所有目标（清空前一天的目标）
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
            Log.d(TAG, "Starting daily goal reset (delete all goals)...");

            // 获取数据库DAO
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            GoalDao goalDao = db.goalDao();

            // 🔴 修改：删除所有目标（而不是重置状态）
            goalDao.deleteAll();

            Log.d(TAG, "All goals deleted successfully for new day");

            // 清空后更新通知
            GoalNotificationManager.updateGoalNotification(getApplicationContext());

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting goals", e);
            return Result.retry();
        }
    }
}