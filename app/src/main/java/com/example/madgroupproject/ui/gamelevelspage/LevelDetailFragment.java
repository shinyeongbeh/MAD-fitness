package com.example.madgroupproject.ui.gamelevelspage;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.entity.GameLevelEntity;
import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;
import com.example.madgroupproject.data.local.entity.UserProfile;
import com.example.madgroupproject.data.viewmodel.GameLevelViewModel;

import java.util.concurrent.Executors;

public class LevelDetailFragment extends Fragment {
    private TextView levelTitleTV, levelNumTV, levelDescTV, levelPercentageTV, levelDateTV;
    private ImageView levelFrameIV, levelProfileIV;
    private GameLevelViewModel viewModel;
    int levelNumber=1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.level_details, container, false);

        levelTitleTV = view.findViewById(R.id.idTVLevelName);
        levelNumTV = view.findViewById(R.id.idTVLevelNum);
        levelDescTV = view.findViewById(R.id.idTVDetail);
        levelFrameIV = view.findViewById(R.id.idIVFrame);
        levelPercentageTV = view.findViewById(R.id.idTVPercentage);
        levelDateTV = view.findViewById(R.id.idTVDate);
        levelProfileIV = view.findViewById(R.id.user);

        viewModel = new ViewModelProvider(this).get(GameLevelViewModel.class);

        // static data from bundle
        if (getArguments() != null && getArguments().containsKey("LEVEL_NUMBER")) {
            levelNumber = getArguments().getInt("LEVEL_NUMBER", 1);
            levelTitleTV.setText(getArguments().getString("LEVEL_TITLE", ""));
            levelDescTV.setText(getArguments().getString("LEVEL_DESC", ""));

            int img = getArguments().getInt("LEVEL_FRAME", R.drawable.apples);
            levelFrameIV.setImageResource(img);

            levelNumTV.setText("Level " + String.valueOf(levelNumber));

        }

        //level percentage
        viewModel.getLevel(levelNumber).observe(getViewLifecycleOwner(), level -> {
            if (level == null) return;

            viewModel.observeProgress().observe(getViewLifecycleOwner(), progress -> {
                if (progress == null) return;

                float percent;

                // NOT STARTED
                if (progress.currentLevel < levelNumber) {
                    levelPercentageTV.setVisibility(View.GONE);
                    levelDateTV.setVisibility(View.GONE);
                }
                // IN PROGRESS
                else if (progress.currentLevel == levelNumber) {
                    percent = (progress.progressValue / level.targetValue) * 100f;
                    levelPercentageTV.setVisibility(View.VISIBLE);
                    levelPercentageTV.setText(String.format("%.1f%%", percent));
                    levelDateTV.setVisibility(View.GONE);
                }
                // COMPLETED
                else {
                    levelPercentageTV.setVisibility(View.VISIBLE);
                    levelPercentageTV.setText("100%");
                    percent=100;
                }
            });
        });

        // Completion date
        viewModel.observeHistoryForLevel(levelNumber)
                .observe(getViewLifecycleOwner(), history -> {
                    if (history != null) {
                        levelDateTV.setVisibility(View.VISIBLE);
                        levelDateTV.setText(history.completedDate);
                    }
                });

        //sync profile pic
        AppDatabase db = AppDatabase.getDatabase(getContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile profile = db.userProfileDao().getProfile();

            if (profile != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Load profile image from URI
                    String uriString = profile.getProfileImageUri();
                    if (uriString != null && !uriString.isEmpty()) {
                        levelProfileIV.setImageURI(Uri.parse(uriString));
                    }

                    // Optionally set other info
                    // levelNameTV.setText(profile.getName());
                });
            }
        });
                return view;
    }
}
