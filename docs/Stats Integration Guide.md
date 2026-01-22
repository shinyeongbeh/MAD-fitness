# Stats Integration — Quick Guide

Purpose: Short, beginner-friendly steps to connect the stats UI to the Room backend. Edit mainly `StatsFragment.java`.

Files to know
- `StatisticsViewModel.java`: call `setSelectedDate("yyyy-MM-dd")` and `setSelectedMonth("yyyy-MM")`. Observe LiveData: `getDailyStats()`, `getMonthlyTotalStats()`, `getMonthlyAverageStats()`.
- `StatisticsRepository.java`: bridges ViewModel → DAO. Use its data classes `MonthlyTotalStats` and `MonthlyAverageStats`.
- `FitnessDataDao.java`: provides LiveData queries: daily rows, monthly SUMs, monthly AVGs. Has `dummyInsert()` for quick testing.
---
- UI (Fragment)
-   ↓
- ViewModel
-   ↓
- Repository
-   ↓
- DAO
-   ↓
- Entity (table)

## Fundamentals of backend data flow
- Define View Model in UI files (fragment Java file)
- View Model files is used to connect UI to Repository files
- Repository files consists of different methods that call DAO methods, return data to View Model
- DAO files contains SQL queries to read/write data from/to Database
- UI (Fragment)

## Quick steps (what to change)
1. Connect UI to backend in `StatsFragment.java`
2. When the user selects a date/month, format it and call the ViewModel:
   - Date: `viewModel.setSelectedDate("yyyy-MM-dd");`
   - Month: `viewModel.setSelectedMonth("yyyy-MM");`
4. Observe LiveData and update UI safely (always check for null/empty). Example:

```java
viewModel.getDailyStats().observe(getViewLifecycleOwner(), result -> {
    if (result != null && !result.isEmpty() && result.get(0) != null) {
        FitnessDataEntity stats = result.get(0);
        dailySteps.setText(String.valueOf(stats.steps));
        dailyDistance.setText(String.valueOf(stats.distanceMeters));
        dailyCalories.setText(String.valueOf(stats.calories));
    } else {
        dailySteps.setText("No data");
        dailyDistance.setText("-");
        dailyCalories.setText("-");
    }
});
```

## Testing tips
- To see what is in the database, https://developer.android.com/studio/inspect/database
- Use the Database Inspector tool in Android Studio to inspect the SQLite database while the app is running. 
- You can modify the content in it easily, or run queries that is already defined in DAO files to see if it returns the expected results.
- You can insert rows by using `dummyInsert()` in `FitnessDataDao.java` by clicking the icon besides the `dummyInsert()` code.