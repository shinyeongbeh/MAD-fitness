package com.example.madgroupproject.main;

import android.content.Context;
import android.util.Log;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.GoalDao;
import com.example.madgroupproject.data.local.entity.GoalEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 管理目标通知的工具类
 * 负责根据目标状态实时更新通知
 */
public class GoalNotificationManager {

    private static final String TAG = "GoalNotificationManager";
    public static final int GOAL_NOTIFICATION_ID = 1001;

    /**
     * 更新目标通知（显示未完成的目标）
     * 这个方法会在后台线程中读取数据库并更新通知
     *
     * 调用时机：
     * 1. 创建新目标后
     * 2. 编辑目标后
     * 3. 删除目标后
     * 4. 切换目标完成状态后
     * 5. 进入Goal页面时
     * 6. 定时触发（通过BroadcastReceiver）
     */
    public static void updateGoalNotification(Context context) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 从数据库读取所有目标
                AppDatabase db = AppDatabase.getDatabase(context);
                GoalDao goalDao = db.goalDao();
                List<GoalEntity> goals = goalDao.getAllGoals();

                // 生成并显示通知
                showGoalNotification(context, goals);

            } catch (Exception e) {
                Log.e(TAG, "Error updating goal notification", e);
            } finally {
                executor.shutdown();
            }
        });
    }

    /**
     * 根据目标列表生成并显示通知
     */
    private static void showGoalNotification(Context context, List<GoalEntity> goals) {
        if (goals == null || goals.isEmpty()) {
            // 情况1：没有设置任何目标
            NotificationUtil.showNotification(
                    context,
                    GOAL_NOTIFICATION_ID,
                    "Daily Goal",
                    "You have no goals set today"
            );
            return;
        }

        // 统计未完成的目标
        StringBuilder message = new StringBuilder();
        int incompleteCount = 0;

        for (GoalEntity goal : goals) {
            if (!goal.isCompleted()) {
                message.append("• ").append(goal.getName()).append("\n");
                incompleteCount++;
            }
        }

        if (incompleteCount == 0) {
            // 情况2：所有目标都完成了
            NotificationUtil.showNotification(
                    context,
                    GOAL_NOTIFICATION_ID,
                    "Great job 🎉",
                    "All goals completed today!"
            );
        } else {
            // 情况3：显示未完成的目标列表
            String title = "Today's Goals";
            if (incompleteCount > 0) {
                title += " (" + incompleteCount + " remaining)";
            }

            NotificationUtil.showNotification(
                    context,
                    GOAL_NOTIFICATION_ID,
                    title,
                    message.toString().trim()
            );
        }
    }

    /**
     * 取消目标通知
     */
    public static void cancelGoalNotification(Context context) {
        NotificationUtil.cancelNotification(context, GOAL_NOTIFICATION_ID);
    }
}
