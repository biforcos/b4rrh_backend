# CLAUDE.md — B4RRHH Backend

This file provides guidance for Claude (and other AI assistants) working on this repository.
For the authoritative rules also followed by GitHub Copilot, see `.github/copilot-instructions.md`
and the files under `.github/instructions/`. This document focuses on navigation, commands, and
workflow; those files own the definitive rule sets.

---

## Project Overview

**B4RRHH** is a backend for an HR application scoped to Personnel Administration in Spain.
It is **not** a generic CRUD service — it models historical, temporal HR data with strict
domain integrity.

| Property | Value |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Build | Maven |
| Database | PostgreSQL 16 |
| Schema migrations | Flyway |
| Persistence | JPA/Hibernate (infrastructure layer only) |
| Security | Spring Security |
| API contract | OpenAPI 3.0.3 (contract-first) |
| Test DB | H2 in-memory |

**In scope**: employees, personal data, employment contracts (Spain-oriented), work center
assignment, collective agreement categories, collective agreements.

**Out of scope**: payroll, legal reporting, time & attendance, absence management, any
payroll-specific logic.

---

## Quick Start

### Prerequisites

- Java 21
- Maven 3.6+
- Docker (for the database)

### Start the database

```bash
docker compose -f docker/postgres/docker-compose.yaml up -d
```

Database: `b4rrhh` | User: `b4rrhh` | Password: `b4rrhh` | Port: `5432`

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
# Application starts on http://localhost:8080
```

### Test

```bash
# All tests (uses H2 in-memory — no external DB needed)
mvn test

# Single test class
mvn test -Dtest=CreatePresenceServiceTest

# With coverage report
mvn clean test jacoco:report
```

---

## Key File Locations

| Purpose | Path |
|---|---|
| Entry point | `src/main/java/com/b4rrhh/B4rrhhBackendApplication.java` |
| Spring config | `src/main/resources/application.yml` |
| OpenAPI contract | `openapi/personnel-administration-api.yaml` |
| Flyway migrations | `src/main/resources/db/migration/V*.sql` |
| Security config | `src/main/java/com/b4rrhh/shared/infrastructure/config/SecurityConfig.java` |
| Docker Compose | `docker/postgres/docker-compose.yaml` |
| Architecture ADRs | `docs/architecture/adr/` |
| Domain docs | `docs/domain/` |
| Naming conventions | `docs/conventions/` |
| Copilot rules | `.github/copilot-instructions.md` |

---

## Project Structure

```
b4rrh_backend/
├── openapi/                        # OpenAPI contract (source of truth for HTTP API)
├── docker/postgres/                # PostgreSQL Docker Compose
├── docs/
│   ├── architecture/adr/           # 17 Architecture Decision Records (ADR-001..017)
│   ├── domain/                     # Domain concepts and models
│   ├── conventions/                # Naming and implementation guides
│   └── metamodel/                  # Rule system metamodel
└── src/
    ├── main/
    │   ├── java/com/b4rrhh/
    │   │   ├── employee/           # Employee bounded context
    │   │   │   ├── address/
    │   │   │   ├── contact/
    │   │   │   ├── contract/
    │   │   │   ├── cost_center/
    │   │   │   ├── employee/
    │   │   │   ├── identifier/
    │   │   │   ├── journey/
    │   │   │   ├── labor_classification/
    │   │   │   ├── lifecycle/
    │   │   │   ├── presence/
    │   │   │   ├── shared/
    │   │   │   ├── temporal/
    │   │   │   └── workcenter/
    │   │   ├── rulesystem/         # Rule system bounded context
    │   │   │   ├── catalogbinding/
    │   │   │   └── catalogoption/
    │   │   └── shared/             # Cross-cutting concerns
    │   │       └── infrastructure/config/
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/       # V1..V32 Flyway SQL migrations
    └── test/
        └── java/com/b4rrhh/        # Mirrors the main source tree
```

### Package layout within each vertical

Every vertical (e.g., `employee.presence`) follows this pattern:

```
[vertical]/
├── domain/
│   ├── model/          # Framework-agnostic domain entities and value objects
│   ├── port/           # Output port interfaces (e.g., PresenceRepository)
│   └── exception/      # Domain-specific exceptions
├── application/
│   ├── usecase/        # One interface + one service class per business action
│   ├── command/        # Immutable command/query objects
│   └── port/           # Input port interfaces
└── infrastructure/
    ├── persistence/    # JPA entities, Spring Data repos, adapter implementations
    └── web/            # REST controllers, DTOs, assemblers
