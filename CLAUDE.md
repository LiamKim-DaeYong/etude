# Etude (backend)

Shared project context, roles and workflow:

@AGENTS.md

This file adds the backend rules for Claude, who works only in `server/`.

## Commands

Run from `server/` (on Windows use `.\gradlew.bat`):

- Build and test: `./gradlew build`
- Single test class: `./gradlew test --tests dev.etude.ModularityTests`
- Run locally: `./gradlew bootRun` (starts PostgreSQL from `compose.yaml`)
- Format Kotlin: `./gradlew ktlintFormat` (`build` runs `ktlintCheck`; style is `intellij_idea`, set in `.editorconfig`)

Docker must be running: tests use Testcontainers, `bootRun` uses Docker Compose. Schema changes go in Flyway migrations under `server/src/main/resources/db/migration/<module>` (Spring Modulith runs them per module, each with its own history table and version numbers); JPA only validates the schema.

## Rules

- Declare dependency versions in `server/gradle/libs.versions.toml`, not in build scripts.
- `ModularityTests` verifies module boundaries. Fix a violation instead of bypassing the test.
- `dev.etude.shared` holds only value types that every module uses with the same meaning (e.g. `Money`) and the common persistence base `BaseEntity` (UUIDv7 id per ADR-0001, id-based `equals`/`hashCode`). Business rules and module-specific concepts stay in their own module. Entities extend `BaseEntity`.
- Application services return DTOs built inside the transaction (e.g. `OrderDetails`), never entities: `open-in-view` is off, so the web layer runs after the transaction ends. Web response DTOs map from these and own the JSON representation.
- API errors are ProblemDetail with a `code` property. `dev.etude.web.GlobalExceptionHandler` adds common codes (`INVALID_REQUEST`, `NOT_FOUND`, `INTERNAL_ERROR`, otherwise the status name), validation `errors`, and hides details of unexpected exceptions. A module's handler sets business codes for its own exceptions and must be annotated `@Order(Ordered.HIGHEST_PRECEDENCE)` so the global catch-all does not take them.
- Turn invalid client input into 400 at the web boundary (e.g. a request's `toMoney()`); do not map `IllegalArgumentException` to 400 globally.
- Get the current time from the injected `Clock` (`Instant.now(clock)`), never `Instant.now()`. The clock ticks in microseconds to match PostgreSQL `timestamptz`.
- Do not use the system default time zone or locale (`LocalDateTime.now()`, `ZoneId.systemDefault()`, `Locale.getDefault()`, `String.format` without a locale). Pass the zone or locale explicitly.
- Mark PRs that change API paths, requests, responses or error formats with `api-change` and fill in the template's API section.

## Workflow

Branch, commit, PR and language rules (also referenced from `AGENTS.md`):

@CONTRIBUTING.md
