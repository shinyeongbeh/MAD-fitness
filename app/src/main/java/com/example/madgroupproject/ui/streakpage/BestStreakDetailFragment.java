package com.example.madgroupproject.ui.streakpage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BestStreakDetailFragment extends Fragment {

    private StreakViewModel viewModel;
    private TextView tvStreakCount, tvStreakDates, tvAchSummary;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_best_streak_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStreakCount = view.findViewById(R.id.tvLongestStreakCount);
        tvStreakDates = view.findViewById(R.id.tvLongestStreakDates);
        tvAchSummary = view.findViewById(R.id.tvAchSummary);
        Button btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        // ✅ 使用 LiveData 实时观察 Best Streak 变化
        loadBestStreakDetails();
    }

    private void loadBestStreakDetails() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<StreakHistoryEntity> allData = viewModel.getAllStreakData();

            if (allData == null || allData.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    tvStreakCount.setText("0 Days");
                    tvStreakDates.setText("No streak records");
                    tvAchSummary.setText("Start tracking your steps to build a streak!");
                });
                return;
            }

            // ✅ 找出最长的连续达标 streak
            int maxCount = 0;
            List<StreakHistoryEntity> maxStreakDays = null;
            int currentCount = 0;
            int currentStartIndex = 0;

            // 按日期排序（从旧到新）
            allData.sort((a, b) -> a.date.compareTo(b.date));

            for (int i = 0; i < allData.size(); i++) {
                StreakHistoryEntity current = allData.get(i);

                if (current.achieved) {
                    if (currentCount == 0) {
                        currentStartIndex = i;
                    }
                    currentCount++;

                    // 检查是否是最后一天或下一天不连续
                    boolean isLastDay = (i == allData.size() - 1);
                    boolean nextDayNotConsecutive = false;

                    if (!isLastDay) {
                        try {
                            LocalDate currentDate = LocalDate.parse(current.date);
                            LocalDate nextDate = LocalDate.parse(allData.get(i + 1).date);

                            // 检查下一天是否达标且连续
                            nextDayNotConsecutive = !nextDate.equals(currentDate.plusDays(1))
                                    || !allData.get(i + 1).achieved;
                        } catch (Exception e) {
                            nextDayNotConsecutive = true;
                        }
                    }

                    // 如果当前 streak 结束，检查是否是最长的
                    if (isLastDay || nextDayNotConsecutive) {
                        if (currentCount > maxCount) {
                            maxCount = currentCount;
                            maxStreakDays = allData.subList(currentStartIndex, i + 1);
                        }
                        currentCount = 0;
                    }
                } else {
                    // 未达标天数重置 streak
                    currentCount = 0;
                }
            }

            final List<StreakHistoryEntity> finalMaxStreak = maxStreakDays;
            final int finalMaxCount = maxCount;

            requireActivity().runOnUiThread(() -> {
                if (finalMaxStreak != null && !finalMaxStreak.isEmpty()) {
                    // ✅ 显示天数
                    tvStreakCount.setText(finalMaxCount + " Days");

                    // ✅ 显示日期范围（格式化为可读的日期）
                    try {
                        LocalDate startDate = LocalDate.parse(finalMaxStreak.get(0).date);
                        LocalDate endDate = LocalDate.parse(finalMaxStreak.get(finalMaxStreak.size() - 1).date);

                        String startMonth = startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                        String endMonth = endDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                        String dateRange;
                        if (startDate.getMonth() == endDate.getMonth() && startDate.getYear() == endDate.getYear()) {
                            // 同一个月：January 1 - 30, 2025
                            dateRange = String.format("%s %d - %d, %d",
                                    startMonth, startDate.getDayOfMonth(),
                                    endDate.getDayOfMonth(), startDate.getYear());
                        } else if (startDate.getYear() == endDate.getYear()) {
                            // 同一年不同月：January 28 - February 15, 2025
                            dateRange = String.format("%s %d - %s %d, %d",
                                    startMonth, startDate.getDayOfMonth(),
                                    endMonth, endDate.getDayOfMonth(),
                                    startDate.getYear());
                        } else {
                            // 跨年：December 20, 2024 - January 15, 2025
                            dateRange = String.format("%s %d, %d - %s %d, %d",
                                    startMonth, startDate.getDayOfMonth(), startDate.getYear(),
                                    endMonth, endDate.getDayOfMonth(), endDate.getYear());
                        }

                        tvStreakDates.setText(dateRange);
                    } catch (Exception e) {
                        tvStreakDates.setText(finalMaxStreak.get(0).date + " - " +
                                finalMaxStreak.get(finalMaxStreak.size() - 1).date);
                    }

                    // ✅ 计算 Achievement Summary
                    int totalSteps = 0;
                    for (StreakHistoryEntity e : finalMaxStreak) {
                        totalSteps += e.steps;
                    }
                    int avgSteps = finalMaxCount > 0 ? totalSteps / finalMaxCount : 0;

                    String summaryText = String.format(
                            "• Maintained goal on %d days\n\n• Total steps: %,d\n\n• Average daily steps: %,d\n\n• Consistency: 100%%",
                            finalMaxCount, totalSteps, avgSteps
                    );
                    tvAchSummary.setText(summaryText);
                } else {
                    tvStreakCount.setText("0 Days");
                    tvStreakDates.setText("No consecutive streak found");
                    tvAchSummary.setText("Complete your daily goals to build a streak!");
                }
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // ✅ 每次进入页面都重新加载，确保显示最新数据
        loadBestStreakDetails();
    }
}


