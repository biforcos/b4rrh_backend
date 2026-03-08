# B4RRHH Backend - Repository Instructions

This repository implements the backend for B4RRHH, an HR application for Spain.

## Core architecture principles

- Use strict hexagonal architecture.
- Keep domain, application, and infrastructure clearly separated.
- The domain layer must be framework-agnostic.
- Do not introduce Spring annotations into domain model classes.
- Do not use JPA entities as domain entities.
- Do not return domain entities directly from REST APIs.
- Use explicit input ports and output ports.
- Controllers are input adapters.
- Persistence implementations are output adapters.
- Business logic belongs in application services and domain services, never in controllers or repositories.

## Contract-first policy

- OpenAPI is the source of truth for all HTTP contracts.
- Never invent endpoints, request bodies, response bodies, status codes, or field names without updating or consulting the OpenAPI contract.
- If an API change is needed, propose the OpenAPI change first.
- The backend must implement the API contract, not define it ad hoc in controller code.
- Never let generated API models leak into the domain model.
- Use generated contract classes only at the API boundary if generation is enabled.

## Functional scope

Current scope is Personnel Administration for Spain only.

Included in pilot scope:
- Employees
- Personal data
- Employment contracts under Spain-oriented modeling
- Work center assignment
- Collective agreement category
- Collective agreement
- Regulation concept similar to HRAccess "reglementation"

Excluded for now:
- Payroll
- Legal reporting
- Time and attendance
- Absence management beyond future placeholders
- Any payroll-specific tables or logic

Payroll must remain clearly separated from Personnel Administration. Do not mix concerns.

## Data model principles

- The root aggregate concept is Employee.
- Employee has an internal numeric identifier used as a technical internal number similar in spirit to a NUDOSS-like internal identifier.
- This internal numeric identifier is not the business employee identifier.
- Most business information is expected to be historical or repetitive and should be modeled in dependent structures rather than flattened into the root employee table.
- Prefer explicit valid-from / valid-to modeling when history matters.
- Avoid deleting business history.
- Do not create or modify the data model autonomously.
- Any new table, column, relationship, unique constraint, or index must be proposed and justified before implementation.
- Any legal or HR business interpretation must be treated as untrusted until confirmed by the user.

## Security principles

- Use Spring Security.
- Design with roles from the start.
- Prefer clear role-based authorization such as ADMIN, HR_MANAGER, and HR_VIEWER unless the user defines otherwise.
- Keep security rules explicit and testable.
- Do not hide authorization decisions in controllers.

## Persistence rules

- Use PostgreSQL.
- Use JPA/Hibernate only in infrastructure adapters.
- Keep repository interfaces in the application/domain boundary as ports.
- Keep Spring Data repositories and JPA entities in infrastructure.
- Avoid bidirectional relationships unless clearly justified.
- Prefer simpler mappings over clever ORM tricks.
- Prevent lazy-loading leaks across architectural boundaries.

## Migration rules

- Use Flyway for schema migrations.
- Schema changes must be deliberate, small, reviewable, and traceable.
- Never modify old migrations once applied.
- Add a new migration for every schema change.
- Do not generate schema changes without an explicit design decision.

## Code style and quality

- Use Java 21.
- Use the latest stable Spring Boot line adopted by the project.
- Prefer constructor injection.
- Avoid field injection.
- Avoid static utility abuse.
- Prefer immutable command/query objects where reasonable.
- Use meaningful names in English.
- Keep methods small and intention-revealing.
- No placeholder code, fake implementations, or silent assumptions.

## Testing policy

- Generate tests for all non-trivial code.
- Prioritize unit tests for domain and application layers.
- Add integration tests for infrastructure adapters and security behavior.
- Do not mark work as done if it is untested.
- If a test cannot be written yet, explain exactly why.
- Never remove tests to make the build pass.

## Copilot behavior rules

- Copilot may generate scaffolding, refactorings, and tests.
- Copilot must not invent HR, labor-law, contract, payroll, or regulatory business rules.
- Copilot must not invent Spain-specific legal interpretations.
- Copilot must not autonomously redesign the data model.
- When business meaning is unclear, leave a short explicit note in comments and ask for user confirmation in the chat instead of guessing.
- Prefer conservative, explicit code over magical abstractions.
- When proposing code, explain where it belongs in the hexagonal architecture.
- Reject shortcuts that mix layers.

## Output expectations

When generating code:
- state the architectural layer affected
- keep the change minimal and coherent
- mention impacted ports, adapters, domain objects, and migrations if any
- mention whether OpenAPI changes are required
- mention whether security implications exist