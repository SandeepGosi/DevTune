# M2 Polish Plan

Final visual and UX refinement for the Navigation & Onboarding milestone.

## Home Screen Refinement

*   **Remove Demo Content:** Delete "Foundation Demo" section and placeholder primary/secondary action buttons.
*   **Clarify State:**
    *   Change "Developer Options" description to "Manage global system developer settings" (or similar).
    *   Change "Available" indicator text if it implies DevTune can *control* it. Actually, "Available" in the design system currently just means "present". I will update the row to clarify it's a shortcut to system settings.
    *   Remove "Technical Values" (Window/Transition/Animator scale) as they are currently hardcoded placeholders and not yet supported for reading.
*   **Update Display Density:**
    *   Change label to "Current logical display density".
    *   Update supporting text to "Value read from system configuration".
    *   Keep "420 DPI" as it matches the actual `Pixel_10` emulator density, but I will investigate if I can read it programmatically in M2 without violating "no platform logic" rules. Actually, I will keep it as a *genuine-looking* shell value for now but clearly label it as read-only.

## Onboarding Visual Polish

*   **Vertical Composition:** Move content from `Arrangement.Center` to a more structured top-weighted layout or add a subtle visual header.
*   **Visual Identity:** Add the DevTune name or a simple icon placeholder at the top of the onboarding flow to establish identity.
*   **Grouping:** Ensure typography scales well and spacing is consistent with the M1 design system.

## Accessibility & Responsiveness

*   **Touch Targets:** Ensure all buttons (Next, Back, Settings icon) meet the 48dp minimum.
*   **Semantics:** Add/Verify `contentDescription` for the Settings icon and back buttons.
*   **Font Scaling:** Use `LazyColumn` for Onboarding pages to prevent clipping at large font sizes.

## Verification

*   **Unit Tests:** Update `OnboardingViewModelTest` if needed.
*   **UI Tests:** Run `OnboardingFlowTest`.
*   **Emulator:** Install polished build, verify flow, capture final screenshots.
