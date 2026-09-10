# DevTune — Master AI-Agent Development Specification

## Mission
Build a production-quality Android utility, not a prototype. Prioritize correctness, safety, maintainability, testability, accessibility, and honest platform behavior.

## Milestones
M0 Research & project foundation
M1 Design system
M2 Navigation & onboarding
M3 Home screen
M4 Developer Options
M5 Animation system
M6 Display Density
M7 Profiles
M8 Quick Settings
M9 Settings & capabilities
M10 Testing & accessibility
M11 OEM/device compatibility
M12 Security & Play Store review
M13 Release candidate
V2: ADB / Power User Mode

## Milestone protocol
Before coding: read PRD/TDD, inspect repository, verify APIs, identify risks, and plan.
During coding: keep changes scoped, add tests with features, preserve architecture, avoid speculative dependencies.
After coding: build, test, lint/static analysis, security review, accessibility review, UX review, fix issues, re-run, update docs, report.

## API research
For Android platform behavior, use official documentation first. Never invent an API or assume a setting is writable simply because Android exposes the setting.

Classify every relevant operation:
READABLE / WRITABLE / USER-GRANTED / PRIVILEGED / ADB-CAPABLE / ROOT-ONLY

Only implement what is appropriate for V1.

## UI quality
Every major screen must be checked for hierarchy, spacing, alignment, typography, touch targets, loading/empty/error states, dark theme, large text, accessibility semantics, and responsive behavior.

## Release gate
Do not declare V1 complete until PRD/TDD requirements are satisfied, builds/tests/lint pass, permissions are justified, security and accessibility reviews pass, documentation is current, unsupported platform features are clearly represented, and the release build is validated.

## Reporting
At milestone completion report implemented items, tests/results, build/lint results, known limitations, files/modules changed, and next milestone.

## Golden rule
Do not optimize for speed of code generation. Optimize for the correct product.
