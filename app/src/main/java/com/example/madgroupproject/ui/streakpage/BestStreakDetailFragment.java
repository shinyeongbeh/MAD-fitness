package com.example.madgroupproject.ui.streakpage;

import android.os.Bundle;
import android.util.Log;
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
import com.example.madgroupproject.data.repository.StreakRepository;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class BestStreakDetailFragment extends Fragment {
    private static final String TAG = "BestStreakDetail";

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

        // 使用 LiveData 实时观察 Best Streak 变化
        setupObserver();
    }

    private void setupObserver() {
        viewModel.getLongestStreakWithDetailsLiveData().observe(getViewLifecycleOwner(), result -> {
            try {
                if (result != null && !result.isEmpty()) {
                    // 显示天数
                    tvStreakCount.setText(result.count + " Days");

                    // 显示日期范围
                    String dateRange = formatDateRange(result.startDate, result.endDate);
                    tvStreakDates.setText(dateRange);

                    // 显示 Achievement Summary
                    String summaryText = String.format(
                            "• Maintained goal on %d days\n\n• Total steps: %,d\n\n• Average daily steps: %,d\n\n• Consistency: 100%%",
                            result.count, result.totalSteps, result.avgSteps
                    );
                    tvAchSummary.setText(summaryText);
                } else {
                    tvStreakCount.setText("0 Days");
                    tvStreakDates.setText("No streak records");
                    tvAchSummary.setText("Start tracking your steps to build a streak!");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating best streak detail UI", e);
                tvStreakCount.setText("0 Days");
                tvStreakDates.setText("Error loading");
                tvAchSummary.setText("Failed to load streak details");
            }
        });
    }

    private String formatDateRange(String startDateStr, String endDateStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            String startMonth = startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String endMonth = endDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            if (startDate.getMonth() == endDate.getMonth() && startDate.getYear() == endDate.getYear()) {
                return String.format("%s %d - %d, %d",
                        startMonth, startDate.getDayOfMonth(),
                        endDate.getDayOfMonth(), startDate.getYear());
            } else if (startDate.getYear() == endDate.getYear()) {
                return String.format("%s %d - %s %d, %d",
                        startMonth, startDate.getDayOfMonth(),
                        endMonth, endDate.getDayOfMonth(),
                        startDate.getYear());
            } else {
                return String.format("%s %d, %d - %s %d, %d",
                        startMonth, startDate.getDayOfMonth(), startDate.getYear(),
                        endMonth, endDate.getDayOfMonth(), endDate.getYear());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date range", e);
            return startDateStr + " - " + endDateStr;
        }
    }
}


