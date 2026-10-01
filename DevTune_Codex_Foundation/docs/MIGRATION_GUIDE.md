# DevTune — Project Migration & Backup Guide

This guide provides step-by-step instructions for backing up DevTune from your current PC and restoring it seamlessly on a new PC to continue development from Milestone M8.

---

## 1. What to Backup (From Current PC)

### Primary Project Folder
Path: `C:\Users\sgosikonda\Desktop\Projects\DevTune\DevTune_Codex_Foundation`

This directory contains:
- Entire Git repository (`.git` directory, commit history up to `537d124`)
- All source code (`app/src/...`)
- Documentation (`docs/PRD.md`, `docs/TDD.md`, `AGENTS.md`, `docs/decisions/ADR-001` through `ADR-009`)
- Artifacts (`.artifacts/`)

> [!NOTE]
> You do **NOT** need to copy `build/`, `.gradle/`, or `.idea/` folders as Gradle will automatically rebuild dependencies on the new PC.

### Recommended Backup Command (PowerShell Zip)
Run this command in PowerShell to create a single zip file `DevTune_Backup.zip` on your Desktop:

```powershell
Compress-Archive -Path "C:\Users\sgosikonda\Desktop\Projects\DevTune\DevTune_Codex_Foundation\*" -DestinationPath "$HOME\Desktop\DevTune_Backup.zip" -Exclude "*.gradle*", "*\build\*", "*\.idea\*"
```

Upload `DevTune_Backup.zip` to Google Drive / OneDrive / USB flash drive, or push to a remote Git repository (e.g. GitHub / GitLab).

---

## 2. Setting Up the New PC

### Step A: Prerequisites Installation
1. **JDK 21**: Install OpenJDK 21 or Eclipse Temurin 21. Set `JAVA_HOME`.
2. **Android Studio**: Install Android Studio (Ladybug / Jellyfish or newer).
3. **Android SDK**:
   - SDK Platform 36 & 37
   - Android SDK Build-Tools & Platform-Tools (`adb`)

### Step B: Project Extraction
Extract `DevTune_Backup.zip` to your desired projects directory on the new PC (e.g. `C:\Users\<YourUser>\Desktop\Projects\DevTune\DevTune_Codex_Foundation`).

### Step C: Build Verification
Open terminal in the project directory on the new PC and run:

```powershell
./gradlew assembleDebug testDebugUnitTest
```

Gradle will automatically download Gradle 9.6.0 wrapper and AGP 9.4.0 dependencies and build the project cleanly.

---

## 3. Emulator & Shizuku Setup on New PC

### Step A: Create Emulator
In Android Studio Device Manager, create an AVD:
- **Device**: Pixel 10 Pro or Pixel 9
- **System Image**: Android 16 / API 36 (`x86_64` 16k page size system image) or API 37
- **Name**: `emulator-5556` (or default emulator)

### Step B: Install DevTune & Shizuku
1. Install DevTune debug APK:
   ```powershell
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. Install Shizuku APK (`moe.shizuku.privileged.api`):
   Download Shizuku APK from GitHub release or transfer `moe.shizuku.privileged.api` and run:
   ```powershell
   adb install -r shizuku.apk
   ```
3. Start Shizuku Service via ADB:
   ```powershell
   adb shell sh /data/data/moe.shizuku.privileged.api/lib/libshizuku.so
   ```
4. Grant Shizuku Authorization for DevTune in the Shizuku app UI or via prompt.

### Step C: Run Runtime Tests
```powershell
./gradlew assembleDebugAndroidTest
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e class com.devtune.QuickSettingsTileRuntimeTest com.devtune.test/com.devtune.HiltTestRunner
```

---

## 4. Milestone Checkpoint Status
- **Current State**: Milestone M8 (Quick Settings Fast / Restore Tile) Completed.
- **Git Commit**: `537d124` (`feat: add quick settings animation tile`).
- **Working Tree**: 100% clean.
