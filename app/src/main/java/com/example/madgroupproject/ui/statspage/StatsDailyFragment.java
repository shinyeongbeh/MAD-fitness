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
import com.example.madgroupproject.data.viewmodel.StatisticsViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StatsDailyFragment extends Fragment {

    private TextView tvSelectedDate, tvDailySteps, tvDailyCalories, tvDailyDistance;
    private LinearLayout layoutCalendarContainer, layoutDateSelector;
    private ImageView ivDropdown;
    private CalendarView calendarView;
    private StatisticsViewModel viewModel;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_daily, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvDailySteps = view.findViewById(R.id.tvDailySteps);
        tvDailyCalories = view.findViewById(R.id.tvDailyCalories);
        tvDailyDistance = view.findViewById(R.id.tvDailyDistance);
        layoutCalendarContainer = view.findViewById(R.id.layoutCalendarContainer);
        layoutDateSelector = view.findViewById(R.id.layoutDateSelector);
        ivDropdown = view.findViewById(R.id.ivDropdown);
        calendarView = view.findViewById(R.id.calendarViewDaily);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // Show today by default
        Calendar calendar = Calendar.getInstance();
        String today = dateFormat.format(calendar.getTime());
        tvSelectedDate.setText("Today 🔻");
        viewModel.setSelectedDate(today);

        // Observe LiveData for real-time updates
        viewModel.getDailyStats().observe(getViewLifecycleOwner(), this::updateDailyStats);

        // Toggle calendar visibility
        layoutDateSelector.setOnClickListener(v -> {
            if (layoutCalendarContainer.getVisibility() == View.GONE) {
                layoutCalendarContainer.setVisibility(View.VISIBLE);
            } else {
                layoutCalendarContainer.setVisibility(View.GONE);
            }
        });

        // Handle calendar selection
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            String selectedDateStr = dateFormat.format(selected.getTime());
            tvSelectedDate.setText(selectedDateStr + " 🔻");
            viewModel.setSelectedDate(selectedDateStr);
            layoutCalendarContainer.setVisibility(View.GONE);
        });
    }

    private void updateDailyStats(FitnessDataEntity data) {
        if (data != null) {
            tvDailySteps.setText(String.valueOf(data.steps));
            tvDailyDistance.setText(data.distanceMeters + " km");
            tvDailyCalories.setText(data.calories + " kcal");
        } else {
            tvDailySteps.setText("0");
            tvDailyDistance.setText("0 km");
            tvDailyCalories.setText("0 kcal");
        }
    }

    private void updateDailyStats(java.util.List<FitnessDataEntity> dataList) {
        if (dataList != null && !dataList.isEmpty()) {
            updateDailyStats(dataList.get(0));
        } else {
            updateDailyStats((FitnessDataEntity) null);
        }
    }
}
