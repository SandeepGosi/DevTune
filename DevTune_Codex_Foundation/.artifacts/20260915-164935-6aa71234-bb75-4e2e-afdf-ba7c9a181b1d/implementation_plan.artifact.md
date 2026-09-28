# Implementation Plan — Milestone M5B: Developer Options Research & Modern Animation Sliders

Upgrade DevTune's animation scale controls with modern Material 3 Compose Sliders (`0.00x..2.00x`, `0.25x` steps, local pending state, preset local update, explicit Apply, unsaved changes indicator, Discard action) and conduct framework compatibility research for Developer Options privileged writes on API 37.

## User Review Required

> [!IMPORTANT]
> 1. **M5B-A Developer Options**: Research-backed conclusion. AOSP API 37 exposes no standalone framework Binder API for enabling Developer Options without package spoofing or shell execution. `WRITE_DEVELOPER_OPTIONS` is classified as `UNSUPPORTED`. Manual 7-tap guidance remains the official, evidence-based user flow.
> 2. **M5B-B Modern Animation Sliders**: Sliders use range `0.00x` to `2.00x` in `0.25x` discrete steps (9 snap positions). Slider drags and Quick Profile taps update **LOCAL PENDING STATE ONLY**. No system writes occur until the user explicitly taps "Apply Changes".
> 3. **M5A Backend Stability**: The proven M5A privileged Binder path (`ShizukuBinderWrapper` + `IWindowManager.setAnimationScale()`), rollback protection, and capability verification remain unchanged.

## Proposed Changes

### Workstream M5B-A: Developer Options Research & Domain Capability

#### [PlatformCapability.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/model/PlatformCapability.kt)
- Declare `WRITE_DEVELOPER_OPTIONS` capability with status `UNSUPPORTED`.

---

### Workstream M5B-B: Modern Animation Slider UI & State Management

#### [HomeViewModel.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeViewModel.kt)
- Add local pending scale state: `pendingWindowScale`, `pendingTransitionScale`, `pendingAnimatorScale`, `hasPendingChanges`.
- Functions:
  - `onPendingWindowScaleChange(scale: Float)`
  - `onPendingTransitionScaleChange(scale: Float)`
  - `onPendingAnimatorScaleChange(scale: Float)`
  - `onApplyPendingScales()`: Delegates `AnimationScales(pendingWindow, pendingTransition, pendingAnimator)` to `SetAnimationScalesUseCase` and updates system state upon success.
  - `onSelectProfilePreset(profile: AnimationScales)`: Updates local pending scale sliders ONLY without writing to system.
  - `onDiscardPendingChanges()`: Resets pending sliders back to current active system scales.

#### [HomeScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeScreen.kt)
- Replace FilterChip selectors with Material 3 `Slider` components for Window, Transition, and Animator scales:
  - Range: `valueRange = 0.0f..2.0f`, `steps = 7` (`0.25f` increments).
  - Selected value header displaying formatted scale string (e.g. `0.50x` or `0.5x`).
- Add "Unsaved Changes" badge when local pending state differs from current system values.
- Add "Discard" action button to revert pending sliders back to system values.
- Enable "Apply Changes" button only when pending values differ from current system values and `WRITE_ANIMATION_SETTINGS` == `AVAILABLE`.
- Update Quick Profiles (`Default 1.00x`, `Fast 0.50x`, `No Animation 0.00x`) to update local pending slider positions without immediate system write.
- Keep sliders disabled/read-only when Shizuku is unverified or unavailable.

---

### Documentation

#### [NEW] [ADR-007-developer-options-write-capability.md](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/docs/decisions/ADR-007-developer-options-write-capability.md)
- Documents Developer Options write research findings on API 37, AOSP framework limitations under Shizuku UID 2000 identity, prohibition of shell/spoofing workarounds, and classification of `WRITE_DEVELOPER_OPTIONS` as `UNSUPPORTED`.

## Verification Plan

### Automated Tests
- **Unit Tests**:
  - `HomeViewModelTest`: Test pending slider state updates, preset profile local updates (no system write), discard action, and `onApplyPendingScales` invocation.
  - `AnimationScalesTest`: Verify slider snap values (`0.00x`, `0.25x`, `0.50x`, `0.75x`, `1.00x`, `1.25x`, `1.50x`, `1.75x`, `2.00x`).
  - `AnimationRepositoryTest`: Retain existing M5A write and rollback tests.
- **Instrumentation Test**:
  - Re-run `ProductionAnimationWriteRuntimeTest` on `emulator-5554` (API 37) to verify M5A write backend remains 100% functional.

### Quality Gate
- `assembleDebug`
- `assembleRelease`
- `testDebugUnitTest`
- `lintDebug`
- `am instrument` on `emulator-5554`
