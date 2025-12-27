package com.example.madgroupproject.ui.statspage;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class stats_weekly extends Fragment {

    private StatisticsViewModel viewModel;
    private FrameLayout weeklyBarChartContainer;
    private TextView tvDailySteps, tvDailyCalories, tvDailyDistance;
    private TextView tvTodayDate, tvTodayDay;

    public stats_weekly() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_weekly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        weeklyBarChartContainer = view.findViewById(R.id.weeklyBarChartContainer);
        tvDailySteps = view.findViewById(R.id.tvDailySteps);
        tvDailyCalories = view.findViewById(R.id.tvDailyCalories);
        tvDailyDistance = view.findViewById(R.id.tvDailyDistance);
        tvTodayDate = view.findViewById(R.id.tvTodayDate);
        tvTodayDay = view.findViewById(R.id.tvTodayDay);

        viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        Calendar calendar = Calendar.getInstance();
        tvTodayDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.getTime()));
        tvTodayDay.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime()));

        setupWeeklyChart(calendar);

        loadDailySummary(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
    }

    private void setupWeeklyChart(Calendar calendar) {
        BarChart barChart = new BarChart(requireContext());
        weeklyBarChartContainer.removeAllViews();
        weeklyBarChartContainer.addView(barChart, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));

        final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        List<BarEntry> entries = new ArrayList<>();

        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0

        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, i + 1);
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.getTime());

            int steps = 0;
            List<FitnessDataEntity> dataList = viewModel.getStatsForDate(dateStr); // create this helper
            if (dataList != null && !dataList.isEmpty() && dataList.get(0) != null) {
                steps = dataList.get(0).steps;
            }

            entries.add(new BarEntry(i, steps));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Steps");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary)); // default
        dataSet.setHighLightColor(getResources().getColor(R.color.accent));

        // Highlight today
        dataSet.getColors().set(todayIndex, getResources().getColor(R.color.accent));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter((value, axis) -> days[(int) value % days.length]);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    private void loadDailySummary(String dateStr) {
        viewModel.setSelectedDate(dateStr);

        viewModel.getDailyStats().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty() && result.get(0) != null) {
                FitnessDataEntity stats = result.get(0);
                tvDailySteps.setText(String.valueOf(stats.steps));
                tvDailyDistance.setText(String.valueOf(stats.distanceMeters) + " m");
                tvDailyCalories.setText(String.valueOf(stats.calories) + " kcal");
            } else {
                tvDailySteps.setText("0");
                tvDailyDistance.setText("0 m");
                tvDailyCalories.setText("0 kcal");
            }
        });
    }
}
