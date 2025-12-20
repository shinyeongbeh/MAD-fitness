# Fitness Data Flow (Backend - DB - UI)

This document explains how fitness data moves through the app, which components are responsible, and how periodic sync is performed.

## Summary
1. UI layer: UI directly fetch data from Recording API for live updates
2. Background sync: a WorkManager worker periodically reads from Recording API and writes to Room DB (FitnessDataEntity). DB is synced every 30 minutes. 
3. **Other modules can read persisted data from Room via FitnessRepository, using `fetchDailyData(date)`**.

## Components (source files)
- `MainActivity`: [app/src/main/java/com/example/madgroupproject/main/MainActivity.java](app/src/main/java/com/example/madgroupproject/main/MainActivity.java)
- `RecordingAPIManager`: reads Google Fit / Recording API (in `fitnessmanager`).
- `FitnessSyncWorker`: [app/src/main/java/com/example/madgroupproject/fitnessmanager/FitnessSyncWorker.java](app/src/main/java/com/example/madgroupproject/fitnessmanager/FitnessSyncWorker.java)
- `FitnessRepository`: [app/src/main/java/com/example/madgroupproject/data/repository/FitnessRepository.java](app/src/main/java/com/example/madgroupproject/data/repository/FitnessRepository.java)
- `AppDatabase`: [app/src/main/java/com/example/madgroupproject/data/local/AppDatabase.java](app/src/main/java/com/example/madgroupproject/data/local/AppDatabase.java)
- `FitnessDataDao`: [app/src/main/java/com/example/madgroupproject/data/local/dao/FitnessDataDao.java](app/src/main/java/com/example/madgroupproject/data/local/dao/FitnessDataDao.java)
- `FitnessDataEntity`: [app/src/main/java/com/example/madgroupproject/data/local/entity/FitnessDataEntity.java](app/src/main/java/com/example/madgroupproject/data/local/entity/FitnessDataEntity.java)

## End-to-end flow (high level)
1. Google Fit / Recording API collects raw activity data on the device (steps, distance, calories).
2. `RecordingAPIManager` exposes two operations used by the app:
   - `subscribeToRecording(...)`: subscribes to continuous recording (called from `MainActivity.startTracking()`).
   - `readDailyTotals()`: reads aggregated totals for the day from Recording API.
3. `MainActivity` calls `RecordingAPIManager.subscribeToRecording()` and schedules a periodic WorkManager job:
   - `MainActivity.scheduleFitnessSync()` enqueues a `PeriodicWorkRequest` for `FitnessSyncWorker` every 30 minutes with `setRequiresBatteryNotLow(true)`.
4. `FitnessSyncWorker` runs on a background thread (WorkManager) and calls into `FitnessRepository.syncTodayFitnessData()`.
5. `FitnessRepository.syncTodayFitnessData()` calls `RecordingAPIManager.readDailyTotals()`, constructs a `FitnessDataEntity` (date, steps, distanceMeters, calories, lastUpdated), and writes it to the Room DB via `FitnessDataDao.insertOrUpdate(...)` (OnConflictStrategy.REPLACE).
6. Current app behavior: the on-screen UI reads live data directly from the Recording API (not the DB). For example, `FitnessDashboard` calls `RecordingAPIManager.readDailyTotals()` on a periodic loop and updates the TextViews.

> Note: the Room DB is updated independently by the background `FitnessSyncWorker` and therefore acts as a persisted copy of the daily totals rather than the live source that the UI currently observes.

## Data ownership & responsibilities
- RecordingAPIManager: interacts with Recording API and returns aggregated daily values.
- FitnessRepository: translates API results into `FitnessDataEntity` and **persists them to Room**. Also provides `fetchDailyData(date)` to read persisted data.
- AppDatabase + FitnessDataDao: storage layer; DAO uses `@Insert(onConflict = REPLACE)` so sync overwrites the existing row for the same date.
- FitnessSyncWorker: scheduled background worker that triggers repository sync periodically.

## Important implementation details observed
- Database: `AppDatabase` is a singleton and exposes `databaseWriteExecutor` (4-thread pool). Writes should be performed off the main thread.
- DAO: `getByDate(String date)` returns `FitnessDataEntity` directly (not LiveData / Flow). That means callers must not call it on the main thread to avoid blocking.
- Repository.syncTodayFitnessData(): called from the Worker is safe because WorkManager runs off the main thread. It constructs the entity for `LocalDate.now().toString()` and calls `insertOrUpdate`.
- Work schedule: `MainActivity` enqueues a unique periodic work named `fitness_sync_work` with policy KEEP, so existing scheduled work is preserved across app restarts.

## Sync frequency and constraints
- Sync frequency: 30 minutes (PeriodicWorkRequest in `MainActivity`).
- Constraints: `.setRequiresBatteryNotLow(true)`, worker will not run when battery is low.

## Quick sequence diagram (text)
- MainActivity -> RecordingAPIManager: subscribeToRecording()
- MainActivity -> WorkManager: enqueueUniquePeriodicWork(fitness_sync_work, 30min)
- WorkManager -> FitnessSyncWorker: doWork()
- FitnessSyncWorker -> FitnessRepository: syncTodayFitnessData()
- FitnessRepository -> RecordingAPIManager: readDailyTotals()
- RecordingAPIManager -> FitnessRepository: daily totals (steps, distance, calories)
- FitnessRepository -> FitnessDataDao: insertOrUpdate(FitnessDataEntity)
- Room DB -> UI (observer) : updated row -> UI updates (if observer used)
