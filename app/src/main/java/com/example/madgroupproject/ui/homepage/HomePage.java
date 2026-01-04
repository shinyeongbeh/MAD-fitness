package com.example.madgroupproject.ui.homepage;

import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.entity.GoalEntity;
import com.example.madgroupproject.data.local.entity.UserProfile;
import com.example.madgroupproject.data.repository.GoalRepository;
import com.example.madgroupproject.data.viewmodel.StreakViewModel;

import java.util.List;
import java.util.concurrent.Executors;

public class HomePage extends Fragment {

    private StreakViewModel streakViewModel;
    private GoalRepository goalRepository;

    private TextView tvStreakNumber;
    private LinearLayout goalsDisplayContainer;
    private TextView tvWelcomeBack;

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

        // 1. 初始化数据
        goalRepository = new GoalRepository(requireContext());
        streakViewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        // 2. 绑定 UI
        tvStreakNumber = view.findViewById(R.id.tvStreakNumber);
        goalsDisplayContainer = view.findViewById(R.id.goalsDisplayContainer);

        if(tvStreakNumber != null) {
            tvStreakNumber.setTextColor(getThemedColor(R.color.text_primary));
        }

        // 3. 数据观察
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

        // 4. 加载 Dashboard
        if (getChildFragmentManager().findFragmentById(R.id.dashboardContainer) == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.dashboardContainer, new FitnessDashboard())
                    .commit();
        }

        if (getChildFragmentManager().findFragmentById(R.id.homeProfilePicContainer) == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.homeProfilePicContainer, new ProfilePicFragment())
                    .commit();
        }
        // Set Welcome Back message with user's username
        tvWelcomeBack = view.findViewById(R.id.TVwelcomeBack);
        String username = "";
        AppDatabase db = AppDatabase.getDatabase(requireContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile profile = db.userProfileDao().getProfile();
            requireActivity().runOnUiThread(() -> {
                if(profile!=null && !profile.getName().isEmpty()) {
                    tvWelcomeBack.setText("Welcome back, " + profile.getName()+".");
                }
            });
        });

        // =======================================================
        // 5. 点击跳转逻辑
        // =======================================================

        // -> 跳转 Streak
        View.OnClickListener toStreakAction = v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_streakFragment);

        View cardStreak = view.findViewById(R.id.cardStreak);
        View btnFire = view.findViewById(R.id.btnHome);
        if (cardStreak != null) cardStreak.setOnClickListener(toStreakAction);
        if (btnFire != null) btnFire.setOnClickListener(toStreakAction);


        // -> 跳转 Goal
        View.OnClickListener toGoalAction = v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_goalFragment);

        View cardGoal = view.findViewById(R.id.cardGoal);
        View btnFlag = view.findViewById(R.id.btnFlag);
        if (cardGoal != null) cardGoal.setOnClickListener(toGoalAction);
        if (btnFlag != null) btnFlag.setOnClickListener(toGoalAction);


        // -> 跳转 Game
        View btnTrophy = view.findViewById(R.id.btnAchievement);
        if (btnTrophy != null) {
            btnTrophy.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_gameLevelFragment)
            );
        }

        // -> 跳转 Stats
        View cardDashboard = view.findViewById(R.id.cardDashboard);
        if (cardDashboard != null) {
            cardDashboard.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_statsFragment)
            );
        }

        // -> Settings
        ImageView btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_settingMainActivity)
            );
        }

        // -> 核心修改：这里修复了 iv_apple 找不到的问题
        // 我们改为查找 cardArticle 和 iv_article_image
        View cardArticle = view.findViewById(R.id.cardArticle);
        ImageView articleImage = view.findViewById(R.id.iv_article_image); // 这里修复了ID

        View.OnClickListener toArticleAction = v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_appleFragment3);

        if (cardArticle != null) cardArticle.setOnClickListener(toArticleAction);
        if (articleImage != null) articleImage.setOnClickListener(toArticleAction);
    }

    // ===========================================
    // 辅助方法
    // ===========================================
    private void displayGoals(List<GoalEntity> goals) {
        if (goalsDisplayContainer == null) return;
        goalsDisplayContainer.removeAllViews();

        if (goals == null || goals.isEmpty()) {
            TextView noGoalsText = new TextView(requireContext());
            noGoalsText.setText("No goals");
            noGoalsText.setTextSize(16);
            noGoalsText.setGravity(Gravity.CENTER);
            noGoalsText.setTextColor(getThemedColor(R.color.text_secondary));
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
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = dpToPx(8);
        itemLayout.setLayoutParams(layoutParams);

        TextView bullet = new TextView(requireContext());
        LinearLayout.LayoutParams bulletParams = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
        bulletParams.rightMargin = dpToPx(12);
        bullet.setLayoutParams(bulletParams);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(getThemedColor(R.color.text_primary));
        bullet.setBackground(circle);

        TextView goalName = new TextView(requireContext());
        goalName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        goalName.setText(goal.getName());
        goalName.setTextSize(16);
        goalName.setTextColor(getThemedColor(R.color.text_primary));

        TextView checkMark = new TextView(requireContext());
        checkMark.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28)));
        checkMark.setGravity(Gravity.CENTER);
        checkMark.setText("✓");
        checkMark.setTextSize(18);
        checkMark.setTextColor(getThemedColor(R.color.white));

        if (goal.isCompleted()) {
            checkMark.setBackgroundResource(R.drawable.checkmark_circle);
        } else {
            checkMark.setBackgroundColor(0x00000000);
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
}