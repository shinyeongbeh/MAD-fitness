package com.example.madgroupproject.goalpage;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.madgroupproject.R;
import com.example.madgroupproject.homepage.HomeActivity;
import com.example.madgroupproject.statspage.StatsActivity;
import com.example.madgroupproject.streakpage.StreakActivity;
import com.example.madgroupproject.gamelevelspage.MainActivity;

public class CreateGoalActivity extends AppCompatActivity {

    private EditText etGoalName;
    private Spinner spinnerLabel;
    private TextView tvReminderTime;
    private SwitchCompat switchReminder;
    private Button btnSaveGoal, btnDeleteGoal, btnBack;

    private int selectedHour = 18;
    private int selectedMinute = 30;
    private boolean isEditMode = false;
    private int goalPosition = -1;

    // Bottom Navigation
    private LinearLayout navHome, navStreak, navFlag, navStats, navMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_create_goal);

        initViews();
        setupBottomNavigation();
        setupSpinner();
        loadIntentData();
        setupListeners();
    }

    private void initViews() {
        etGoalName = findViewById(R.id.etGoalName);
        spinnerLabel = findViewById(R.id.spinnerLabel);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        switchReminder = findViewById(R.id.switchReminder);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);
        btnDeleteGoal = findViewById(R.id.btnDeleteGoal);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupBottomNavigation() {
        navHome = findViewById(R.id.navHome);
        navStreak = findViewById(R.id.navStreak);
        navFlag = findViewById(R.id.navFlag);
        navStats = findViewById(R.id.navStats);
        navMore = findViewById(R.id.navMore);

        // 高亮当前页面（Goals）
        highlightNavItem(navFlag);

        // 设置点击监听 - 跳转到对应页面
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
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

    private void setupSpinner() {
        String[] labels = {"Exercise", "Habit", "Relax", "Work", "Study", "Health"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLabel.setAdapter(adapter);
    }

    private void loadIntentData() {
        // 检查是否是编辑模式
        if (getIntent().hasExtra("goal_position")) {
            isEditMode = true;
            goalPosition = getIntent().getIntExtra("goal_position", -1);
            String goalName = getIntent().getStringExtra("goal_name");
            String goalLabel = getIntent().getStringExtra("goal_label");

            etGoalName.setText(goalName);
            setSpinnerValue(goalLabel);
            btnDeleteGoal.setVisibility(View.VISIBLE);
        } else {
            // 新建模式下隐藏删除按钮
            btnDeleteGoal.setVisibility(View.GONE);
        }

        // 检查是否是从 Suggested Goals 点击
        if (getIntent().hasExtra("suggested_name")) {
            String suggestedName = getIntent().getStringExtra("suggested_name");
            String suggestedLabel = getIntent().getStringExtra("suggested_label");

            etGoalName.setText(suggestedName);
            setSpinnerValue(suggestedLabel);
        }
    }

    private void setSpinnerValue(String label) {
        ArrayAdapter adapter = (ArrayAdapter) spinnerLabel.getAdapter();
        int position = adapter.getPosition(label);
        if (position >= 0) {
            spinnerLabel.setSelection(position);
        }
    }

    private void setupListeners() {
        // Time picker
        tvReminderTime.setOnClickListener(v -> showTimePicker());

        // Save button
        btnSaveGoal.setOnClickListener(v -> saveGoal());

        // Delete button
        btnDeleteGoal.setOnClickListener(v -> deleteGoal());

        // Back button
        btnBack.setOnClickListener(v -> finish());
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateTimeDisplay();
                },
                selectedHour,
                selectedMinute,
                true
        );
        timePickerDialog.show();
    }

    private void updateTimeDisplay() {
        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
        tvReminderTime.setText(time);
    }

    private void saveGoal() {
        String goalName = etGoalName.getText().toString().trim();
        String label = spinnerLabel.getSelectedItem().toString();
        boolean reminderEnabled = switchReminder.isChecked();

        if (goalName.isEmpty()) {
            Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show();
            return;
        }

        // 返回结果
        Intent resultIntent = new Intent();
        resultIntent.putExtra("goal_name", goalName);
        resultIntent.putExtra("goal_label", label);

        if (isEditMode) {
            resultIntent.putExtra("goal_position", goalPosition);
        }

        Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void deleteGoal() {
        // 返回删除标记
        Intent resultIntent = new Intent();
        resultIntent.putExtra("goal_deleted", true);
        resultIntent.putExtra("goal_position", goalPosition);

        Toast.makeText(this, "Goal deleted!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}