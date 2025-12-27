package com.example.madgroupproject.ui.statspage;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.repository.StatisticsRepository;
import com.example.madgroupproject.data.viewmodel.StatisticsViewModel;
import com.google.android.material.tabs.TabLayout;

public class StatsFragment extends Fragment {

    // ===== EXISTING FIELDS (UNCHANGED) =====
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

    // ===== ADDED (REQUIRED FOR TABS) =====
    private FrameLayout frameLayout;
    private TabLayout tabLayout;

    public StatsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // ===== ADDED: TabLayout & FrameLayout init =====
        frameLayout = view.findViewById(R.id.FrameLayoutStats);
        tabLayout = view.findViewById(R.id.TabLayoutStats);

        // Load default fragment (Weekly)
        if (savedInstanceState == null) {
            loadChildFragment(new stats_weekly());
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;

                if (tab.getPosition() == 0) {
                    fragment = new stats_weekly();
                } else {
                    fragment = new stats_monthly();
                }

                loadChildFragment(fragment);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // ===== EXISTING CODE (UNCHANGED) =====
        dailySteps = view.findViewById(R.id.dailySteps);
        dailyDistance = view.findViewById(R.id.dailyDistance);
        dailyCalories = view.findViewById(R.id.dailyCalories);
        monthlyTotalSteps = view.findViewById(R.id.monthlyTotalSteps);
        monthlyTotalDistance = view.findViewById(R.id.monthlyTotalDistance);
        monthlyTotalCalories = view.findViewById(R.id.monthlyTotalCalories);
        monthlyAvgSteps = view.findViewById(R.id.monthlyAvgSteps);
        monthlyAvgDistance = view.findViewById(R.id.monthlyAvgDistance);
        monthlyAvgCalories = view.findViewById(R.id.monthlyAvgCalories);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        viewModel.setSelectedDate("2025-12-24");

        viewModel.getDailyStats().observe(
                getViewLifecycleOwner(),
                result -> {
                    if (result != null && !result.isEmpty() && result.get(0) != null) {
                        FitnessDataEntity stats = result.get(0);
                        dailySteps.setText("Daily Steps: " + stats.steps);
                        dailyDistance.setText("Daily Distance: " + stats.distanceMeters);
                        dailyCalories.setText("Daily Calories Burnt: " + stats.calories);
                    } else {
                        dailySteps.setText("Daily Steps: -");
                        dailyDistance.setText("Daily Distance: -");
                        dailyCalories.setText("Daily Calories Burnt: -");
                    }
                }
        );

        viewModel.setSelectedMonth("2025-12");
        viewModel.getMonthlyTotalStats().observe(
                getViewLifecycleOwner(),
                result -> {
                    if (result != null && !result.isEmpty() && result.get(0) != null) {
                        StatisticsRepository.MonthlyTotalStats stats = result.get(0);
                        monthlyTotalSteps.setText("Monthly Total Steps: " + stats.steps);
                        monthlyTotalDistance.setText("Monthly Total Distance: " + stats.distanceMeters);
                        monthlyTotalCalories.setText("Monthly Total Calories Burnt: " + stats.calories);
                    } else {
                        monthlyTotalSteps.setText("Monthly Total Steps: -");
                        monthlyTotalDistance.setText("Monthly Total Distance: -");
                        monthlyTotalCalories.setText("Monthly Total Calories Burnt: -");
                    }
                }
        );

        viewModel.getMonthlyAverageStats().observe(
                getViewLifecycleOwner(),
                result -> {
                    if (result != null && !result.isEmpty() && result.get(0) != null) {
                        StatisticsRepository.MonthlyAverageStats stats = result.get(0);
                        monthlyAvgSteps.setText("Monthly Avg Steps: " + stats.avgSteps);
                        monthlyAvgDistance.setText("Monthly Avg Distance: " + stats.avgDistance);
                        monthlyAvgCalories.setText("Monthly Avg Calories Burnt: " + stats.avgCalories);
                    } else {
                        monthlyAvgSteps.setText("Monthly Avg Steps: -");
                        monthlyAvgDistance.setText("Monthly Avg Distance: -");
                        monthlyAvgCalories.setText("Monthly Avg Calories Burnt: -");
                    }
                }
        );
    }

    // ===== ADDED: helper method (no existing code touched) =====
    private void loadChildFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.FrameLayoutStats, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
