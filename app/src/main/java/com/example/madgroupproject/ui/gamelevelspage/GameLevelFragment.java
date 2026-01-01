package com.example.madgroupproject.ui.gamelevelspage;

/* references
recyclerView: https://www.geeksforgeeks.org/android/android-recyclerview/

 */

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.madgroupproject.R;
import java.util.ArrayList;

public class GameLevelFragment extends Fragment {

    private RecyclerView levelRV;
    private ArrayList<LevelsRVModel> levelsRVModelArrayList;
    private LevelRVAdapter levelRVAdapter;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = getLayoutInflater().inflate(R.layout.fragment_game_level, container,false);

        levelRV = view.findViewById(R.id.RVLevels);
        levelsRVModelArrayList = new ArrayList<>();

        levelRVAdapter = new LevelRVAdapter(getContext(), levelsRVModelArrayList);
        levelRV.setLayoutManager(new LinearLayoutManager(getContext()));
        levelRV.setAdapter(levelRVAdapter);

        addDataToList();
        levelRVAdapter.notifyDataSetChanged();

        return view;
    }

    private void addDataToList(){
        levelsRVModelArrayList.add(new LevelsRVModel("First Steps", 1, "Walk 100 steps", R.drawable.frame_1, R.color.beginner));
        levelsRVModelArrayList.add(new LevelsRVModel("Distance Debut", 2, "Cover 1 km distance", R.drawable.frame_2, R.color.beginner));
        levelsRVModelArrayList.add(new LevelsRVModel("Casual Stroller", 3, "Walk 1500 steps", R.drawable.frame_3, R.color.beginner));
        levelsRVModelArrayList.add(new LevelsRVModel("Road Starter", 4, "Cover 2 km distance", R.drawable.frame_4, R.color.beginner));
        levelsRVModelArrayList.add(new LevelsRVModel("Daily Mover", 5, "Walk 2500 steps", R.drawable.frame_5, R.color.beginner));
        levelsRVModelArrayList.add(new LevelsRVModel("Path Explorer", 6, "Cover 3 km distance", R.drawable.frame_6, R.color.beginner));
        levelsRVModelArrayList.add(new LevelsRVModel("Steady Strider", 7, "Walk 3500 steps", R.drawable.frame_7, R.color.intermediate));
        levelsRVModelArrayList.add(new LevelsRVModel("Route Runner", 8, "Cover 4 km distance", R.drawable.frame_8, R.color.intermediate));
        levelsRVModelArrayList.add(new LevelsRVModel("Pace Builder", 9, "Walk 4500 steps", R.drawable.frame_9, R.color.intermediate));
        levelsRVModelArrayList.add(new LevelsRVModel("Distance Climber", 10, "Cover 5 km distance", R.drawable.frame_10, R.color.intermediate));
        levelsRVModelArrayList.add(new LevelsRVModel("Endurance Walker", 11, "Walk 6000 steps", R.drawable.frame_11, R.color.intermediate));
        levelsRVModelArrayList.add(new LevelsRVModel("Long Hauler", 12, "Cover 6.5 km distance", R.drawable.frame_12, R.color.intermediate));

        levelsRVModelArrayList.add(new LevelsRVModel("Step Champion", 13, "Walk 7000 steps", R.drawable.frame_13, R.color.expert));
        levelsRVModelArrayList.add(new LevelsRVModel("Mileage Master", 14, "Cover 7.5 km distance", R.drawable.frame_14, R.color.expert));
        levelsRVModelArrayList.add(new LevelsRVModel("Power Walker", 15, "Walk 8000 steps", R.drawable.frame_15, R.color.expert));
        levelsRVModelArrayList.add(new LevelsRVModel("Distance Pro", 16, "Cover 8.5 km distance", R.drawable.frame_16, R.color.expert));
        levelsRVModelArrayList.add(new LevelsRVModel("Relentless Steps", 17, "Walk 9000 steps", R.drawable.frame_17, R.color.expert));
        levelsRVModelArrayList.add(new LevelsRVModel("Ultra Trekker", 18, "Cover 10 km distance", R.drawable.frame_18, R.color.professional));
        levelsRVModelArrayList.add(new LevelsRVModel("Step Legend", 19, "Walk 110000 steps", R.drawable.frame_19, R.color.professional));
        levelsRVModelArrayList.add(new LevelsRVModel("Distance Legend", 20, "Cover 12 km distance", R.drawable.frame_20, R.color.professional));

    }

}