```

---

## Architecture

This project uses **hexagonal (ports and adapters) architecture**, organized
**by business vertical first**, then by layer.

### Layer responsibilities

| Layer | What belongs here |
|---|---|
| **domain** | Entities, value objects, domain services, domain rules, output port interfaces. No Spring, no JPA. |
| **application** | Use cases (one per business action), orchestration services, input/output ports, commands. |
| **infrastructure** | REST controllers (input adapters), JPA entities, Spring Data repos, persistence adapters (output adapters), assemblers, Spring config. |

### Forbidden patterns

- Controller calling a repository directly
- Business logic in a controller or JPA entity
- Spring annotations (`@Entity`, `@Component`, etc.) in the domain layer
- Domain entities returned from REST endpoints
- JPA entities exposed outside the infrastructure layer
- "service utils" catch-all classes

### URL identity pattern

Employee-scoped endpoints use the **business key** in the path, not the internal DB id:

```
/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/[resource]
```

Example: `POST /employees/ESP/INTERNAL/00042/presences`

---

## Development Workflow

Always follow this order when adding or changing a feature:

1. **OpenAPI first** — Update `openapi/personnel-administration-api.yaml` before writing any code.
2. **Domain model** — Add or update framework-agnostic domain entities and ports.
3. **Domain tests** — Write unit tests covering domain invariants.
4. **Flyway migration** — Add a new `V{n}__description.sql` if the schema changes. Never edit existing migrations.
5. **Application service / use case** — Implement the business workflow.
6. **Infrastructure adapters** — Controller, JPA entity, persistence adapter, assembler.
7. **Integration tests** — Cover the adapter and controller behavior.

---

## Testing Conventions

| Test type | Scope | Tools |
|---|---|---|
| Unit | domain, application | JUnit 5, Mockito (`@ExtendWith(MockitoExtension.class)`) |
| Integration | infrastructure adapters, controllers | `@SpringBootTest`, `@WebMvcTest`, H2 |
| Domain model | invariants on domain objects | JUnit 5 (no mocks needed) |

**Naming**: `*ServiceTest`, `*UseCaseTest` for unit tests; `*IntegrationTest`, `*HttpTest` for integration tests.

**Policy** (from `.github/copilot-instructions.md`):
- Every non-trivial piece of code must have a test.
- Never remove tests to make the build pass.
- If a test cannot be written yet, explain why explicitly.

---

## Code Conventions

- **Constructor injection** — always; never `@Autowired` on fields.
- **Immutability** — domain entities use `final` fields; commands/queries are immutable value objects.
- **Naming** — English throughout; PascalCase for classes, camelCase for methods, snake_case for DB columns/tables.
- **One use case per business action** — `CreatePresenceUseCase` / `CreatePresenceService`, not a generic `PresenceService`.
- **Assemblers at boundaries** — explicit mapper/assembler classes convert domain objects to DTOs at the controller boundary.
- **Small, intention-revealing methods** — no placeholder code, no silent assumptions.

---

## Data Modeling Notes

- **Employee identity**: technical surrogate `id` (internal, DB-generated) + business `employeeNumber` (string) + `ruleSystemCode` + `employeeTypeCode`.
- **Historical data**: most HR facts use `startDate`/`endDate` or `validFrom`/`validTo`. Business history is never deleted.
- **Temporal modeling**: two entities for the same period of time should not overlap — domain services enforce this.
- **Rule system**: companies, work centers, cost centers, and other parameterized entities live in the `rulesystem` bounded context.
- **Schema**: two PostgreSQL schemas — `employee` and `rulesystem`.

---

## AI Assistant Guardrails

The following rules apply to Claude (and mirror the rules in `.github/copilot-instructions.md`
for Copilot). Read that file for the full authoritative list.

### Claude may help with

- Scaffolding new verticals following the existing package structure
- Refactoring within a layer without crossing architectural boundaries
- Generating tests for existing or new code
- Writing Flyway migrations **after** the schema change has been explicitly approved

### Claude must NOT do without explicit user confirmation

- Invent HR business rules, labor law interpretations, or Spain-specific legal logic
- Redesign or extend the data model (no new tables, columns, constraints, or indexes)
- Change the OpenAPI contract unilaterally
- Mix architectural layers (e.g., add business logic to a controller)
- Generate placeholder implementations or silent assumptions
- Autonomously introduce payroll, legal reporting, or out-of-scope features

### When in doubt

Leave an explicit comment in code (e.g., `// TODO: confirm business rule with domain owner`)
and state the uncertainty in the chat. Do not guess and do not proceed silently.

### Output checklist

When proposing or generating code, state:
1. Which architectural layer is affected
2. Which ports, adapters, domain objects, or migrations are impacted
3. Whether an OpenAPI change is required
4. Whether security implications exist (roles, authorization)

---

## Architecture Decision Records

17 ADRs are documented under `docs/architecture/adr/`. Before making structural decisions,
check whether an existing ADR already addresses the topic. Key ones:

| ADR | Topic |
|---|---|
| ADR-001 | Vertical architecture and API identity |
| ADR-003 | Rule entity metamodel strategy |
| ADR-004 | Employee business key strategy |
| ADR-007 | Employee lifecycle workflows |
| ADR-008 | Strong timeline replace pattern |
| ADR-011 | Shared lookup decision matrix |
| ADR-017 | Cost center as distributed timeline vertical |

---

## Current Project Status (as of early 2026)

- Architecture foundation and ADRs: established
- Flyway schema: V1–V32 applied
- Verticals implemented (partially or fully): presence, contact, address, contract, cost_center, identifier, workcenter, labor_classification, lifecycle
- OpenAPI contract: in active evolution
- Security: permissive dev config; role-based rules planned (`ADMIN`, `HR_MANAGER`, `HR_VIEWER`)
- CI/CD: no automated pipeline yet; builds and tests run locally via Maven
