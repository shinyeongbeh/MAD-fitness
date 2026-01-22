# StepUp 🏃‍♂️

A gamified fitness tracking Android application that motivates users to stay active through streaks, levels, and personalized goals.

## 📱 Features

- **Live Fitness Tracking** — Real-time step counting, distance tracking, and calorie monitoring via Google Fit API
- **Daily Streak System** — Track consecutive days of meeting step goals with calendar visualization
- **Gamified Levels** — Level-up system based on cumulative steps/distance to unlock achievements
- **Statistics Dashboard** — View fitness analytics with daily, weekly, and monthly breakdowns
- **Custom Goal Setting** — Create personalized fitness goals with automatic daily reset
- **Background Sync** — Automatic periodic sync (every 15 minutes) to persist fitness data
- **Smart Notifications** — Push notifications for goal reminders and streak achievements
- **User Profile & Settings** — Dark mode, theme options, notification preferences

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Platform** | Android (SDK 29+) |
| **Language** | Java 17, Kotlin (Gradle scripts) |
| **UI** | Material Design 3, Jetpack Compose |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Database** | Room Persistence Library (SQLite) |
| **Background Tasks** | WorkManager |
| **Fitness Data** | Google Fit Local Recording API |
| **Navigation** | Jetpack Navigation Component |

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                            │
│         (Fragments: Stats, Streak, Game, Goals, etc.)       │
└─────────────────────────┬───────────────────────────────────┘
                          │ observes LiveData
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                        │
│   (StatisticsViewModel, StreakViewModel, GameLevelViewModel)│
└─────────────────────────┬───────────────────────────────────┘
                          │ calls methods
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                     Repository Layer                        │
│    (FitnessRepository, StreakRepository, GoalRepository)    │
└─────────────────────────┬───────────────────────────────────┘
                          │ executes queries
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                        DAO Layer                            │
│      (FitnessDataDao, StreakHistoryDao, GameLevelDao)       │
└─────────────────────────┬───────────────────────────────────┘
                          │ read/write
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   Room Database (SQLite)                    │
│   Tables: fitness_data, streak_history, game_levels, etc.   │
└─────────────────────────────────────────────────────────────┘
```

## 📖 Documentation

Documentation is available in the `/docs` folder:
- [Fitness Data Flow](docs/Fitness%20Data%20Flow.md) — How fitness data moves through the app
- [Streak Data Flow & Integration Guide](docs/Streak%20Data%20Flow%20&%20Integration%20Guide.md) — Streak data integration guide
- [Stats Integration Guide](docs/Stats%20Integration%20Guide.md) — Statistics UI integration guide
- [Game Integration Guide](docs/Game%20Integration%20Guide.md) — Game data integration guideline

## 👥 Team

- Mobile Application Development Group Project  
- Universiti Malaya
- Team name: Group EverythingWillBeOk
- Team members:
  - Beh Shin Yeong
  - Teng Wei Yi
  - Zhu Jiayi
  - Wong Fang Yee
  - Chaw Yu En
  - Shi Nian

## 📄 License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.
