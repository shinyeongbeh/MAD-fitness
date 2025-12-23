package com.example.madgroupproject.ui.streakpage;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class StreakFragment extends Fragment {

    private GridLayout calendarGrid;
    private Button btnChangeStreakGoal;
    private CardView cardBestStreak;
    private TextView tvTodaySteps;
    private TextView tvCurrentStreak;
    private TextView tvStreakDate;
    private TextView tvBestStreak;
    private TextView tvBestStreakDates; // ✅ 新增：Best Streak 日期范围
    private ImageView ivTodayCheck; // ✅ Today 区域的勾

    private StreakViewModel viewModel;
    private List<StreakHistoryEntity> monthData;
    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streak, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        // ✅ 观察 Best Streak - 数据库变化时自动更新
        viewModel.getBestStreakLiveData()
                .observe(getViewLifecycleOwner(), best -> {
                    if (best != null && best > 0) {
                        tvBestStreak.setText(best + " days");
                    } else {
                        tvBestStreak.setText("0 days");
                    }
                });

        // ✅ 观察并计算 Best Streak 的日期范围
        loadBestStreakDateRange();

        String yearMonth = String.format("%04d-%02d", currentYear, currentMonth);

        // ✅ 观察当月数据 - 日历会实时更新颜色
        viewModel.getMonthStreakLive(yearMonth).observe(getViewLifecycleOwner(), list -> {
            monthData = list != null ? list : List.of();
            initializeCalendar(); // 重绘日历
            loadBestStreakDateRange(); // ✅ 同时更新 Best Streak 日期范围
        });

        // ✅ 观察 Current Streak - 实时更新
        viewModel.getStreakLiveData().observe(getViewLifecycleOwner(), currentResult -> {
            if (currentResult != null && currentResult.streakCount > 0) {
                tvCurrentStreak.setText(String.format("CURRENT STREAK: %d DAYS", currentResult.streakCount));
            } else {
                tvCurrentStreak.setText("CURRENT STREAK: 0 DAYS");
            }
        });

        // ✅ 观察今日步数 - 实时更新步数和勾的图标
        viewModel.getLiveStepsFromStreakEntity().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                // 更新步数显示
                tvTodaySteps.setText(String.format("Steps: %d/%d", result.steps, result.minStepsRequired));

                // ✅ 根据达标状态切换图标
                if (result.achieved) {
                    // 达标：显示绿色圆底白勾
                    ivTodayCheck.setImageResource(R.drawable.streak_ic_check_green_circle);
                    tvTodaySteps.setTextColor(Color.parseColor("#4CAF50")); // 绿色文字
                } else {
                    // 未达标：显示灰色圆底白勾
                    ivTodayCheck.setImageResource(R.drawable.streak_ic_check_gray_circle);
                    tvTodaySteps.setTextColor(Color.parseColor("#999999")); // 灰色文字
                }
            } else {
                tvTodaySteps.setText("Steps: 0/0");
                ivTodayCheck.setImageResource(R.drawable.streak_ic_check_gray_circle);
                tvTodaySteps.setTextColor(Color.parseColor("#999999"));
            }
        });

        // 按钮导航
        btnChangeStreakGoal.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_streakFragment_to_changeStreakFragment)
        );

        cardBestStreak.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_streakFragment_to_bestStreakDetailFragment)
        );

        // ✅ 监听从 ChangeStreakFragment 返回的数据
        getParentFragmentManager().setFragmentResultListener("streak_goal_update",
                getViewLifecycleOwner(), (requestKey, bundle) -> {
                    // LiveData 会自动触发更新，这里不需要额外操作
                });
    }

    private void initViews(View view) {
        calendarGrid = view.findViewById(R.id.calendarGrid);
        btnChangeStreakGoal = view.findViewById(R.id.btnChangeStreakGoal);
        cardBestStreak = view.findViewById(R.id.cardBestStreak);
        tvTodaySteps = view.findViewById(R.id.tvTodaySteps);
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvStreakDate = view.findViewById(R.id.tvStreakDate);
        tvBestStreak = view.findViewById(R.id.tvBestStreak);
        tvBestStreakDates = view.findViewById(R.id.tvBestStreakDates); // ✅ 新增
        ivTodayCheck = view.findViewById(R.id.ivTodayCheck); // ✅ 直接通过 id 获取

        // 设置今天的日期显示
        LocalDate today = LocalDate.now();
        String monthName = today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        tvStreakDate.setText(String.format("Today: %s %d", monthName, today.getDayOfMonth()));
    }

    private void initializeCalendar() {
        // 保留前 7 个 header（星期标题），移除其他日期格子
        int childCount = calendarGrid.getChildCount();
        if (childCount > 7) {
            calendarGrid.removeViews(7, childCount - 7);
        }

        LocalDate firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
        int startDay = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = firstOfMonth.lengthOfMonth();

        // 添加空白格子（第一天之前）
        for (int i = 0; i < startDay; i++) {
            TextView empty = new TextView(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 120; // 固定高度
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
            params.height = 120; // 固定高度
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
    }

    private TextView createDayView(int day) {
        TextView view = new TextView(requireContext());
        view.setText(String.valueOf(day));
        view.setGravity(Gravity.CENTER);
        view.setPadding(16, 16, 16, 16);
        view.setTextSize(16);
        view.setTypeface(null, android.graphics.Typeface.BOLD); // ✅ 修复：使用 setTypeface

        String date = String.format("%04d-%02d-%02d", currentYear, currentMonth, day);

        boolean achieved = false;
        boolean hasData = false;

        // ✅ 检查这一天是否有数据和是否达标
        if (monthData != null) {
            for (StreakHistoryEntity e : monthData) {
                if (e.date.equals(date)) {
                    achieved = e.achieved;
                    hasData = true;
                    break;
                }
            }
        }

        // ✅ 根据状态设置颜色
        if (!hasData) {
            // 没有数据：灰色背景
            view.setBackgroundResource(R.drawable.streak_calendar_day_inactive);
            view.setTextColor(Color.parseColor("#999999"));
        } else if (achieved) {
            // 达标：绿色背景
            view.setBackgroundResource(R.drawable.streak_calendar_day_completed);
            view.setTextColor(Color.WHITE);
        } else {
            // 有数据但未达标：灰色背景
            view.setBackgroundResource(R.drawable.streak_calendar_day_inactive);
            view.setTextColor(Color.parseColor("#999999"));
        }

        // 点击跳转到详情页
        view.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("date", date);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_streakFragment_to_dayDetailFragment, args);
        });

        return view;
    }

    /**
     * ✅ 加载 Best Streak 的日期范围
     */
    private void loadBestStreakDateRange() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            List<StreakHistoryEntity> allData = viewModel.getAllStreakData();

            if (allData == null || allData.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    tvBestStreakDates.setText("No records");
                });
                return;
            }

            // 按日期排序
            allData.sort((a, b) -> a.date.compareTo(b.date));

            // 找出最长连续 streak
            int maxCount = 0;
            int maxStartIndex = 0;
            int maxEndIndex = 0;
            int currentCount = 0;
            int currentStartIndex = 0;

            for (int i = 0; i < allData.size(); i++) {
                StreakHistoryEntity current = allData.get(i);

                if (current.achieved) {
                    if (currentCount == 0) {
                        currentStartIndex = i;
                    }
                    currentCount++;

                    boolean isLastDay = (i == allData.size() - 1);
                    boolean nextDayNotConsecutive = false;

                    if (!isLastDay) {
                        try {
                            LocalDate currentDate = LocalDate.parse(current.date);
                            LocalDate nextDate = LocalDate.parse(allData.get(i + 1).date);
                            nextDayNotConsecutive = !nextDate.equals(currentDate.plusDays(1))
                                    || !allData.get(i + 1).achieved;
                        } catch (Exception e) {
                            nextDayNotConsecutive = true;
                        }
                    }

                    if (isLastDay || nextDayNotConsecutive) {
                        if (currentCount > maxCount) {
                            maxCount = currentCount;
                            maxStartIndex = currentStartIndex;
                            maxEndIndex = i;
                        }
                        currentCount = 0;
                    }
                } else {
                    currentCount = 0;
                }
            }

            if (maxCount > 0) {
                String startDate = allData.get(maxStartIndex).date;
                String endDate = allData.get(maxEndIndex).date;

                try {
                    LocalDate start = LocalDate.parse(startDate);
                    LocalDate end = LocalDate.parse(endDate);

                    String startMonth = start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    String endMonth = end.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

                    String dateRange;
                    if (start.getMonth() == end.getMonth()) {
                        dateRange = String.format("%s %d - %d", startMonth, start.getDayOfMonth(), end.getDayOfMonth());
                    } else {
                        dateRange = String.format("%s %d - %s %d",
                                startMonth, start.getDayOfMonth(),
                                endMonth, end.getDayOfMonth());
                    }

                    String finalDateRange = dateRange;
                    requireActivity().runOnUiThread(() -> {
                        tvBestStreakDates.setText(finalDateRange);
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        tvBestStreakDates.setText(startDate + " - " + endDate);
                    });
                }
            } else {
                requireActivity().runOnUiThread(() -> {
                    tvBestStreakDates.setText("No streak");
                });
            }
        });
    }
}
