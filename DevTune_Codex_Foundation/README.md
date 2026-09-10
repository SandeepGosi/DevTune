# DevTune

DevTune is an Android power-user utility focused on convenient access to Developer Options, animation preferences, display-density information, profiles, and Quick Settings.

## Project documents
- `AGENTS.md` — repository-level Codex instructions
- `docs/PRD.md` — product requirements
- `docs/TDD.md` — technical design
- `docs/MASTER_AGENT_SPEC.md` — Codex development operating specification

## Current status
M0 — project foundation/documentation.

No production implementation has been created yet.

## V1 philosophy
V1 is a normal Play-Store-compatible Android application. It must not bypass Android security restrictions. Future ADB/power-user functionality is intentionally separated behind capability/provider abstractions.
