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
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkInfo;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.repository.FitnessRepository;
import com.example.madgroupproject.data.repository.GoalRepository;
import com.example.madgroupproject.data.repository.StreakRepository;
import com.example.madgroupproject.fitnessmanager.FitnessSyncWorker;
import com.example.madgroupproject.fitnessmanager.RecordingAPIManager;
import com.example.madgroupproject.goalmanager.DailyGoalResetScheduler;
import com.example.madgroupproject.util.MidnightChangeListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.fitness.LocalRecordingClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // 全局午夜监听器
    private MidnightChangeListener midnightListener;
    private GoalRepository goalRepository;
    private StreakRepository streakRepository;
    private SharedPreferences prefs;

    // ✅ 添加flag防止同一天重复显示Toast
    private boolean hasShownTodayToast = false;

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
                }else{
                    startTracking();
                    startStepForegroundService();// for pinned notification
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Create notification channel
        NotificationUtil.createNotificationChannel(this);

        // DEMO: show immediately for Daily goals
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            sendBroadcast(new Intent(this, GoalNotificationReceiver.class));
        }, 1000);

        // DEMO: show immediately for Streak
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            sendBroadcast(new Intent(this, StreakNotificationReceiver.class));
        }, 1000);

        // 初始化仓库和SharedPreferences
        goalRepository = new GoalRepository(this);
        streakRepository = new StreakRepository(this);
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // ✅ 启动时检查是否跨日(处理用户在午夜后首次打开App的情况)
        checkAndHandleAppStartup();

        // ✅ 设置全局午夜监听器(处理App运行中跨日的情况)
        setupGlobalMidnightListener();

        // ✅ Start daily goal reset scheduler (作为后备机制)
        Log.d(TAG, "========================================");
        Log.d(TAG, "📅 Scheduling Daily Goal Reset...");

        try {
            DailyGoalResetScheduler.scheduleDailyReset(this);
            String nextReset = DailyGoalResetScheduler.getNextResetTime();
            Log.d(TAG, "✅ Next goal reset at: " + nextReset);

            // 🔍 Verify the task was scheduled
            verifyResetTaskScheduled();

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to schedule reset", e);
        }

        Log.d(TAG, "========================================");

        //For notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
                return; // stop here
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

    /**
     * ✅ 启动时检查:如果上次运行日期 != 今天,执行清理
     */
    private void checkAndHandleAppStartup() {
        String lastRunDate = prefs.getString("last_run_date", "");
        String today = LocalDate.now().toString();

        Log.d(TAG, "📅 Checking app startup - Last run: " + lastRunDate + ", Today: " + today);

        if (!lastRunDate.equals(today)) {
            Log.d(TAG, "🔄 App opened on new day, performing cleanup...");
            performMidnightCleanup("AppStartup");
            prefs.edit().putString("last_run_date", today).apply();
        } else {
            Log.d(TAG, "✅ App opened on same day, no cleanup needed");
            // ✅ 如果是同一天，说明已经显示过Toast了
            hasShownTodayToast = true;
        }
    }

    /**
     * ✅ 设置全局午夜监听器
     */
    private void setupGlobalMidnightListener() {
        Log.d(TAG, "🌙 Setting up global midnight listener...");

        midnightListener = new MidnightChangeListener(this);
        midnightListener.addListener(() -> {
            runOnUiThread(() -> {
                Log.d(TAG, "🌙🌙🌙 MIDNIGHT PASSED! New day started!");

                // ✅ 重置Toast flag，允许显示新一天的Toast
                hasShownTodayToast = false;

                performMidnightCleanup("MidnightListener");

                // 更新最后运行日期
                String today = LocalDate.now().toString();
                prefs.edit().putString("last_run_date", today).apply();
                Log.d(TAG, "📅 Updated last_run_date to: " + today);
            });
        });

        Log.d(TAG, "✅ Global midnight listener setup complete");
    }

    /**
     * ✅ 统一的午夜清理逻辑 - 修改为重置goal状态而非删除
     */
    private void performMidnightCleanup(String source) {
        Log.d(TAG, "🧹 Performing midnight cleanup from: " + source);

        // 1️⃣ ✅ 修改：重置所有Goal的状态为未完成（而非删除）
        goalRepository.resetAllGoalsStatus(new GoalRepository.OnResultListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "✅ Goals status reset for new day (goals preserved)");
                GoalNotificationManager.updateGoalNotification(MainActivity.this);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "❌ Error resetting goals status", e);
            }
        });

        // 2️⃣ 创建新一天的Streak记录
        streakRepository.autoInitTodayRecord();
        Log.d(TAG, "✅ New streak record initialized");

        // 3️⃣ 显示统一的新一天提示（只显示一次）
        if (!hasShownTodayToast) {
            Toast.makeText(this,
                    "Happy new day! 🎉",
                    Toast.LENGTH_SHORT).show();
            hasShownTodayToast = true;
            Log.d(TAG, "✅ Toast shown for new day");
        } else {
            Log.d(TAG, "⏭️ Toast already shown today, skipping");
        }

        // 4️⃣ 发送广播通知所有Fragment刷新（Fragment不再显示Toast）
        Intent intent = new Intent("com.example.madgroupproject.MIDNIGHT_PASSED");
        sendBroadcast(intent);
        Log.d(TAG, "📡 Broadcast sent to all fragments");

    }

    /**
     * 🔍 Verify that the reset task was successfully scheduled
     */
    private void verifyResetTaskScheduled() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                ListenableFuture<List<WorkInfo>> future =
                        WorkManager.getInstance(this)
                                .getWorkInfosForUniqueWork("daily_goal_reset_work");

                List<WorkInfo> workInfos = future.get();

                if (workInfos == null || workInfos.isEmpty()) {
                    Log.e(TAG, "❌ CRITICAL: Reset task NOT found in WorkManager!");
                    Log.e(TAG, "❌ Goals will NOT reset at midnight!");
                } else {
                    for (WorkInfo workInfo : workInfos) {
                        Log.d(TAG, "✅ Reset task verified:");
                        Log.d(TAG, "   State: " + workInfo.getState());
                        Log.d(TAG, "   ID: " + workInfo.getId());

                        if (workInfo.getState() == WorkInfo.State.ENQUEUED) {
                            Log.d(TAG, "✅ Task is properly ENQUEUED and will run at midnight");
                        } else {
                            Log.w(TAG, "⚠️ Unexpected state: " + workInfo.getState());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error verifying reset task", e);
            }
        }, 2000); // Check after 2 seconds to ensure task is registered
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            checkPermissionAndStartTracking(); // continue flow
        }
    }

    private void startStepForegroundService() {
        Intent intent = new Intent(this, StepTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
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
                startStepForegroundService(); // start pinned notification
            }
        } else {
            startTracking();
            startStepForegroundService(); // start pinned notification
        }
    }

    private void startTracking() {
        RecordingAPIManager recordingAPIManager = new RecordingAPIManager(this);
        recordingAPIManager.subscribeToRecording(this);

        triggerDatabaseInspectorRoom();
        scheduleFitnessSync();
    }

    //background sync to database every 15 min
    private void scheduleFitnessSync() {
        PeriodicWorkRequest work =
                new PeriodicWorkRequest.Builder(
                        FitnessSyncWorker.class,
                        15,
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ 销毁全局午夜监听器
        if (midnightListener != null) {
            midnightListener.destroy();
            midnightListener = null;
            Log.d(TAG, "🌙 Global midnight listener destroyed");
        }
    }
}