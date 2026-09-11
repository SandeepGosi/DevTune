# M0 verification plan

- `testDebugUnitTest` verifies local JVM test configuration.
- `lintDebug` checks Android lint for the debug variant.
- `assembleDebug` and `assembleRelease` compile both application artifacts.
- `connectedDebugAndroidTest` is the required device/emulator verification for the Compose launch test and Hilt test runner. It can only be run when an Android device or emulator is available.

The instrumentation test uses `HiltTestRunner`, launches `MainActivity` through Hilt, and asserts that the initial Compose content is displayed.

## Environment result (2026-09-10)

The final Gradle 9.6.0 wrapper was verified with Android Studio's bundled JDK 21 using `gradlew.bat --version`. `assembleDebug`, `assembleRelease`, `testDebugUnitTest`, and `lintDebug` were each attempted; `assembleDebug` was retried after the AGP 9.4 / Gradle 9.6.0 alignment. Build execution could not proceed to project evaluation in this hosted environment: Gradle failed while creating its single-use daemon with `java.io.IOException: Unable to establish loopback connection`. The failure occurred before dependency resolution, compilation, lint, or test execution. No build, lint, unit-test, or instrumentation-test success is claimed until the commands are run in an environment that permits Gradle's local loopback daemon.
