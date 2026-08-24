# MediRemind — Medication Reminder & Alarm

A production-ready Android app (Kotlin + Jetpack Compose + Material 3) that manages a
precise, multi-phase medication schedule with exact-time notifications and a
full-screen alarm activity.

## Architecture

```
UI (Compose)  ──►  ViewModel (@HiltViewModel, StateFlow)
                        │
                        ▼
                  Use Cases (domain)
                        │
          Repository (interface) ◄── Hilt @Binds
                        │
          RepositoryImpl (Room + DataStore)
                        │
        ┌───────────────┴────────────────┐
   Local DB (Room)              Preferences (DataStore)
        │
   AlarmManagerHelper (setExactAndAllowWhileIdle)
        │
   AlarmReceiver ──► NotificationHelper (full-screen intent + actions)
   BootReceiver  ──► reschedules on reboot / time change
```

* **Clean Architecture** — `data` / `domain` / `presentation` / `alarm` layers.
* **MVVM** — `StateFlow` exposed from Hilt ViewModels, collected in Compose.
* **Hilt** — dependency injection (`@HiltAndroidApp`, `@AndroidEntryPoint`,
  `@HiltViewModel`, `@EntryPoint` for the BroadcastReceiver).
* **Room** — `MedicationEntity`, `IntakeLogEntity`, DAOs, `TypeConverters`
  (stores `List<String>` components and `LocalDate` as text).
* **Exact alarms** — `AlarmManager.setExactAndAllowWhileIdle` (Doze-safe),
  re-armed for the next day every time they fire. Handles `SCHEDULE_EXACT_ALARM`
  / `USE_EXACT_ALARM` and requests the permission from the UI when missing.

## Hardcoded seed schedule (`data/SeedData.kt`)

| # | Medication | Time | Notes | Active |
|---|------------|------|-------|--------|
| 1 | Grandaxin (50 mg, 1 tab) | 10:00 | after breakfast | Day 1–30 |
| 2 | IV & Injections Session (L-Lysine, Neuroxidol, Cortexin, B6) | 11:00 | 2 IV + 2 IM, after meal | Day 1–10 |
| 3 | Fevarin (50 mg, ½ tab) | 20:00 | after dinner, do not chew | Day 5–30 (delayed) |
| 4 | Rotalud (2 mg, 1 tab) | 21:30 | before sleep | Day 1–20 |

## Key files

* **Data** — `data/local/entity/*`, `data/local/Converters.kt`, `data/local/dao/*`, `data/local/AppDatabase.kt`, `data/repository/*`, `data/SeedData.kt`, `data/preferences/AppPreferences.kt`
* **Domain** — `domain/model/*`, `domain/repository/MedicationRepository.kt`, `domain/usecase/*`, `domain/util/ScheduleExtensions.kt`
* **Alarm engine** — `alarm/AlarmContract.kt`, `alarm/AlarmManagerHelper.kt`, `alarm/NotificationHelper.kt`, `alarm/AlarmReceiver.kt`, `alarm/BootReceiver.kt`
* **Presentation** — `presentation/theme/Theme.kt`, `presentation/viewmodel/*`, `presentation/ui/screen/*`, `presentation/ui/component/*`
* **Wiring** — `MedicationReminderApp.kt`, `MainActivity.kt`, `AlarmActivity.kt`, `di/*`

## Build

Requires Android SDK 34, JDK 17, and the Android Gradle Plugin toolchain.

```bash
./gradlew assembleDebug
```

### Runtime permissions
* **Notifications** (Android 13+): requested on first launch.
* **Alarms & reminders** (Android 12+): if not granted, a dialog opens
  *Settings → Alarms & reminders* for the app, then re-schedules.

## Behaviour notes
* The daily alarm chain is rebuilt on every `BOOT_COMPLETED`, `TIME_SET`,
  `TIMEZONE_CHANGED` and `MY_PACKAGE_REPLACED`.
* “Mark as Taken” / “Snooze 10 min” are wired both as notification actions
  (when the phone is locked / heads-up) and as large buttons in the full-screen
  `AlarmActivity`.
* Snooze sets a one-shot exact alarm 10 minutes out (does not extend the daily chain).
