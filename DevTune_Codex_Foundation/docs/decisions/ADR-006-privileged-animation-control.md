# ADR-006: Privileged Animation Scale Control Architecture

## Status
Accepted

## Context
Standard Android applications cannot modify system animation scales (`WINDOW_ANIMATION_SCALE`, `TRANSITION_ANIMATION_SCALE`, `ANIMATOR_DURATION_SCALE`) because the underlying framework methods require `android.permission.SET_ANIMATION_SCALE` (protection level: `signature|privileged|development`).
Phase 1 (M5) investigated whether `Settings.Global.putFloat()` via Shizuku `UserService` could modify animation settings, but `UserService` running under `app_process` inherits package identity `"com.devtune"` while running under UID 2000, causing a `SecurityException: Given calling package com.devtune does not match caller's uid 2000` at `ActivityManagerService.getContentProvider()`.

Phase 2 controlled runtime validation (M5) discovered and proved that direct WindowManager Binder forwarding via `ShizukuBinderWrapper(SystemServiceHelper.getSystemService("window"))` -> `IWindowManager.Stub.asInterface()` bypasses `ContentResolver` package identity validation completely. Calling `IWindowManager.setAnimationScale(int which, float scale)` directly under UID 2000 (`shell`) succeeds with zero exceptions.

## Decision
1. **Shizuku Binder Forwarding Architecture**:
   - DevTune wraps the system `"window"` Binder via `ShizukuBinderWrapper` and casts it to `IWindowManager.Stub.asInterface()`.
   - All privileged writes call `setAnimationScale(which, scale)` individually (`0` = Window, `1` = Transition, `2` = Animator).
   - Method `setAnimationScales(float[])` is **NOT** used because reflection and runtime testing confirmed array-based methods are not present in the API 37 `IWindowManager` AIDL interface.

2. **Dynamic Runtime Capability Gating**:
   - `WRITE_ANIMATION_SETTINGS` is NOT granted automatically merely because Shizuku is authorized.
   - Capability `WRITE_ANIMATION_SETTINGS` becomes `AVAILABLE` **ONLY** after a successful 11-step runtime verification cycle:
     1. Pre-read original scales (`win`, `trans`, `anim`).
     2. Write Window scale to `0.5x`.
     3. Read back and verify `win == 0.5x` while `trans` and `anim` remain unchanged.
     4. Restore exact original scales (`win`, `trans`, `anim`).
     5. Final read-back verification.

3. **Rollback & Safety Guarantee**:
   - Every multi-value or profile write operation captures original pre-write state.
   - If any write or read-back step fails, all original animation scales are restored immediately, and a typed domain error (`AnimationError`) is returned.

4. **Clean Architecture Layer Isolation**:
   - UI Layer (`HomeScreen.kt`, `HomeViewModel.kt`) -> Use Cases (`SetAnimationScalesUseCase`, `VerifyAnimationWriteUseCase`) -> Repository (`AnimationRepositoryImpl`) -> Platform Boundary (`PrivilegedSystemBridge`).
   - Android Binder, Shizuku, and framework classes NEVER leak into the domain/UI layers.

5. **Display Density Distinction**:
   - Display density modification (`WRITE_DISPLAY_DENSITY`) remains `RESTRICTED / UNSUPPORTED` on API 37+ because AOSP removed forced density AIDL methods from `IWindowManager` in API 37.

## Consequences
- **Positive**: Enables production-quality, instantaneous, privilege-verified animation scale modifications without shell commands or package spoofing.
- **Limitations**: Requires user to install and authorize Shizuku. Privileged Binder APIs are internal framework AIDL methods and may vary across future Android versions or OEM-modified ROMs. Capability-driven fallback gracefully degrades to read-only mode if Shizuku binder dies or API is unavailable.
