# ADR-009: Quick Settings Tile & Fast Actions

## Status
Accepted

## Context
Milestone M8 provides Android Quick Settings (`TileService`) integration allowing power users to toggle `Fast (0.50x)` and `Standard (1.00x)` animation profiles directly from the Quick Settings shade.

## Design Decisions

### 1. Non-Active Tile Lifecycle
- `DevTuneTileService` uses standard non-active `TileService` lifecycle without `ACTIVE_TILE` metadata in `AndroidManifest.xml`.
- State is refreshed in `onStartListening()`, resources cancelled in `onStopListening()`. No long-lived process is maintained for the tile.

### 2. Action Model: Simplified Fast / Restore Toggle
- Replaces continuous state cycling with a clean Fast / Restore toggle:
  - Standard state (scales != `0.50x`): Label `"DevTune Fast"`, Subtitle `"Apply 0.50x"`, State `STATE_INACTIVE`. Tap applies `Fast (0.50x)`.
  - Fast state (scales == `0.50x`): Label `"DevTune Fast"`, Subtitle `"Restore Standard 1.00x"`, State `STATE_ACTIVE`. Tap restores `Standard (1.00x)`.

### 3. Priority for Active M7 Temporary Session
- If a temporary session is `ACTIVE`, `RESTORE_PENDING`, or `FAILED_TO_RESTORE`, Quick Settings actions **will NOT overwrite or replace the M7 session**.
- Tapping the tile collapses the QS shade and launches DevTune app so the user can manage or restore the session safely without risking pre-session baseline scales.

### 4. Shared Transaction Lock Serialization
- `AnimationRepositoryImpl` enforces a shared `writeMutex = Mutex()` across both `HomeViewModel` and `DevTuneTileService`.
- Serializes complete logical transactions (read -> write -> read-back verification -> rollback) to eliminate race conditions and scale corruption.

### 5. API 34+ Activity Launch Path
- On API 34+ (Android 14+), activity launch uses `TileService.startActivityAndCollapse(PendingIntent)`. On API < 34, uses `TileService.startActivityAndCollapse(Intent)`.

### 6. Policy & Compliance Statement
- No prohibited implementation mechanisms (shell commands, `Runtime.exec`, `rish`, accessibility, package spoofing, reflection) were introduced. Distribution/policy compliance requires separate review.
