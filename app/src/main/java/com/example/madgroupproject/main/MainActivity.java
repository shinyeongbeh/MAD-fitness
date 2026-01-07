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

    // ✅ 使用静态变量实现全局单例保护(防止Activity重建导致的重复Toast)
    private static String lastToastDate = ""; // 上次显示Toast的日期
    private static long lastToastTimestamp = 0; // 上次显示Toast的时间戳
    private static final long TOAST_COOLDOWN_MS = 5000; // 5秒冷却时间

    // 实例级别的flag
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
        Log.d(TAG, "   lastToastDate (static): " + lastToastDate);

        if (!lastRunDate.equals(today)) {
            Log.d(TAG, "🔄 App opened on new day, performing cleanup...");
            performMidnightCleanup("AppStartup");
            prefs.edit().putString("last_run_date", today).apply();
        } else {
            Log.d(TAG, "✅ App opened on same day, no cleanup needed");
            // ✅ 如果是同一天,说明已经显示过Toast了
            hasShownTodayToast = true;
            // ✅ 同步静态变量
            if (!today.equals(lastToastDate)) {
                Log.d(TAG, "   Syncing static lastToastDate to today");
                lastToastDate = today;
            }
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
                Log.d(TAG, "   Current hasShownTodayToast: " + hasShownTodayToast);
                Log.d(TAG, "   Thread: " + Thread.currentThread().getName());

                // ✅ 重置Toast flag,允许显示新一天的Toast
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
     * ✅ 增强版: 使用静态变量实现跨Activity实例的防重复保护
     */
    private void performMidnightCleanup(String source) {
        long currentTime = System.currentTimeMillis();
        String today = LocalDate.now().toString();

        Log.d(TAG, "🧹 performMidnightCleanup called from: " + source);
        Log.d(TAG, "   hasShownTodayToast: " + hasShownTodayToast);
        Log.d(TAG, "   lastToastDate (static): " + lastToastDate);
        Log.d(TAG, "   today: " + today);
        Log.d(TAG, "   Time since last toast: " + (currentTime - lastToastTimestamp) + "ms");
        Log.d(TAG, "   Thread: " + Thread.currentThread().getName());

        // 🔴 关键:添加调用堆栈日志,帮助追踪重复调用
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Log.d(TAG, "📞 Call stack:");
        for (int i = 0; i < Math.min(8, stackTrace.length); i++) {
            Log.d(TAG, "   " + i + ": " + stackTrace[i].toString());
        }

        // 1️⃣ ✅ 修改:重置所有Goal的状态为未完成(而非删除)
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

        // 3️⃣ ✅ 增强版: 使用静态变量检查,防止Activity重建导致重复Toast
        boolean shouldShowToast = false;

        // 检查1: 静态日期是否不同(说明是新的一天)
        if (!today.equals(lastToastDate)) {
            Log.d(TAG, "   Static date check: different day, reset flags");
            lastToastDate = today;
            hasShownTodayToast = false;
            shouldShowToast = true;
        }

        // 检查2: 冷却时间
        if (currentTime - lastToastTimestamp <= TOAST_COOLDOWN_MS) {
            Log.d(TAG, "   Cooldown check: too soon (" +
                    (currentTime - lastToastTimestamp) + "ms < " +
                    TOAST_COOLDOWN_MS + "ms)");
            shouldShowToast = false;
        }

        // 检查3: 今天是否已经显示过(实例级别)
        if (hasShownTodayToast) {
            Log.d(TAG, "   Instance check: already shown today");
            shouldShowToast = false;
        }

        if (shouldShowToast) {
            Log.d(TAG, "🎉 Showing Toast: Happy new day!");
            Toast.makeText(this,
                    "Happy new day! 🎉",
                    Toast.LENGTH_SHORT).show();
            hasShownTodayToast = true;
            lastToastTimestamp = currentTime;
            lastToastDate = today;
            Log.d(TAG, "✅ Toast shown - Updated all flags");
            Log.d(TAG, "   lastToastDate (static): " + lastToastDate);
            Log.d(TAG, "   lastToastTimestamp (static): " + lastToastTimestamp);
        } else {
            Log.d(TAG, "⏭️ Toast skipped - one or more checks failed");
        }

        // 4️⃣ 发送广播通知所有Fragment刷新(Fragment不再显示Toast)
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