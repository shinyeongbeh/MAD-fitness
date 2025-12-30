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

    // Insert goal
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

    // Update goal
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

    // Delete goal
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

    // Delete goal by ID
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

    // Get all goals
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

    // Get all goals (LiveData)
    public LiveData<List<GoalEntity>> getAllGoalsLive() {
        return goalDao.getAllGoalsLive();
    }

    // Get incomplete goals
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

    // Update completion status (fixed version)
    public void updateCompletedStatus(int goalId, boolean completed, OnResultListener<Void> listener) {
        executorService.execute(() -> {
            try {
                // Try using direct SQL update
                try {
                    goalDao.updateCompletedStatus(goalId, completed);
                    Log.d(TAG, "Direct SQL update succeeded for goalId: " + goalId);
                } catch (Exception sqlEx) {
                    // If direct update fails, fall back to query-then-update approach
                    Log.d(TAG, "Direct update failed, trying query-then-update approach");
                    GoalEntity goal = goalDao.getGoalById(goalId);
                    if (goal != null) {
                        goal.setCompleted(completed);
                        goalDao.update(goal);
                        Log.d(TAG, "Query-then-update succeeded for goalId: " + goalId);
                    } else {
                        throw new Exception("Goal not found with id: " + goalId);
                    }
                }

                if (listener != null) {
                    listener.onSuccess(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating completed status for goalId: " + goalId, e);
                // Print full stack trace
                e.printStackTrace();
                if (listener != null) {
                    listener.onError(e);
                }
            }
        });
    }

    // Get icon resource ID based on label
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

    // Callback interface
    public interface OnResultListener<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
