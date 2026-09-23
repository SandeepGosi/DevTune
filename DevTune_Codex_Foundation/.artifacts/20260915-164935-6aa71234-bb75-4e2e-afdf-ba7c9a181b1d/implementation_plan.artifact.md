# Implementation Plan — Milestone M6: Read-Only Display Density Monitoring

Implement comprehensive, production-quality, read-only display density monitoring in DevTune.

## User Review Required

> [!IMPORTANT]
> Milestone M6 strictly implements **READ-ONLY** display density monitoring. Write capability (`WRITE_DISPLAY_DENSITY`) is classified as `RESTRICTED / UNSUPPORTED` based on Phase 1 research. No system settings will be modified, and no hidden APIs, reflection, or shell commands will be used.

## Proposed Changes

### Domain Layer

#### [NEW] [DisplayDensityInfo.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/model/DisplayDensityInfo.kt)
- Represents strongly typed logical density metrics:
  - `currentDensityDpi`: Int
  - `densityScale`: Float (`currentDensityDpi / 160.0f`)
  - `widthPx`: Int
  - `heightPx`: Int
  - `smallestWidthDp`: Int
  - `configurationDensityDpi`: Int
  - `displayMetricsDensityDpi`: Int
  - `forcedDensityDpi`: Int? (nullable)

#### [NEW] [DisplayDensityRepository.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/repository/DisplayDensityRepository.kt)
- Read-only interface emitting `Flow<DisplayDensityInfo>`.

#### [NEW] [GetDisplayDensityInfoUseCase.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/domain/usecase/GetDisplayDensityInfoUseCase.kt)
- Use case for retrieving `DisplayDensityInfo`.

---

### Platform & Data Layer

#### [NEW] [DisplayDensityProvider.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/platform/DisplayDensityProvider.kt)
- Boundary for public SDK display density inspection.

#### [NEW] [AndroidDisplayDensityProvider.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/platform/AndroidDisplayDensityProvider.kt)
- Implementation using `Resources.displayMetrics`, `Resources.configuration`, and public `Settings.Secure.getString` for `"display_density_forced"`.

#### [NEW] [DisplayDensityRepositoryImpl.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/data/repository/DisplayDensityRepositoryImpl.kt)
- Maps platform provider data to domain models.

---

### Dependency Injection

#### [PlatformModule.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/di/PlatformModule.kt)
- Binds `DisplayDensityProvider` to `AndroidDisplayDensityProvider`.

#### [RepositoryModule.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/di/RepositoryModule.kt)
- Binds `DisplayDensityRepository` to `DisplayDensityRepositoryImpl`.

---

### Presentation Layer

#### [HomeViewModel.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeViewModel.kt)
- Collects `DisplayDensityInfo` and exposes it as StateFlow to the UI.

#### [HomeScreen.kt](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/app/src/main/java/com/devtune/ui/screens/home/HomeScreen.kt)
- Displays compact "Display Density" card with logical DPI, scale, resolution, smallest width, and read-only status badge.
- Shows detail sheet with comprehensive breakdown and read-only architectural explanation on click.

---

### Documentation

#### [NEW] [ADR-005-display-density.md](file:///C:/Users/sgosikonda/Desktop/Projects/DevTune/DevTune_Codex_Foundation/docs/decisions/ADR-005-display-density.md)
- Documents density concepts, public read APIs, absence of public write APIs, M5 lessons, forbidden alternatives, and capability gating.

## Verification Plan

### Automated Tests
- **Unit Tests**:
  - `DisplayDensityInfoTest`: Scale calculation, smallest width cross-check, zero-DPI protection, null forced density.
  - `DisplayDensityRepositoryTest`: Verifies flow emissions.
- **Instrumentation Test**:
  - `DisplayDensityRuntimeTest`: Verifies real Android display metrics on `emulator-5554`.

### Quality Gate
- `assembleDebug`
- `assembleRelease`
- `testDebugUnitTest`
- `lintDebug`
- Git commit: `feat: add read-only display density monitoring`
