package com.example.madgroupproject.ui.statspage;

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
import com.example.madgroupproject.data.repository.StatisticsRepository;
import com.example.madgroupproject.data.viewmodel.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsMonthlyFragment extends Fragment {

    private LinearLayout layoutMonthSelector, layoutMonthCalendarContainer;
    private TextView tvSelectedMonth, tvMonthlyTotalSteps, tvMonthlyTotalCalories, tvMonthlyTotalDistance;
    private TextView tvMonthlyAvgSteps, tvMonthlyAvgCalories, tvMonthlyAvgDistance;
    private CalendarView calendarViewMonth;
    private ImageView ivMonthDropdown;
    private BarChart barChartMonthly;

    private Calendar selectedCalendar;
    private StatisticsViewModel viewModel;

    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats_monthly, container, false);

        // ===== Find Views =====
        layoutMonthSelector = view.findViewById(R.id.layoutMonthSelector);
        layoutMonthCalendarContainer = view.findViewById(R.id.layoutMonthCalendarContainer);
        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);
        ivMonthDropdown = view.findViewById(R.id.ivMonthDropdown);
        calendarViewMonth = view.findViewById(R.id.calendarViewMonth);

        tvMonthlyTotalSteps = view.findViewById(R.id.tvMonthlyTotalSteps);
        tvMonthlyTotalCalories = view.findViewById(R.id.tvMonthlyTotalCalories);
        tvMonthlyTotalDistance = view.findViewById(R.id.tvMonthlyTotalDistance);

        tvMonthlyAvgSteps = view.findViewById(R.id.tvMonthlyAvgSteps);
        tvMonthlyAvgCalories = view.findViewById(R.id.tvMonthlyAvgCalories);
        tvMonthlyAvgDistance = view.findViewById(R.id.tvMonthlyAvgDistance);

        barChartMonthly = view.findViewById(R.id.barChartMonthly);

        selectedCalendar = Calendar.getInstance();
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // ===== Month Selector Click =====
        layoutMonthSelector.setOnClickListener(v -> {
            if (layoutMonthCalendarContainer.getVisibility() == View.GONE) {
                layoutMonthCalendarContainer.setVisibility(View.VISIBLE);
            } else {
                layoutMonthCalendarContainer.setVisibility(View.GONE);
            }
        });

        // ===== CalendarView Date Change =====
        calendarViewMonth.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, 1); // first day of month

            String monthText = monthYearFormat.format(selectedCalendar.getTime()) + " 🔻";
            tvSelectedMonth.setText(monthText);
            layoutMonthCalendarContainer.setVisibility(View.GONE);

            loadMonthlyData();
            updateBarChart();
        });

        // ===== Load initial data =====
        String monthText = monthYearFormat.format(selectedCalendar.getTime()) + " 🔻";
        tvSelectedMonth.setText(monthText);

        setupBarChart();
        loadMonthlyData();

        return view;
    }

    // ===== Load Monthly Total & Average =====
    private void loadMonthlyData() {
        String month = monthFormat.format(selectedCalendar.getTime()); // yyyy-MM

        // Monthly totals
        viewModel.getMonthlyTotals(month).observe(getViewLifecycleOwner(), totals -> {
            if (totals != null && !totals.isEmpty()) {
                StatisticsRepository.MonthlyTotalStats total = totals.get(0);

                tvMonthlyTotalSteps.setText(String.valueOf(total.steps));
                tvMonthlyTotalCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", total.calories));
                tvMonthlyTotalDistance.setText(String.format(Locale.getDefault(), "%.2f km", total.distanceMeters / 1000f));
            } else {
                tvMonthlyTotalSteps.setText("0");
                tvMonthlyTotalCalories.setText("0 kcal");
                tvMonthlyTotalDistance.setText("0.00 km");
            }
        });

        // Monthly averages
        viewModel.getMonthlyAverages(month).observe(getViewLifecycleOwner(), averages -> {
            if (averages != null && !averages.isEmpty()) {
                StatisticsRepository.MonthlyAverageStats avg = averages.get(0);

                tvMonthlyAvgSteps.setText(String.valueOf((int) avg.avgSteps));
                tvMonthlyAvgCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", avg.avgCalories));
                tvMonthlyAvgDistance.setText(String.format(Locale.getDefault(), "%.2f km", avg.avgDistance / 1000f));
            } else {
                tvMonthlyAvgSteps.setText("0");
                tvMonthlyAvgCalories.setText("0 kcal");
                tvMonthlyAvgDistance.setText("0.00 km");
            }
        });
    }

    // ===== Setup Bar Chart =====
    private void setupBarChart() {
        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.getAxisRight().setEnabled(false);
        barChartMonthly.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartMonthly.getXAxis().setGranularity(1f);
        barChartMonthly.setFitBars(true);

        barChartMonthly.getXAxis().setValueFormatter(new ValueFormatter() {
            private final String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < months.length) return months[(int) value];
                return "";
            }
        });

        updateBarChart();
    }

    // ===== Update Bar Chart =====
    private void updateBarChart() {
        String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(selectedCalendar.getTime());

        viewModel.getYearlySteps(year).observe(getViewLifecycleOwner(), dataList -> {
            List<BarEntry> entries = new ArrayList<>();

            if (dataList != null) {
                // Sum steps for each month
                for (int i = 0; i < 12; i++) {
                    int monthSteps = 0;

                    for (FitnessDataEntity data : dataList) {
                        try {
                            String[] parts = data.date.split("-"); // "yyyy-MM-dd"
                            int dataYear = Integer.parseInt(parts[0]);
                            int dataMonth = Integer.parseInt(parts[1]) - 1;

                            if (Integer.toString(dataYear).equals(year) && dataMonth == i) {
                                monthSteps += data.steps;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    entries.add(new BarEntry(i, monthSteps));
                }
            }

            BarDataSet set = new BarDataSet(entries, "Steps");
            set.setColor(getResources().getColor(R.color.primary, null));

            BarData barData = new BarData(set);
            barData.setBarWidth(0.5f);

            barChartMonthly.setData(barData);
            barChartMonthly.invalidate();
        });
    }
}
