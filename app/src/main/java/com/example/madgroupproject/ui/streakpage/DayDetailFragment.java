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

    private StreakViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.streak_day_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView title = view.findViewById(R.id.tvDayTitle);
        Button back = view.findViewById(R.id.btnBack);

        TextView tvSteps = view.findViewById(R.id.tvSteps);
        TextView tvGoal = view.findViewById(R.id.tvGoal);
        TextView tvStatus = view.findViewById(R.id.tvStatus);

        // 检查控件是否为 null
        if (title == null) Log.e("DayDetailFragment", "tvDayTitle is null!");
        if (tvSteps == null) Log.e("DayDetailFragment", "tvSteps is null!");
        if (tvGoal == null) Log.e("DayDetailFragment", "tvGoal is null!");
        if (tvStatus == null) Log.e("DayDetailFragment", "tvStatus is null!");
        if (back == null) Log.e("DayDetailFragment", "btnBack is null!");

        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        Bundle args = getArguments();
        String dateStr = (args != null) ? args.getString("date", LocalDate.now().toString()) : LocalDate.now().toString();

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            parsedDate = LocalDate.now();
        }

        if (title != null) {
            title.setText(parsedDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                    + " " + parsedDate.getDayOfMonth());
        }

        if (back != null) {
            back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        }

        viewModel.getStreakByDate(dateStr).observe(getViewLifecycleOwner(), streak -> {
            if (tvSteps != null && tvGoal != null && tvStatus != null) {
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
            }
        });
    }
}

