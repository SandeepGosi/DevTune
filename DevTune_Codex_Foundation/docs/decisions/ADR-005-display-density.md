# ADR-005: Read-Only Display Density Architecture & Capability Gating

* **Status**: Accepted
* **Deciders**: Principal Android Architect, DevTune Core Engineering
* **Date**: 2026-09-23

---

## Context

Milestone M6 requires evaluating and establishing DevTune's architecture for **Display Density Control** (DPI, density scale, screen resolution, and Smallest Width).

Following the technical findings of Milestone M5 (where Shizuku `UserService` write calls via `ContentResolver` failed due to AOSP package/UID identity enforcement at `ActivityManagerService`), Phase 1 feasibility research was conducted specifically for display density modification.

---

## Technical Feasibility Findings

### 1. Density Concepts & Terminology
* **`DisplayMetrics.densityDpi`**: Active density in dots per inch reported to screen rendering engines. Scale is calculated via `densityDpi / 160.0f`.
* **`Configuration.densityDpi`**: Active density stored in `Resources.configuration`.
* **Smallest Width (`swXXXdp`)**: Derived layout metric (`(min(widthPx, heightPx) * 160) / densityDpi`).
* **Display Size Scaling**: User-facing scale factor in Settings -> Display Size.
* **Terminology Requirement**: Density metrics are designated as **"Current logical density"** rather than "Physical DPI" to accurately reflect system software scaling overrides versus physical display panel specifications.

### 2. Android Framework Paths for Density Modification
* **`WindowManagerService.setForcedDisplayDensityForUser(int displayId, int density, int userId)`**:
  - Defined in `android.view.IWindowManager.aidl`.
  - Marked **`@hide`** in AOSP.
  - Requires `android.permission.WRITE_SECURE_SETTINGS`.
* **`wm density` Shell Command**:
  - Executes `/system/bin/wm`, which invokes hidden `IWindowManager` methods via shell.
  - Executing shell commands via `Runtime.exec()`, `ProcessBuilder()`, `Shizuku.newProcess()`, or `rish` is strictly forbidden under DevTune non-negotiable safety rules.

### 3. Shizuku & UserService Identity Enforcement
* `IWindowManager` is an `@hide` internal interface. Accessing it via `ShizukuBinderWrapper` or AIDL requires non-SDK / hidden APIs or reflection, violating DevTune safety rules.
* Invoking `Settings.Secure.putString(resolver, "display_density_forced", ...)` from a Shizuku `UserService` triggers the same `SecurityException` established in M5 (`callingPackage "com.devtune" does not match caller's uid 2000`).

---

## Decision

1. **Gating Rules**:
   - **`READ_DISPLAY_DENSITY` = `AVAILABLE`**: Full read-only inspection of logical density, scale factor, screen resolution, Smallest Width (`swXXXdp`), and forced density override detection using public Android SDK APIs (`Resources.displayMetrics`, `Resources.configuration`, `Settings.Secure.getString`).
   - **`WRITE_DISPLAY_DENSITY` = `RESTRICTED / UNSUPPORTED`**: Write capability is classified as `RESTRICTED` in DevTune V1. No write APIs, apply/save buttons, or Shizuku write authorization flows will be exposed for display density.

2. **User Communication**:
   - The UI displays a compact, informative "Display Density" card and a detail metrics sheet.
   - When users inspect density controls, the app clearly explains:
     *"Changing display density requires system-level access that is not supported by DevTune on this Android configuration."*

---

## Consequences

* **Positive**:
  - 100% Play Store compliance guaranteed.
  - Zero hidden API usage, zero reflection, zero shell command execution, zero package identity spoofing.
  - Accurate, real-time logical density monitoring across all Android versions.
* **Negative**:
  - Users cannot modify screen DPI directly within DevTune V1; they must use Android Developer Options or Display Settings.

---

## Compliance & Security Checklist

* [x] No `Settings.Secure.put*` or `Settings.Global.put*` calls
* [x] No `wm density` or shell command execution
* [x] No `@hide` `IWindowManager` AIDL or reflection
* [x] No package identity spoofing
* [x] No root execution
