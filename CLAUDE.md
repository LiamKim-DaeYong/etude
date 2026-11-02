# Etude

Order, inventory and payment system for exploring design problems under high concurrency.

- `server/`: Kotlin, Spring Boot 4.1, Java 25, PostgreSQL. Modular monolith on Spring Modulith, package `dev.etude`
- `web/`: frontend, stack undecided
- `docs/`: ADRs in `docs/decisions/`, concept notes in `docs/learning/`

## Commands

Run from `server/` (on Windows use `.\gradlew.bat`):

- Build and test: `./gradlew build`
- Single test class: `./gradlew test --tests dev.etude.ModularityTests`
- Run locally: `./gradlew bootRun` (starts PostgreSQL from `compose.yaml`)

Docker must be running: tests use Testcontainers, `bootRun` uses Docker Compose. Schema changes go in Flyway migrations under `server/src/main/resources/db/migration`; JPA only validates the schema.

## Working agreement

The repository owner writes the design and core code. Claude explains concepts, asks design questions, challenges decisions, and reviews, tests and debugs. Do not implement features unless explicitly asked. Setup, build and refactoring chores are fine on request; say what will change before changing code.

## Rules

- Declare dependency versions in `server/gradle/libs.versions.toml`, not in build scripts.
- `ModularityTests` verifies module boundaries. Fix a violation instead of bypassing the test.
- Keep decision status separate from implementation and verification status. If something is unverified, say so.
- Back performance and concurrency claims with measurements, and state the environment and conditions.
- To change a decision, write a new ADR linked to the old one. See `docs/README.md` for documentation rules.

## Workflow

Branch, commit, PR and language rules:

@CONTRIBUTING.md
