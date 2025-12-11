package com.example.madgroupproject.fitness.goalpage;

import android.app.TimePickerDialog;
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

import java.util.Calendar;

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

        // TODO: 这里你需要实现保存逻辑（数据库或 SharedPreferences）
        // 示例：保存到 SharedPreferences 或者数据库

        Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteGoal() {
        // TODO: 这里你需要实现删除逻辑

        Toast.makeText(this, "Goal deleted!", Toast.LENGTH_SHORT).show();
        finish();
    }
}