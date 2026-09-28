# ADR-007: Developer Options Write Capability Classification

## Status
Accepted

## Context
Milestone M5B-A investigated whether an authorized Shizuku session could enable the Android Developer Options state directly without requiring the user to manually perform seven taps on "Build number" in system settings.

`Settings.Global.DEVELOPMENT_SETTINGS_ENABLED` exists as a database key in `SettingsProvider`. However:
1. Attempting `ContentResolver.putInt(cr, "development_settings_enabled", 1)` from inside an application process under Shizuku's Shell UID 2000 identity fails with `SecurityException: Given calling package com.devtune does not match caller's uid 2000` at `ActivityManagerService.getContentProvider()`.
2. AOSP documentation and framework source code confirm that directly writing `Settings.Global.DEVELOPMENT_SETTINGS_ENABLED = 1` has no effect on enabling Developer Options system-wide on modern Android versions without system-level broadcast dispatching.
3. No standalone framework Binder interface method exists on `IActivityManager`, `IWindowManager`, or `IUserManager` for toggling Developer Options.

## Decision
1. DevTune will **NOT** attempt shell execution (`adb shell settings put`), package spoofing, or simulated UI interactions to enable Developer Options.
2. Capability `WRITE_DEVELOPER_OPTIONS` is classified as **`UNSUPPORTED`**.
3. DevTune retains its clean, evidence-based capability model:
   - `READ_DEVELOPER_OPTIONS` = `AVAILABLE`
   - `OPEN_DEVELOPER_OPTIONS` = `AVAILABLE`
   - `WRITE_DEVELOPER_OPTIONS` = `UNSUPPORTED`
4. DevTune will continue providing clear step-by-step user guidance instructions ("Tap Build number 7 times") for enabling Developer Options manually.

## Consequences
- Maintains 100% Play Store compliance, zero-spoofing security guarantees, and Clean Architecture purity.
- Accurately informs users that enabling Developer Options requires a one-time manual setup step per Android security design.
