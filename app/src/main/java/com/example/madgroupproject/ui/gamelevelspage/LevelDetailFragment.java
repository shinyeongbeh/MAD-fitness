package com.example.madgroupproject.ui.gamelevelspage;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.entity.GameLevelEntity;
import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;
import com.example.madgroupproject.data.viewmodel.GameLevelViewModel;

import java.util.concurrent.Executors;

public class LevelDetailFragment extends Fragment {
    private TextView levelTitleTV, levelNumTV, levelDescTV, levelPercentageTV, levelDateTV;
    private ImageView levelFrameIV;
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

        viewModel = new ViewModelProvider(this).get(GameLevelViewModel.class);

        // static data from bundle
        if (getArguments() != null && getArguments().containsKey("LEVEL_NUMBER")) {
            levelNumber = getArguments().getInt("LEVEL_NUMBER", 1);
            levelTitleTV.setText(getArguments().getString("LEVEL_TITLE", ""));
            levelDescTV.setText(getArguments().getString("LEVEL_DESC", ""));

            int img = getArguments().getInt("LEVEL_FRAME", R.drawable.apples);
            levelFrameIV.setImageResource(img);

            levelNumTV.setText("Level " +String.valueOf(levelNumber));

        }

        //dynamic data
//        viewModel.getLevel(levelNumber).observe(getViewLifecycleOwner(), level -> {
//            if (level != null) {
//                //example: levelNumTV.setText(String.valueOf(level.levelNum));
//                // You can update other fields here if needed
//            }
//        });

        // dynamic data (percentage)
        viewModel.observeProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null && progress.currentLevel == levelNumber) {
                    float percentage = ((float) progress.progressValue /
                            getTargetValue(levelNumber)) * 100;
                    levelPercentageTV.setText(String.format("%.0f%%", percentage));
            }

        });
        //show when in progress
        levelPercentageTV.setVisibility(View.GONE);
        viewModel.observeProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress == null) return;

            Executors.newSingleThreadExecutor().execute(() -> {
                GameLevelHistoryEntity history = viewModel.getHistoryForLevel(levelNumber);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (history != null) {
                        // Level completed
                        levelPercentageTV.setVisibility(View.VISIBLE);
                        levelPercentageTV.setText("100%");
                        levelDateTV.setVisibility(View.VISIBLE);
                        levelDateTV.setText(history.completedDate);
                    } else if (progress.currentLevel == levelNumber) {
                        // Level in progress
                        levelPercentageTV.setVisibility(View.VISIBLE);
                        float percentage = (progress.progressValue / getTargetValue(levelNumber)) * 100f;
                        levelPercentageTV.setText(String.format("%.0f%%", percentage));
                        levelDateTV.setVisibility(View.GONE);
                    } else {
                        // Level not started yet
                        levelPercentageTV.setVisibility(View.GONE);
                        levelDateTV.setVisibility(View.GONE);
                    }
                });
            });
        });

        //show when completed
        levelDateTV.setVisibility(View.GONE);

        Executors.newSingleThreadExecutor().execute(() -> {
            GameLevelHistoryEntity history =
                    viewModel.getHistoryForLevel(levelNumber);

            if (getActivity() == null) return;

            getActivity().runOnUiThread(() -> {
                if (history != null) {
                    levelDateTV.setVisibility(View.VISIBLE);
                    levelDateTV.setText(history.completedDate);
                } else {
                    levelDateTV.setVisibility(View.GONE);
                }
            });
        });

        return view;
    }

    // Helper method to get target value for level (could query ViewModel or database)
    private int getTargetValue(int levelNumber) {
        GameLevelEntity level = viewModel.getLevel(levelNumber).getValue();
        return level != null ? level.targetValue : 100; // fallback
    }
}
