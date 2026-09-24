# Implementation Plan — Milestone M5A: Production Shizuku Animation Controls

Convert the proven M5 privileged animation Binder path into a production-quality DevTune Advanced Mode feature with Clean Architecture, robust error handling, dynamic capability gating, rollback safety, and polished Material 3 Compose UI controls.

## User Review Required

> [!IMPORTANT]
> Milestone M5A strictly implements **PRODUCTION PRIVILEGED ANIMATION CONTROLS ONLY**. Display density write remains classified as `RESTRICTED / UNSUPPORTED` per M6 findings. All privileged animation operations transact directly with `WindowManagerService` via `ShizukuBinderWrapper` + `IWindowManager.setAnimationScale()` under Shizuku's UID 2000 (`shell`) identity. No shell commands (`Runtime.exec`, `ProcessBuilder`, `Shizuku.newProcess`, `rish`), no package spoofing, and no reflection are used.

## Proposed Changes

### Domain Layer

#### [AnimationScales.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/model/AnimationScales.kt)
- Strongly typed domain model representing Window, Transition, and Animator scales.
- Implements value range validation (`0.0f..10.0f`), rejecting `NaN`, positive/negative infinity, and out-of-range values.

#### [NEW] [AnimationError.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/model/AnimationError.kt)
- Sealed class representing typed domain/application errors: `ShizukuUnavailable`, `ShizukuPermissionRequired`, `BinderUnavailable`, `InvalidScaleValue`, `WriteFailed`, `ReadBackFailed`, `RestorationFailed`.

#### [AnimationRepository.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/repository/AnimationRepository.kt)
- Boundary interface declaring read and privileged write operations:
  - `fun getAnimationScales(): Flow<AnimationScales>`
  - `suspend fun setAnimationScale(which: Int, scale: Float): Result<Unit>`
  - `suspend fun applyAnimationScales(scales: AnimationScales): Result<Unit>` (includes pre-read, write, verification, and automatic rollback protection)
  - `suspend fun verifyAnimationWriteCapability(): Result<Unit>`

#### [VerifyAnimationWriteUseCase.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/usecase/VerifyAnimationWriteUseCase.kt)
- Executes controlled write-verification cycle (pre-reads original, writes Window scale to `0.5x`, verifies read-back, restores original, verifies restoration).
- Used by `PlatformCapabilitiesUseCase` to dynamically grant `WRITE_ANIMATION_SETTINGS = AVAILABLE`.

#### [NEW] [SetAnimationScalesUseCase.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/usecase/SetAnimationScalesUseCase.kt)
- Use case for validating input scales and delegating application to `AnimationRepository` with rollback safety.

---

### Platform & Data Layer

#### [PrivilegedSystemBridge.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/platform/shizuku/PrivilegedSystemBridge.kt)
- Isolated boundary interface for privileged WindowManager operations:
  - `fun isShizukuReady(): Boolean`
  - `fun getShizukuBackend(): String?`
  - `fun getShizukuUid(): Int`
  - `suspend fun setAnimationScale(which: Int, scale: Float): Result<Unit>`
  - `suspend fun setAnimationScales(window: Float, transition: Float, animator: Float): Result<Unit>`

#### [ShizukuPrivilegedBridge.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/platform/shizuku/ShizukuPrivilegedBridge.kt)
- Implements `PrivilegedSystemBridge` using `ShizukuBinderWrapper(SystemServiceHelper.getSystemService("window"))` -> `IWindowManager.Stub.asInterface(...)`.
- Calls `wm.setAnimationScale(which, scale)` individually for API 37 compatibility.
- Handles Shizuku binder lifecycle, unready states, and remote exceptions safely.

#### [AnimationRepositoryImpl.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/data/repository/AnimationRepositoryImpl.kt)
- Combines `SystemControlProvider` for standard SDK reads and `PrivilegedSystemBridge` for privileged writes with rollback protection and read-back verification.

---

### Presentation Layer

#### [HomeViewModel.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeViewModel.kt)
- Exposes `animationScales`, `shizukuState`, `capabilities`, and `animationWriteCapability`.
- Automates dynamic capability verification when Shizuku becomes `Authorized`.
- Exposes actions: `applyAnimationScales(scales)`, `applyProfile(profile)`, and user feedback messaging state (`userMessage`).

#### [HomeScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeScreen.kt)
- Standard Mode: Renders read-only animation cards + Advanced Mode prompt card ("Shizuku Required") with "Set up Shizuku" action.
- Advanced Mode (`WRITE_ANIMATION_SETTINGS` == `AVAILABLE`):
  - Renders interactive animation scale controls for Window, Transition, and Animator scales with common preset chips (`0x`, `0.25x`, `0.5x`, `0.75x`, `1x`, `1.5x`, `2x`).
  - Quick Action Profiles ("Default 1.0x", "Fast 0.5x", "No Animation 0x").
  - Explicit **Apply** button with loading state during write-verification cycle.
  - Shizuku status indicator chip (`AUTHORIZED`, `NOT RUNNING`, `CONNECTION LOST`).

---

### Documentation

#### [NEW] [ADR-006-privileged-animation-control.md](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/docs/decisions/ADR-006-privileged-animation-control.md)
- Documents decision context, Shizuku Binder forwarding via `IWindowManager.setAnimationScale`, API 37 compatibility, SET_ANIMATION_SCALE permission, dynamic runtime verification cycle, rollback guarantees, and OEM/version limitations.

## Verification Plan

### Automated Tests
- **Unit Tests**:
  - `AnimationScalesTest`: Range validation (`0.0f..10.0f`), `NaN`, `Infinity` checks.
  - `AnimationRepositoryTest`: Flow emissions, successful write, partial write failure with automatic rollback, read-back mismatch failure.
  - `VerifyAnimationWriteUseCaseTest`: Verification cycle success and failure paths.
  - `ShizukuPrivilegedBridgeTest`: Shizuku ready/unready, binder death, write success, write failure.
- **Instrumentation Test**:
  - `ProductionAnimationWriteRuntimeTest`: Executes on `emulator-5554` (Android 17 / API 37) under Shizuku ADB backend (UID 2000):
    - Reads pre-test scales.
    - Writes `WINDOW_ANIMATION_SCALE` to `0.5x`.
    - Reads back and asserts `WINDOW` == `0.5x`.
    - Restores pre-test scales.
    - Asserts final scales match pre-test state.

### Quality Gate
- `assembleDebug`
- `assembleRelease`
- `testDebugUnitTest`
- `lintDebug`
- `am instrument` on `emulator-5554`
