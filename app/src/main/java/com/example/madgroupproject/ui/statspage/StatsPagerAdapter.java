package com.example.madgroupproject.ui.statspage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.madgroupproject.ui.statspage.StatsDailyFragment;
import com.example.madgroupproject.ui.statspage.StatsMonthlyFragment;
import com.example.madgroupproject.ui.statspage.StatsWeeklyFragment;

public class StatsPagerAdapter extends FragmentStateAdapter {

    public StatsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity); // this works
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new StatsDailyFragment();
            case 1: return new StatsWeeklyFragment();
            case 2: return new StatsMonthlyFragment();
            default: return new StatsDailyFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
