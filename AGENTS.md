# Etude

Order, inventory and payment system for exploring design problems under high concurrency.

- `server/`: backend. Kotlin, Spring Boot 4.1, Java 25, PostgreSQL. Modular monolith on Spring Modulith, package `dev.etude`
- `web/`: frontend. Area rules in `web/AGENTS.md`
- `docs/`: ADRs in `docs/decisions/`, concept notes in `docs/learning/`

## Roles

- The repository owner sets direction, approves designs and approves every merge.
- Claude implements the backend (`server/`). Codex implements the frontend (`web/`). Root `CLAUDE.md` holds Claude's backend rules and belongs to the backend area.
- Change only your own area. Shared files (root `README.md`, `CONTRIBUTING.md`, `AGENTS.md`, `.github/`, `docs/README.md`) change in a separate `area:shared` PR.
- For design decisions, present options with a recommendation and get the owner's approval before implementing.
- Record progress, dependencies and API changes in issues and PRs, not only in chat, so the other agent can read them with `gh`.

## Workflow

Follow `CONTRIBUTING.md` for branches, commits, PRs, issues, labels, the BE/FE collaboration rules and the writing language.

## Global service baseline

Build as a service for many countries from the start, so a new market does not require data or API changes.

- **Time:** store and exchange instants in UTC (ISO-8601 with `Z`). Business dates such as "today" or a daily settlement use the market's time zone explicitly (e.g. `Asia/Seoul`). Display conversion to the user's time zone happens in the client.
- **Money:** an amount always travels with its ISO 4217 currency code. Never assume a currency. Rounding follows the currency's minor units.
- **Text:** the server returns machine-readable values (`code`, enum names), not display text. Clients localize. Error `detail` is English for developers.
- **Environment:** results must not depend on the server's default time zone or locale.

## Rules

- Keep decision status separate from implementation and verification status. If something is unverified, say so.
- Back performance and concurrency claims with measurements, and state the environment and conditions.
- To change a decision, write a new ADR linked to the old one. Pick the ADR number when opening the PR (next number on `main`). See `docs/README.md` for documentation rules.

## Backend API (for frontend integration)

- Customer API under `/api/v1`, back-office API under `/admin/v1`.
- Errors are ProblemDetail (`application/problem+json`) with a `code` property; validation failures add `errors: [{field, code, message}]` where `code` is the violated constraint (e.g. `NotBlank`). Branch on `code`, not on `detail`.
- Instants are ISO-8601 UTC strings ending in `Z`.
- Amounts are decimal strings (e.g. `"10000"`, `"12.50"`) next to an ISO 4217 currency code. Do not parse them into floating-point numbers for arithmetic.
- Run the backend locally: `cd server && ./gradlew bootRun` (Docker must be running; starts PostgreSQL from `compose.yaml`).
- PRs labeled `api-change` change paths, requests, responses or errors.
