package com.example.madgroupproject.ui.goalpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.GoalEntity;
import com.example.madgroupproject.data.repository.GoalRepository;
import com.example.madgroupproject.main.GoalNotificationManager;
import com.example.madgroupproject.util.MidnightChangeListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoalFragment extends Fragment {

    private static final String TAG = "GoalFragment";

    private LinearLayout goalsContainer;
    private Button btnCreateGoal;
    private List<GoalEntity> goalsList = new ArrayList<>();
    private GoalRepository goalRepository;
    private Handler mainHandler;

    // Flag to prevent triggering switch listener during UI updates
    private boolean isUpdatingUI = false;

    // ✅ 添加广播接收器
    private BroadcastReceiver midnightReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goalRepository = new GoalRepository(requireContext());
        mainHandler = new Handler(Looper.getMainLooper());

        // ✅ 注册广播接收器
        setupMidnightBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goal, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners(view);

        // Use LiveData to observe database changes
        observeGoals();

        // Listen for results from CreateGoalDialogFragment
        getParentFragmentManager().setFragmentResultListener("goal_request", this, (requestKey, result) -> {
            if (result.containsKey("goal_deleted")) {
                int goalId = result.getInt("goal_id", -1);
                if (goalId > 0) {
                    deleteGoalFromDatabase(goalId);
                }
            } else if (result.containsKey("goal_name")) {
                String goalName = result.getString("goal_name");
                String goalLabel = result.getString("goal_label");
                int goalId = result.getInt("goal_id", -1);

                if (goalName != null && goalLabel != null) {
                    if (goalId > 0) {
                        // Edit existing goal
                        updateGoalInDatabase(goalId, goalName, goalLabel);
                    } else {
                        // Create new goal
                        createGoalInDatabase(goalName, goalLabel);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // ✅ 只更新通知,不再检查日期变化(MainActivity已处理)
        GoalNotificationManager.updateGoalNotification(requireContext());
        Log.d(TAG, "onResume - Updated notification");
    }

    /**
     * ✅ 设置广播接收器监听午夜事件
     */
    private void setupMidnightBroadcastReceiver() {
        midnightReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.madgroupproject.MIDNIGHT_PASSED".equals(intent.getAction())) {
                    Log.d(TAG, "📡 Received midnight broadcast!");

                    // LiveData会自动刷新UI(因为数据库状态已重置)
                    // MainActivity已显示Toast，这里不再重复显示
                    Log.d(TAG, "✅ Goals UI will auto-refresh via LiveData");
                }
            }
        };

        IntentFilter filter = new IntentFilter("com.example.madgroupproject.MIDNIGHT_PASSED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(midnightReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(midnightReceiver, filter);
        }

        Log.d(TAG, "✅ Midnight broadcast receiver registered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // ✅ 取消注册广播接收器
        if (midnightReceiver != null) {
            try {
                requireContext().unregisterReceiver(midnightReceiver);
                Log.d(TAG, "Midnight broadcast receiver unregistered");
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
    }

    private void initViews(View view) {
        goalsContainer = view.findViewById(R.id.goalsContainer);
        btnCreateGoal = view.findViewById(R.id.btnCreateGoal);
    }

    // Use LiveData to observe database changes for automatic UI updates
    private void observeGoals() {
        goalRepository.getAllGoalsLive().observe(getViewLifecycleOwner(), new Observer<List<GoalEntity>>() {
            @Override
            public void onChanged(List<GoalEntity> goals) {
                Log.d(TAG, "LiveData triggered, received " + goals.size() + " goals");

                // Update local list
                goalsList.clear();
                goalsList.addAll(goals);

                // Refresh UI
                displayGoals();

                // Update notification
                GoalNotificationManager.updateGoalNotification(requireContext());
            }
        });
    }

    // Create new goal
    private void createGoalInDatabase(String name, String label) {
        GoalEntity newGoal = new GoalEntity();
        newGoal.setName(name);
        newGoal.setLabel(label);
        newGoal.setIconRes(GoalRepository.getIconForLabel(label));
        newGoal.setDisplayOrder(goalsList.size());

        goalRepository.insertGoal(newGoal, new GoalRepository.OnResultListener<Long>() {
            @Override
            public void onSuccess(Long result) {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Goal created!", Toast.LENGTH_SHORT).show();
                    // LiveData will automatically update UI, no need to manually call loadGoalsFromDatabase()
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Failed to create goal", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Update goal
    private void updateGoalInDatabase(int goalId, String name, String label) {
        // Find the corresponding goal
        GoalEntity goalToUpdate = null;
        for (GoalEntity goal : goalsList) {
            if (goal.getId() == goalId) {
                goalToUpdate = goal;
                break;
            }
        }

        if (goalToUpdate != null) {
            goalToUpdate.setName(name);
            goalToUpdate.setLabel(label);
            goalToUpdate.setIconRes(GoalRepository.getIconForLabel(label));

            goalRepository.updateGoal(goalToUpdate, new GoalRepository.OnResultListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Goal updated!", Toast.LENGTH_SHORT).show();
                        // LiveData will automatically update UI
                    });
                }

                @Override
                public void onError(Exception e) {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Failed to update goal", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    // Delete goal
    private void deleteGoalFromDatabase(int goalId) {
        goalRepository.deleteGoalById(goalId, new GoalRepository.OnResultListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Goal deleted!", Toast.LENGTH_SHORT).show();
                    // LiveData will automatically update UI
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Failed to delete goal", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayGoals() {
        // Set flag to indicate UI is being updated
        isUpdatingUI = true;

        goalsContainer.removeAllViews();

        for (GoalEntity goal : goalsList) {
            View goalView = createGoalView(goal);
            goalsContainer.addView(goalView);
        }

        // UI update complete
        isUpdatingUI = false;
    }

    private View createGoalView(GoalEntity goal) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_goal, goalsContainer, false);

        ImageView goalIcon = view.findViewById(R.id.goalIcon);
        TextView goalName = view.findViewById(R.id.goalName);
        TextView goalLabel = view.findViewById(R.id.goalLabel);
        SwitchCompat goalSwitch = view.findViewById(R.id.goalSwitch);
        View goalBorder = view.findViewById(R.id.goalBorder);

        goalIcon.setImageResource(goal.getIconRes());
        goalName.setText(goal.getName());
        goalLabel.setText("Label: " + goal.getLabel());
        goalSwitch.setChecked(goal.isCompleted());

        if (goal.isCompleted()) {
            goalBorder.setVisibility(View.VISIBLE);
        } else {
            goalBorder.setVisibility(View.GONE);
        }

        // Improved: Check if change is caused by UI update in listener
        goalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Ignore if this change is caused by UI update
            if (isUpdatingUI) {
                return;
            }

            // User manual operation, update status
            updateGoalStatus(goal, goalSwitch, goalBorder, isChecked);
        });

        // Click entire card to jump to edit page
        view.setOnClickListener(v -> {
            CreateGoalDialogFragment.newEditInstance(
                    goal.getId(),
                    goal.getName(),
                    goal.getLabel()
            ).show(getParentFragmentManager(), "edit_goal");
        });

        return view;
    }

    private void setupListeners(View root) {
        btnCreateGoal.setOnClickListener(v -> {
            new CreateGoalDialogFragment().show(getParentFragmentManager(), "create_goal");
        });

        // Suggested goals
        root.findViewById(R.id.suggestedExercise).setOnClickListener(v -> {
            CreateGoalDialogFragment.newSuggestedInstance("Exercise 30min", "Exercise")
                    .show(getParentFragmentManager(), "suggested");
        });

        root.findViewById(R.id.suggestedWakeup).setOnClickListener(v -> {
            CreateGoalDialogFragment.newSuggestedInstance("Early Wake-up", "Habit")
                    .show(getParentFragmentManager(), "suggested");
        });

        root.findViewById(R.id.suggestedSleep).setOnClickListener(v -> {
            CreateGoalDialogFragment.newSuggestedInstance("Sleep 8hours", "Relax")
                    .show(getParentFragmentManager(), "suggested");
        });
    }

    private void updateGoalStatus(GoalEntity goal, SwitchCompat switchView, View borderView, boolean newStatus) {
        // Save original status
        boolean originalStatus = goal.isCompleted();

        // Immediately update UI
        borderView.setVisibility(newStatus ? View.VISIBLE : View.GONE);

        // Update database
        goalRepository.updateCompletedStatus(goal.getId(), newStatus, new GoalRepository.OnResultListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(() -> {
                    goal.setCompleted(newStatus);
                    Log.d(TAG, "Status update successful: " + goal.getName() + " -> " + newStatus);
                    GoalNotificationManager.updateGoalNotification(requireContext());
                    // LiveData will automatically trigger UI update to ensure synchronization
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    Log.e(TAG, "Status update failed", e);
                    Toast.makeText(requireContext(), "Failed to update status", Toast.LENGTH_SHORT).show();

                    // Complete rollback
                    switchView.setChecked(originalStatus);
                    goal.setCompleted(originalStatus);
                    borderView.setVisibility(originalStatus ? View.VISIBLE : View.GONE);
                });
            }
        });
    }
}