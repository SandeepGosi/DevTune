# ADR-004: Controlled Animation Write Experiment

## Status
Proposed

## Context
Milestone M5 aims to establish safe and reversible modification of Android animation settings via Shizuku. The target settings are:
* `Settings.Global.WINDOW_ANIMATION_SCALE`
* `Settings.Global.TRANSITION_ANIMATION_SCALE`
* `Settings.Global.ANIMATOR_DURATION_SCALE`

## Constraints
1. **Safety Boundary**: No shell commands, no root, no hidden APIs, no reflection.
2. **Architecture**: Clean Architecture must be preserved.
3. **Reversibility**: Original values must be captured, verified, and restored.
4. **Transparency**: Actual read-back verification is required for success reporting.

## Decision: Implementation Path for Privileged Writes

### 1. Analysis of Public SDK APIs
The standard way to modify global settings is `Settings.Global.putFloat(ContentResolver, String, float)`. However, this requires the `android.permission.WRITE_SECURE_SETTINGS` permission. Since DevTune is a 3rd party app, it cannot hold this signature-level permission. Calling this API from the app process results in a `SecurityException`.

### 2. Analysis of Shizuku Capabilities
Shizuku provides privileged access by attributing calls to the Shell (UID 2000) or Root (UID 0) identities. There are three primary ways to use Shizuku for this:
1. **Shell Commands**: Using `cmd settings put global ...`. (FORBIDDEN by safety boundary).
2. **Binder Wrapping**: Wrapping a system service binder (e.g., `window`). This requires adding hidden system AIDL files to the project, which is brittle and involves "Hidden APIs".
3. **User Service (AIDL)**: Spawning a process with Shell/Root identity that can run Java code. This process can call the public `Settings.Global.putFloat` API because it holds the necessary permission.

### 3. Selection: Shizuku User Service
We have selected the **Shizuku User Service** path as the most compliant and robust approach for M5. 

**Rationale**:
* **Safety**: It avoids arbitrary shell command execution.
* **Public APIs**: The privileged process uses the public, stable `Settings.Global` API.
* **Stability**: It does not depend on version-specific system AIDL definitions or transaction IDs.
* **Architecture**: It follows the recommended Shizuku pattern for complex logic.

We have determined that a public API path *within the app process* is impossible due to Android's UID-based security model. Therefore, a remote service with a simple custom AIDL is necessary to bridge the privilege gap.

## Experimental Flow
1. **Pre-Read**: Capture all 3 scales via standard `ContentResolver`.
2. **Write**: Attempt to set `WINDOW_ANIMATION_SCALE` to 0.5 via the User Service.
3. **Verify**: Read the value back via `ContentResolver`.
4. **Restore**: Restore all 3 original scales via the User Service.
5. **Post-Verify**: Read back all 3 values to ensure system state is returned to original.

## Consequences
* A small AIDL file will be introduced for communication between the app and the User Service.
* The application will manifest a `Service` entry for Shizuku's provider (already done in M4).
* The `WRITE_ANIMATION_SETTINGS` capability will only be marked `AVAILABLE` if the above experiment succeeds at runtime.
