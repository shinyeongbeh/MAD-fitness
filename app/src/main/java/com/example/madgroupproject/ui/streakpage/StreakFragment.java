package com.example.madgroupproject.ui.streakpage;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;

import java.time.LocalDate;

public class StreakFragment extends Fragment {

    private GridLayout calendarGrid;
    private Button btnChangeStreakGoal;
    private CardView cardBestStreak;
    private TextView tvTodaySteps;
    private TextView tvCurrentStreak;
    private TextView tvStreakDate;

    private StreakViewModel viewModel;

    // Hardcoded completed days (5-8 Feb)
    private boolean[] completedDays = new boolean[29];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (int i = 5; i <= 8; i++) {
            completedDays[i - 1] = true;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streak, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initializeCalendar();

        // Set up navigation
        btnChangeStreakGoal.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_streakFragment_to_changeStreakFragment)
        );

        cardBestStreak.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_streakFragment_to_bestStreakDetailFragment)
        );

        // View Model defined here, connect to the UI components
        // These UI components should update live when database changes
        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);
        // Listen for step changes
        viewModel.getStreakLiveData().observe(getViewLifecycleOwner(), result -> {
            if(result!=null) {
                tvCurrentStreak.setText(String.format("CURRENT STREAK: %d DAYS", result.streakCount));
            } else {
                tvCurrentStreak.setText("CURRENT STREAK: 0 DAYS");
            }
        });

        viewModel.getLiveStepsFromStreakEntity().observe(getViewLifecycleOwner(), result -> {
            if(result!=null) {
                tvTodaySteps.setText(String.format("Steps: %d/%d", result.steps, result.minStepsRequired));
            } else {
                tvTodaySteps.setText("Steps: 0");
            }
        });
    }

    private void initViews(View view) {
        calendarGrid = view.findViewById(R.id.calendarGrid);
        btnChangeStreakGoal = view.findViewById(R.id.btnChangeStreakGoal);
        cardBestStreak = view.findViewById(R.id.cardBestStreak);
        tvTodaySteps = view.findViewById(R.id.tvTodaySteps);
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvStreakDate = view.findViewById(R.id.tvStreakDate);
        tvStreakDate.setText(LocalDate.now().toString());
    }

    private void initializeCalendar() {
        calendarGrid.removeAllViews();

        // Add weekday headers (S M T W T F S) — assume already in XML
        // Or add programmatically if needed

        int startDay = 6; // Feb 2025 starts on Saturday
        int daysInMonth = 28;

        // Add empty cells before day 1
        for (int i = 0; i < startDay; i++) {
            TextView empty = new TextView(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i, 1f);
            params.rowSpec = GridLayout.spec(1);
            empty.setLayoutParams(params);
            calendarGrid.addView(empty);
        }

        int row = 1, col = startDay;
        for (int day = 1; day <= daysInMonth; day++) {
            TextView dayView = createDayView(day);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(col, 1f);
            params.rowSpec = GridLayout.spec(row);
            params.setMargins(4, 4, 4, 4);
            dayView.setLayoutParams(params);
            calendarGrid.addView(dayView);

            if (++col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private TextView createDayView(int day) {
        TextView view = new TextView(requireContext());
        view.setText(String.valueOf(day));
        view.setGravity(Gravity.CENTER);
        view.setPadding(16, 16, 16, 16);
        view.setTextSize(16);

        if (completedDays[day - 1]) {
            view.setBackgroundResource(R.drawable.streak_calendar_day_completed);
            view.setTextColor(android.graphics.Color.WHITE);
        } else if (day == 1) {
            view.setBackgroundResource(R.drawable.streak_calendar_day_normal);
            view.setTextColor(android.graphics.Color.BLACK);
        } else {
            view.setBackgroundResource(R.drawable.streak_calendar_day_inactive);
            view.setTextColor(android.graphics.Color.BLACK);
        }

        view.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("day", day);
            args.putString("month", "Feb");
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_streakFragment_to_dayDetailFragment, args);
        });

        return view;
    }
}