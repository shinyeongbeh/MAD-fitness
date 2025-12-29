# Game Entities & Architecture
## To do:
1. Integrate UI with the backend game data model.
  - You might need to create new DAO methods, Repository methods, and ViewModel methods to support game features.
  - Repository and ViewModel Java file is not created yet.
2. Ensure `GameLevelEntity` always have data inside it. (This is the table that defines the levels and their targets)

### Fundamentals of backend data flow
- Define View Model in UI files (fragment Java file)
- View Model files is used to connect UI to Repository files
- Repository files consists of different methods that call DAO methods, return data to View Model
- DAO files contains SQL queries to read/write data from/to Database
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

## Purpose
Below are brief reference for the game-related data model and recommended responsibilities for DAOs, Repository, and ViewModel.

## Entities (what they represent)

- **GameLevelEntity** ([app/src/main/java/com/example/madgroupproject/data/local/entity/GameLevelEntity.java](app/src/main/java/com/example/madgroupproject/data/local/entity/GameLevelEntity.java))
  - Fields: `levelNum` (PK), `gameType` ("STEPS"|"DISTANCE"), `targetValue`.
  - Represents the definition and target of each level.

- **GameLevelHistoryEntity** ([app/src/main/java/com/example/madgroupproject/data/local/entity/GameLevelHistoryEntity.java](app/src/main/java/com/example/madgroupproject/data/local/entity/GameLevelHistoryEntity.java))
  - Fields: `levelNum` (PK), `gameType`, `completedDate` (yyyy-MM-dd).
  - Records completed levels for history and analytics.

- **GameProgressEntity** ([app/src/main/java/com/example/madgroupproject/data/local/entity/GameProgressEntity.java](app/src/main/java/com/example/madgroupproject/data/local/entity/GameProgressEntity.java))
  - Fields: `id`, `currentLevel`, `progressValue`, `lastSyncedFitnessValue`, `lastSyncedDate`.
  - Single-row store for the currently active level and accumulated progress.

## DAOs (responsibilities)

- Provide Room-backed data access for each entity: queries to read (prefer `LiveData` / `Flow`) and methods to insert/update/delete.
- Add convenience LiveData/Flow getters used by UI, for example:
  - `LiveData<GameProgressEntity> observeProgress();`
- Keep SQL and simple transactions inside DAO when appropriate; more complex cross-DAO operations belong in the Repository.

## Repository (what it should provide)

- Provide methods to connect ViewModel functions to DAO operations.
- May refer to other repositories or data sources if needed (e.g., StatisticsRepository, StreakRepository).

## ViewModel (what it should provide)

- Provide methods to connect UI (Java fragment) to Repository functions.
- May refer to other ViewModels if needed (e.g., StatisticsViewModel).

## Syncing & background updates
- data is automatically synced in the background via WorkManager, so the steps/distance data is updated periodically into game data models.
- but still in the process of testing by Shin

## Integration notes for UI
- Use **LiveData** so that UI components can observe data changes in DB automatically.
- Trigger manual refresh or user actions through ViewModel methods rather than calling DAOs directly.

## Testing tips
- To see what is in the database, https://developer.android.com/studio/inspect/database
- Use the Database Inspector tool in Android Studio to inspect the SQLite database while the app is running. 
- You can modify the content in it easily, or run queries that is already defined in DAO files to see if it returns the expected results.
- You can insert rows by using `dummyInsert()` in `FitnessDataDao.java` by clicking the icon besides the `dummyInsert()` code.
