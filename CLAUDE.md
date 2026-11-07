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

- Repositories extend `Repository<T, ID>` and declare only the methods they need (no `JpaRepository`: it exposes deletes and bulk reads, and Kotlin's `findByIdOrNull` bypasses a redeclared `findById` with an entity graph).
- Integration tests use only `@IntegrationTest` (test sources, root package). Adding `@Import`, `@MockitoBean` or other context configuration to one test class creates another Spring context and another PostgreSQL container.
- Declare dependency versions in `server/gradle/libs.versions.toml`, not in build scripts.
- `ModularityTests` verifies module boundaries. Fix a violation instead of bypassing the test.
- A module's base package (`dev.etude.<module>`) is its public API for other modules: interfaces, DTOs, exceptions and events that another module may use (e.g. `ProductCatalog`, `ProductInfo`). Subpackages (`application`, `domain`, `web`) are internal. Implement a public interface in the module's application service.
- `dev.etude.platform` holds cross-cutting infrastructure that modules never call: `platform.web` (errors, Money JSON, OpenAPI) and `platform.time` (`Clock`, auditing). The root package holds only `EtudeApplication`.
- `dev.etude.shared` holds only value types that every module uses with the same meaning (e.g. `Money`) and the common persistence base `BaseEntity` (UUIDv7 id per ADR-0001, id-based `equals`/`hashCode`). Business rules and module-specific concepts stay in their own module. Entities extend `BaseEntity`.
- Application services return DTOs built inside the transaction (e.g. `OrderDetails`), never entities: `open-in-view` is off, so the web layer runs after the transaction ends. Web response DTOs map from these and own the JSON representation.
- API errors are ProblemDetail with a `code` property. `dev.etude.platform.web.GlobalExceptionHandler` adds common codes (`INVALID_REQUEST`, `NOT_FOUND`, `INTERNAL_ERROR`, otherwise the status name), validation `errors`, and hides details of unexpected exceptions. A module's handler sets business codes for its own exceptions and must be annotated `@Order(Ordered.HIGHEST_PRECEDENCE)` so the global catch-all does not take them.
- Money crosses the API only as `{"amount": "12.50", "currency": "USD"}` through `dev.etude.platform.web.MoneyJson`. Request and response DTOs use `Money` fields directly; do not add separate amount/currency fields. Reading rejects unknown currencies and amounts with more decimals than the currency allows (`Money.ofExact`), reported as 400 with the field path.
- Turn other invalid client input into 400 at the web boundary; do not map `IllegalArgumentException` to 400 globally.
- Get the current time from the injected `Clock` (`Instant.now(clock)`), never `Instant.now()`. The clock ticks in microseconds to match PostgreSQL `timestamptz`.
- Do not use the system default time zone or locale (`LocalDateTime.now()`, `ZoneId.systemDefault()`, `Locale.getDefault()`, `String.format` without a locale). Pass the zone or locale explicitly.
- The OpenAPI documents are generated from the code (springdoc) and committed as `server/openapi/{customer,admin}.json`. `OpenApiSpecTest` rewrites them and fails when they are out of date; commit the regenerated files. Use enum types (not `String`) for enum-valued response fields so the documents list the values. Give handler methods unique, descriptive names (they become `operationId`s) and name nested DTO classes with `@Schema(name = ...)`.
- Mark PRs that change API paths, requests, responses or error formats with `api-change`, fill in the template's API section, and include the regenerated OpenAPI files.

## Workflow

Branch, commit, PR and language rules (also referenced from `AGENTS.md`):

@CONTRIBUTING.md
