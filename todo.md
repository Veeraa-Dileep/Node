# Node – Fitness Tracking App | Master TODO

> Derived from: `prd.md`, `designdoc.md`, `techstack.md`, `NameAndIcon.md`, `Insrtuction.md`

---

## Phase 0 — Project Setup

- [ ] Create new Android project (Kotlin, Jetpack Compose, Material 3)
- [ ] Configure Hilt for dependency injection
- [ ] Set up Room database with initial schema
- [ ] Add Navigation Compose for screen routing
- [ ] Configure StateFlow + Coroutines for state management
- [ ] Add library dependencies (Vico for charts, Coil for images, WorkManager)
- [ ] Set up JUnit + Compose UI test scaffolding
- [ ] Set app name to **"Node"** (from `NameAndIcon.md`)
- [ ] Set app icon to `Node_Logo.png` (from `NameAndIcon.md`)

---

## Phase 1 — Core Data Models & Workout Logging

### 1.1 Data Models (Room Entities)
- [ ] Create `WorkoutSession` entity (id, date, time, duration, notes)
- [ ] Create `Exercise` entity (id, sessionId, name, order)
- [ ] Create `SetEntry` entity (id, exerciseId, weight, reps, notes)
- [ ] Create `PersonalRecord` entity (id, exerciseName, maxVolume, date)
- [ ] Create `BodyWeight` entity (id, weight, timestamp)
- [ ] Create `BodyMeasurement` entity (id, chest, arms, waist, thighs, date)
- [ ] Create `ProgressPhoto` entity (id, filePath, date, notes)
- [ ] Define Room DAOs for all entities
- [ ] Create Room database class with all entities registered
- [ ] Write migration strategy for future schema changes

### 1.2 Repository Layer
- [ ] Create `WorkoutRepository` (CRUD for sessions, exercises, sets)
- [ ] Create `BodyDataRepository` (weight, measurements)
- [ ] Create `PhotoRepository` (progress photos)
- [ ] Create `PRRepository` (personal record queries)

### 1.3 Workout Logging Screen
- [ ] Build `LogWorkoutScreen` composable (full-screen, distraction-free)
- [ ] Add sticky header with live workout timer
- [ ] Create exercise card component (name + set rows)
- [ ] Implement set row inputs: weight, reps fields
- [ ] Add "Add Exercise" button
- [ ] Add "Add Set" button within each exercise card
- [ ] Add expandable notes field per exercise
- [ ] Implement inline PR indicator (icon + highlight on new PR)
- [ ] Auto-create session with current date/time on screen open
- [ ] Implement save/finish workout action
- [ ] Implement edit/delete for sets and exercises
- [ ] Ensure full offline functionality (Room persistence)
- [ ] Create `LogWorkoutViewModel` with StateFlow

### 1.4 Workout History Screen
- [ ] Build `HistoryScreen` composable
- [ ] Implement chronological list view (default)
  - [ ] Date header grouping
  - [ ] Workout summary card (exercises, duration, volume)
- [ ] Implement calendar view (toggle)
  - [ ] Highlight workout days on calendar
  - [ ] Tap day → show workout detail
- [ ] Add filter by exercise name
- [ ] Add search functionality
- [ ] Create `HistoryViewModel`

---

## Phase 2 — PR Detection & Body Tracking

### 2.1 Personal Records (PRs)
- [ ] Implement PR calculation logic: highest (weight × reps) per exercise
- [ ] Auto-detect and highlight new PRs during workout logging
- [ ] Create PR history view per exercise
- [ ] Ensure PRs are calculated locally (offline)

### 2.2 Body Weight Tracking
- [ ] Build weight entry screen (value + timestamp)
- [ ] Create line graph visualization using Vico
- [ ] Implement editable weight history
- [ ] Set up weekly weigh-in reminder via WorkManager

### 2.3 Body Measurements Tracking
- [ ] Build measurement entry screen (chest, arms, waist, thighs)
- [ ] Create historical view of measurements
- [ ] Create line graphs per measurement using Vico
- [ ] Set up monthly measurement reminder via WorkManager

