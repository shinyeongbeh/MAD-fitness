package com.example.madgroupproject.fitness.streakpage;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import com.example.madgroupproject.R;
import com.example.madgroupproject.fitness.homepage.HomeActivity;
import com.example.madgroupproject.fitness.goalpage.GoalActivity;
import com.example.madgroupproject.fitness.statspage.StatsActivity;
import com.example.madgroupproject.fitness.gamelevelspage.MainActivity;

public class StreakActivity extends AppCompatActivity {

    private GridLayout calendarGrid;
    private Button btnChangeGoal;
    private CardView bestStreakCard;
    private TextView tvStepsProgress;
    private LinearLayout navHome, navStreak, navFlag, navStats, navMore;

    // 记录哪些日期已完成目标（5-8号为绿色）
    private boolean[] completedDays = new boolean[29]; // February has 28/29 days
    private int currentGoal = 1000; // 当前目标步数
    private int currentSteps = 760; // 当前步数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏（如果有的话）
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.streak_main_activity);

        calendarGrid = findViewById(R.id.calendarGrid);
        btnChangeGoal = findViewById(R.id.btnChangeGoal);
        bestStreakCard = findViewById(R.id.bestStreakCard);
        tvStepsProgress = findViewById(R.id.tvStepsProgress);

        // 设置完成的日期（5-8号）
        for (int i = 5; i <= 8; i++) {
            completedDays[i - 1] = true;
        }

        // 初始化日历
        initializeCalendar();

        // 设置底部导航栏点击事件
        setupBottomNavigation();

        // 设置"Change Streak Goal"按钮点击事件
        btnChangeGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StreakActivity.this, ChangeGoalActivity.class);
                intent.putExtra("currentGoal", currentGoal);
                startActivityForResult(intent, 1);
            }
        });

        // 设置"Best Streak"卡片点击事件
        bestStreakCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StreakActivity.this, BestStreakDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeCalendar() {
        // February 2025 starts on Saturday (day 6)
        // Add empty cells for days before the 1st
        int startDay = 6; // Saturday

        for (int i = 0; i < startDay; i++) {
            TextView emptyView = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i, 1f);
            params.rowSpec = GridLayout.spec(0);
            emptyView.setLayoutParams(params);
            calendarGrid.addView(emptyView);
        }

        // Add calendar days
        int daysInMonth = 28; // February 2025
        int currentRow = 0;
        int currentCol = startDay;

        for (int day = 1; day <= daysInMonth; day++) {
            TextView dayView = createDayView(day);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(currentCol, 1f);
            params.rowSpec = GridLayout.spec(currentRow);
            params.setMargins(4, 4, 4, 4);

            dayView.setLayoutParams(params);
            calendarGrid.addView(dayView);

            currentCol++;
            if (currentCol == 7) {
                currentCol = 0;
                currentRow++;
            }
        }
    }

    private TextView createDayView(final int day) {
        TextView dayView = new TextView(this);
        dayView.setText(String.valueOf(day));
        dayView.setTextColor(Color.BLACK);
        dayView.setTextSize(16);
        dayView.setGravity(Gravity.CENTER);
        dayView.setPadding(16, 16, 16, 16);

        // 设置背景颜色
        if (completedDays[day - 1]) {
            // 已完成的日期显示为深绿色
            dayView.setBackground(getResources().getDrawable(R.drawable.streak_calendar_day_completed));
            dayView.setTextColor(Color.WHITE);
        } else {
            // 其他日期显示为灰色背景
            dayView.setBackground(getResources().getDrawable(R.drawable.streak_calendar_day_inactive));
            dayView.setTextColor(Color.BLACK);
        }

        // 设置点击事件（仅对第2天）
        if (day == 2) {
            dayView.setClickable(true);
            dayView.setFocusable(true);
            dayView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StreakActivity.this, DayDetailActivity.class);
                    intent.putExtra("day", day);
                    intent.putExtra("month", "Feb");
                    startActivity(intent);
                }
            });
        }

        return dayView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            currentGoal = data.getIntExtra("newGoal", 1000);
            updateStepsProgress();
        }
    }

    private void updateStepsProgress() {
        tvStepsProgress.setText("Steps: " + currentSteps + "/" + currentGoal);
    }

    private void setupBottomNavigation() {
        // 1. 获取所有导航项
        navHome = findViewById(R.id.navHome);
        navStreak = findViewById(R.id.navStreak);
        navFlag = findViewById(R.id.navFlag);
        navStats = findViewById(R.id.navStats);
        navMore = findViewById(R.id.navMore);

        // 2. 高亮当前页面（Streak）
        highlightNavItem(navStreak);

        // 3. 设置点击监听
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navStreak.setOnClickListener(v -> {
            // 已经在连续记录页面
        });

        navFlag.setOnClickListener(v -> {
            startActivity(new Intent(this, GoalActivity.class));
            finish();
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

    // 高亮选中的导航项
    private void highlightNavItem(LinearLayout selectedItem) {
        // 先重置所有导航项为灰色
        resetNavItems();

        // 将选中项的图标设置为绿色
        ImageView imageView = (ImageView) selectedItem.getChildAt(0);
        ImageViewCompat.setImageTintList(
                imageView,
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.nav_selected))
        );
    }

    // 重置所有导航项为灰色
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
}