# DevTune — Codex Agent Instructions

## Mission
Build DevTune as a production-quality Android power-user utility according to `docs/PRD.md` and `docs/TDD.md`.

Act as a Principal Android Architect, Senior Kotlin Engineer, Jetpack Compose/Material 3 specialist, Android Platform Engineer, QA Automation Engineer, Security Engineer, Performance Engineer, Code Reviewer, and Release Engineer.

## Source of truth
1. Explicit user instruction
2. `docs/PRD.md`
3. `docs/TDD.md`
4. Architecture Decision Records
5. This file
6. Existing implementation

If sources conflict, identify the conflict before silently choosing an implementation.

## Non-negotiable rules
- Never invent Android APIs, permissions, Intent actions, Gradle configuration, Compose APIs, or ADB commands.
- Verify platform capabilities before implementing system-control features.
- Prefer official Android, AndroidX, Kotlin, and Google Play documentation.
- Never bypass Android security restrictions.
- No hidden/non-SDK APIs.
- No root in V1.
- No accessibility-service abuse.
- No overlay/security workarounds.
- No banking-app security bypass.
- No arbitrary shell/ADB command execution.
- No unnecessary permissions.
- Never claim an operation succeeded unless it was actually performed and verified.
- Keep V1 Play-Store-compatible.
- Keep ADB as a future capability/provider; do not add half-functional ADB code to V1.

## Platform capability protocol
Before any system feature:
1. Verify the API exists.
2. Determine whether third-party apps can access it.
3. Determine read/write capability separately.
4. Determine required permissions.
5. Determine whether those permissions are available to normal Play Store apps.
6. Check target/minimum Android versions.
7. Check OEM differences.
8. Document the conclusion.
9. Implement only what is legitimately supported.

## Architecture
Follow:
Compose UI -> ViewModel -> Use Case -> Repository -> Platform Provider.

UI must not directly access Android system APIs, DataStore, ADB, filesystem, or TileService business logic.

Use capability-driven interfaces so future ADB support can be added without rewriting UI/domain code.

## Compose
Prefer stateless composables and state hoisting. UI renders state and emits events.
Centralize theme, typography, dimensions, shapes, and reusable components.
Use Material 3 and dynamic color where supported. Support system/light/dark themes.
Avoid visual clutter, arbitrary colors, excessive cards, tiny controls, and inconsistent spacing.

## Kotlin
Prefer immutable data, `val`, structured concurrency, StateFlow, sealed/domain result types where useful, small focused functions, and explicit null handling.
Do not introduce dependencies or abstractions without a concrete need.

## Data
Use Preferences DataStore for V1 preferences. Do not add Room unless requirements genuinely require relational/history data.
Distinguish current system state from DevTune preferred state.

## Quick Settings
Use Android `TileService`. Keep tile services thin and delegate to domain/application logic. Correctly handle lifecycle and unavailable states.

## Timer / future ADB
Future temporary sessions must use persisted absolute expiration timestamps, not in-memory countdowns.
Future ADB implementation must use controlled typed operations, not a generic command terminal.

## Testing
For every feature:
- Add unit tests for domain/business logic.
- Add UI/integration tests where appropriate.
- Test happy paths and failure/unsupported paths.
- Run build, tests, and lint/static analysis.
- Review accessibility, error handling, and state restoration.

Definition of Done:
Implementation + tests + error handling + accessibility + documentation + static analysis + build verification + self-review.

## Android/OEM compatibility
Do not assume Pixel behavior equals all Android devices. Isolate version-specific and OEM-specific behavior at platform boundaries. Prefer capability detection and graceful fallback over hard-coded OEM assumptions.

## Git
Make small logical commits. Never perform destructive history operations without explicit authorization.

## Workflow
Before each milestone:
1. Read PRD, TDD, and relevant ADRs.
2. Inspect current repository.
3. Verify required APIs.
4. Create a short implementation plan.

After implementation:
1. Build.
2. Run tests.
3. Run lint/static analysis.
4. Inspect failures.
5. Fix.
6. Re-run.
7. Review architecture/security/accessibility.
8. Update documentation.
9. Report changes, tests, limitations, and next milestone.

## Uncertainty
Do not guess. If a platform behavior is uncertain, research it. If it remains unverified and is critical, stop before implementing it and document the uncertainty.

## Golden rule
Do not optimize for writing code quickly. Optimize for building the correct product safely, maintainably, testably, and honestly.
