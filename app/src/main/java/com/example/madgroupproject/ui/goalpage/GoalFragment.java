package com.example.madgroupproject.ui.goalpage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.GoalPreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GoalFragment extends Fragment {

//    private static final int REQUEST_CREATE_GOAL = 100;
//    private static final int REQUEST_EDIT_GOAL = 101;

    private LinearLayout goalsContainer;
    private Button btnCreateGoal;
    private List<Goal> goalsList;
    private GoalPreferenceManager goalManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goalManager = new GoalPreferenceManager(requireContext());
        //goals data are fetched when the fragment is on create
        goalsList = goalManager.loadGoals();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        displayGoals();
        setupListeners(view);

        // Listen for results from CreateGoalDialogFragment
        getParentFragmentManager().setFragmentResultListener("goal_request", this, (requestKey, result) -> {
            if (result.containsKey("goal_deleted")) {
                int position = result.getInt("goal_position", -1);
                if (position >= 0 && position < goalsList.size()) {
                    goalsList.remove(position);
                    goalManager.saveGoals(goalsList);
                    displayGoals();
                    Toast.makeText(requireContext(), "Goal deleted!", Toast.LENGTH_SHORT).show();
                }
            } else if (result.containsKey("goal_name")) {
                String goalName = result.getString("goal_name");
                String goalLabel = result.getString("goal_label");
                if (goalName != null && goalLabel != null) {
                    int position = result.getInt("goal_position", -1);
                    if (position >= 0 && position < goalsList.size()) {
                        // Edit existing goal
                        Goal goal = goalsList.get(position);
                        goal.setName(goalName);
                        goal.setLabel(goalLabel);
                        goal.setIconRes(goalManager.getIconForLabel(goalLabel));
                    } else {
                        // Create new goal
                        int iconRes = goalManager.getIconForLabel(goalLabel);
                        goalsList.add(new Goal(goalName, goalLabel, iconRes, false));
                    }


                    //For Notification Part
                    // Save this goal as today's notification goal
                    SharedPreferences prefs =
                            requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE);

                    prefs.edit()
                            .putString("daily_goal", goalName)
                            .apply();




                    goalManager.saveGoals(goalsList);
                    displayGoals();
                    Toast.makeText(requireContext(), "Goal saved!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initViews(View view) {
        goalsContainer = view.findViewById(R.id.goalsContainer);
        btnCreateGoal = view.findViewById(R.id.btnCreateGoal);
    }

//    private void loadGoals() {
//        // 从 SharedPreferences 加载保存的 Goals
//        String goalsJson = sharedPreferences.getString("goals_list", null);
//
//        if (goalsJson != null) {
//            Type type = new TypeToken<ArrayList<GoalActivity.Goal>>(){}.getType();
//            goalsList = gson.fromJson(goalsJson, type);
//        } else {
//            // 如果没有保存的数据，使用默认数据
//            goalsList = new ArrayList<>();
//            goalsList.add(new GoalActivity.Goal("Walk the dog", "Exercise", getIconForLabel("Exercise"), false));
//            goalsList.add(new GoalActivity.Goal("Drink 8 glass water", "Habit", getIconForLabel("Habit"), false));
//            goalsList.add(new GoalActivity.Goal("Listening to Podcast", "Relax", getIconForLabel("Relax"), false));
//            saveGoals();
//        }
//
//        displayGoals();
//    }

//    private void saveGoals() {
//        String goalsJson = gson.toJson(goalsList);
//        sharedPreferences.edit().putString("goals_list", goalsJson).apply();
//    }

    private void displayGoals() {
        goalsContainer.removeAllViews();

        for (int i = 0; i < goalsList.size(); i++) {
            View goalView = createGoalView(goalsList.get(i), i);
            goalsContainer.addView(goalView);
        }
    }

    private View createGoalView(Goal goal, int position) {
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

        goalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            goal.setCompleted(isChecked);
            if (isChecked) {
                goalBorder.setVisibility(View.VISIBLE);
            } else {
                goalBorder.setVisibility(View.GONE);
            }
        });

        // 点击整个卡片跳转到编辑页面
        view.setOnClickListener(v -> {
            CreateGoalDialogFragment.newEditInstance(goal.getName(), goal.getLabel(), position)
                    .show(getParentFragmentManager(), "edit_goal");
        });

        return view;
    }

    private void setupListeners(View root) {
        btnCreateGoal.setOnClickListener(v -> {
            new CreateGoalDialogFragment().show(getParentFragmentManager(), "create_goal");
        });

        // Suggested Goals
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




    // Goal Model Class
    public static class Goal {
        private String name;
        private String label;
        private int iconRes;
        private boolean completed;

        public Goal(String name, String label, int iconRes, boolean completed) {
            this.name = name;
            this.label = label;
            this.iconRes = iconRes;
            this.completed = completed;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public int getIconRes() { return iconRes; }
        public void setIconRes(int iconRes) { this.iconRes = iconRes; }

        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }

}