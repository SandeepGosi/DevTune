# ADR-004: Controlled Animation Setting Writes & Capability Gating

## Status
Proposed (Pending Runtime Verification)

## Context
Milestone M5 investigates whether DevTune can safely READ, WRITE, VERIFY, and RESTORE Android animation settings through Shizuku:
* `Settings.Global.WINDOW_ANIMATION_SCALE`
* `Settings.Global.TRANSITION_ANIMATION_SCALE`
* `Settings.Global.ANIMATOR_DURATION_SCALE`

## Technical Analysis of Write Mechanisms

### 1. App-Process ContentResolver (`Settings.Global.putFloat`)
* **Mechanism**: Standard public SDK API called directly in the application process.
* **Result**: **FAILS** with `SecurityException: Permission denial: writing to settings requires android.permission.WRITE_SECURE_SETTINGS`.
* **Reason**: Third-party applications run under an unprivileged UID (e.g., `10xxx`), which lacks `WRITE_SECURE_SETTINGS`. The app process cannot acquire this signature permission at runtime.

### 2. Shizuku Binder Wrapping (`ShizukuBinderWrapper` + `IWindowManager`)
* **Mechanism**: Wrapping the system's `window` service binder (`Shizuku.getService("window")`) and invoking `IWindowManager.setAnimationScale(int, float)`.
* **Result**: Direct IPC to `WindowManagerService`.
* **Pros**: Requires no separate process or custom AIDL service.
* **Cons**: Uses an internal `@hide` platform AIDL interface (`com.android.view.IWindowManager`), which is not part of the public Android SDK.

### 3. Shizuku User Service (`Shizuku.bindUserService` + `IAnimationService.aidl`)
* **Mechanism**: Spawning a dedicated process via Shizuku (`app_process`) running under Shell (UID 2000) or Root (UID 0). The remote process calls `Settings.Global.putFloat(context.contentResolver, ...)`.
* **Result**: Successful write using **public SDK APIs** (`Settings.Global.putFloat`), because the calling UID in the remote process is UID 2000 / UID 0 (which holds `WRITE_SECURE_SETTINGS`).
* **Security Boundary**: The custom AIDL (`IAnimationService`) exposes strictly typed animation write operations (`writeScale(name, value)`) and explicitly forbids generic command execution or shell execution.

## Decision: Architectural Choice
We selected the **Shizuku User Service** approach with a narrow custom AIDL interface (`IAnimationService`).

### Rationale:
1. **Public API Integrity**: The write operation itself uses the official, public `Settings.Global.putFloat` SDK API rather than uncommitted platform AIDL methods.
2. **Security**: The custom AIDL exposes ONLY `writeScale(name, value)` and contains zero arbitrary execution vectors.
3. **No Shell**: It completely avoids `Runtime.exec()`, `ProcessBuilder`, or `Shizuku.newProcess()`.

## Capability Gating & Persistence Rule
* **No Premature Availability**: `WRITE_ANIMATION_SETTINGS` MUST NOT be marked `AVAILABLE` merely because Shizuku is authorized or the User Service connects.
* **Experiment Requirement**: Availability requires real-world execution of the complete cycle:
  `READ` -> `WRITE (0.5x)` -> `READ-BACK VERIFY` -> `RESTORE` -> `READ-BACK VERIFY`.
* **Dynamic Evaluation**: Capability persistence in `DataStore` stores diagnostic metadata (`lastVerifiedBackend`, `lastVerifiedAndroidVersion`, `lastVerifiedAt`, `lastVerifiedSuccess`). Current capability is dynamically evaluated: if the current Shizuku backend or Android SDK version differs from the verified run, capability falls back to `RESTRICTED`.

## Runtime Verification Status
* **Status**: **PENDING**
* **Reason**: Shizuku is not installed or configured on the current test emulator instance. Real-world execution must occur on a Shizuku-enabled runtime before M5 can be marked fully approved.
