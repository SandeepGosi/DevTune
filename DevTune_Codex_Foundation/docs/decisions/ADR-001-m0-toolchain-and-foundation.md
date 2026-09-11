# ADR-001: M0 toolchain and project foundation

## Status

Accepted — 2026-09-10.

## Context

M0 needs a current, stable, Play-ready Android build foundation without implementing product controls.

## Decision

- Use Android Gradle Plugin 9.4.0, Gradle 9.6.0, and JDK 21 compatibility.
- Use compile and target SDK 37 (Android 16 DP) to satisfy current AndroidX dependency requirements. Use min SDK 24.
- Use Kotlin through AGP 9 built-in Kotlin support, with KSP for Hilt code generation.
- Use Kotlin 2.4.20 with KSP 2.3.12. KSP's current release identifiers no longer mirror the Kotlin version; 2.3.12 is the published KSP release compatible with Kotlin 2.4.20. AGP 9's built-in Kotlin is retained; Kotlin Gradle Plugin 2.4.20 is added to the buildscript classpath as the AGP 9 migration guidance prescribes when using a version newer than AGP's bundled KGP.
- Integrate the `org.jetbrains.kotlin.plugin.compose` Gradle plugin (Compose Compiler) as required for Kotlin 2.0+.
- Set `org.gradle.jvmargs=-Xmx2048m` to ensure stable compilation.
- Use Compose BOM 2026.08.00 and Material 3 1.4.0, Navigation Compose 2.10.1, Hilt 2.60.1, DataStore 1.2.0, Lifecycle 2.11.0, and Activity 1.13.0.
- Keep the app permission-free in M0. No system-control provider, TileService, ADB, or privileged operation is introduced.

## Consequences

The app starts with a small Material 3 Compose surface, Hilt entry points, navigation, and a Preferences DataStore data source. Future work must retain the specified layer flow and verify every platform capability before implementation.

## Sources

- Android Gradle Plugin compatibility: https://developer.android.com/build/releases/about-agp
- Kotlin/AGP compatibility: https://developer.android.com/build/kotlin-support
- Android Studio releases: https://developer.android.com/studio/releases
- Compose BOM: https://developer.android.com/develop/ui/compose/bom
- AndroidX stable releases: https://developer.android.com/jetpack/androidx/versions
- Hilt Gradle setup: https://dagger.dev/hilt/gradle-setup.html
- KSP releases: https://github.com/google/ksp/releases
- KSP plugin metadata: https://central.sonatype.com/artifact/com.google.devtools.ksp/com.google.devtools.ksp.gradle.plugin
- Google Play target API requirement: https://developer.android.com/google/play/requirements/target-sdk
