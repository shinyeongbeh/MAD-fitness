package com.example.madgroupproject.ui.streakpage;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.madgroupproject.R;

public class DayDetailFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.streak_day_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.tvDayTitle);
        Button back = view.findViewById(R.id.btnBack);

        int day = getArguments().getInt("day", 1);
        String month = getArguments().getString("month", "Feb");
        title.setText(month + " " + day);

        back.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );
    }
}