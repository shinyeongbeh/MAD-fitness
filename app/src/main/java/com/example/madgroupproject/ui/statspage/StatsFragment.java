package com.example.madgroupproject.ui.statspage;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.repository.StatisticsRepository;
import com.example.madgroupproject.data.viewmodel.StatisticsViewModel;

public class StatsFragment extends Fragment {
    private TextView dailySteps;
    private TextView dailyDistance;
    private TextView dailyCalories;
    private TextView monthlyTotalSteps;
    private TextView monthlyTotalDistance;
    private TextView monthlyTotalCalories;
    private TextView monthlyAvgSteps;
    private TextView monthlyAvgDistance;
    private TextView monthlyAvgCalories;
    private StatisticsViewModel viewModel;

    public StatsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dailySteps = view.findViewById(R.id.dailySteps);
        dailyDistance = view.findViewById(R.id.dailyDistance);
        dailyCalories = view.findViewById(R.id.dailyCalories);
        monthlyTotalSteps = view.findViewById(R.id.monthlyTotalSteps);
        monthlyTotalDistance = view.findViewById(R.id.monthlyTotalDistance);
        monthlyTotalCalories = view.findViewById(R.id.monthlyTotalCalories);
        monthlyAvgSteps = view.findViewById(R.id.monthlyAvgSteps);
        monthlyAvgDistance = view.findViewById(R.id.monthlyAvgDistance);
        monthlyAvgCalories = view.findViewById(R.id.monthlyAvgCalories);

        // initialize view model
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);


        // TODO: transform to any Java methods that suits the UI
        // You are encouraged to move the codes to another method for better readability

        // lets say user choose to view the statistics on 24/12/2025
        // need to set the day in view model first
        // TODO: currently date is hardcoded, change it so that it can receives data from user whether they want which date (etc. user press 22-12-2025 -> set date to the date)
        viewModel.setSelectedDate("2025-12-24"); // yyyy-MM-dd

        // then get the daily data from the view model
        // here use observe so that when DB changes, UI changes
        viewModel.getDailyStats().observe(
          getViewLifecycleOwner(),
                result -> {
                    if (result != null && !result.isEmpty() && result.get(0) != null) {
                        FitnessDataEntity stats = result.get(0); // get latest live data
                        dailySteps.setText("Daily Steps: "+String.valueOf(stats.steps));
                        dailyDistance.setText("Daily Distance: "+String.valueOf(stats.distanceMeters));
                        dailyCalories.setText("Daily Calories Burnt: "+String.valueOf(stats.calories));
                    } else {
                        dailySteps.setText("Daily Steps: -");
                        dailyDistance.setText("Daily Distance: -");
                        dailyCalories.setText("Daily Calories Burnt: -");
                    }
                }
        );
        // TODO: currently month is hardcoded, change it so that it can receives data from user whether they want which month
        viewModel.setSelectedMonth("2025-12");
        viewModel.getMonthlyTotalStats().observe(
                getViewLifecycleOwner(),
                result -> {
                    if (result != null && !result.isEmpty() && result.get(0) != null) {
                        StatisticsRepository.MonthlyTotalStats stats = result.get(0); // get latest live data
                        monthlyTotalSteps.setText("Monthly Total Steps: "+String.valueOf(stats.steps));
                        monthlyTotalDistance.setText("Monthly Total Distance: "+String.valueOf(stats.distanceMeters));
                        monthlyTotalCalories.setText("Monthly Total Calories Burnt: "+String.valueOf(stats.calories));
                    } else {
                        monthlyTotalSteps.setText("Monthly Total Steps: -");
                        monthlyTotalDistance.setText("Monthly Total Distance: -");
                        monthlyTotalCalories.setText("Monthly Total Calories Burnt: -");
                    }
                }
        );

        // TODO: currently month is hardcoded, change it so that it can receives data from user whether they want which month
        viewModel.setSelectedMonth("2025-12");
        viewModel.getMonthlyAverageStats().observe(
                getViewLifecycleOwner(),
                result -> {
                    if (result != null && !result.isEmpty() && result.get(0) != null) {
                        StatisticsRepository.MonthlyAverageStats stats = result.get(0); // get latest live data
                        monthlyAvgSteps.setText("Monthly Avg Steps: "+String.valueOf(stats.avgSteps));
                        monthlyAvgDistance.setText("Monthly Avg Distance: "+String.valueOf(stats.avgDistance));
                        monthlyAvgCalories.setText("Monthly Avg Calories Burnt: "+String.valueOf(stats.avgCalories));
                    } else {
                        monthlyAvgSteps.setText("Monthly Avg Steps: -");
                        monthlyAvgDistance.setText("Monthly Avg Distance: -");
                        monthlyAvgCalories.setText("Monthly Avg Calories Burnt: -");
                    }

                }
        );
    }
}