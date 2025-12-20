package com.example.madgroupproject.fitnessmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.fitness.FitnessLocal;
import com.google.android.gms.fitness.LocalRecordingClient;
import com.google.android.gms.fitness.data.LocalBucket;
import com.google.android.gms.fitness.data.LocalDataPoint;
import com.google.android.gms.fitness.data.LocalDataSet;
import com.google.android.gms.fitness.data.LocalDataType;
import com.google.android.gms.fitness.data.LocalField;
import com.google.android.gms.fitness.request.LocalDataReadRequest;
import com.google.android.gms.fitness.result.LocalDataReadResponse;
import com.google.android.gms.tasks.Tasks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RecordingAPIManager {
    private static final String TAG = "RecordingAPIManager";
    private final LocalRecordingClient recordingClient;

    public RecordingAPIManager(Context context) {
        this.recordingClient = FitnessLocal.getLocalRecordingClient(context);
    }

    // 1. Subscribe (Start Background Tracking)
    public void subscribeToRecording(Activity activity) {
        List<LocalDataType> dataTypes = Arrays.asList(
                LocalDataType.TYPE_STEP_COUNT_DELTA,
                LocalDataType.TYPE_DISTANCE_DELTA,
                LocalDataType.TYPE_CALORIES_EXPENDED
        );

        for (LocalDataType dataType : dataTypes) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 101);


            }
            recordingClient.subscribe(dataType)
                    .addOnSuccessListener(v -> Log.d(TAG, "Subscribed: " + dataType.toString()))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to subscribe", e));
        }
    }

    // 2. Read Data synchronously (Must be called from background thread)
    public DataRecordingAPI readDailyTotals() {
        long endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        long startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        LocalDataReadRequest readRequest = new LocalDataReadRequest.Builder()
                .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
                .aggregate(LocalDataType.TYPE_DISTANCE_DELTA)
                .aggregate(LocalDataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.SECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        int steps = 0;
        float distance = 0f;
        float calories = 0f;



        try {
            // 1. BLOCK and wait for the result here using Tasks.await()
            LocalDataReadResponse response = Tasks.await(recordingClient.readData(readRequest));

            for (LocalBucket bucket : response.getBuckets()) {
                if (bucket.getDataSets().isEmpty()) continue;

                LocalDataSet dataSet = bucket.getDataSets().get(0);
                for (LocalDataPoint dp : dataSet.getDataPoints()) {

                    String typeName = dp.getDataType().getName();
                    Log.i(TAG, "Data point:");
                    Log.i(TAG, "\tType: " + typeName);
//                    Log.i(TAG, "\tStart: " + dp.getStartTime(TimeUnit.MINUTES));
//                    Log.i(TAG, "\tEnd: " + dp.getEndTime(TimeUnit.MINUTES));


                    // Logic to extract values
                    if (typeName.equals("com.google.step_count.delta")) {

                        for (LocalField field : dp.getDataType().getFields()) {
                            steps = dp.getValue(field).asInt();
                            Log.i(TAG, "\tLocalField: " + field.getName()+" LocalValue: "+ dp.getValue(field));
                        }
                    }
                    else if (typeName.equals("com.google.distance.delta")) {
                        for (LocalField field : dp.getDataType().getFields()) {
                            distance = dp.getValue(field).asFloat();
                            Log.i(TAG, "\tLocalField: " + field.getName()+" LocalValue: "+ dp.getValue(field));
                        }
                    }
                    else if (typeName.equals("com.google.calories.expended")) {
                        for (LocalField field : dp.getDataType().getFields()) {
                            calories = dp.getValue(field).asFloat();
                            Log.i(TAG, "\tLocalField: " + field.getName()+" LocalValue: "+ dp.getValue(field));
                        }
                    }
                }
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error reading data", e);
        }

        return new DataRecordingAPI(steps, distance, calories);
    }

    // Simple POJO holder
    public static class DataRecordingAPI {
        public int steps;
        public float distance;
        public float calories;

        public DataRecordingAPI(int steps, float distance, float calories) {
            this.steps = steps;
            this.distance = distance;
            this.calories = calories;
        }
    }
}