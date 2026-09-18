# ADR-003: Shizuku Integration Foundation

## Status
Accepted

## Context
DevTune requires privileged access to modify system settings such as Animation Scales and Display Density (Smallest width) without requiring Root or manual ADB commands for every change. Shizuku provides a standard way for third-party apps to use system APIs with user authorization.

## Decision
We have implemented a foundation layer for Shizuku integration following a capability-driven architecture.

### Key Architectural Choices:
1.  **Optional Layer**: Shizuku is strictly optional. The app remains fully functional (in read-only mode) if Shizuku is missing or stopped.
2.  **Domain Abstraction**: The core logic depends on a domain-level `ShizukuState` rather than Shizuku SDK classes.
3.  **Binder Lifecycle**: We use Shizuku's official `OnBinderReceivedListener` and `OnBinderDeadListener` to react to service state changes in real-time via a `callbackFlow`. This avoids polling and ensures reactive UI updates.
4.  **Backend Distinction (ADB vs ROOT)**: The implementation distinguishes between `ADB` (UID 2000) and `ROOT` (UID 0) backends. If the identity cannot be determined, it reports `UNKNOWN`. This supports future capability resolution as ADB identity often has different permission constraints than Root.
5.  **Deferred Privileged Writes**: Milestone M4 is restricted to **status detection and permission management**. No system writes or `UserService` (AIDL) implementations are included to minimize complexity during the foundation phase and ensure a safe implementation path.
6.  **Capability-Driven Design**: Shizuku status is mapped to specific capabilities. Future write operations (`WRITE_ANIMATION_SETTINGS`, etc.) remain `RESTRICTED` or `UNKNOWN` in this milestone, regardless of Shizuku authorization, until they are individually verified.

## Consequences
*   **Safety**: Users must explicitly grant permission through the Shizuku manager app. No privileged settings are modified in this foundation phase.
*   **Transparency**: The UI clearly reports the current Shizuku status and backend type.
*   **Maintainability**: Future milestones can build on the `ShizukuProvider` interface to implement specific write operations once verified.
*   **Compatibility**: Shizuku authorization does not guarantee unrestricted access; ADB-backed sessions have documented limitations that will be handled per-operation.
