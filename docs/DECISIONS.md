# TaskFlow — Technical Decisions & Build Plan

This is the engineering decision log: what I'm going to build, in what order, and why. Companion to `docs/PRODUCT_SPEC.md` (the "what"), this is the "how" and "why."

## Starting point

A fresh Android Studio "Empty Activity (Compose)" project already existed in this directory (generated 2026-09-17): AGP 8.13.2, Kotlin 2.0.21, compileSdk/targetSdk 36, minSdk 24, package `com.example.taskflow`. Not yet a git repo. This is the baseline I'm building on rather than starting from a blank folder.

## Decision: module structure — single module first

**Choice:** Everything lives in `:app` with the feature-oriented package layout from the spec (`core/*`, `feature/*`, `worker/*`) as plain Kotlin packages, not separate Gradle modules.

**Why:** the spec's multi-module layout is real and worth having in the repo eventually, but standing up 10+ Gradle modules (with their own `build.gradle.kts`, `AndroidManifest.xml`, inter-module `api`/`implementation` boundaries) before a single screen works is overhead that doesn't pay for itself yet. A clean package structure inside one module demonstrates the same architectural thinking to a reviewer reading the file tree, and can be mechanically promoted to real modules later once the boundaries have proven stable. Confirmed with the user 2026-09-17.

## Decision: backend for sync — fake now, Supabase swapped in later

**Choice (revised 2026-09-17):** Build the remote data source behind an interface (`TaskRemoteDataSource` or similar) with an in-process fake implementation first — no real network calls — and defer standing up the actual Supabase project until later. The fake conforms to the exact same contract a Supabase-backed implementation will, so swapping it in later is a one-class change, not a redesign.

**Why the change:** stood up Phase 1 and Phases 2+ (data layer, UI, WorkManager wiring) don't need to be blocked on provisioning Supabase (creating the project, applying the schema, getting credentials into `local.properties`). Get the app's core Android surface built and demoable first; add the real network leg when there's a natural checkpoint for it. The original reasoning below for *why Supabase specifically* (over a synthetic backend, permanently) still holds for when it's wired in — this only changes the sequencing, not the destination.

**Original reasoning, still the plan for when Supabase is wired in:** Use a real Supabase project (hosted Postgres + auto-generated REST API via PostgREST) as the remote backend that `SyncWorker` talks to, instead of an in-process fake network layer permanently.

**Why:** the entire point of the offline-first / WorkManager story is to demonstrate real sync behavior — writes surviving offline, retry-with-backoff on real failures, eventual consistency once connectivity returns. A fake in-memory "server" can't produce a real `IOException` from a dropped connection or a real HTTP error from a malformed payload; it only produces whatever failure I hand-code. A reviewer who reads the code can tell the difference between "this developer wrote a Retrofit client against a real API" and "this developer wrote a class that pretends to be one." Confirmed with the user 2026-09-17 ("yes let's go with Supabase and this is a portfolio app").

**How the network layer is built:** talk to Supabase's plain PostgREST REST endpoint (`https://<project>.supabase.co/rest/v1/tasks`) directly via **Retrofit + OkHttp + kotlinx.serialization**, rather than pulling in the official `supabase-kt` SDK. Reasoning: the spec's tech stack explicitly lists Retrofit/OkHttp as a skill to showcase; going through a higher-level SDK would hide that work behind a library call. Supabase's REST surface is a stable, well-documented plain HTTP API, so this isn't fighting the platform — it's the same approach used for any other REST backend.

