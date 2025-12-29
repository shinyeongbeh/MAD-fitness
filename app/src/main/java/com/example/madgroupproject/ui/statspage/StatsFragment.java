package com.example.madgroupproject.ui.statspage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.madgroupproject.R;
import com.google.android.material.tabs.TabLayout;

public class StatsFragment extends Fragment {

    private FrameLayout frameLayout;
    private TabLayout tabLayout;

    public StatsFragment() {}

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

        frameLayout = view.findViewById(R.id.FrameLayoutStats);
        tabLayout = view.findViewById(R.id.TabLayoutStats);

        // Default tab → Weekly
        if (savedInstanceState == null) {
            loadChildFragment(new stats_weekly());
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment =
                        (tab.getPosition() == 0)
                                ? new stats_weekly()
                                : new stats_monthly();

                loadChildFragment(fragment);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadChildFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.FrameLayoutStats, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }
}
