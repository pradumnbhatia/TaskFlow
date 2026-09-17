# TaskFlow — Product Specification

> **TaskFlow — Smart, Offline-First Task Management**
> Simple tasks. Big progress. Organize today. Build a better tomorrow.

This document captures the full product design as agreed before any code was written (2026-09-17). It is the source of truth for what v1 (MVP) includes and excludes.

## Purpose

TaskFlow is the first entry in a portfolio series of small, production-shaped Android apps meant to demonstrate real Android engineering skill (not just UI demos) to prospective Upwork clients browsing the GitHub repo. It should read as a small production app, while the code underneath specifically showcases Android fundamentals, Room, and WorkManager.

Planned portfolio series (for context, not built here):
- **TaskFlow** → Android fundamentals + Room + WorkManager
- Food Delivery → KMP
- Multiplayer → WebSockets + real-time architecture
- StreamBox → Media3 + adaptive/foldable UI
- Leaderboard → algorithms + modular architecture

## 1. Navigation

Bottom navigation with 3 top-level destinations: **Home**, **Tasks**, **Profile**. Secondary screens are pushed from these.

## 2. Home screen (main showcase screen)

- Greeting ("Good morning, {name}")
- Today's progress card: total tasks, completed count, completion percentage
- "Today's Tasks" list with priority + due time, "See all" link
- FAB to create a task
- Empty state when there are no tasks

Demonstrates Compose UI state handling.

## 3. Tasks screen

- Segmented filters: All / Today / Upcoming / Completed
- Search icon
- Task cards: title, status circle, priority dot + label, due day/time, overflow menu
- Interactions: tap → Task Details, swipe → complete, long press → actions, search, filter, sort
- Filter sheet: Status (All/Pending/Completed), Priority (High/Medium/Low)

Demonstrates reactive filtering via Flow rather than manual UI-side filtering.

## 4. Create Task

Reached via FAB. Fields:
- Title (required — validation: "Task title is required" when empty)
- Description (optional)
- Priority: Low / Medium / High (segmented, default Medium)
- Due date (date picker)
- Due time (time picker)
- Reminder toggle
- Save Task button

## 5. Task Details

- Title, priority badge, description
- Due date/time
- Reminder setting ("10 minutes before")
- Created date, Status
- "Mark as Completed" primary action
- Edit / Delete secondary actions

Demonstrates state transitions (Pending → Completed, edit, delete).

## 6. Edit Task

Same UI/component as Create Task, pre-populated (`CreateTaskScreen` and `EditTaskScreen` share one `TaskEditor` component). Demonstrates reusable Compose components.

## 7. Completed Tasks

Reached via the Tasks filters. Shows completed tasks with relative completion date ("Completed today/yesterday/N days ago"). "Clear completed tasks" action behind a confirmation dialog (no silent destructive delete).

## 8. Profile / Statistics

Turns the Profile tab into a productivity dashboard rather than a dead-end settings link:
- Avatar + name
- Stats: Tasks Done, Completion Rate, Current Streak
- Settings entry points: Notifications, Appearance, Preferences, About TaskFlow

## 9. Settings

- **Notifications**: Task reminders (on/off), Daily summary (on/off), Completed task sound (on/off)
- **Appearance**: System / Light / Dark
- **Preferences**: default task priority, default reminder time, week start day (Mon/Sun)

## 10. Notifications

- Notification channels
- Runtime notification permission (Android 13+)
- PendingIntent → deep link into task details
- Background scheduling
- Example: "🔔 Task reminder — Fix payment API — Due in 10 minutes"

## 11. WorkManager — real background jobs, not decorative

**Job 1 — Background sync**: Room holds pending local changes → WorkManager pushes them to the remote backend. On failure (e.g. no network), WorkManager retries with backoff; when connectivity returns, sync succeeds. This is the app's central offline-first proof point.

**Job 2 — Cleanup**: `PeriodicWorkRequest` running a `CleanupWorker` that archives/removes very old completed tasks.

## 12. Offline-first architecture

```
UI → ViewModel → UseCase → Repository → Room (source of truth) ⇄ Sync Worker ⇄ Remote API
```

Creating a task writes to Room and updates the UI immediately; sync to the remote backend happens afterward via WorkManager. The app stays fully usable offline.

## 13. Data model (v1)

```kotlin
Task(
    id,
    title,
    description,
    priority,       // LOW | MEDIUM | HIGH
    status,         // PENDING | COMPLETED
    dueDate,
    dueTime,
    reminderEnabled,
    createdAt,
    completedAt,
    updatedAt,
)
```

Categories/tags are an explicit later-version idea, not v1.

## 14. Architecture

Feature-oriented package structure (module boundaries can be promoted to real Gradle modules later if scope justifies it — see `docs/DECISIONS.md` for the v1 call on this):

```
TaskFlow
├── app
├── core/{database, network, common, designsystem, navigation}
├── feature/{home, tasks, taskeditor, taskdetails, profile, settings}
└── worker/{SyncWorker, CleanupWorker}
```

## 15. Technology stack

- **UI**: Kotlin, Jetpack Compose, Material 3, Navigation Compose
- **Architecture**: MVVM, Clean Architecture principles, Repository pattern, Use Cases
- **Async**: Coroutines, Flow, StateFlow
- **Local**: Room
- **Background**: WorkManager
- **DI**: Hilt
- **Networking**: Retrofit, OkHttp, kotlinx.serialization
- **Testing**: JUnit, MockK, Compose UI testing
- **Other**: DataStore for preferences, notifications

## 16. Explicitly out of scope for v1

Kept out deliberately so TaskFlow doesn't become a 2-month project and stays focused on the skills it's meant to demonstrate:

- Authentication / Google login
- Payments
- Real-time collaboration
- Chat
- Calendar integration
- AI assistant
- Complex custom backend
- Social features

## Final MVP shape

```
                 TASKFLOW
                    │
       ┌────────────┼────────────┐
       ↓            ↓            ↓
     HOME         TASKS       PROFILE
       │            │            │
       │       ┌────┴────┐       │
       │       ↓         ↓       │
       │    DETAILS   FILTERS    │
       │       │               SETTINGS
       │       ↓
       │    EDIT TASK
       │
       └── CREATE TASK
```

```
Compose → MVVM → Use Cases → Repository → Room ⇄ Retrofit
                                              ↑
                                        WorkManager
```

## Decisions confirmed after the initial design pass

- **Backend for sync**: Supabase (real Postgres + auto REST API) is the target, but v1 builds against a fake `TaskRemoteDataSource` first and swaps in the real Supabase-backed implementation later (see `docs/DECISIONS.md` §Backend for rationale, sequencing, and the RLS caveat).
- **Module structure for v1**: single Gradle module (`:app`) with the feature-oriented package layout above; promote to real multi-module only if/when it earns its keep.
