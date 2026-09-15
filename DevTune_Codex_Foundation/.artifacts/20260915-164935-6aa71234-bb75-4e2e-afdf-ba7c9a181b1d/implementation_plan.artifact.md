# Implementation Plan - M2: Navigation & Onboarding

Establish the production-ready navigation architecture and first-run onboarding experience for DevTune.

## User Review Required

> [!NOTE]
> I am adopting the new Type-Safe Navigation Compose (available in Navigation 2.10.1) which uses Kotlin Serialization. This requires adding the `kotlinx.serialization` plugin.

## Proposed Changes

### Build Configuration

#### [libs.versions.toml](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/gradle/libs.versions.toml)
- Add `serialization` version and `kotlinx-serialization-json` library.
- Add `kotlin-serialization` plugin.

#### [build.gradle.kts (Project)](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/build.gradle.kts)
- Apply `kotlin-serialization` plugin (apply false).

#### [app/build.gradle.kts](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/build.gradle.kts)
- Apply `kotlin-serialization` plugin.
- Add `kotlinx-serialization-json` dependency.

---

### Data Layer

#### [UserPreferencesDataSource.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/data/datastore/UserPreferencesDataSource.kt)
- Add `isOnboardingCompleted` to `UserPreferences` data class.
- Add `setOnboardingCompleted` method.

#### [NEW] [UserPreferencesRepositoryImpl.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/data/repository/UserPreferencesRepositoryImpl.kt)
- Implementation of the domain repository using `UserPreferencesDataSource`.

---

### Domain Layer

#### [NEW] [UserPreferences.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/model/UserPreferences.kt)
- Domain model for user preferences.

#### [NEW] [UserPreferencesRepository.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/repository/UserPreferencesRepository.kt)
- Repository interface.

#### [NEW] [OnboardingUseCases.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/usecase/OnboardingUseCases.kt)
- `GetOnboardingStatusUseCase`
- `CompleteOnboardingUseCase`

---

### Navigation

#### [DevTuneDestination.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/navigation/DevTuneDestination.kt)
- Refactor to use `@Serializable` objects for type-safe navigation.

#### [DevTuneNavHost.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/navigation/DevTuneNavHost.kt)
- Update to use type-safe navigation and include `Onboarding` and `Settings` routes.

---

### UI Layer

#### [NEW] [OnboardingScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/onboarding/OnboardingScreen.kt)
- 3-step onboarding flow.

#### [NEW] [OnboardingViewModel.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/onboarding/OnboardingViewModel.kt)
- Handles onboarding state and completion.

#### [HomeScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeScreen.kt)
- Refine layout and add navigation to Settings.

#### [NEW] [SettingsScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/settings/SettingsScreen.kt)
- Basic shell for future settings.

---

### Dependency Injection

#### [NEW] [RepositoryModule.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/di/RepositoryModule.kt)
- Hilt module to bind `UserPreferencesRepository`.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `UserPreferencesRepositoryTest`: Verify DataStore interactions.
    - `OnboardingViewModelTest`: Verify state transitions and completion logic.
- **UI Tests**:
    - `OnboardingFlowTest`: Verify navigation from Onboarding to Home.
    - `NavigationTest`: Verify navigation between Home and Settings.

Commands:
- `.\gradlew.bat testDebugUnitTest`
- `.\gradlew.bat connectedDebugAndroidTest` (if device available)

### Manual Verification
- Fresh install: Verify onboarding appears.
- Complete onboarding: Verify navigation to Home and persistence (onboarding doesn't reappear on restart).
- Navigate to Settings and back.
- Verify Dark mode and Dynamic color in onboarding.
- Accessibility: Use TalkBack on Onboarding screens.
