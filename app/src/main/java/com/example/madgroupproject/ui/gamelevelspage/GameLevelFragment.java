package com.example.madgroupproject.ui.gamelevelspage;

/* references
recyclerView: https://www.geeksforgeeks.org/android/android-recyclerview/

 */

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
        levelsRVModelArrayList.add((new LevelsRVModel("First Steps",1, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Warm-Up Walker",2, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Casual Stroller",3, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Consistent Starter",4, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Daily Mover",5, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Path Explorer",6, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Steady Strider",7, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Routine Walker",8, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Pace Setter",9, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Weekend Warrior",10, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Trail Tracker",11, "Walk 100 steps",R.drawable.husky)));
        levelsRVModelArrayList.add((new LevelsRVModel("Speed Walker",12, "Walk 100 steps",R.drawable.husky)));

    }

}