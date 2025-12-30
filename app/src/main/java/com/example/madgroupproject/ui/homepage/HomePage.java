package com.example.madgroupproject.ui.homepage;

import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.GoalEntity;
import com.example.madgroupproject.data.repository.GoalRepository;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;

import java.util.List;

public class HomePage extends Fragment {

    private StreakViewModel streakViewModel;
    private GoalRepository goalRepository;

    private TextView tvStreakNumber;
    private LinearLayout goalsDisplayContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goalRepository = new GoalRepository(requireContext());
        streakViewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        tvStreakNumber = view.findViewById(R.id.tvStreakNumber);
        goalsDisplayContainer = view.findViewById(R.id.goalsDisplayContainer);

        // ✅ Streak 数字（使用 theme-aware 颜色）
        tvStreakNumber.setTextColor(getThemedColor(R.color.text_primary));

        streakViewModel.getCurrentStreakLiveData()
                .observe(getViewLifecycleOwner(), result -> {
                    if (result != null && result.streakCount >= 0) {
                        tvStreakNumber.setText(String.valueOf(result.streakCount));
                    } else {
                        tvStreakNumber.setText("0");
                    }
                });

        goalRepository.getAllGoalsLive()
                .observe(getViewLifecycleOwner(), this::displayGoals);

        if (getChildFragmentManager().findFragmentById(R.id.dashboardContainer) == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.dashboardContainer, new FitnessDashboard())
                    .commit();
        }

        ImageView btnSettings = view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_settingMainActivity)
        );

        ImageView appleImage = view.findViewById(R.id.iv_apple);
        appleImage.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_appleFragment3)
        );
    }

    private void displayGoals(List<GoalEntity> goals) {
        goalsDisplayContainer.removeAllViews();

        if (goals == null || goals.isEmpty()) {
            TextView noGoalsText = new TextView(requireContext());
            noGoalsText.setText("No goals");
            noGoalsText.setTextSize(16);
            noGoalsText.setGravity(Gravity.CENTER);
            noGoalsText.setTextColor(getThemedColor(R.color.text_secondary));

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = dpToPx(16);
            params.bottomMargin = dpToPx(16);
            noGoalsText.setLayoutParams(params);

            goalsDisplayContainer.addView(noGoalsText);
            return;
        }

        for (GoalEntity goal : goals) {
            goalsDisplayContainer.addView(createGoalItemView(goal));
        }
    }

    private View createGoalItemView(GoalEntity goal) {
        LinearLayout itemLayout = new LinearLayout(requireContext());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = dpToPx(8);
        itemLayout.setLayoutParams(layoutParams);

        // ● Bullet
        TextView bullet = new TextView(requireContext());
        LinearLayout.LayoutParams bulletParams =
                new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
        bulletParams.rightMargin = dpToPx(12);
        bullet.setLayoutParams(bulletParams);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(getThemedColor(R.color.text_primary));
        bullet.setBackground(circle);

        // Goal name
        TextView goalName = new TextView(requireContext());
        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f);
        goalName.setLayoutParams(nameParams);
        goalName.setText(goal.getName());
        goalName.setTextSize(16);
        goalName.setTextColor(getThemedColor(R.color.text_primary));

        // ✓ Check mark
        TextView checkMark = new TextView(requireContext());
        LinearLayout.LayoutParams checkParams =
                new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
        checkMark.setLayoutParams(checkParams);
        checkMark.setGravity(Gravity.CENTER);
        checkMark.setText("✓");
        checkMark.setTextSize(18);
        checkMark.setTypeface(null, android.graphics.Typeface.BOLD);
        checkMark.setTextColor(getThemedColor(R.color.white));

        if (goal.isCompleted()) {
            checkMark.setBackgroundResource(R.drawable.checkmark_circle);
        } else {
            checkMark.setBackgroundResource(R.drawable.ic_check_gray_circle);
        }

        itemLayout.addView(bullet);
        itemLayout.addView(goalName);
        itemLayout.addView(checkMark);

        return itemLayout;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private int getThemedColor(int colorResId) {
        return ContextCompat.getColor(requireContext(), colorResId);
    }

    private boolean isDarkMode() {
        int nightMode =
                getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}
