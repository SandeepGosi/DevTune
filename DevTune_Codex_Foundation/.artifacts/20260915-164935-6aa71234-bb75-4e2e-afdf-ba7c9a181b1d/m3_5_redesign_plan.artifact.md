# Implementation Plan - M3.5 Premium UI Redesign

Transform DevTune into a premium, dashboard-oriented Android power-utility using Material 3 and a dark-first aesthetic.

## Proposed Changes

### Theme & Colors

#### [Color.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/theme/Color.kt)
- Update primary colors to a deep indigo/purple family.
- Refine tonal surfaces for a more premium "dashboard" look.

#### [Theme.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/theme/Theme.kt)
- Ensure dynamic color is handled gracefully but defaults to the custom indigo/purple tonal foundation.

---

### UI Components (Refinement & Additions)

#### [NEW] [FeatureCard.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/components/FeatureCard.kt)
- A reusable Material 3 card with tonal elevation, integrated icons, and clear typography for major sections (Developer Options, Animation, Density).

#### [NEW] [DashboardHeader.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/components/DashboardHeader.kt)
- A compact hero header with the branding "DevTune" and subtitle "Android Power Utility".

#### [StatusIndicator.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/components/StatusIndicator.kt)
- Refine to support a more "badge-like" appearance within cards.

---

### Home Screen Redesign

#### [HomeScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeScreen.kt)
- Structure:
    1. `DashboardHeader`
    2. **System Status Card**: Real-time summary of Developer Options, Density, and Animation.
    3. **Action Modules**: Visually distinct cards for Developer Options (with navigation), Animation (read-only info), and Display Density.
    4. **Profiles Preview**: A horizontal or grid layout showing built-in profiles (Default, Fast, No Animation) as visual samples.
    5. **Advanced Controls Preview**: A placeholder card for Shizuku/Advanced features.

---

### Onboarding & Settings

#### [OnboardingScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/onboarding/OnboardingScreen.kt)
- Visually update to match the new dashboard style.
- Improve vertical positioning and content grouping.

#### [SettingsScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/settings/SettingsScreen.kt)
- Apply the new color system and refined row components.

## Verification Plan

### Automated Tests
- Run existing unit tests (`OnboardingViewModelTest`, `PlatformCapabilitiesUseCaseTest`).
- Run instrumentation tests (`AndroidSystemControlProviderTest`).

### Manual Verification
- **Visual Audit (Emulator):**
    - Inspect Home screen layout on API 37 emulator.
    - Verify dark vs light theme visual balance.
    - Check information density and spacing.
- **Interaction Check:**
    - Ensure all clickable cards/buttons trigger the correct dialogs/intents.
    - Verify onboarding flow remains functional.
- **Accessibility Check:**
    - Verify TalkBack on new cards.
    - Check text scaling/responsiveness.
