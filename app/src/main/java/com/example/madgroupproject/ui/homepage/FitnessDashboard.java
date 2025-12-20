package com.example.madgroupproject.ui.homepage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.fitnessmanager.FitnessManager;
import com.example.madgroupproject.fitnessmanager.SyncWorker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.LocalRecordingClient;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class FitnessDashboard extends Fragment {

    private TextView tvSteps, tvDistance, tvCalories;
    private FitnessManager fitnessManager;
    private AppDatabase db;
    private Handler handler;
    private Runnable liveUpdateRunnable;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startTracking();
                } else {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            });

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

        // Google Play Services check
        int minVersion = LocalRecordingClient.LOCAL_RECORDING_CLIENT_MIN_VERSION_CODE;
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext(), minVersion);

        if (result != ConnectionResult.SUCCESS) {
            // this dialog will ask the user to update Google Play Services
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(requireActivity(), result, 9000)
                    .show();
        }

        fitnessManager = new FitnessManager(requireContext());
        db = AppDatabase.getDatabase(requireContext());
        handler = new Handler(Looper.getMainLooper());

        checkPermissionAndStart();
    }

    private void checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
            } else {
                startTracking();
            }
        } else {
            startTracking();
        }
    }

    private void startTracking() {
        // 1. Subscribe for background tracking
        fitnessManager.subscribeToRecording(requireActivity());

        // 2. Schedule background worker (every 15 mins)
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "FitnessSync",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );

        // When database is changed, UI updates
        String today = LocalDate.now().toString();
        db.fitnessDataDao().getStatsLive(today).observe(getViewLifecycleOwner(), new Observer<FitnessDataEntity>() {
            @Override
            public void onChanged(FitnessDataEntity stat) {
                if (stat != null) {
                    tvSteps.setText("Steps: " + stat.steps);
                    tvDistance.setText(String.format("Distance: %.2f m", stat.distanceMeters));
                    tvCalories.setText(String.format("Calories: %.2f kcal", stat.calories));
                } else {
                    tvSteps.setText("Steps: 0");
                    tvDistance.setText("Distance: 0.00 m");
                    tvCalories.setText("Calories: 0.00 kcal");
                }
            }
        });
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
        backgroundExecutor.shutdownNow(); // Prevent leaks
    }

    private void startLiveUpdateLoop() {
        liveUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                backgroundExecutor.execute(() -> {
                    FitnessManager.DailyData data = fitnessManager.readDailyTotals();
                    String today = LocalDate.now().toString();
                    FitnessDataEntity stat = new FitnessDataEntity(today, data.steps, data.distance, data.calories);

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.fitnessDataDao().insertOrUpdate(stat);
                    });
                });
                handler.postDelayed(this, 60 * 1000);
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