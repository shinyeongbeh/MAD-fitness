# Streak Data Flow
- Define View Model in UI files (fragment Java file)
- View Model files is used to connect UI to Repository files
- Repository files consists of different methods that call DAO methods, return data to View Model
- DAO files contains SQL queries to read/write data from/to Database
- UI (Fragment)
-   ↓
- ViewModel
-   ↓
- Repository
-   ↓
- DAO
-   ↓
- Entity (table)

**To Do:**
1. Connect calendar view to backend real data
  - create a method in StreakRepository to get data for calendar view, inside the method, call DAO method in StreakHistoryDao to get data from database
  - the DAO method to be called is getByDate(String date), and it will return StreakHistoryEntity Object for the given date. The DAO method is already created.
  - then, create new method in StreakViewModel to call the new method in StreakRepository
  - finally, in the DayDetailFragment Java file, call the new method in StreakViewModel to get data for calendar view
  - so when calendar view is added to UI, it should fetch all the streak data in this current month -> then can show whether the day is coloured or not based on whether the target is achieved for that day
2. Connect longest streak view to backend real data
  - Create a methods in StreakRepository to compute the longest streak from the database
    - The method can call DAO method getAchievedDaysDescLive() which is already created to get all achieved days in descending order. All the achieved days will be returned.
    - Then, compute the longest streak from the list of achieved days using Java logic, here requires some logical thinking learnt in FOP or Data Structure course. 
  - Create suitable methods in StreakViewModel to call the methods in StreakRepository
  - Finally, in the StreakFragment Java file, call the methods in StreakViewModel to get the longest streak data and display it in the UI
3. Connect Editing minStepsRequired / streak target functionality to the backend
  - same, need to create method in DAO to update the minStepsRequired field for a given date
  - then, create method in StreakRepository to call the DAO method
  - then, create method in StreakViewModel to call the method in StreakRepository
  - finally, in the fragment Java file, call the method in StreakView
3. UI live updates
  - Ensure that the UI components observing LiveData from StreakViewModel are properly updating when the underlying data changes in the database.
  - This means you should use **LiveData** in DAO methods so that when connecting to UI, can get automatic updates when a database row is changed. 
4. Testing
  - https://developer.android.com/studio/inspect/database
  - Use the Database Inspector tool in Android Studio to inspect the SQLite database while the app is running. 
  - You can modify the content in it easily, or run queries that is already defined in DAO files to see if it returns the expected results.


**Files**
- **`StreakHistoryEntity.java`:** StreakHistoryEntity.java  
  - Data model for one day's streak. Think of this as the schema for a single row in a table. Fields:
    - `date` (primary key, format "yyyy-MM-dd") — identifies the day.
    - `steps` — integer step count for that day.
    - `achieved` — boolean indicating if the day's target was reached.
    - `minStepsRequired` — the target steps for that day.
    - `lastUpdated` — timestamp when record was last changed.
- **`FitnessSyncWorker.java`:** FitnessSyncWorker.java  
  - Worker (background job) that runs periodic sync (30 min). The streak entity is updated every 30 min with the latest step count fetched from Recording API. 
- **`StreakHistoryDao.java`:** StreakHistoryDao.java  
  - DAO = Data Access Object. This interface contains the SQL queries the app uses to read and write streak records. Key methods:
    - `observeByDate(String date)` — returns a LiveData-wrapped `StreakHistoryEntity` for a date (UI can observe changes automatically).
    - `getByDate(String date)` — returns the entity synchronously (useful inside background threads).
    - `getAchievedDaysDescLive()` — returns LiveData list of achieved days ordered newest first.
    - `insertOrUpdate(StreakHistoryEntity)` — writes the record, replacing existing row on conflict.
- **`AppDatabase.java`:** AppDatabase.java  
  - Singleton Room database instance. Provides access to DAOs (including `streakHistoryDao()`) and manages write threads. Acts as the app’s local storage (backed by SQLite).
- **`StreakViewModel.java`:** StreakViewModel.java  
  - ViewModel used by UI screens. It asks `StreakRepository` for LiveData that the UI can observe:
    - current streak summary (`getStreakLiveData()`)
    - current day’s `StreakHistoryEntity` as LiveData (`getLiveStepsFromStreakEntity()`)

Simple linear diagram:
- `FitnessSyncWorker` → `FitnessRepository` (gets steps) → `StreakRepository.insertOrUpdateSteps()` → `AppDatabase` → `StreakHistoryDao.insertOrUpdate()` → DB row updated → Room notifies LiveData → `StreakViewModel` → UI.


**Glossary (plain language)**
- **Database:** A place in the app where data is stored persistently (here, a small local file on the device backed by SQLite).
- **Entity:** A data structure that maps to one table row in the database (here `StreakHistoryEntity`).
- **DAO (Data Access Object):** A set of methods that run SQL to read/write entities (here `StreakHistoryDao`).
- **Query:** An instruction to read or write data in the database (examples are `SELECT` and `INSERT`).
- **LiveData:** A live, observable data holder that UI can subscribe to — updates automatically when data changes.
- **Worker:** Background job that runs off the UI thread (here used to fetch step counts periodically).

**Quick checklist / where to look**
- Today’s sync logic: `FitnessSyncWorker.java`  
- Data schema: `StreakHistoryEntity.java`  
- DB access & queries: `StreakHistoryDao.java` and `AppDatabase.java`  
- UI-facing data: `StreakViewModel.java` and `StreakRepository` (repository not attached; check its implementation for business rules like how `achieved` is computed).
