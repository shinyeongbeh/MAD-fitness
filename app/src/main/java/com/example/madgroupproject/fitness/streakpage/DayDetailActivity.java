package com.example.madgroupproject.fitness.streakpage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.ColorStateList;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.madgroupproject.R;
import com.example.madgroupproject.fitness.gamelevelspage.MainActivity;
import com.example.madgroupproject.fitness.goalpage.GoalActivity;
import com.example.madgroupproject.fitness.homepage.HomeActivity;
import com.example.madgroupproject.fitness.statspage.StatsActivity;

public class DayDetailActivity extends AppCompatActivity {

    private TextView tvDayTitle;
    private Button btnBack;
    private LinearLayout navHome, navStreak, navFlag, navStats, navMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streak_day_detail);

        tvDayTitle = findViewById(R.id.tvDayTitle);
        btnBack = findViewById(R.id.btnBack);

        // 获取传递的日期信息
        int day = getIntent().getIntExtra("day", 1);
        String month = getIntent().getStringExtra("month");

        // 设置标题
        tvDayTitle.setText(month + " " + day);

        // 设置返回按钮
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

