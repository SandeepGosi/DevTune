# Implementation Plan - M4: Shizuku Foundation

Establish the core Shizuku integration layer as an optional privileged capability provider.

## User Review Required

> [!IMPORTANT]
> This milestone focus on **read-only status and capability detection**. No system settings will be modified in M4.

## Proposed Changes

### Build Configuration

#### [libs.versions.toml](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/gradle/libs.versions.toml)
- Add `shizuku = "13.1.5"` version.
- Add `shizuku-api` and `shizuku-provider` libraries.

#### [app/build.gradle.kts](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/build.gradle.kts)
- Add Shizuku dependencies.

---

### Domain Layer

#### [NEW] [ShizukuState.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/model/ShizukuState.kt)
- Enum/Sealed class for states: `NOT_INSTALLED`, `STOPPED`, `PERMISSION_REQUIRED`, `AUTHORIZED`, `CONNECTION_LOST`, `UNKNOWN`.
- `ShizukuBackend`: `ADB`, `ROOT`, `NONE`.

#### [NEW] [ShizukuRepository.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/repository/ShizukuRepository.kt)
- Interface for observing state and requesting permission.

#### [NEW] [ShizukuUseCases.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/usecase/ShizukuUseCases.kt)
- `GetShizukuStateUseCase`
- `RequestShizukuPermissionUseCase`

---

### Platform Layer

#### [NEW] [ShizukuProvider.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/platform/shizuku/ShizukuProvider.kt)
- Interface for Shizuku interaction.

#### [NEW] [AndroidShizukuProvider.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/platform/shizuku/AndroidShizukuProvider.kt)
- Implementation using `riikka.shizuku` APIs.
- Manages Binder listeners and permission callbacks.

---

### Data Layer

#### [NEW] [ShizukuRepositoryImpl.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/data/repository/ShizukuRepositoryImpl.kt)
- Maps platform Shizuku states to domain models.

---

### Presentation Layer

#### [HomeViewModel.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeViewModel.kt)
- Collect Shizuku state and expose it to the UI.

#### [HomeScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeScreen.kt)
- Update "Advanced Controls" card with real Shizuku status and a "Request Permission" or "Setup Guidance" action.

---

### Dependency Injection

#### [PlatformModule.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/di/PlatformModule.kt)
- Provide `ShizukuProvider` and `ShizukuRepository`.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `ShizukuStateMappingTest`: Verify platform-to-domain state transitions.
    - `ShizukuRepositoryTest`: Verify flow emission on binder events.

### Manual Verification
- Verify Home screen shows "Shizuku not detected" if absent.
- If Shizuku is running:
    - Verify "Permission Required" state.
    - Verify successful authorization flow.
    - Verify "Backend Type" detection (ADB vs Root).
- Verify state updates when Shizuku service is stopped/started while DevTune is in foreground.
