package com.example.madgroupproject.fitness.goalpage;

import android.content.Intent;
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
import com.example.madgroupproject.fitness.homepage.HomeActivity;
import com.example.madgroupproject.fitness.streakpage.StreakActivity;
import com.example.madgroupproject.fitness.statspage.StatsActivity;

import java.util.ArrayList;
import java.util.List;

public class GoalActivity extends AppCompatActivity {

    private LinearLayout goalsContainer;
    private Button btnCreateGoal;
    private List<Goal> goalsList;

    // Bottom Navigation
    private LinearLayout navHome, navStreak, navFlag, navStats, navMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            // 跳转到 More 页面（如果有的话）
            // startActivity(new Intent(this, MoreActivity.class));
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
        // 清空现有列表，避免重复
        goalsList.clear();

        // 示例数据 - 你可以从数据库或 SharedPreferences 加载
        goalsList.add(new Goal("Walk the dog", "Exercise", R.drawable.ic_exercise, false));
        goalsList.add(new Goal("Drink 8 glass water", "Habit", R.drawable.ic_water, false));
        goalsList.add(new Goal("Listening to Podcast", "Relax", R.drawable.ic_podcast, false));

        displayGoals();
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
        });

        // 点击整个卡片跳转到编辑页面
        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGoalActivity.class);
            intent.putExtra("goal_position", position);
            intent.putExtra("goal_name", goal.getName());
            intent.putExtra("goal_label", goal.getLabel());
            startActivity(intent);
        });

        return view;
    }

    private void setupListeners() {
        btnCreateGoal.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGoalActivity.class);
            startActivity(intent);
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
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从其他页面返回时重新显示现有数据，而不是重新加载
        displayGoals();
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
        public String getLabel() { return label; }
        public int getIconRes() { return iconRes; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}