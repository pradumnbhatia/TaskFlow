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

## Phase 1 build fix: androidx.core / androidx.lifecycle pinned below their newest releases

While building Phase 1, `androidx.core:core-ktx:1.19.0` and `androidx.lifecycle:lifecycle-runtime-ktx:2.11.0` (the versions Android Studio's project template had pre-selected) turned out to require `compileSdk 37` + AGP 9.1+ — one rung past the AGP/compileSdk ceiling this project deliberately stays under (see the Compose BOM decision above). Pinned back to `core-ktx 1.17.0` and `lifecycle-runtime-ktx 2.10.0`, both confirmed compatible with `compileSdk 36` / AGP 8.13.2. `:app:assembleDebug` and `:app:testDebugUnitTest` both pass as of this fix.

## Open item for the (deferred) Supabase swap-in

When we're ready to wire up the real backend: a Supabase project URL and publishable API key, plus the `tasks` table schema applied (columns mirroring the `Task` data model) with the RLS policy described above. Until then, `SyncWorker` runs against the fake `TaskRemoteDataSource` and every other phase proceeds unblocked.
