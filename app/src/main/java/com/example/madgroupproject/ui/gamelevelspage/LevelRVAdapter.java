package com.example.madgroupproject.ui.gamelevelspage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

//        holder.itemView.setOnClickListener(new View.OnClickListener()){
//            @Override
//            public void onClick(View v){
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setData(Uri.parse(levelsRVModelArrayList.get(position).getLevelUrl());
//            context.startActivity(i);
//            }
//        }


    }

    @Override
    public int getItemCount() {
        return levelsRVModelArrayList.size();
    }


    //viewholder class
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