---

## Phase 3 — Progress Photos, Dashboard & Reminders

### 3.1 Progress Photos
- [ ] Implement photo capture (camera) and gallery upload
- [ ] Auto-stamp photos with date
- [ ] Build chronological gallery view (rounded thumbnails, date overlay)
- [ ] Implement side-by-side compare mode (slider or tap-to-toggle)
- [ ] Store photos locally with backup support
- [ ] Set up 4-week photo reminder via WorkManager

### 3.2 Dashboard Screen
- [ ] Build `DashboardScreen` composable
- [ ] Add greeting header (e.g., "Good Evening") with current week selector
- [ ] Create stats summary card (workouts this week, total volume, total time)
- [ ] Add quick filter pills (Chest / Back / Legs / Arms)
- [ ] Show recent activity cards:
  - [ ] Last workout summary
  - [ ] Recent PRs
  - [ ] Upcoming reminders
- [ ] Add quick action buttons: Log Workout, Add Weight, Add Photo
- [ ] Create `DashboardViewModel`

### 3.3 Reminder System
- [ ] Implement WorkManager-based reminder scheduling
- [ ] Weekly weigh-in notification
- [ ] Monthly measurements notification
- [ ] 4-week progress photo notification
- [ ] Allow enabling/disabling individual reminders in Settings

---

## Phase 4 — Settings, Backup, Polish & APK

### 4.1 Progress Screen (Tabs)
- [ ] Build `ProgressScreen` with tabbed interface
- [ ] Tab 1 — PRs: list/graph of personal records
- [ ] Tab 2 — Weight: weight trend graph
- [ ] Tab 3 — Measurements: per-measurement graphs
- [ ] Tab 4 — Photos: gallery + compare mode
- [ ] Style graphs: dark background, accent-colored lines, minimal gridlines

### 4.2 Settings Screen
- [ ] Build `SettingsScreen` (simple list layout, no cards)
- [ ] Appearance section → dark mode toggle (default on)
- [ ] Reminders section → toggle weigh-in, measurements, photos reminders
- [ ] Data section → backup, restore, export actions

### 4.3 Backup & Restore
- [ ] Implement JSON export of all data
- [ ] Implement JSON import / restore
- [ ] Add file picker for restore flow
- [ ] Validate imported data before applying

### 4.4 Bottom Navigation
- [ ] Implement Material 3 bottom navigation bar
- [ ] Tabs: Dashboard, History, **Log Workout (center FAB)**, Progress, Settings
- [ ] Center "+" button for Log Workout (inspired by design doc)

### 4.5 Theming & UI Polish
- [ ] Apply dark theme colors: bg `#0E0E0E`, surface `#1A1A1A`, accent gradient `#FF9F1C → #FFB703`
- [ ] Set typography: Roboto/Inter, bold stats, muted metadata
- [ ] Style all cards: 16dp rounded corners, slight elevation, consistent padding
- [ ] Buttons: primary gradient, secondary outlined, destructive red text
- [ ] Icons: line style, high contrast
- [ ] Accessibility: 4.5:1 contrast, large tap targets, scalable fonts, focus states
- [ ] Micro-animations for logging completion
- [ ] Haptic feedback on PR detection

### 4.6 Performance & Testing
- [ ] Ensure <1s load time for history and graphs
- [ ] Write JUnit tests for ViewModels and repositories
- [ ] Write Compose UI tests for critical flows (logging, history, progress)
- [ ] Zero data-loss validation

### 4.7 Build APK
- [ ] Configure release build in `build.gradle`
- [ ] Generate signed APK via Android Studio
- [ ] Test APK on physical device
- [ ] Document APK build steps for future reference

---

## Future Enhancements (Post-v1)
- [ ] Exercise name normalization / auto-suggest
- [ ] CSV export
- [ ] Optional light mode
- [ ] Custom accent color themes
- [ ] Multi-device sync (Google Drive)
- [ ] Wearable integrations
- [ ] iOS version
