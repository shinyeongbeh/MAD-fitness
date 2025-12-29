package com.example.madgroupproject.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.GoalDao;
import com.example.madgroupproject.data.local.entity.GoalEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalRepository {

    private static final String TAG = "GoalRepository";
    private final GoalDao goalDao;
    private final ExecutorService executorService;

    public GoalRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.goalDao = db.goalDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // 插入目标
    public void insertGoal(GoalEntity goal, OnResultListener<Long> listener) {
        executorService.execute(() -> {
            try {
                long id = goalDao.insert(goal);
                if (listener != null) {
                    listener.onSuccess(id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting goal", e);
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    // 更新目标
    public void updateGoal(GoalEntity goal, OnResultListener<Void> listener) {
        executorService.execute(() -> {
            try {
                goalDao.update(goal);
                if (listener != null) {
                    listener.onSuccess(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating goal", e);
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    // 删除目标
    public void deleteGoal(GoalEntity goal, OnResultListener<Void> listener) {
        executorService.execute(() -> {
            try {
                goalDao.delete(goal);
                if (listener != null) {
                    listener.onSuccess(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting goal", e);
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    // 根据ID删除目标
    public void deleteGoalById(int goalId, OnResultListener<Void> listener) {
        executorService.execute(() -> {
            try {
                goalDao.deleteById(goalId);
                if (listener != null) {
                    listener.onSuccess(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting goal by id", e);
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    // 获取所有目标
    public void getAllGoals(OnResultListener<List<GoalEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<GoalEntity> goals = goalDao.getAllGoals();
                if (listener != null) {
                    listener.onSuccess(goals);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting all goals", e);
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    // 获取所有目标（LiveData）
    public LiveData<List<GoalEntity>> getAllGoalsLive() {
        return goalDao.getAllGoalsLive();
    }

    // 获取未完成的目标
    public void getIncompleteGoals(OnResultListener<List<GoalEntity>> listener) {
        executorService.execute(() -> {
            try {
                List<GoalEntity> goals = goalDao.getIncompleteGoals();
                if (listener != null) {
                    listener.onSuccess(goals);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting incomplete goals", e);
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    // 更新完成状态
// 更新完成状态（修复版）
    public void updateCompletedStatus(int goalId, boolean completed, OnResultListener<Void> listener) {
        executorService.execute(() -> {
            try {
                // ✅ 尝试使用直接的SQL更新
                try {
                    goalDao.updateCompletedStatus(goalId, completed);
                    Log.d(TAG, "✅ Direct SQL update succeeded for goalId: " + goalId);
                } catch (Exception sqlEx) {
                    // ✅ 如果直接更新失败，回退到先查询再更新
                    Log.d(TAG, "Direct update failed, trying query-then-update approach");
                    GoalEntity goal = goalDao.getGoalById(goalId);
                    if (goal != null) {
                        goal.setCompleted(completed);
                        goalDao.update(goal);
                        Log.d(TAG, "✅ Query-then-update succeeded for goalId: " + goalId);
                    } else {
                        throw new Exception("Goal not found with id: " + goalId);
                    }
                }

                if (listener != null) {
                    listener.onSuccess(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error updating completed status for goalId: " + goalId, e);
                // 打印完整堆栈跟踪
                e.printStackTrace();
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    // ❌ 移除默认目标初始化方法
    // 不再需要 initializeDefaultGoals()

    // 根据label获取对应的图标资源ID
    public static int getIconForLabel(String label) {
        switch (label) {
            case "Exercise": return R.drawable.ic_exercise;
            case "Habit": return R.drawable.ic_water;
            case "Relax": return R.drawable.ic_podcast;
            case "Work": return R.drawable.ic_work;
            case "Study": return R.drawable.ic_study;
            case "Health": return R.drawable.ic_health;
            default: return R.drawable.ic_exercise;
        }
    }

    // 回调接口
    public interface OnResultListener<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
