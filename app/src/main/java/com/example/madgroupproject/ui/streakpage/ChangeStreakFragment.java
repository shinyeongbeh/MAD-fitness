package com.example.madgroupproject.ui.streakpage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.StreakPreferenceManager;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;

public class ChangeStreakFragment extends Fragment {

    private TextView tvCurrentStreak;
    private EditText etNewStreak;
    private Button btnChange, btnCancel;

    private StreakPreferenceManager streakManager;
    private StreakViewModel viewModel;
    private int currentGoal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        streakManager = new StreakPreferenceManager(requireContext());
        currentGoal = streakManager.getDailyGoal();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streak_change_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        etNewStreak = view.findViewById(R.id.etNewStreak);
        btnChange = view.findViewById(R.id.btnChange);
        btnCancel = view.findViewById(R.id.btnCancel);

        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        tvCurrentStreak.setText(currentGoal + " steps daily");

        btnChange.setOnClickListener(v -> handleChangeGoal());
        btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    private void handleChangeGoal() {
        String input = etNewStreak.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a goal", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int newGoal = Integer.parseInt(input);

            if (newGoal <= 0) {
                Toast.makeText(requireContext(), "Goal must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // 禁用按钮防止重复点击
            btnChange.setEnabled(false);
            btnCancel.setEnabled(false);

            // 使用回调确认操作完成
            viewModel.updateMinSteps(newGoal, (success, errorMessage) -> {
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(requireContext(),
                                "Goal updated successfully!",
                                Toast.LENGTH_SHORT).show();

                        // LiveData 会自动更新，直接返回
                        NavHostFragment.findNavController(this).navigateUp();
                    } else {
                        Toast.makeText(requireContext(),
                                "Failed to update goal: " + (errorMessage != null ? errorMessage : "Unknown error"),
                                Toast.LENGTH_LONG).show();

                        // 重新启用按钮
                        btnChange.setEnabled(true);
                        btnCancel.setEnabled(true);
                    }
                });
            });

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }
}