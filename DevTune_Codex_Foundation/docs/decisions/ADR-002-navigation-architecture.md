# ADR-002: Navigation Architecture and Type-Safety

## Status

Accepted — 2026-09-15.

## Context

DevTune needs a scalable navigation architecture that allows adding future destinations (Developer Options, Animation, Display Density, Profiles) without significant restructuring. It also needs to be production-ready and leverage modern Android development practices.

## Decision

- Use **Navigation Compose 2.10.1** (as established in the foundation).
- Adopt **Type-Safe Navigation** using Kotlin Serialization.
- Represent destinations as `@Serializable` data objects within a sealed interface `DevTuneDestination`.
- Centralize navigation logic in `DevTuneNavHost`.
- Use **Clean Architecture** patterns to decouple navigation from business logic:
    - `OnboardingViewModel` determines if onboarding is complete via use cases.
    - `DevTuneNavHost` handles the actual transition from `Onboarding` to `Home`.

## Consequences

- Type-safety ensures that route arguments (if added later) are verified at compile time.
- Adding a new destination only requires adding a `@Serializable` object to `DevTuneDestination` and a `composable` call in `DevTuneNavHost`.
- The application flow (Onboarding -> Home -> Settings) is clearly defined and maintainable.
- Requires `kotlinx.serialization` plugin and library.

## Alternatives Considered

- **String-based Routes:** Error-prone and lacks compile-time verification for arguments.
- **Third-party Navigation Libraries:** Avoided to minimize unnecessary dependencies and stick to official Jetpack libraries.
