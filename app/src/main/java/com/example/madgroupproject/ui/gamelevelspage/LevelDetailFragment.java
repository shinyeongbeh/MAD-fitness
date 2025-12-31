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
import com.example.madgroupproject.data.viewmodel.GameLevelViewModel;

public class LevelDetailFragment extends Fragment {
    private TextView levelTitleTV, levelNumTV, levelDescTV, levelPercentageTV, levelDateTV;
    private ImageView levelImgIV;
    private GameLevelViewModel viewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.level_details, container, false);

        levelTitleTV = view.findViewById(R.id.idTVLevelName);
        levelNumTV = view.findViewById(R.id.idTVLevelNum);
        levelDescTV = view.findViewById(R.id.idTVDetail);
        levelImgIV = view.findViewById(R.id.idIVLevel);
        levelPercentageTV = view.findViewById(R.id.idTVPercentage);
        levelDateTV = view.findViewById(R.id.idTVDate);

        viewModel = new ViewModelProvider(this).get(GameLevelViewModel.class);

        // get the level number (either from arguments or default to 1)
        int levelNumber;
        if (getArguments() != null && getArguments().containsKey("LEVEL_NUMBER")) {
            levelNumber = getArguments().getInt("LEVEL_NUMBER", 1);
            levelTitleTV.setText(getArguments().getString("LEVEL_TITLE", ""));
            levelDescTV.setText(getArguments().getString("LEVEL_DESC", ""));
            int img = getArguments().getInt("LEVEL_IMG", R.drawable.apples);
            levelImgIV.setImageResource(img);
            //static game leve (from fragment)
            levelNumTV.setText("Level " +String.valueOf(levelNumber));

        } else {
            levelNumber = 1;
        }

        //dynamic data (game level)
//        viewModel.getLevel(levelNumber).observe(getViewLifecycleOwner(), level -> {
//            if (level != null) {
//                //levelNumTV.setText(String.valueOf(level.levelNum));
//                // You can update other fields here if needed
//            }
//        });

        // dynamic data (percentage)
        viewModel.observeProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                if (progress.currentLevel == levelNumber) {
                    float percentage = ((float) progress.progressValue /
                            getTargetValue(levelNumber)) * 100;
                    levelPercentageTV.setText(String.format("%.0f%%", percentage));
                    levelDateTV.setText(progress.lastSyncedDate != null
                            ? progress.lastSyncedDate
                            : "");
                }
            }
        });

        return view;
    }

    // Helper method to get target value for level (could query ViewModel or database)
    private int getTargetValue(int levelNumber) {
        GameLevelEntity level = viewModel.getLevel(levelNumber).getValue();
        return level != null ? level.targetValue : 100; // fallback
    }
}
