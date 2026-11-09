# Etude

Multi-tenant business system (orders, inventory, production) and website for small manufacturers, built to explore design problems in data isolation, concurrency and external integration. Direction: `docs/decisions/0005-product-direction.md`.

- `server/`: backend. Kotlin, Spring Boot 4.1, Java 25, PostgreSQL. Modular monolith on Spring Modulith, package `com.etude`
- `web/`: frontend. Area rules in `web/AGENTS.md`
- `docs/`: ADRs in `docs/decisions/`, concept notes in `docs/learning/`

## Roles

- The repository owner sets direction, approves designs and approves every merge.
- Claude implements the backend (`server/`). Codex implements the frontend (`web/`). Root `CLAUDE.md` holds Claude's backend rules and belongs to the backend area.
- Change only your own area. Shared files (root `README.md`, `CONTRIBUTING.md`, `AGENTS.md`, `.github/`, `docs/README.md`, `docs/glossary.md`) change in a separate `area:shared` PR. Exception: a PR that adds or renames a domain term updates `docs/glossary.md` in the same PR.
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

- Use the domain terms and naming rules in `docs/glossary.md` for code, API, database, docs, issues and PRs. Do not use the words its "쓰지 않는 말" column lists.
- Keep decision status separate from implementation and verification status. If something is unverified, say so.
- Back performance and concurrency claims with measurements, and state the environment and conditions.
- To change a decision, write a new ADR linked to the old one. Pick the ADR number when opening the PR (next number on `main`). See `docs/README.md` for documentation rules.

## Backend API

The OpenAPI documents generated from the backend code are the API contract:

- `server/openapi/customer.json`: customer API (`/api/v1`)
- `server/openapi/admin.json`: back-office API (`/admin/v1`)

Their `info.description` holds the conventions (UTC instants, `Money` objects, `Problem` errors with `code`). Generate client types from these files. PRs labeled `api-change` update them. With the backend running (`cd server && ./gradlew bootRun`, Docker required), Swagger UI is at `http://localhost:8080/swagger-ui.html`.
