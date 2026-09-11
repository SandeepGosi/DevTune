# M0 verification plan

- `testDebugUnitTest` verifies local JVM test configuration.
- `lintDebug` checks Android lint for the debug variant.
- `assembleDebug` and `assembleRelease` compile both application artifacts.
- `connectedDebugAndroidTest` is the required device/emulator verification for the Compose launch test and Hilt test runner. It can only be run when an Android device or emulator is available.

The instrumentation test uses `HiltTestRunner`, launches `MainActivity` through Hilt, and asserts that the initial Compose content is displayed.

## Environment result (2026-09-10)

The final Gradle 9.6.0 wrapper was verified with Android Studio's bundled JDK 21 using `gradlew.bat --version`. `assembleDebug`, `assembleRelease`, `testDebugUnitTest`, and `lintDebug` were each attempted; `assembleDebug` was retried after the AGP 9.4 / Gradle 9.6.0 alignment. Build execution could not proceed to project evaluation in this hosted environment: Gradle failed while creating its single-use daemon with `java.io.IOException: Unable to establish loopback connection`. The failure occurred before dependency resolution, compilation, lint, or test execution. No build, lint, unit-test, or instrumentation-test success is claimed until the commands are run in an environment that permits Gradle's local loopback daemon.

## M0 toolchain stabilization (2026-09-11)

The toolchain was stabilized by addressing multiple blockers:

1.  **KSP Version Correction**: Corrected `com.google.devtools.ksp` to version `2.3.12`. The previously referenced `2.4.20-2.0.4` was an invalid/unpublished artifact.
2.  **Compose Compiler Integration**: Added the `org.jetbrains.kotlin.plugin.compose` Gradle plugin, which is required for Kotlin 2.0+.
3.  **SDK Alignment**: Upgraded `compileSdk` to `37` and `minSdk` to `24` to satisfy modern AndroidX dependency requirements (Navigation 2.10.1, Compose 1.12.0).
4.  **Memory Optimization**: Increased JVM heap to 2GB (`org.gradle.jvmargs=-Xmx2048m`) to prevent daemon thrashing during compilation.
5.  **Code Correction**: Added missing `ThemeMode` enum and associated imports in `UserPreferencesDataSource.kt`, and applied `@OptIn(ExperimentalMaterial3Api::class)` to `HomeScreen.kt`.

### Verification Results

All foundations are now verified successfully:

- `.\gradlew.bat assembleDebug`: **SUCCESSFUL**
- `.\gradlew.bat assembleRelease`: **SUCCESSFUL**
- `.\gradlew.bat testDebugUnitTest`: **SUCCESSFUL** (1 test passed)
- `.\gradlew.bat lintDebug`: **SUCCESSFUL** (Report generated)

The M0 Android foundation is now fully compilable and verified.
