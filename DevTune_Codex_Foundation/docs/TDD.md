# DevTune — Technical Design Document

## Stack
- Kotlin
- Jetpack Compose
- Material 3
- Clean Architecture + MVVM
- Navigation Compose
- Kotlin Coroutines
- StateFlow
- Hilt
- Preferences DataStore
- Room only if later justified
- TileService for Quick Settings
- JUnit + Compose/UI/instrumentation testing
- Gradle Kotlin DSL
- Version Catalog

Library and SDK versions must be verified against current stable compatible versions when the project is initialized.

## Architecture
Presentation: Compose UI, ViewModels, UI state.
Domain: Models, use cases, repository interfaces, capability models.
Data: Repositories, DataStore, mappers.
Platform: Android Settings integration, Developer Options, animation access, display density, Quick Settings, future ADB provider.

Flow:
Compose UI -> ViewModel -> Use Case -> Repository -> Platform Provider

## Suggested package layout
`app/src/main/java/com/devtune/`
- `DevTuneApplication.kt`
- `MainActivity.kt`
- `di/`
- `navigation/`
- `ui/theme/`
- `ui/components/`
- `ui/screens/`
- `domain/model/`
- `domain/repository/`
- `domain/usecase/`
- `data/repository/`
- `data/datastore/`
- `data/mapper/`
- `platform/developer/`
- `platform/animation/`
- `platform/density/`
- `platform/quicksettings/`
- `util/`

Exact structure may be refined, but layer responsibilities are mandatory.

## Capability model
Capabilities:
- READ_DEVELOPER_OPTIONS
- OPEN_DEVELOPER_OPTIONS
- READ_ANIMATION_SETTINGS
- WRITE_ANIMATION_SETTINGS
- READ_DISPLAY_DENSITY
- WRITE_DISPLAY_DENSITY
- CONTROL_DEVELOPER_OPTIONS
- ADB_AVAILABLE

States:
- Available
- Unavailable
- Restricted
- Unknown

UI must depend on capability state, not a hard-coded V1/V2 check.

## Developer Options
Use an abstraction such as DeveloperOptionsController with `getStatus()` and `openDeveloperOptions()`. V1 uses only supported public mechanisms. Future ADB implementation is separate.

## Animation
Domain model: `AnimationSettings(windowScale, transitionScale, animatorDurationScale)`.
Repository separates reading current state from storing preferred state. Do not imply write capability where it does not exist.

## Display Density
Domain model distinguishes current density, logical dimensions, physical resolution when available, and optional device information. Validate custom values before future application.

## Profiles
Profile includes id, name, animationSettings, preferredDensity, developerOptionsPreference, built-in flag, and timestamps as needed.

Built-ins: DEFAULT, FAST, NO_ANIMATION, CUSTOM.

## DataStore
Store theme, dynamic color, onboarding state, selected profile, default temporary duration, preferred animation settings, preferred density, and Quick Settings preferences.

## Quick Settings
Use thin TileService implementations. Delegate business logic to use cases/capability layer. Handle lifecycle correctly.

## Timer / future sessions
`TemporarySession(startedAt, expiresAt, previousState, targetState)`.
Use persisted absolute expiration timestamps. Scheduling mechanism will be finalized in V2 according to Android background-execution constraints.

## Error handling
Structured domain errors such as PlatformUnavailable, PermissionDenied, CapabilityUnavailable, UnsupportedDevice, InvalidValue, OperationFailed, Unknown. Do not expose raw Android exceptions directly to UI.

## Security
Least privilege. No hidden APIs, root in V1, arbitrary shell, accessibility workaround, unnecessary permissions, or security bypass.

## Testing
Unit: domain models, use cases, profile logic, validators, timer logic, capability logic.
Repository: DataStore, mapping, repository behavior.
UI: onboarding, navigation, profile selection, settings, dark mode, accessibility, errors.
Instrumentation/device: Settings integration, Quick Settings, device configuration, OEM behavior.

## CI/CD
At minimum: Debug build, Release build, Unit tests, Lint/static analysis.

## Documentation
Maintain PRD.md, TDD.md, architecture/, decisions/, testing/, compatibility/. Record major architectural decisions as ADRs.

## Future ADB architecture
`SystemControlProvider` -> `StandardProvider` / future `AdbProvider`.
Do not implement speculative root support.
Future ADB must expose typed, validated operations rather than arbitrary command execution.
