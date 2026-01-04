package com.example.madgroupproject.ui.statspage;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.viewmodel.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsWeeklyFragment extends Fragment {

    private LinearLayout layoutWeekSelector, layoutCalendarContainer;
    private TextView tvSelectedWeek;
    private ImageView ivWeekDropdown;
    private CalendarView calendarViewWeek;

    private TextView tvWeeklyTotalSteps, tvWeeklyTotalCalories, tvWeeklyTotalDistance;
    private TextView tvWeeklyAvgSteps, tvWeeklyAvgCalories, tvWeeklyAvgDistance;
    private BarChart barChartWeekly;

    private StatisticsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_weekly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Find Views =====
        layoutWeekSelector = view.findViewById(R.id.layoutWeekSelector);
        layoutCalendarContainer = view.findViewById(R.id.layoutCalendarContainer);
        tvSelectedWeek = view.findViewById(R.id.tvSelectedWeek);
        ivWeekDropdown = view.findViewById(R.id.ivWeekDropdown);
        calendarViewWeek = view.findViewById(R.id.calendarViewWeek);

        tvWeeklyTotalSteps = view.findViewById(R.id.tvWeeklyTotalSteps);
        tvWeeklyTotalCalories = view.findViewById(R.id.tvWeeklyTotalCalories);
        tvWeeklyTotalDistance = view.findViewById(R.id.tvWeeklyTotalDistance);

        tvWeeklyAvgSteps = view.findViewById(R.id.tvWeeklyAvgSteps);
        tvWeeklyAvgCalories = view.findViewById(R.id.tvWeeklyAvgCalories);
        tvWeeklyAvgDistance = view.findViewById(R.id.tvWeeklyAvgDistance);

        barChartWeekly = view.findViewById(R.id.weeklyBarChart);

        // ===== ViewModel =====
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // ===== Week Selector Click =====
        layoutWeekSelector.setOnClickListener(v -> {
            if (layoutCalendarContainer.getVisibility() == View.GONE) {
                layoutCalendarContainer.setVisibility(View.VISIBLE);
            } else {
                layoutCalendarContainer.setVisibility(View.GONE);
            }
        });

        // ===== CalendarView Date Change =====
        calendarViewWeek.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);

            // Compute start and end of week (Sunday → Saturday)
            Calendar start = (Calendar) selected.clone();
            start.add(Calendar.DAY_OF_WEEK, - (start.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_WEEK, 6);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startStr = sdf.format(start.getTime());
            String endStr = sdf.format(end.getTime());

            tvSelectedWeek.setText(startStr + " → " + endStr + " 🔻");
            layoutCalendarContainer.setVisibility(View.GONE);

            // Load weekly stats and chart
            loadWeeklyStats(startStr, endStr);
            loadWeeklyBarChart(startStr, endStr);
        });

        // ===== Load current week by default =====
        Calendar today = Calendar.getInstance();
        Calendar weekStart = (Calendar) today.clone();
        weekStart.add(Calendar.DAY_OF_WEEK, - (weekStart.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_WEEK, 6);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startStr = sdf.format(weekStart.getTime());
        String endStr = sdf.format(weekEnd.getTime());
        tvSelectedWeek.setText(startStr + " → " + endStr + " 🔻");

        loadWeeklyStats(startStr, endStr);
        loadWeeklyBarChart(startStr, endStr);
    }

    private void loadWeeklyStats(String startDate, String endDate) {
        viewModel.getWeeklyStats(startDate, endDate).observe(getViewLifecycleOwner(), dataList -> {
            int totalSteps = 0;
            float totalDistance = 0f;
            float totalCalories = 0f;

            if (dataList != null && !dataList.isEmpty()) {
                for (FitnessDataEntity data : dataList) {
                    totalSteps += data.steps;
                    totalCalories += data.calories;
                    totalDistance += data.distanceMeters / 1000f; // meters → km
                }

                int days = dataList.size();

                tvWeeklyTotalSteps.setText(String.valueOf(totalSteps));
                tvWeeklyTotalCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", totalCalories));
                tvWeeklyTotalDistance.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance));

                tvWeeklyAvgSteps.setText(String.valueOf(totalSteps / days));
                tvWeeklyAvgCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", totalCalories / days));
                tvWeeklyAvgDistance.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance / days));
            } else {
                tvWeeklyTotalSteps.setText("0");
                tvWeeklyTotalCalories.setText("0 kcal");
                tvWeeklyTotalDistance.setText("0 km");

                tvWeeklyAvgSteps.setText("0");
                tvWeeklyAvgCalories.setText("0 kcal");
                tvWeeklyAvgDistance.setText("0 km");
            }
        });
    }

    private void loadWeeklyBarChart(String startDate, String endDate) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Generate labels for the week
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat labelFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date start = sdf.parse(startDate);
            calendar.setTime(start);

            for (int i = 0; i < 7; i++) {
                labels.add(labelFormat.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_WEEK, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            for (int i = 0; i < 7; i++) labels.add("-");
        }

        // Observe each day
        calendar = Calendar.getInstance();
        try {
            Date start = sdf.parse(startDate);
            calendar.setTime(start);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 7; i++) {
            final String dateStr = sdf.format(calendar.getTime());
            final int index = i;

            viewModel.getWeeklyStats(dateStr, dateStr).observe(getViewLifecycleOwner(), dataList -> {
                int steps = 0;
                if (dataList != null && !dataList.isEmpty() && dataList.get(0) != null) {
                    steps = dataList.get(0).steps;
                }

                if (entries.size() > index) {
                    entries.set(index, new BarEntry(index, steps));
                } else {
                    while (entries.size() < index) entries.add(new BarEntry(entries.size(), 0));
                    entries.add(new BarEntry(index, steps));
                }

                BarDataSet dataSet = new BarDataSet(entries, "Steps");
                dataSet.setColor(Color.parseColor("#81C784"));
                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.9f);

                barChartWeekly.setData(barData);
                barChartWeekly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                barChartWeekly.getXAxis().setGranularity(1f);
                barChartWeekly.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChartWeekly.getAxisRight().setEnabled(false);
                barChartWeekly.getAxisLeft().setAxisMinimum(0f);
                barChartWeekly.getDescription().setEnabled(false);
                barChartWeekly.invalidate();
            });

            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
    }
}