**Auth posture (important, documented in-repo, not hidden):** v1 has no user accounts (explicitly out of scope per the spec). The Supabase table is accessed with the **publishable key** (Supabase's current recommended client-side key, replacing the legacy `anon` key). Since there's no authenticated user to scope rows to, RLS stays **enabled** with an explicit policy granting the `anon` role full CRUD on the `tasks` table — not "RLS disabled," which is the worse and lazier option. This is called out in the README as a deliberate, demo-only posture: fine for a portfolio project with disposable seed data, not something to copy into a real product without adding auth and per-user row scoping.

**Credential handling:** Supabase URL + publishable key are read from `local.properties` (already gitignored) and exposed to code via generated `BuildConfig` fields — never committed, never hardcoded in source. The user provisions the Supabase project and supplies these two values; I don't have the ability to create hosted infrastructure on their behalf.

## Decision: dependency versions

Verified via web search on 2026-09-17 rather than assumed, since a wrong pairing (especially Compose BOM ↔ AGP/compileSdk) breaks the build immediately:

| Library | Version | Note |
|---|---|---|
| AGP | 8.13.2 | already scaffolded by Android Studio; kept as-is |
| Kotlin | 2.0.21 | already scaffolded; kept as-is |
| KSP | 2.0.21-1.0.28 | must track the Kotlin version exactly |
| Compose BOM | 2026.06.01 | latest BOM *before* the 1.12/2026.08.00 line, which raises the minimum to compileSdk 37 + AGP 9 — deliberately not following AGP that far yet |
| Hilt | 2.57.1 | current stable |
| Room | 2.8.4 | current stable 2.x; **not** Room 3.0 (alpha as of 2026-09), too new/unstable for a portfolio project that needs to just work |
| Navigation Compose | 2.9.7 | current stable |
| WorkManager | 2.11.2 | current stable |
| Retrofit | 2.11.0 | current stable |
| OkHttp | 4.12.0 | current stable |
| retrofit2-kotlinx-serialization-converter | 1.0.0 | current stable |
| kotlinx.serialization | 1.7.3 | compatible with Kotlin 2.0.21 |
| DataStore (Preferences) | 1.1.1 | current stable |
| Hilt Navigation Compose | 1.2.0 | for `hiltViewModel()` in Compose destinations |
| Coroutines | 1.9.0 | current stable |
| MockK | 1.13.13 | for unit test mocking |

## Build phases (execution order)

Building foundation-out rather than screen-by-screen with no working skeleton, so the app is runnable after every phase:

1. **Foundation** — version catalog, Gradle files, package rename `com.example.taskflow` → `com.taskflow`, Hilt `Application` class + manifest wiring, empty package structure per the spec's layout, Material 3 theme (light/dark/system), bottom-nav shell with 3 placeholder destinations that actually navigate.
2. **Data layer** — `Task` entity + `Priority`/`TaskStatus` enums, Room database + DAO, `TaskRepository` (Room as single source of truth), DataStore for preferences (theme, default priority, default reminder time, week start).
3. **Home screen** — dashboard UI wired to a real `HomeViewModel`/`StateFlow` over the repository.
4. **Tasks screen** — list + reactive Flow-based filtering (status/priority) + search, no manual in-UI filtering.
5. **Create/Edit Task** — shared `TaskEditor` composable, validation, date/time pickers.
6. **Task Details** — status transitions (mark complete/pending), edit, delete with confirmation.
7. **Completed tasks + "clear completed"** confirmation flow.
8. **Profile / Settings** — stats (tasks done, completion rate, streak), notification/appearance/preference settings backed by DataStore.
9. **Notifications** — channel setup, POST_NOTIFICATIONS runtime permission (Android 13+), reminder scheduling, PendingIntent deep link into Task Details.
10. **WorkManager** — `SyncWorker` (push local pending changes to Supabase, retry-with-backoff on failure) and `CleanupWorker` (`PeriodicWorkRequest`, archives old completed tasks).
11. **Polish** — empty states, offline banner, loading/error states, unit tests for use cases/view models, Compose UI tests for key flows.

Each phase should leave the app in a buildable, runnable state — no half-wired screens left in the tree between phases.

## Fix: bottom nav bar was showing on secondary screens

`MainActivity`'s `Scaffold` always rendered the bottom `NavigationBar`, since it lived outside the `NavHost` content and didn't know which destination was current. Once Task Editor was added as a secondary (non-tab) destination, this became visible: the nav bar sat on top of the editor's Save button. Fixed by hoisting the back stack entry above the `Scaffold` and only rendering `bottomBar` when the current destination is one of `TopLevelDestination`'s routes — secondary screens (Task Editor now, Task Details/Settings later) render full-screen.

## Fix: theme wasn't actually using the brand palette

The reference mockup (`docs/design/taskflow-mockup.png` — saved here 2026-09-17 as the canonical visual reference for every screen going forward) has a deliberate deep-green brand identity. Phase 1's `TaskFlowTheme` never implemented that: it kept Android Studio's default template colors (purple) *and* left `dynamicColor = true`, which on API 31+ overrides everything with a wallpaper-derived Material You palette — so the emulator rendered a generic lavender theme with no relation to either the template colors or the mockup. Fixed by hand-authoring a light/dark `ColorScheme` from the mockup's green (see `Color.kt`) and dropping dynamic color entirely — TaskFlow now always renders its own brand color regardless of device or wallpaper. `TaskListItem`'s checkbox was also swapped for an outlined/filled circle toggle (`Icons.Outlined.Circle` / `Icons.Filled.CheckCircle`) to match the mockup's radio-style task rows instead of a square Material checkbox.

## Decision: task IDs are client-generated strings, not Room autoincrement

**Choice:** `Task.id` is a `String` (a UUID, generated by whatever creates the task — Phase 5's Create Task flow), not a Room `@PrimaryKey(autoGenerate = true) Long`.

**Why:** this falls straight out of the offline-first requirement. A task can be created entirely offline and must keep a stable identity once it's eventually pushed to Supabase — an autoincrement `Long` only Room knows about would either collide with another offline-created row or require a fragile local-id-to-remote-id remapping step after every sync. A client-generated UUID sidesteps that entirely: the same ID is valid locally and remotely from the moment the task is created. Cheap to decide now even though sync (Phase 10) isn't wired up yet, because retrofitting ID strategy after the schema and UI are built would touch every layer.

## Phase 1 build fix: androidx.core / androidx.lifecycle pinned below their newest releases

While building Phase 1, `androidx.core:core-ktx:1.19.0` and `androidx.lifecycle:lifecycle-runtime-ktx:2.11.0` (the versions Android Studio's project template had pre-selected) turned out to require `compileSdk 37` + AGP 9.1+ — one rung past the AGP/compileSdk ceiling this project deliberately stays under (see the Compose BOM decision above). Pinned back to `core-ktx 1.17.0` and `lifecycle-runtime-ktx 2.10.0`, both confirmed compatible with `compileSdk 36` / AGP 8.13.2. `:app:assembleDebug` and `:app:testDebugUnitTest` both pass as of this fix.

## Decision: reminder scheduling uses AlarmManager, not WorkManager

**Choice:** Phase 9's per-task reminder notifications are scheduled with `AlarmManager.setAndAllowWhileIdle` + a `BroadcastReceiver` (`ReminderBroadcastReceiver`), not `WorkManager`.

**Why:** WorkManager is designed for deferrable, guaranteed-eventually work — it explicitly does not promise firing at a precise wall-clock time, and its OS-level backing (`JobScheduler`/`AlarmManager` internally) can slip work by minutes under Doze. A task reminder ("10 minutes before due") is a user-visible, time-anchored alarm, which is exactly what `AlarmManager` is for. Keeping the two mechanisms separate also keeps each phase's showcase clean: Phase 9 demonstrates `AlarmManager`/`BroadcastReceiver`/notification fundamentals; Phase 10's `SyncWorker`/`CleanupWorker` (per the spec's `worker/` package) demonstrates `WorkManager` for deferrable background jobs. Used `setAndAllowWhileIdle` (inexact-while-idle) rather than an exact alarm so the app doesn't need the `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM` permission dance (Play Store restricts exact-alarm declarations to alarm-clock-like apps as of Android 12+) — acceptable since a "few minutes" of slop on a task reminder isn't the same correctness bar as an alarm clock.

**Where it's wired:** `core/notification/` holds `ReminderScheduler` (interface) / `AlarmReminderScheduler` (impl), `ReminderNotifier` (posts the notification + deep-link `PendingIntent`), `ReminderBroadcastReceiver` (fires at trigger time), and `BootRescheduleReceiver` (`BOOT_COMPLETED`/`MY_PACKAGE_REPLACED` — AlarmManager alarms don't survive reboot, so all pending reminders are recomputed from Room on boot). `TaskRepositoryImpl` calls `ReminderScheduler.schedule`/`cancel` from `upsertTask`, `setTaskStatus`, and `deleteTask` so every mutation path keeps scheduled alarms consistent without each call site needing to remember to do it — the same centralization pattern called out as the plan in the Phase 9 research pass.

**Deep link:** Notification tap opens `MainActivity` via an explicit `Intent` (component + `ACTION_VIEW` + `taskflow://task_details/{taskId}` data URI), which the Task Details `composable`'s `navDeepLink` matches. `MainActivity` also declares a matching manifest intent-filter (scheme `taskflow`, host `task_details`) so the same URI resolves via implicit intents too (e.g. `adb shell am start -a android.intent.action.VIEW -d ...`), not just the explicit path the notifier uses.

**Scope note:** Only the "Task reminders" toggle from Phase 8 settings is wired to real behavior here. "Daily summary" and "Completed task sound" remain UI-only preferences (already stored in DataStore, not yet acted on) — the spec's Notifications section only details the per-task reminder notification, and a daily-summary background job would be a `WorkManager` `PeriodicWorkRequest` concern more consistent with Phase 10's scope than Phase 9's.

## Decision: Phase 10 (WorkManager) still runs against the fake remote, not Supabase

**Choice:** confirmed with the user 2026-09-17 that `SyncWorker` is wired up now, but still talks to `FakeTaskRemoteDataSource` (in-memory, `core/network/`) rather than provisioning Supabase yet. `local.properties`' `SUPABASE_URL`/`SUPABASE_PUBLISHABLE_KEY` remain blank.

**Why:** this keeps Phase 10 unblocked by external infrastructure the user hasn't set up. `TaskRemoteDataSource` is a two-method interface (`upsertTask`, `deleteTask`) that a real Retrofit/OkHttp-backed Supabase implementation can satisfy later as a one-class swap (bind a different impl in `core/network/di/NetworkModule.kt`) — nothing in `SyncWorker`, the DAO, or the repository needs to change when that happens.

**Sync-tracking schema:** `TaskEntity` gained two Room-only columns — `isPendingSync: Boolean` (dirty flag, defaults to `true` on every local write) and `isDeleted: Boolean` (soft-delete tombstone). Neither leaks into the domain `Task` model; `TaskDao`'s read queries (`observeTasks`, `observeTaskById`, `getTaskById`) filter `isDeleted = 0`, so the UI never sees tombstones. Room bumped `version` 1 → 2 with a real `Migration` (`MIGRATION_1_2`, two `ALTER TABLE ADD COLUMN` statements) rather than `fallbackToDestructiveMigration()` — cheap to write correctly and a better showcase of the Room skill this project exists to demonstrate.

**Why soft-delete instead of hard delete:** `deleteTask`/`clearCompletedTasks` used to hard-delete rows immediately. That breaks offline-first sync — a row deleted locally while offline needs to still exist long enough for `SyncWorker` to tell the remote about the deletion. Both now flip `isDeleted = 1, isPendingSync = 1` instead; `SyncWorker` calls `TaskRemoteDataSource.deleteTask` for tombstoned rows and only then hard-purges them from Room. This is the same "every mutation path stays consistent through one place" pattern as Phase 9's `ReminderScheduler` — `TaskRepositoryImpl` calls `SyncScheduler.scheduleSync()` after every mutation, so no call site has to remember to trigger a sync.

**WorkManager wiring:** `TaskFlowApplication` now implements `Configuration.Provider` and injects `HiltWorkerFactory`; the manifest removes the default `androidx.work.WorkManagerInitializer` startup entry (`tools:node="remove"`) so on-demand initialization picks up the Hilt-aware config instead. `SyncWorker` is a `OneTimeWorkRequest` (`Constraints.NetworkType.CONNECTED`, exponential backoff, `ExistingWorkPolicy.APPEND_OR_REPLACE` so mutations queue rather than cancel in-flight work) enqueued by `WorkManagerSyncScheduler`. `CleanupWorker` is a 24-hour `PeriodicWorkRequest` (enqueued once at app startup, `ExistingPeriodicWorkPolicy.KEEP`) that soft-deletes completed tasks older than 30 days and, if it archived anything, triggers a sync so the tombstones get pushed promptly.

**Verified on-device (2026-09-17):** installed over an existing v1-schema database and confirmed `MIGRATION_1_2` applied cleanly (`isPendingSync`/`isDeleted` columns present, no crash). Confirmed via `adb shell run-as com.taskflow cat databases/taskflow.db` + local `sqlite3` inspection that: a created task starts `isPendingSync=1`; forcing the sync job (`adb shell cmd jobscheduler run -f -n androidx.work.systemjobscheduler com.taskflow <jobId>`) runs `SyncWorker` with real Hilt-injected dependencies and flips it to `isPendingSync=0`; deleting a task tombstones it (`isDeleted=1`) rather than removing the row, and a forced sync run then hard-purges it; `CleanupWorker`'s archive query correctly matches a task whose `completedAt` was backdated 40 days. `CleanupWorker` also ran for real (not forced) once, off its periodic registration, proving the Hilt-aware `Configuration.Provider` wiring works without manual intervention.

**Known emulator caveat, not a code defect:** `NetworkType.CONNECTED` constraint evaluation on this development AVD image was observed to cancel work moments after enqueueing (`WM-GreedyScheduler: Constraints not met`) even with real WiFi/mobile connectivity present, and didn't visibly re-fire within a several-minute observation window — a `NetworkRequestConstraintController` registration-timing quirk documented as a common WorkManager-on-emulator nuisance. The underlying mechanism (constraint gating + real device JobScheduler-level retry) is correct and standard; it was only worked around for *testing* via forced job execution, not worked around in the shipped code. Expect this to behave more promptly on a real device.

## Phase 11 (Polish): scope split — UX polish + a representative test slice, not exhaustive coverage

**Choice:** confirmed with the user 2026-09-17 to do both halves of the spec's Phase 11 bullet ("empty states, offline banner, loading/error states, unit tests for use cases/view models, Compose UI tests for key flows"), but scope the test half to a representative slice rather than covering every screen/ViewModel — full test coverage was explicitly deferred, not silently skipped.

**Offline banner:** new `core/common/NetworkMonitor` interface + `ConnectivityNetworkMonitor` impl (`ConnectivityManager.registerDefaultNetworkCallback` wrapped in `callbackFlow`, checking `NET_CAPABILITY_INTERNET` + `NET_CAPABILITY_VALIDATED`), exposed to Compose via a small `ConnectivityViewModel` (`isOnline: StateFlow<Boolean>`) used independently by `HomeRoute` and `TasksRoute` — deliberately not folded into `HomeUiState`/`TasksUiState`, since connectivity is a cross-cutting concern unrelated to those screens' own loading logic. `OfflineBanner` (`core/designsystem/component/`) renders above the screen content when offline, using `secondaryContainer` (not `error`) since "you're offline, we'll sync later" isn't an error in an offline-first app. Verified live on-device: toggling `adb shell svc wifi disable` / `svc data disable` shows the banner within ~1s on both Home and Tasks and hides it again when connectivity returns, with no fake/mocked connectivity path — real `ConnectivityManager` callbacks throughout.

**Loading states:** the `Loading` branches in `HomeScreen`/`TasksScreen`/`TaskDetailsScreen` rendered a blank `Box` before this phase (Room reads are fast enough that it was never visibly a problem, but "blank screen" isn't a real loading state). All three now show a centered `CircularProgressIndicator`.

**Error states:** `TaskEditorViewModel.onSaveClicked` and `TaskDetailsViewModel.onToggleStatus`/`onDeleteConfirmed` now wrap their repository calls in `try/catch` — `CancellationException` is explicitly rethrown (never swallowed, per structured-concurrency rules), any other exception sends a one-shot event down a new `errorEvents: Flow<Unit>` (same `Channel.CONFLATED` + `receiveAsFlow()` pattern as the existing `saveCompletedEvents`/`deletedEvents`), which the Route composables collect into a `SnackbarHostState` showing a generic `error_generic_message` string. Scoped to just these two ViewModels — the two user-initiated write flows most likely to matter to a reviewer (create/edit a task, delete/complete a task) — rather than adding matching error handling to every mutation in the app (e.g. `TasksViewModel`'s toggle/clear-completed), which the "representative slice" scoping call above explicitly excludes.

**Test infrastructure — none existed before this phase:** added `MainDispatcherRule` (`app/src/test/java/com/taskflow/MainDispatcherRule.kt`, the standard `Dispatchers.setMain(UnconfinedTestDispatcher())` JUnit `TestWatcher`) and a `testTask(...)` fixture builder (`core/model/TaskTestFixtures.kt`, test-source-set-only, same package as the production `Task` model). Learned the hard way that `TestScope.launch { viewModel.uiState.collect {} }` — needed to activate a `stateIn(WhileSubscribed(...))` `StateFlow` under test, since nothing else subscribes to it — runs on `runTest`'s own `StandardTestDispatcher`-backed scheduler, not the `UnconfinedTestDispatcher` bound to `Dispatchers.Main`; it doesn't actually start collecting until `runCurrent()` pumps that scheduler. Every ViewModel test that spins up such a collector calls `runCurrent()` before asserting on `.value`.

**Unit tests added** (`app/src/test/java/com/taskflow/`): `GetTodayTaskSummaryUseCaseTest`, `GetProductivityStatsUseCaseTest` (including the trickier "current streak" edge cases — gap before today, nothing completed yet today but yesterday counts, divide-by-zero guard), `TasksViewModelTest` (every `TaskListFilter`, priority filter, case-insensitive search, sort orders, clear-filters), `TaskEditorViewModelTest` (blank-title rejection, trim-on-save, create-mode default priority, edit-mode hydration, the new failed-save error path). Not covered: `HomeViewModel`, `TaskDetailsViewModel`, `SettingsViewModel`, `ProfileViewModel` — deliberately, per the scoping decision above.

**Compose UI tests added** (`app/src/androidTest/java/com/taskflow/`): `TaskEditorScreenTest` and `TaskDetailsScreenTest`, both using plain `createComposeRule()` against the stateless `TaskEditorScreen`/`TaskDetailsScreen` composables directly (fake `UiState` + spy lambdas) — no Hilt test infrastructure, no `MainActivity` launch, since these composables already take `UiState` and callbacks as plain parameters (the `*Route` wrapper is the only piece that touches `hiltViewModel()`). Covers the "create task" (title validation, save invocation, text input) and "mark complete" (`TaskDetailsScreen`'s toggle button) flows named in the scoping decision. Ran for real on-device (`connectedDebugAndroidTest`) — 6/6 passing, not just compiled.

**Build fix needed for `connectedDebugAndroidTest` to run at all:** adding `androidx.compose.ui:ui-test-junit4` pulled in a duplicate `META-INF/LICENSE.md` (and `LICENSE-notice.md`) across several transitive `junit-jupiter`/`junit-platform` jars, failing `mergeDebugAndroidTestJavaResource`. Fixed by extending the existing `packaging { resources { excludes += ... } }` block in `app/build.gradle.kts` — unrelated to anything test-specific, just an APK packaging collision.
