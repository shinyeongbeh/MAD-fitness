package com.example.madgroupproject.homepage;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import com.example.madgroupproject.R;
import com.example.madgroupproject.streakpage.StreakActivity;
import com.example.madgroupproject.goalpage.GoalActivity;
import com.example.madgroupproject.statspage.StatsActivity;
import com.example.madgroupproject.gamelevelspage.MainActivity;
import com.example.madgroupproject.settingpage.SettingMainActivity;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout navHome, navStreak, navFlag, navStats, navMore;
    private ImageView btnSettings;  // 设置按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_home);

        // 初始化设置按钮
        btnSettings = findViewById(R.id.btnSettings);

        // 设置按钮点击事件
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到设置页面
                Intent intent = new Intent(HomeActivity.this, SettingMainActivity.class);
                startActivity(intent);
            }
        });

        // 设置底部导航栏点击事件
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // 1. 获取所有导航项
        navHome = findViewById(R.id.navHome);
        navStreak = findViewById(R.id.navStreak);
        navFlag = findViewById(R.id.navFlag);
        navStats = findViewById(R.id.navStats);
        navMore = findViewById(R.id.navMore);

        // 2. 高亮当前页面（Home）
        highlightNavItem(navHome);

        // 3. 设置点击监听
        navHome.setOnClickListener(v -> {
            // 已经在首页，不需要操作
        });

        navStreak.setOnClickListener(v -> {
            startActivity(new Intent(this, StreakActivity.class));
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

        navMore.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingMainActivity.class));
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