# ADR-008: Temporary Animation Profiles & Automatic Restoration

## Status
Accepted

## Context
Milestone M7 introduces temporary animation profiles where users can apply custom or preset animation scale settings for a defined duration (5 min, 15 min, 30 min, 1 hour, 2 hours, 4 hours), with automatic restoration to pre-session scales upon expiration.

## Design Principles & Decisions

### 1. Authoritative Pre-Session Capture & Restoration
- Before applying any temporary profile, DevTune reads the exact current system animation scales (`originalScales`).
- DevTune persists `originalScales`, `targetScales`, `startedAtEpochMillis`, and `expiresAtEpochMillis` in Preferences DataStore.
- Restoration MUST return to `originalScales`. DevTune never assumes `1.0x` as default.

### 2. Time Strategy & Absolute Timestamps
- `expiresAtEpochMillis` is the single source of truth for session expiration.
- Remaining time is derived dynamically as `max(0, expiresAtEpochMillis - currentTimeMillis)`.
- Countdown timers in UI are visual derivations, not persisted integer counters.

### 3. WorkManager Best-Effort Scheduling
- `WorkManager` is used as a best-effort background execution trigger across process kills.
- When `AnimationRestorationWorker` executes, it compares `currentTimeMillis()` against `expiresAtEpochMillis`.
- WorkManager execution timing is subject to Android OS Doze mode and battery optimizations. Absolute timestamp comparison prevents premature restoration.

### 4. Dual Shizuku Recovery Paths
- **Path A (DevTune Process Alive)**: `HomeViewModel` / `ShizukuPrivilegedBridge` observes `ShizukuState` flow. Upon Shizuku becoming `Authorized`, if a session has status `RESTORE_PENDING` / `FAILED_TO_RESTORE` and `currentTime >= expiresAt`, restoration automatically executes.
- **Path B (DevTune Process Not Alive / App Launch)**: Startup recovery flow in `HomeViewModel` checks Preferences DataStore on launch. In parallel, `WorkManager` periodically executes.

### 5. Crash Recovery Algorithm During `APPLYING` State
- If the process dies during the `APPLYING` state, startup recovery reads actual system scales (`actualScales`):
  - `actual == target`: Writes completed before crash. Transition status to `ACTIVE` and resume countdown.
  - `actual == original`: Crash occurred before write. Transition status to `CANCELLED` and clear session.
  - `actual` partially changed: Crash occurred mid-write. Execute immediate restoration to `originalScales`.

### 6. User Manual Changes Ownership Rule
- An active temporary session owns the system animation values until restored or cancelled.
- Any manual scale changes made by the user in Settings or Developer Options during an active temporary session will be overwritten at expiration to guarantee restoration to the pre-session baseline.

### 7. Force-Stop and Reboot Limitations
- If the user force-stops DevTune in Android Settings, Android OS cancels WorkManager jobs. Startup recovery detects the expired session and executes restoration as soon as DevTune is next opened.
- On reboot, if Shizuku is not running, session status becomes `RESTORE_PENDING` until Shizuku is re-authorized.

### 8. Policy & Compliance Statement
- No prohibited implementation mechanisms (shell commands, `Runtime.exec`, `rish`, accessibility, package spoofing, reflection) were introduced by M7. Distribution/policy compliance requires separate review.
