// ========================================
// 6. DayDetailFragment.java (完整改进版)
// ========================================
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;

import java.time.LocalDate;

public class DayDetailFragment extends Fragment {
    private static final String TAG = "DayDetailFragment";

    private StreakViewModel viewModel;
    private TextView title, tvSteps, tvGoal, tvStatus;
    private Button btnBack;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streak_day_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        // 获取传递的日期参数
        Bundle args = getArguments();
        String dateStr = (args != null) ? args.getString("date", LocalDate.now().toString())
                : LocalDate.now().toString();

        setupUI(dateStr);
        setupObserver(dateStr);
    }

    private void initViews(View view) {
        title = view.findViewById(R.id.tvDayTitle);
        tvSteps = view.findViewById(R.id.tvSteps);
        tvGoal = view.findViewById(R.id.tvGoal);
        tvStatus = view.findViewById(R.id.tvStatus);
        btnBack = view.findViewById(R.id.btnBack);

        // 检查控件是否为 null
        if (title == null) Log.e(TAG, "tvDayTitle is null!");
        if (tvSteps == null) Log.e(TAG, "tvSteps is null!");
        if (tvGoal == null) Log.e(TAG, "tvGoal is null!");
        if (tvStatus == null) Log.e(TAG, "tvStatus is null!");
        if (btnBack == null) Log.e(TAG, "btnBack is null!");

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        }
    }

    private void setupUI(String dateStr) {
        try {
            LocalDate parsedDate = LocalDate.parse(dateStr);
            if (title != null) {
                String monthName = parsedDate.getMonth().getDisplayName(
                        java.time.format.TextStyle.FULL,
                        java.util.Locale.ENGLISH
                );
                title.setText(monthName + " " + parsedDate.getDayOfMonth());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + dateStr, e);
            if (title != null) {
                title.setText("Invalid Date");
            }
        }
    }

    private void setupObserver(String dateStr) {
        viewModel.getStreakByDate(dateStr).observe(getViewLifecycleOwner(), streak -> {
            try {
                if (tvSteps == null || tvGoal == null || tvStatus == null) {
                    Log.e(TAG, "One or more TextViews are null");
                    return;
                }

                if (streak == null) {
                    tvSteps.setText("Steps: 0");
                    tvGoal.setText("Goal: -");
                    tvStatus.setText("Not recorded");
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
                    return;
                }

                tvSteps.setText("Steps: " + streak.steps);
                tvGoal.setText("Goal: " + streak.minStepsRequired);

                if (streak.achieved) {
                    tvStatus.setText("Achieved ✅");
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                } else {
                    tvStatus.setText("Not achieved ❌");
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI with streak data", e);
            }
        });
    }
}

