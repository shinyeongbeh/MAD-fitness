package com.example.madgroupproject.main;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.repository.FitnessRepository;
import com.example.madgroupproject.fitnessmanager.FitnessSyncWorker;
import com.example.madgroupproject.fitnessmanager.RecordingAPIManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.fitness.LocalRecordingClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // for debugging only, may delete later
    // used so that the db is shown in Android Studio's Database Inspector
    private void triggerDatabaseInspectorRoom() {
        Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        FitnessRepository repository = new FitnessRepository(getApplicationContext());
                        repository.fetchDailyData("2025-12-21");
                        Log.i("DEBUG DB", "successfully fetch");
                    } catch(Exception e) {
                        e.printStackTrace();
                        Log.e("DEBUG DB", e.toString());
                    }
                }
        );

    }


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Create notification channel
        NotificationUtil.createNotificationChannel(this);

        // Show dummy notification
        NotificationUtil.showNotification(this);

     /*   // 🔔 DEMO: Show notification on app open
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (NotificationUtil.isNotificationEnabled(this)) {
                showDemoGoalNotification();
            }
        }, 3000);
*/
        //checkPermissionAndStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        //bottom navigation bar
        BottomNavigationView bottomBar = findViewById(R.id.bottom_nav_view);
        NavHostFragment mainFragmentSection = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_fragment_area);
        NavController navController = mainFragmentSection.getNavController();

        NavigationUI.setupWithNavController(bottomBar, navController);

        checkGooglePlayService();
        checkPermissionAndStartTracking();



    }

    private void checkGooglePlayService() {
        int minVersion = LocalRecordingClient.LOCAL_RECORDING_CLIENT_MIN_VERSION_CODE;
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this, minVersion);

        if (result != ConnectionResult.SUCCESS) {
            // this dialog will ask the user to update Google Play Services
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, result, 9000)
                    .show();
        }
    }
    private void checkPermissionAndStartTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
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
        RecordingAPIManager recordingAPIManager = new RecordingAPIManager(this);
        recordingAPIManager.subscribeToRecording(this);

        triggerDatabaseInspectorRoom();
        scheduleFitnessSync();
    }

    //background sync to database every 30 min
    private void scheduleFitnessSync() {
        PeriodicWorkRequest work =
                new PeriodicWorkRequest.Builder(
                        FitnessSyncWorker.class,
                        30,
                        TimeUnit.MINUTES
                )
                        .setConstraints(
                                new Constraints.Builder()
                                        .setRequiresBatteryNotLow(true)
                                        .build()
                        )
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "fitness_sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                work
        );
    }


   /* private void showDemoGoalNotification() {
        SharedPreferences prefs =
                getSharedPreferences("user_data", MODE_PRIVATE);

        String goal = prefs.getString("daily_goal", "No goal set");

        NotificationUtil.showNotification(
                this);

    }*/


}