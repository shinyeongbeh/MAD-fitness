package com.example.madgroupproject.ui.gamelevelspage;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.madgroupproject.R;

public class LevelDetailFragment extends Fragment {
    private TextView levelTitleTV, levelNumTV, levelDescTV;
    private ImageView levelImgIV;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.level_details, container, false);

        levelTitleTV = view.findViewById(R.id.idTVLevelName);
        levelNumTV = view.findViewById(R.id.idTVLevelNum);
        levelDescTV = view.findViewById(R.id.idTVDetail);
        levelImgIV = view.findViewById(R.id.idIVLevel);

        if (getArguments() != null) {
            levelNumTV.setText(getArguments().getString("LEVEL_NUMBER"));
            levelTitleTV.setText(getArguments().getString("LEVEL_TITLE"));
            levelDescTV.setText(getArguments().getString("LEVEL_DESC"));

            int img = getArguments().getInt("LEVEL_IMG");
            levelImgIV.setImageResource(img);
        }

        return view;
    }
}
