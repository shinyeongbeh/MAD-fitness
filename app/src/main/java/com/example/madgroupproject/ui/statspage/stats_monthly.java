package com.example.madgroupproject.ui.statspage;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.repository.StatisticsRepository;
import com.example.madgroupproject.data.viewmodel.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class stats_monthly extends Fragment {

    private TextView tvMonthlyTotalSteps, tvMonthlyTotalDistance, tvMonthlyTotalCalories;
    private TextView tvMonthlyAvgSteps, tvMonthlyAvgDistance, tvMonthlyAvgCalories;
    private Spinner spinnerMonth;
    private BarChart barChartMonthly;
    private StatisticsViewModel viewModel;

    public stats_monthly() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_monthly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        tvMonthlyTotalSteps = view.findViewById(R.id.tvMonthlyTotalSteps);
        tvMonthlyTotalDistance = view.findViewById(R.id.tvMonthlyTotalDistance);
        tvMonthlyTotalCalories = view.findViewById(R.id.tvMonthlyTotalCalories);
        tvMonthlyAvgSteps = view.findViewById(R.id.tvMonthlyAvgSteps);
        tvMonthlyAvgDistance = view.findViewById(R.id.tvMonthlyAvgDistance);
        tvMonthlyAvgCalories = view.findViewById(R.id.tvMonthlyAvgCalories);
        barChartMonthly = view.findViewById(R.id.barChartMonthly);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = new SimpleDateFormat("MMMM", Locale.getDefault())
                    .format(new Calendar.Builder().set(Calendar.MONTH, i).build().getTime());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        int currentMonthIndex = calendar.get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonthIndex);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int year = calendar.get(Calendar.YEAR);
                int month = position + 1; // Calendar month is 0-based
                String formattedMonth = String.format(Locale.getDefault(), "%04d-%02d", year, month);
                viewModel.setSelectedMonth(formattedMonth);

                loadMonthlyTotalStats();
                loadMonthlyAverageStats();
                loadMonthlyBarChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadMonthlyTotalStats();
        loadMonthlyAverageStats();
        loadMonthlyBarChart();
    }

    private void loadMonthlyTotalStats() {
        viewModel.getMonthlyTotalStats().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty() && result.get(0) != null) {
                StatisticsRepository.MonthlyTotalStats stats = result.get(0);
                tvMonthlyTotalSteps.setText(String.valueOf(stats.steps));
                tvMonthlyTotalDistance.setText(stats.distanceMeters + " km");
                tvMonthlyTotalCalories.setText(stats.calories + " kcal");
            } else {
                tvMonthlyTotalSteps.setText("-");
                tvMonthlyTotalDistance.setText("- km");
                tvMonthlyTotalCalories.setText("- kcal");
            }
        });
    }

    private void loadMonthlyAverageStats() {
        viewModel.getMonthlyAverageStats().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty() && result.get(0) != null) {
                StatisticsRepository.MonthlyAverageStats stats = result.get(0);
                tvMonthlyAvgSteps.setText(String.valueOf(stats.avgSteps));
                tvMonthlyAvgDistance.setText(stats.avgDistance + " km");
                tvMonthlyAvgCalories.setText(stats.avgCalories + " kcal");
            } else {
                tvMonthlyAvgSteps.setText("-");
                tvMonthlyAvgDistance.setText("- km");
                tvMonthlyAvgCalories.setText("- kcal");
            }
        });
    }

    private void loadMonthlyBarChart() {
        viewModel.getMonthlyDailyStats().observe(getViewLifecycleOwner(), dailyStats -> {
            if (dailyStats != null && !dailyStats.isEmpty()) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                for (int i = 0; i < dailyStats.size(); i++) {
                    FitnessDataEntity data = dailyStats.get(i);
                    entries.add(new BarEntry(i, data.steps));


                    labels.add(new SimpleDateFormat("dd", Locale.getDefault()).format(data.date));
                }

                BarDataSet dataSet = new BarDataSet(entries, "Steps");
                dataSet.setColor(Color.parseColor("#81C784"));
                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.9f);

                barChartMonthly.setData(barData);
                barChartMonthly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                barChartMonthly.getXAxis().setGranularity(1f);
                barChartMonthly.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChartMonthly.getAxisRight().setEnabled(false);
                barChartMonthly.getDescription().setEnabled(false);
                barChartMonthly.invalidate();
            } else {
                barChartMonthly.clear();
            }
        });
    }
}
