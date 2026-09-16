# Developer Options UX Improvement Plan

Address the issue where users are stuck if Developer Options are not enabled.

## Findings & Support
*   **Automation:** Enabling Developer Options (seven-tap flow) **cannot** be automated by a standard 3rd-party app without root/ADB.
*   **Intent `ACTION_DEVICE_INFO_SETTINGS`:** Officially supported since API 1. This intent successfully opens the "About Phone" or "Device Information" screen on most Android versions.
*   **State Detection:** `Settings.Global.DEVELOPMENT_SETTINGS_ENABLED` is reliable for determining if the "Developer Options" entry is visible in system settings.

## Proposed UX Flow

### 1. Developer Options Already Enabled
*   **UI:** Shows "Enabled" with primary color.
*   **Action:** "Open Developer Options".
*   **Intent:** `Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS`.

### 2. Developer Options Disabled/Hidden
*   **UI:** Shows "Not enabled" with outline/gray color.
*   **Instructions:**
    1.  Tap "Enable Developer Options".
    2.  In the system screen that opens, find "Build number".
    3.  Tap it 7 times until enabled.
    4.  Return to DevTune.
*   **Action:** "Enable Developer Options".
*   **Intent:** `Settings.ACTION_DEVICE_INFO_SETTINGS`.

## Proposed Changes

### Platform Layer (`SystemControlProvider.kt`)
*   Add `openDeviceInfoSettings(): Boolean`.

### Domain Layer
*   Update `SystemState` to include `canOpenDevOptions` as a first-class property (though we already have it in the ViewModel, moving it to the state model is cleaner for UI rendering).
*   Add `OpenDeviceInfoSettingsUseCase`.

### Presentation Layer
*   **HomeViewModel:**
    *   Expose a state that distinguishes between "Already Enabled" and "Need to Enable".
    *   Add `openDeveloperOptions()` logic: if enabled, open dev settings; if disabled, open device info.
*   **HomeScreen:**
    *   When disabled, show an "Instruction Dialog" or "Instruction Section" before/during the intent launch.
    *   Implement **Resume Refresh**: The Home screen will automatically refresh its state when the app is resumed (via `LifecycleEventObserver` or simply the polling logic already in the Repository).

## OEM Compatibility
*   Use generic "Find Build number in About or Software information" to cover variation (Samsung vs Pixel vs Xiaomi).
*   Always verify intent resolution before launching.
