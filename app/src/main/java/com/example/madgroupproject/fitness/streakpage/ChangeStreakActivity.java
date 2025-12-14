package com.example.madgroupproject.fitness.streakpage;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.madgroupproject.R;
import com.example.madgroupproject.fitness.gamelevelspage.MainActivity;
import com.example.madgroupproject.fitness.goalpage.GoalActivity;
import com.example.madgroupproject.fitness.homepage.HomeActivity;
import com.example.madgroupproject.fitness.statspage.StatsActivity;

public class ChangeStreakActivity extends AppCompatActivity {

    private TextView tvCurrentStreak;
    private EditText etNewStreak;
    private Button btnChange;
    private Button btnCancel;
    private int currentGoal;

    private LinearLayout navHome, navStreak, navFlag, navStats, navMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.streak_change_goal);

        // 初始化视图
        initViews();

        // 获取当前目标
        currentGoal = getIntent().getIntExtra("currentGoal", 1000);

        // 显示当前目标
        tvCurrentStreak.setText(currentGoal + " steps daily");

        // 设置点击事件
        setupClickListeners();

        // 设置底部导航
        setupBottomNavigation();
    }

    private void initViews() {
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
        etNewStreak = findViewById(R.id.etNewStreak);
        btnChange = findViewById(R.id.btnChange);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupClickListeners() {
        // 设置Change按钮点击事件
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newGoalText = etNewStreak.getText().toString().trim();

                if (newGoalText.isEmpty()) {
                    Toast.makeText(ChangeStreakActivity.this,
                            "Please enter a goal", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int newGoal = Integer.parseInt(newGoalText);
                    if (newGoal <= 0) {
                        Toast.makeText(ChangeStreakActivity.this,
                                "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 这里可以保存新目标到SharedPreferences或数据库
                    Toast.makeText(ChangeStreakActivity.this,
                            "Goal changed to " + newGoal + " steps daily",
                            Toast.LENGTH_SHORT).show();

                    // 返回结果给StreakActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newGoal", newGoal);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // 返回上一个界面
                } catch (NumberFormatException e) {
                    Toast.makeText(ChangeStreakActivity.this,
                            "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 设置Cancel按钮点击事件
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一个界面
            }
        });
    }

    private void setupBottomNavigation() {
        navHome = findViewById(R.id.navHome);
        navStreak = findViewById(R.id.navStreak);
        navFlag = findViewById(R.id.navFlag);
        navStats = findViewById(R.id.navStats);
        navMore = findViewById(R.id.navMore);

        // 高亮当前页面（Streak）
        highlightNavItem(navStreak);

        // 设置点击监听 - 跳转到对应页面
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navStreak.setOnClickListener(v -> {
            // 返回到 Streak 页面
            finish();
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
}