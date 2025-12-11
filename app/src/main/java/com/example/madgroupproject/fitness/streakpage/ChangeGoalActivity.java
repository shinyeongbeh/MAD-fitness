package com.example.madgroupproject.fitness.streakpage;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.madgroupproject.R;
import com.example.madgroupproject.fitness.gamelevelspage.MainActivity;
import com.example.madgroupproject.fitness.goalpage.GoalActivity;
import com.example.madgroupproject.fitness.homepage.HomeActivity;
import com.example.madgroupproject.fitness.statspage.StatsActivity;

public class ChangeGoalActivity extends AppCompatActivity {

    private EditText etNewGoal;
    private Button btnChange;
    private Button btnCancel;
    private int currentGoal;

    private LinearLayout navHome, navStreak, navFlag, navStats, navMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streak_change_goal);

        etNewGoal = findViewById(R.id.etNewGoal);
        btnChange = findViewById(R.id.btnChange);
        btnCancel = findViewById(R.id.btnCancel);

        // 获取当前目标
        currentGoal = getIntent().getIntExtra("currentGoal", 1000);

        // 设置Change按钮点击事件
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newGoalText = etNewGoal.getText().toString().trim();

                if (newGoalText.isEmpty()) {
                    Toast.makeText(ChangeGoalActivity.this,
                            "Please enter a goal", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int newGoal = Integer.parseInt(newGoalText);
                    if (newGoal <= 0) {
                        Toast.makeText(ChangeGoalActivity.this,
                                "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 这里可以保存新目标到SharedPreferences或数据库
                    Toast.makeText(ChangeGoalActivity.this,
                            "Goal changed to " + newGoal + " steps daily",
                            Toast.LENGTH_SHORT).show();

                    // 返回结果给MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newGoal", newGoal);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // 返回上一个界面
                } catch (NumberFormatException e) {
                    Toast.makeText(ChangeGoalActivity.this,
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
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        navHome = findViewById(R.id.navHome);
        navStreak = findViewById(R.id.navStreak);
        navFlag = findViewById(R.id.navFlag);
        navStats = findViewById(R.id.navStats);
        navMore = findViewById(R.id.navMore);

        // 高亮当前页面（Goals）
        highlightNavItem(navStreak);

        navHome.setOnClickListener(v -> finish());
        navStreak.setOnClickListener(v -> finish());
        navFlag.setOnClickListener(v -> finish());
        navStats.setOnClickListener(v -> finish());
        navMore.setOnClickListener(v -> finish());
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