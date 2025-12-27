package com.example.madgroupproject.ui.gamelevelspage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madgroupproject.R;

import java.util.ArrayList;

public class LevelRVAdapter extends RecyclerView.Adapter<LevelRVAdapter.ViewHolder> {
    private ArrayList<LevelsRVModel> levelsRVModelArrayList;
    private Context context;

    public LevelRVAdapter(Context context, ArrayList<LevelsRVModel> levelsRVModelArrayList) {
        this.context = context;
        this.levelsRVModelArrayList = levelsRVModelArrayList;
    }

    @NonNull
    @Override
    public LevelRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.level_rv_item,parent,false);
        return new ViewHolder(view);
    }


    //deal with clicks here
    @Override
    public void onBindViewHolder(@NonNull LevelRVAdapter.ViewHolder holder, int position) {
        holder.levelNameTV.setText(levelsRVModelArrayList.get(position).getLevelName());
        holder.levelNum.setText(levelsRVModelArrayList.get(position).getLevelNum());
        holder.levelDetailTV.setText(levelsRVModelArrayList.get(position).getLevelDetail());
        holder.levelImg.setImageResource(levelsRVModelArrayList.get(position).getLevelImg());

        LevelsRVModel level = levelsRVModelArrayList.get(position);

        //onclick
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();

            bundle.putString("LEVEL_NUMBER", level.getLevelNum());
            bundle.putString("LEVEL_TITLE", level.getLevelName());
            bundle.putString("LEVEL_DESC", level.getLevelDetail());
            bundle.putInt("LEVEL_IMG", level.getLevelImg());


            Navigation.findNavController(v)
                    .navigate(
                            R.id.action_gameLevelFragment_to_levelDetailFragment,
                            bundle
                    );
        });
    }

    @Override
    public int getItemCount() {
        return levelsRVModelArrayList.size();
    }


    //ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView levelNameTV, levelDetailTV,levelNum;
        private ImageView levelImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            levelNameTV = itemView.findViewById(R.id.idTVLevelName);
            levelDetailTV = itemView.findViewById(R.id.idTVDetail);
            levelImg = itemView.findViewById(R.id.idIVLevel);
            levelNum = itemView.findViewById(R.id.idTVLevelNum);
        }
    }
}
