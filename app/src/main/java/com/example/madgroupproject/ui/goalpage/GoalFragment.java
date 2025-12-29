package com.example.madgroupproject.ui.goalpage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

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

import java.util.ArrayList;
import java.util.List;

public class GoalFragment extends Fragment {

    private LinearLayout goalsContainer;
    private Button btnCreateGoal;
    private List<GoalEntity> goalsList = new ArrayList<>();
    private GoalRepository goalRepository;
    private Handler mainHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goalRepository = new GoalRepository(requireContext());
        mainHandler = new Handler(Looper.getMainLooper());

        // ❌ 移除默认目标初始化
        // goalRepository.initializeDefaultGoals(null);
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
        loadGoalsFromDatabase();
        setupListeners(view);

        // 监听来自CreateGoalDialogFragment的结果
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
                        // 编辑现有目标
                        updateGoalInDatabase(goalId, goalName, goalLabel);
                    } else {
                        // 创建新目标
                        createGoalInDatabase(goalName, goalLabel);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次回到这个页面时更新通知
        GoalNotificationManager.updateGoalNotification(requireContext());
    }

    private void initViews(View view) {
        goalsContainer = view.findViewById(R.id.goalsContainer);
        btnCreateGoal = view.findViewById(R.id.btnCreateGoal);
    }

    // 从数据库加载目标
    private void loadGoalsFromDatabase() {
        goalRepository.getAllGoals(new GoalRepository.OnResultListener<List<GoalEntity>>() {
            @Override
            public void onSuccess(List<GoalEntity> result) {
                mainHandler.post(() -> {
                    goalsList.clear();
                    goalsList.addAll(result);
                    displayGoals();

                    // 🔔 加载完成后更新通知
                    GoalNotificationManager.updateGoalNotification(requireContext());
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Failed to load goals", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // 创建新目标
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
                    loadGoalsFromDatabase();

                    // 🔔 创建目标后立即更新通知
                    GoalNotificationManager.updateGoalNotification(requireContext());
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

    // 更新目标
    private void updateGoalInDatabase(int goalId, String name, String label) {
        // 找到对应的目标
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
                        loadGoalsFromDatabase();

                        // 🔔 更新目标后立即更新通知
                        GoalNotificationManager.updateGoalNotification(requireContext());
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

    // 删除目标
    private void deleteGoalFromDatabase(int goalId) {
        goalRepository.deleteGoalById(goalId, new GoalRepository.OnResultListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), "Goal deleted!", Toast.LENGTH_SHORT).show();
                    loadGoalsFromDatabase();

                    // 🔔 删除目标后立即更新通知
                    GoalNotificationManager.updateGoalNotification(requireContext());
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
        goalsContainer.removeAllViews();

//        // ✅ 如果没有目标，显示空状态提示
//        if (goalsList.isEmpty()) {
//            View emptyView = LayoutInflater.from(requireContext()).inflate(
//                    R.layout.empty_goals_view, goalsContainer, false);
//            goalsContainer.addView(emptyView);
//            return;
//        }

        for (GoalEntity goal : goalsList) {
            View goalView = createGoalView(goal);
            goalsContainer.addView(goalView);
        }
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

        // 使用lambda表达式简化
        goalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateGoalStatus(goal, goalSwitch, goalBorder, isChecked);
        });

        // 点击整个卡片跳转到编辑页面
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

        // 建议的目标
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
        // 保存原始状态
        boolean originalStatus = goal.isCompleted();

        // 立即更新UI
        borderView.setVisibility(newStatus ? View.VISIBLE : View.GONE);

        // 更新数据库
        goalRepository.updateCompletedStatus(goal.getId(), newStatus, new GoalRepository.OnResultListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                mainHandler.post(() -> {
                    goal.setCompleted(newStatus);
                    Log.d("GoalFragment", "状态更新成功: " + goal.getName() + " -> " + newStatus);
                    GoalNotificationManager.updateGoalNotification(requireContext());
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    Log.e("GoalFragment", "状态更新失败", e);
                    Toast.makeText(requireContext(), "Failed to update status", Toast.LENGTH_SHORT).show();

                    // 完全回滚
                    switchView.setChecked(originalStatus);
                    goal.setCompleted(originalStatus); // 确保本地对象也回滚
                    borderView.setVisibility(originalStatus ? View.VISIBLE : View.GONE);
                });
            }
        });
    }
}