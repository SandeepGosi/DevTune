# M3 Addition: Contextual Navigation Plan

Improve Home screen actions for real system values by implementing contextual navigation to relevant system settings.

## Research Findings

### Animation Scales
*   **Deep Link Status:** There is **no public, supported Android Intent** that deep-links directly to the individual "Animation Scale" preferences (Window, Transition, Animator).
*   **Location:** These settings are located within the **Developer Options** screen.
*   **Strategy:** Open `ACTION_APPLICATION_DEVELOPMENT_SETTINGS` and provide fallback instructions.

### Logical Display Density
*   **Deep Link Status:** There is **no public, supported Android Intent** that deep-links directly to "Display size" (logical density) settings.
*   **Closest Legitimate Screen:** `ACTION_DISPLAY_SETTINGS` (Display settings) is the closest public intent. On many devices, "Display size" is found here. On others (or for "Smallest width"), it is in Developer Options.
*   **Strategy:** Prefer `ACTION_DISPLAY_SETTINGS` as the primary target for density, but also provide a path to Developer Options if that's where the user prefers to look. For M3, I will implement navigation to `ACTION_DISPLAY_SETTINGS` with a fallback message.

## Proposed Changes

### Platform Layer (`SystemControlProvider.kt`)
*   Add `openDisplaySettings(): Boolean`.

### Domain Layer
*   Add `OpenDisplaySettingsUseCase`.
*   Update `CapabilityState` logic to reflect navigation support.

### Presentation Layer
*   **HomeViewModel:**
    *   Expose capabilities: `canOpenAnimationSettings`, `canOpenDisplaySettings`.
    *   Add logic to open Developer Options when "Animation Scales" are clicked.
    *   Add logic to open Display Settings when "Logical Display Density" is clicked.
*   **HomeScreen:**
    *   Update `ValueRow` or `SettingRow` to be clickable where supported.
    *   Add informational messages/dialogs when direct deep-linking is unavailable.

## OEM Considerations
*   Since direct links don't exist, we rely on broad public intents (`APPLICATION_DEVELOPMENT_SETTINGS`, `DISPLAY_SETTINGS`).
*   The fallback messages will use generic terminology ("Look for 'Display size' or 'Smallest width'") to remain compatible.

## UI/UX Flow for Fallbacks
1. User taps "Animation Scales".
2. App opens Developer Options.
3. App shows a brief Toast or Snackbar (or informational state on return) explaining: "Android doesn't support a direct link to these settings. Look for 'Animation scale' in the screen that just opened." (Actually, since we are moving the user *away* from the app, we should show the info *before* or *on return*).
4. Requirement says: "Clearly tell the user... Provide concise guidance". I will implement an informational dialog similar to the one created for enabling Dev Options.
