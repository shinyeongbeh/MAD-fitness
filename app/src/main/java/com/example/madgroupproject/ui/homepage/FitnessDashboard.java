package com.example.madgroupproject.ui.homepage;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.fitnessmanager.RecordingAPIManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FitnessDashboard extends Fragment {

    private TextView tvSteps, tvDistance, tvCalories;
//    private ViewModel viewModel;
    private RecordingAPIManager recordingAPIManager;
    private AppDatabase db;
    private Handler handler;
    private Runnable liveUpdateRunnable;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fitness_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvSteps = view.findViewById(R.id.tvSteps);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvCalories = view.findViewById(R.id.tvCalories);

//        viewModel = new ViewModelProvider(this).get(FitnessUIViewModel.class);
        handler = new Handler(Looper.getMainLooper());
        recordingAPIManager = new RecordingAPIManager(getContext());
        startLiveUpdateLoop();
    }

    @Override
    public void onResume() {
        super.onResume();
        startLiveUpdateLoop();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLiveUpdateLoop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLiveUpdateLoop();
    }

    private void startLiveUpdateLoop() {
        liveUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                //if fragment is not shown in the UI
                // only allow live UI updates when fragment is shown in UI
                if(!isAdded()) {
                    return;
                }
                backgroundExecutor.execute(() -> {
                    //read from Fitness Manager Recording API
                    RecordingAPIManager.DataRecordingAPI data = recordingAPIManager.readDailyTotals();

                    if(isAdded()) {
                        handler.post(() -> {
                            tvSteps.setText(String.valueOf(data.steps));
                            tvDistance.setText(String.format("%.2f m", data.distance));
                            tvCalories.setText(String.format("%.2f kcal", data.calories));
                        });
                    }
                });
                if(isAdded()){
                    handler.postDelayed(this, 60 * 1000); //update every min
                }
            }
        };
        handler.post(liveUpdateRunnable);
    }

    private void stopLiveUpdateLoop() {
        if (handler != null && liveUpdateRunnable != null) {
            handler.removeCallbacks(liveUpdateRunnable);
        }
    }
}
