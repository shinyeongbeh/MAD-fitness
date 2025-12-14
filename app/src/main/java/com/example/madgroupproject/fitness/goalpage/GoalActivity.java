package com.example.madgroupproject.fitness.goalpage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.madgroupproject.R;
import com.example.madgroupproject.fitness.gamelevelspage.MainActivity;
import com.example.madgroupproject.fitness.homepage.HomeActivity;
import com.example.madgroupproject.fitness.streakpage.StreakActivity;
import com.example.madgroupproject.fitness.statspage.StatsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GoalActivity extends AppCompatActivity {

    private static final int REQUEST_CREATE_GOAL = 100;
    private static final int REQUEST_EDIT_GOAL = 101;

    private LinearLayout goalsContainer;
    private Button btnCreateGoal;
    private List<Goal> goalsList;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    // Bottom Navigation
    private LinearLayout navHome, navStreak, navFlag, navStats, navMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_goals);

        initViews();
        setupBottomNavigation();
        loadGoals();
        setupListeners();
    }

    private void initViews() {
        goalsContainer = findViewById(R.id.goalsContainer);
        btnCreateGoal = findViewById(R.id.btnCreateGoal);
        goalsList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("GoalsData", MODE_PRIVATE);
        gson = new Gson();
    }

    private void setupBottomNavigation() {
        navHome = findViewById(R.id.navHome);
        navStreak = findViewById(R.id.navStreak);
        navFlag = findViewById(R.id.navFlag);
        navStats = findViewById(R.id.navStats);
        navMore = findViewById(R.id.navMore);

        // 高亮当前页面（Flag/Goals）
        highlightNavItem(navFlag);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navStreak.setOnClickListener(v -> {
            startActivity(new Intent(this, StreakActivity.class));
            finish();
        });

        navFlag.setOnClickListener(v -> {
            // 已经在 Goals 页面
        });

        navStats.setOnClickListener(v -> {
            startActivity(new Intent(this, StatsActivity.class));
            finish();
        });

        navMore.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void highlightNavItem(LinearLayout selectedItem) {
        resetNavItems();
        ImageView imageView = (ImageView) selectedItem.getChildAt(0);
        ImageViewCompat.setImageTintList(
                imageView,
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.nav_selected))
        );
    }

    private void resetNavItems() {
        LinearLayout[] navItems = {navHome, navStreak, navFlag, navStats, navMore};
        ColorStateList grayColor = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.nav_unselected)
        );

        for (LinearLayout navItem : navItems) {
            ImageView imageView = (ImageView) navItem.getChildAt(0);
            ImageViewCompat.setImageTintList(imageView, grayColor);
        }
    }

    private void loadGoals() {
        // 从 SharedPreferences 加载保存的 Goals
        String goalsJson = sharedPreferences.getString("goals_list", null);

        if (goalsJson != null) {
            Type type = new TypeToken<ArrayList<Goal>>(){}.getType();
            goalsList = gson.fromJson(goalsJson, type);
        } else {
            // 如果没有保存的数据，使用默认数据
            goalsList = new ArrayList<>();
            goalsList.add(new Goal("Walk the dog", "Exercise", getIconForLabel("Exercise"), false));
            goalsList.add(new Goal("Drink 8 glass water", "Habit", getIconForLabel("Habit"), false));
            goalsList.add(new Goal("Listening to Podcast", "Relax", getIconForLabel("Relax"), false));
            saveGoals();
        }

        displayGoals();
    }

    private void saveGoals() {
        String goalsJson = gson.toJson(goalsList);
        sharedPreferences.edit().putString("goals_list", goalsJson).apply();
    }

    private void displayGoals() {
        goalsContainer.removeAllViews();

        for (int i = 0; i < goalsList.size(); i++) {
            Goal goal = goalsList.get(i);
            View goalView = createGoalView(goal, i);
            goalsContainer.addView(goalView);
        }
    }

    private View createGoalView(Goal goal, int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_goal, goalsContainer, false);

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
            saveGoals();
        });

        // 点击整个卡片跳转到编辑页面
        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGoalActivity.class);
            intent.putExtra("goal_position", position);
            intent.putExtra("goal_name", goal.getName());
            intent.putExtra("goal_label", goal.getLabel());
            startActivityForResult(intent, REQUEST_EDIT_GOAL);
        });

        return view;
    }

    private void setupListeners() {
        btnCreateGoal.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGoalActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_GOAL);
        });

        // Suggested Goals
        findViewById(R.id.suggestedExercise).setOnClickListener(v -> {
            createSuggestedGoal("Exercise 30min", "Exercise");
        });

        findViewById(R.id.suggestedWakeup).setOnClickListener(v -> {
            createSuggestedGoal("Early Wake-up", "Habit");
        });

        findViewById(R.id.suggestedSleep).setOnClickListener(v -> {
            createSuggestedGoal("Sleep 8hours", "Relax");
        });
    }

    private void createSuggestedGoal(String name, String label) {
        Intent intent = new Intent(this, CreateGoalActivity.class);
        intent.putExtra("suggested_name", name);
        intent.putExtra("suggested_label", label);
        startActivityForResult(intent, REQUEST_CREATE_GOAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CREATE_GOAL) {
                // 创建新 Goal
                String goalName = data.getStringExtra("goal_name");
                String goalLabel = data.getStringExtra("goal_label");

                if (goalName != null && goalLabel != null) {
                    int iconRes = getIconForLabel(goalLabel);
                    Goal newGoal = new Goal(goalName, goalLabel, iconRes, false);
                    goalsList.add(newGoal);
                    saveGoals();
                    displayGoals();
                }
            } else if (requestCode == REQUEST_EDIT_GOAL) {
                // 检查是否是删除操作
                boolean isDeleted = data.getBooleanExtra("goal_deleted", false);

                if (isDeleted) {
                    int position = data.getIntExtra("goal_position", -1);
                    if (position >= 0 && position < goalsList.size()) {
                        goalsList.remove(position);
                        saveGoals();
                        displayGoals();
                    }
                } else {
                    // 更新 Goal
                    int position = data.getIntExtra("goal_position", -1);
                    String goalName = data.getStringExtra("goal_name");
                    String goalLabel = data.getStringExtra("goal_label");

                    if (position >= 0 && position < goalsList.size() && goalName != null && goalLabel != null) {
                        Goal goal = goalsList.get(position);
                        goal.setName(goalName);
                        goal.setLabel(goalLabel);
                        goal.setIconRes(getIconForLabel(goalLabel));
                        saveGoals();
                        displayGoals();
                    }
                }
            }
        }
    }

    // 根据 Label 获取对应的图标
    private int getIconForLabel(String label) {
        switch (label) {
            case "Exercise":
                return R.drawable.ic_exercise;
            case "Habit":
                return R.drawable.ic_water;
            case "Relax":
                return R.drawable.ic_podcast;
            case "Work":
                return R.drawable.ic_work;
            case "Study":
                return R.drawable.ic_study;
            case "Health":
                return R.drawable.ic_health;
            default:
                return R.drawable.ic_exercise; // 默认图标
        }
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