package com.example.madgroupproject.ui.statspage;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class stats_weekly extends Fragment {

    private TextView tvDailySteps, tvDailyDistance, tvDailyCalories;
    private BarChart barChartWeekly;
    private StatisticsViewModel viewModel;

    public stats_weekly() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_weekly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDailySteps = view.findViewById(R.id.tvDailySteps);
        tvDailyDistance = view.findViewById(R.id.tvDailyDistance);
        tvDailyCalories = view.findViewById(R.id.tvDailyCalories);
        barChartWeekly = view.findViewById(R.id.weeklyBarChart);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // Get current week (Sunday → Saturday)
        Calendar calendar = Calendar.getInstance();
        int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_WEEK, - (todayDayOfWeek - Calendar.SUNDAY)); // go to Sunday

        List<String> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            weekDates.add(dateStr);
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        loadWeeklyBarChart(weekDates);

        // Load today's stats
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        viewModel.setSelectedDate(todayStr);
        viewModel.getDailyStats().observe(getViewLifecycleOwner(), dataList -> {
            if (dataList != null && !dataList.isEmpty()) {
                FitnessDataEntity data = dataList.get(0);
                tvDailySteps.setText(String.valueOf(data.steps));
                tvDailyDistance.setText(data.distanceMeters + " km");
                tvDailyCalories.setText(data.calories + " kcal");
            } else {
                tvDailySteps.setText("0");
                tvDailyDistance.setText("0 km");
                tvDailyCalories.setText("0 kcal");
            }
        });
    }

    private void loadWeeklyBarChart(List<String> weekDates) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat labelFormat = new SimpleDateFormat("EEE", Locale.getDefault()); // Mon, Tue, etc.

        for (int i = 0; i < weekDates.size(); i++) {
            String dateStr = weekDates.get(i);
            try {
                Date dateObj = originalFormat.parse(dateStr);
                labels.add(labelFormat.format(dateObj));
            } catch (ParseException e) {
                e.printStackTrace();
                labels.add("-"); // fallback
            }

            final int index = i;
            viewModel.setSelectedDate(dateStr);
            viewModel.getDailyStats().observe(getViewLifecycleOwner(), dataList -> {
                int steps = 0;
                if (dataList != null && !dataList.isEmpty() && dataList.get(0) != null) {
                    steps = dataList.get(0).steps;
                }

                // Update or add entry
                if (entries.size() > index) {
                    entries.set(index, new BarEntry(index, steps));
                } else {
                    entries.add(new BarEntry(index, steps));
                }

                // Draw chart
                BarDataSet dataSet = new BarDataSet(entries, "Steps");
                dataSet.setColor(Color.parseColor("#81C784"));
                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.9f);

                barChartWeekly.setData(barData);
                barChartWeekly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                barChartWeekly.getXAxis().setGranularity(1f);
                barChartWeekly.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChartWeekly.getAxisRight().setEnabled(false);
                barChartWeekly.getAxisLeft().setAxisMinimum(0f); // Y-axis starts from 0
                barChartWeekly.getDescription().setEnabled(false);
                barChartWeekly.invalidate();
            });
        }
    }
}
