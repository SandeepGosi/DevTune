# Implementation Plan - M3: Platform Capabilities

Establish the foundation for real-world Android platform access using capability-driven design.

## Platform Research Findings

### Developer Options
*   **Status Detection:** Can be read via `Settings.Global.getInt(contentResolver, "development_settings_enabled", 0)`.
*   **Navigation:** Official Intent action is `android.settings.APPLICATION_DEVELOPMENT_SETTINGS`.
*   **Control:** Third-party apps **cannot** enable/disable Developer Options programmatically (requires `WRITE_SECURE_SETTINGS`, a signature/privileged permission).
*   **Compatibility:** Intent is standard but some OEMs (e.g., Xiaomi) may have intermediate screens or non-standard paths.

### Animation Settings
*   **Keys:** `WINDOW_ANIMATION_SCALE`, `TRANSITION_ANIMATION_SCALE`, `ANIMATOR_DURATION_SCALE`.
*   **Read:** Normal apps **can** read these values using `Settings.Global.getFloat`.
*   **Write:** Normal apps **cannot** write these values (requires `WRITE_SECURE_SETTINGS`).
*   **State:** Values are floats (1.0 = 1x, 0.5 = 0.5x, 0.0 = Off).

### Display Density
*   **API:** `resources.configuration.densityDpi` or `context.display.getMetrics`.
*   **Value:** Represents the logical density bucket (DPI), not necessarily the exact physical hardware DPI.
*   **Read:** Standard public API, no special permissions.

## Proposed Changes

### Domain Layer

#### [PlatformCapability.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/model/PlatformCapability.kt)
*   Ensure `CapabilityState` and `PlatformCapability` are robust.

#### [NEW] [AnimationSettings.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/model/AnimationSettings.kt)
*   Data class for animation scales.

#### [NEW] [PlatformCapabilitiesUseCase.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/usecase/PlatformCapabilitiesUseCase.kt)
*   `GetCapabilityStateUseCase`
*   `GetAnimationSettingsUseCase`
*   `GetDisplayDensityUseCase`
*   `OpenDeveloperOptionsUseCase`

---

### Platform Layer

#### [SystemControlProvider.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/platform/SystemControlProvider.kt)
*   Update interface with methods for reading status and launching intents.

#### [NEW] [AndroidSystemControlProvider.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/platform/AndroidSystemControlProvider.kt)
*   Implementation using `ContentResolver`, `Settings.Global`, and `Resources`.

---

### Data Layer

#### [NEW] [PlatformRepository.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/repository/PlatformRepository.kt) / [PlatformRepositoryImpl.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/data/repository/PlatformRepositoryImpl.kt)
*   Bridge between domain use cases and the platform provider.

---

### Presentation Layer

#### [HomeViewModel.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeViewModel.kt)
*   Expose `HomeUiState` containing real platform data.
*   Handle the "Open Developer Options" event.

#### [HomeScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeScreen.kt)
*   Update to observe `HomeUiState` and display real values.
*   Connect "Developer Options" row to the ViewModel action.

---

### Dependency Injection

#### [NEW] [PlatformModule.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/di/PlatformModule.kt)
*   Hilt module to provide `SystemControlProvider`.

## Verification Plan

### Automated Tests
*   **Unit Tests:**
    *   `AndroidSystemControlProviderTest`: Verify mapping of `Settings.Global` values to domain models.
    *   `HomeViewModelTest`: Verify state updates when platform data changes.
*   **Instrumentation Tests:**
    *   `PlatformCapabilityTest`: Verify reading real values from the emulator.

### Manual Verification
*   Verify Home screen displays correct logical DPI (420 on emulator).
*   Verify Home screen displays correct Animation scales.
*   Verify "Developer Options" row correctly shows "Enabled" (if enabled on emulator).
*   Clicking "Developer Options" opens the system settings screen.
*   Verify behavior when a setting is missing (null value handling).
