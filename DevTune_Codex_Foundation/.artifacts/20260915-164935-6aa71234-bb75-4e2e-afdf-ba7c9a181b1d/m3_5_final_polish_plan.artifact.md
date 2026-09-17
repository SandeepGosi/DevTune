# Implementation Plan - M3.5 Final Visual Polish

Elevate the DevTune UI from a Material 3 prototype to a premium, immersive power-user dashboard.

## Visual Strategy

*   **Immersive Tonal Foundation:** Enhance the dark theme with deep indigo/purple background and nested tonal surfaces.
*   **System Status Hero:** Re-implement the System Status Overview as a high-impact "Hero Card" using `PrimaryContainer` colors.
*   **Refined Information Density:** Reduce vertical gaps and improve content grouping to maximize visible information.
*   **Semantic Polish:** Use bolder typography for status values and more distinct visual separators for sections.

## Proposed Changes

### Theme & Colors

#### [Color.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/theme/Color.kt)
*   Refine the Indigo palette for deeper blacks and more vibrant primary accents.

### UI Components

#### [NEW] [StatusBadge.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/components/StatusBadge.kt)
*   A more compact and visually refined version of the status indicator for use in small spaces.

#### [FeatureCard.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/components/FeatureCard.kt)
*   Improve card styling: slightly higher default elevation, refined internal padding, and clearer primary actions.

### Home Screen Redesign

#### [HomeScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeScreen.kt)
*   **Header:** More compact branding.
*   **Hero Card:** A full-width "System Status" card that feels like a real dashboard module.
*   **Feature Modules:** Unified card layouts for Developer Options, Density, and Animation.
*   **Profiles & Advanced:** Visually integrated as grid/chip sections rather than standard lists.
*   **Wording Updates:** Apply "Read-only without advanced system access" and other state-dependent text fixes.

---

## Verification Plan

### Automated Tests
*   Verify all existing 7 tests pass.

### Manual Visual Verification
*   **Dark Theme:** Confirm immersive dashboard look.
*   **Information Density:** Ensure key stats are visible without scrolling.
*   **Responsiveness:** Verify layout on emulator.
*   **Functionality:** Confirm manual Dev Options enablement flow and instruction dialogs remain intact.
