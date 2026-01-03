package com.example.madgroupproject.ui.streakpage;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;
import com.example.madgroupproject.data.repository.StreakRepository;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;
import com.example.madgroupproject.util.MidnightChangeListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class StreakFragment extends Fragment {
    private static final String TAG = "StreakFragment";

    private GridLayout calendarGrid;
    private Button btnChangeStreakGoal;
    private CardView cardBestStreak;
    private TextView tvTodaySteps;
    private TextView tvCurrentStreak;
    private TextView tvStreakDate;
    private TextView tvBestStreak;
    private TextView tvBestStreakDates;
    private TextView tvMonthTitle;
    private ImageView ivTodayCheck;
    private ImageButton btnPrevMonth, btnNextMonth;

    private StreakViewModel viewModel;
    private List<StreakHistoryEntity> monthData;

    private YearMonth currentYearMonth;

    private LiveData<List<StreakHistoryEntity>> currentMonthLiveData;

    // ✅ 添加广播接收器
    private BroadcastReceiver midnightReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ✅ 注册广播接收器
        setupMidnightBroadcastReceiver();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streak, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ 先初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        // ✅ 确保今天的记录存在(MainActivity已经初始化过,这里作为保险)
        viewModel.autoInitTodayRecord();

        // ✅ 从 ViewModel 恢复月份(ViewModel 在 Fragment 重建时会保留)
        currentYearMonth = viewModel.getCurrentViewingMonthValue();
        Log.d(TAG, "Restored month from ViewModel: " + currentYearMonth);

        initViews(view);
        setupObservers();
        setupClickListeners();
        setupMonthNavigation();
    }

    private void initViews(View view) {
        calendarGrid = view.findViewById(R.id.calendarGrid);
        btnChangeStreakGoal = view.findViewById(R.id.btnChangeStreakGoal);
        cardBestStreak = view.findViewById(R.id.cardBestStreak);
        tvTodaySteps = view.findViewById(R.id.tvTodaySteps);
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvStreakDate = view.findViewById(R.id.tvStreakDate);
        tvBestStreak = view.findViewById(R.id.tvBestStreak);
        tvBestStreakDates = view.findViewById(R.id.tvBestStreakDates);
        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        ivTodayCheck = view.findViewById(R.id.ivTodayCheck);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);

        updateTodayDateDisplay();
        updateMonthTitle();
    }

    private void updateTodayDateDisplay() {
        LocalDate today = LocalDate.now();
        String monthName = today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String dateText = String.format("Today: %s %d", monthName, today.getDayOfMonth());

        if (tvStreakDate != null) {
            tvStreakDate.setText(dateText);
        }

        Log.d(TAG, "Updated today date display to: " + today);
    }

    private void updateMonthTitle() {
        if (tvMonthTitle != null) {
            String monthName = currentYearMonth.getMonth()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            tvMonthTitle.setText(String.format("%s %d", monthName, currentYearMonth.getYear()));
            Log.d(TAG, "Updated month title to: " + monthName + " " + currentYearMonth.getYear());
        }
    }

    private void setupMonthNavigation() {
        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                Log.d(TAG, "Previous month clicked. Current: " + currentYearMonth);
                currentYearMonth = currentYearMonth.minusMonths(1);
                viewModel.setCurrentViewingMonth(currentYearMonth);
                updateMonthTitle();
                loadMonthData();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                YearMonth now = YearMonth.now();
                Log.d(TAG, "Next month clicked. Current: " + currentYearMonth + ", Now: " + now);
                if (currentYearMonth.isBefore(now)) {
                    currentYearMonth = currentYearMonth.plusMonths(1);
                    viewModel.setCurrentViewingMonth(currentYearMonth);
                    updateMonthTitle();
                    loadMonthData();
                } else {
                    Toast.makeText(requireContext(),
                            "Cannot view future months",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadMonthData() {
        String yearMonth = String.format("%04d-%02d",
                currentYearMonth.getYear(),
                currentYearMonth.getMonthValue());

        Log.d(TAG, "Loading month data for: " + yearMonth);

        if (currentMonthLiveData != null) {
            currentMonthLiveData.removeObservers(getViewLifecycleOwner());
            Log.d(TAG, "Removed previous observer");
        }

        currentMonthLiveData = viewModel.getMonthStreakLive(yearMonth);
        currentMonthLiveData.observe(getViewLifecycleOwner(), list -> {
            monthData = list != null ? list : List.of();
            Log.d(TAG, "Month data loaded: " + monthData.size() + " records for " + yearMonth);
            initializeCalendar();
        });
    }

    private void setupObservers() {
        viewModel.getLongestStreakWithDetailsLiveData().observe(getViewLifecycleOwner(), result -> {
            try {
                if (result != null && !result.isEmpty()) {
                    tvBestStreak.setText(result.count + " days");
                    String dateRange = formatDateRange(result.startDate, result.endDate);
                    tvBestStreakDates.setText(dateRange);
                } else {
                    tvBestStreak.setText("0 days");
                    tvBestStreakDates.setText("No streak");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating best streak UI", e);
                tvBestStreak.setText("0 days");
                tvBestStreakDates.setText("Error loading");
            }
        });

        loadMonthData();

        viewModel.getCurrentStreakLiveData().observe(getViewLifecycleOwner(), currentResult -> {
            try {
                if (currentResult != null && currentResult.streakCount > 0) {
                    tvCurrentStreak.setText(String.format("CURRENT STREAK: %d DAYS", currentResult.streakCount));
                } else {
                    tvCurrentStreak.setText("CURRENT STREAK: 0 DAYS");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating current streak UI", e);
                tvCurrentStreak.setText("CURRENT STREAK: 0 DAYS");
            }
        });

        viewModel.getTodayStepsLiveData().observe(getViewLifecycleOwner(), result -> {
            try {
                if (result != null) {
                    tvTodaySteps.setText(String.format("Steps: %d/%d", result.steps, result.minStepsRequired));

                    if (result.achieved) {
                        ivTodayCheck.setImageResource(R.drawable.streak_ic_check_green_circle);
                        tvTodaySteps.setTextColor(getThemedColor(R.color.streak_completed));
                    } else {
                        ivTodayCheck.setImageResource(R.drawable.streak_ic_check_gray_circle);
                        tvTodaySteps.setTextColor(getThemedColor(R.color.text_light_gray));
                    }
                } else {
                    tvTodaySteps.setText("Steps: 0/0");
                    ivTodayCheck.setImageResource(R.drawable.streak_ic_check_gray_circle);
                    tvTodaySteps.setTextColor(getThemedColor(R.color.text_light_gray));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating today steps UI", e);
            }
        });
    }

    private void setupClickListeners() {
        btnChangeStreakGoal.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to change goal. Current month: " + currentYearMonth);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_streakFragment_to_changeStreakFragment);
        });

        cardBestStreak.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to best streak detail. Current month: " + currentYearMonth);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_streakFragment_to_bestStreakDetailFragment);
        });
    }

    private void initializeCalendar() {
        try {
            int childCount = calendarGrid.getChildCount();
            if (childCount > 7) {
                calendarGrid.removeViews(7, childCount - 7);
            }

            LocalDate firstOfMonth = currentYearMonth.atDay(1);
            int startDay = firstOfMonth.getDayOfWeek().getValue() % 7;
            int daysInMonth = currentYearMonth.lengthOfMonth();

            Log.d(TAG, "Initializing calendar for " + currentYearMonth +
                    ", days: " + daysInMonth + ", start day: " + startDay);

            // 添加空白格子
            for (int i = 0; i < startDay; i++) {
                TextView empty = new TextView(requireContext());
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 120;
                params.columnSpec = GridLayout.spec(i, 1f);
                params.rowSpec = GridLayout.spec(1);
                empty.setLayoutParams(params);
                calendarGrid.addView(empty);
            }

            // 添加日期格子
            int row = 1, col = startDay;
            for (int day = 1; day <= daysInMonth; day++) {
                TextView dayView = createDayView(day);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 120;
                params.columnSpec = GridLayout.spec(col, 1f);
                params.rowSpec = GridLayout.spec(row);
                params.setMargins(4, 4, 4, 4);
                dayView.setLayoutParams(params);
                calendarGrid.addView(dayView);

                if (++col == 7) {
                    col = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing calendar", e);
        }
    }

    private TextView createDayView(int day) {
        TextView view = new TextView(requireContext());
        view.setText(String.valueOf(day));
        view.setGravity(Gravity.CENTER);
        view.setPadding(16, 16, 16, 16);
        view.setTextSize(16);
        view.setTypeface(null, android.graphics.Typeface.BOLD);

        String date = String.format("%04d-%02d-%02d",
                currentYearMonth.getYear(),
                currentYearMonth.getMonthValue(),
                day);

        boolean achieved = false;
        boolean hasData = false;

        if (monthData != null) {
            for (StreakHistoryEntity e : monthData) {
                if (e.date.equals(date)) {
                    achieved = e.achieved;
                    hasData = true;
                    break;
                }
            }
        }

        if (!hasData) {
            view.setBackgroundResource(R.drawable.streak_calendar_day_inactive);
            view.setTextColor(getThemedColor(R.color.text_light_gray));
        } else if (achieved) {
            view.setBackgroundResource(R.drawable.streak_calendar_day_completed);
            view.setTextColor(Color.WHITE);
        } else {
            view.setBackgroundResource(R.drawable.streak_calendar_day_inactive);
            view.setTextColor(getThemedColor(R.color.text_light_gray));
        }

        view.setOnClickListener(v -> {
            Log.d(TAG, "Date clicked: " + date + ", current month: " + currentYearMonth);
            Bundle args = new Bundle();
            args.putString("date", date);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_streakFragment_to_dayDetailFragment, args);
        });

        return view;
    }

    private String formatDateRange(String startDateStr, String endDateStr) {
        try {
            LocalDate start = LocalDate.parse(startDateStr);
            LocalDate end = LocalDate.parse(endDateStr);

            String startMonth = start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            String endMonth = end.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            if (start.getMonth() == end.getMonth() && start.getYear() == end.getYear()) {
                return String.format("%s %d - %d", startMonth, start.getDayOfMonth(), end.getDayOfMonth());
            } else if (start.getYear() == end.getYear()) {
                return String.format("%s %d - %s %d",
                        startMonth, start.getDayOfMonth(),
                        endMonth, end.getDayOfMonth());
            } else {
                return String.format("%s %d, %d - %s %d, %d",
                        startMonth, start.getDayOfMonth(), start.getYear(),
                        endMonth, end.getDayOfMonth(), end.getYear());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date range", e);
            return startDateStr + " - " + endDateStr;
        }
    }

    private int getThemedColor(int colorResId) {
        return ContextCompat.getColor(requireContext(), colorResId);
    }

    private boolean isDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called. Current month: " + currentYearMonth);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called. Current month: " + currentYearMonth);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // ✅ 取消注册广播接收器
        if (midnightReceiver != null) {
            try {
                requireContext().unregisterReceiver(midnightReceiver);
                Log.d(TAG, "Midnight broadcast receiver unregistered");
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver", e);
            }
        }
    }

    /**
     * ✅ 设置广播接收器（修复版 - 使用主线程Handler）
     */
    private void setupMidnightBroadcastReceiver() {
        midnightReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.madgroupproject.MIDNIGHT_PASSED".equals(intent.getAction())) {
                    Log.d(TAG, "📡 Received midnight broadcast!");
                    Log.d(TAG, "   Thread: " + Thread.currentThread().getName());

                    if (!isAdded()) {
                        Log.e(TAG, "❌ Fragment not added, skipping update");
                        return;
                    }

                    // 🔴 关键修复：使用主线程Handler + 防止闪退
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isAdded()) {
                            Log.e(TAG, "❌ Fragment not added (delayed check)");
                            return;
                        }

                        try {
                            Log.d(TAG, "🔄 Starting UI update on main thread...");

                            // 🔑 关键修复: 先确保今日记录已初始化，防止闪退
                            viewModel.autoInitTodayRecord();
                            Log.d(TAG, "✅ Ensured today's record exists");

                            // ✅ 刷新今日日期，让Today步数LiveData观察新的日期
                            viewModel.refreshTodayDate();
                            Log.d(TAG, "✅ Refreshed today's date for LiveData");

                            // 1️⃣ 更新日期显示
                            updateTodayDateDisplay();

                            // 2️⃣ 如果当前查看的是当月,刷新日历数据
                            YearMonth now = YearMonth.now();
                            if (currentYearMonth != null && currentYearMonth.equals(now)) {
                                Log.d(TAG, "🔄 Refreshing calendar for new day...");
                                loadMonthData();
                            }

                            // MainActivity已显示Toast，这里不再重复显示
                            Log.d(TAG, "✅ UI update complete!");
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error during midnight update", e);
                            // 防止崩溃，显示友好提示
                            if (isAdded()) {
                                Toast.makeText(requireContext(),
                                        "Please refresh the page",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 500);
                }
            }
        };

        IntentFilter filter = new IntentFilter("com.example.madgroupproject.MIDNIGHT_PASSED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(midnightReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(midnightReceiver, filter);
        }

        Log.d(TAG, "✅ Midnight broadcast receiver registered");
    }
}

