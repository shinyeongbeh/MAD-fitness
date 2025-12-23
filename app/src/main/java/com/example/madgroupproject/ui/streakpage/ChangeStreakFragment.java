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

import java.time.LocalDate;

public class ChangeStreakFragment extends Fragment {

    private TextView tvCurrentStreak;
    private EditText etNewStreak;
    private Button btnChange, btnCancel;

    private StreakPreferenceManager streakManager;
    private int currentGoal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        streakManager = new StreakPreferenceManager(requireContext());
        currentGoal = streakManager.getDailyGoal();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.streak_change_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        etNewStreak = view.findViewById(R.id.etNewStreak);
        btnChange = view.findViewById(R.id.btnChange);
        btnCancel = view.findViewById(R.id.btnCancel);

        tvCurrentStreak.setText(currentGoal + " steps daily");

        btnChange.setOnClickListener(v -> {
            String input = etNewStreak.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a goal", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int newGoal = Integer.parseInt(input);
                if (newGoal <= 0) throw new NumberFormatException();

                // ✅ 保存新目标到 SharedPreferences
                streakManager.setDailyGoal(newGoal);

                StreakViewModel viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

                // ✅ 更新数据库中所有天数的目标和 achieved 状态
                // 注意：这里不传 date，而是更新所有记录
                viewModel.updateMinSteps(LocalDate.now().toString(), newGoal);

                Toast.makeText(requireContext(), "Goal updated! Recalculating all streaks...", Toast.LENGTH_SHORT).show();

                // ✅ 发送结果回 StreakFragment
                Bundle result = new Bundle();
                result.putInt("newGoal", newGoal);
                getParentFragmentManager().setFragmentResult("streak_goal_update", result);

                // ✅ 延迟返回，给数据库时间更新
                view.postDelayed(() -> {
                    NavHostFragment.findNavController(this).navigateUp();
                }, 500);

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }
}