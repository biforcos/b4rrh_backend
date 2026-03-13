# ADR-003 — Rule Entity Metamodel Strategy

## Status
Accepted

## Context

B4RRHH uses a configurable catalog metamodel to avoid hardcoding many domain values
inside each vertical.

The current metamodel is based on these core resources:

- `rulesystem.rule_system`
- `rulesystem.rule_entity_type`
- `rulesystem.rule_entity`

This metamodel is already used by verticals such as `employee.presence`
and `employee.contact`, where domain codes are validated against catalog values
defined per rule system.

The project has also adopted two major architectural conventions:

1. code is organized by vertical first, with hexagonal layers inside each vertical
2. public APIs must use business keys and functional codes, never technical IDs

Those conventions are defined in ADR-001. `employee.contact` is defined as the
reference vertical in ADR-002. fileciteturn8file0 fileciteturn8file1

As the project grows, the metamodel becomes a foundational mechanism, so its
strategy must be made explicit to avoid ambiguity and accidental redesigns.

---

# 1. Decision

B4RRHH will keep a **catalog metamodel based on rule systems**, where catalog
values are represented through `rule_entity` records grouped by
`rule_entity_type` and scoped by `rule_system`.

The metamodel is not an incidental implementation detail. It is a **core domain
mechanism** used to make functional codes configurable by rule system.

---

# 2. Core Resources

## 2.1. `rulesystem.rule_system`

Represents a functional system of rules, usually associated with a country,
regulatory context or equivalent domain partition.

Examples:

- `ESP`
- `PRT`
- other future systems

## 2.2. `rulesystem.rule_entity_type`

Represents the category of configurable values.

Examples:

- `EMPLOYEE_CONTACT_TYPE`
- `EMPLOYEE_ENTRY_REASON`
- `EMPLOYEE_EXIT_REASON`
- future domain types

## 2.3. `rulesystem.rule_entity`

Represents an actual catalog value inside a rule system and type.

Examples:

- `EMPLOYEE_CONTACT_TYPE` + `ESP` + `EMAIL`
- `EMPLOYEE_CONTACT_TYPE` + `ESP` + `MOBILE`
- `EMPLOYEE_CONTACT_TYPE` + `PRT` + `EMAIL`

---

# 3. Main Modeling Rule

Catalog values are defined **per rule system**, even if the same code appears in
multiple rule systems.

This means that values like `EMAIL`, `PHONE`, `MOBILE`, etc. may be repeated for
different rule systems.

This duplication is intentional.

It reflects that:

- the business scope is the rule system
- semantics may diverge in the future
- validation must remain explicit and local to the rule system
- the project currently avoids introducing a higher-level hierarchy of
  universal/global/regional catalog families

---

# 4. What Is Explicitly Rejected

For now, B4RRHH does **not** introduce an additional metamodel layer such as:

- global entities
- common/shared entities across all rule systems
- country families
- inheritance between rule systems
- fallback from one rule system to another
- multi-level catalog resolution

Those ideas may appear later if truly needed, but they are explicitly out of
scope now.

The current strategy prefers **duplication with clarity** over abstraction with
ambiguity.

---

# 5. Validation Rule

Whenever a domain field represents a configurable code, it must be validated
against `rulesystem.rule_entity` using the full functional context:

- `ruleSystemCode`
- `ruleEntityTypeCode`
- `code`

Validation may additionally check:

- active flag
- validity period

That additional validation is acceptable as shared infrastructure policy, but it
must not distort the domain model of each vertical. This is already consistent
with the project guidance on catalog validation reuse. fileciteturn8file0

---

# 6. API Identity Rule for Metamodel Resources

The public API for metamodel resources must also follow the project-wide rule:
use functional business codes, never technical IDs. ADR-001 makes this mandatory
for the whole project. fileciteturn8file0

Therefore:

- `rule_system` is identified by `ruleSystemCode`
- `rule_entity_type` is identified by `ruleEntityTypeCode`
- `rule_entity` is identified functionally by:
  - `ruleSystemCode`
  - `ruleEntityTypeCode`
  - `code`

Technical database IDs may exist internally, but they must not drive API paths.

---

# 7. Search and Retrieval Semantics

For `rule_entity`, the preferred public API semantics are progressive filtering
by business codes, not technical-ID lookup.

Valid query styles include:

- list all entities for a rule system
- list all entities for a rule system and entity type
- get a specific entity by rule system + entity type + code

Typical examples:

- `GET /rule-entities?ruleSystemCode=ESP`
- `GET /rule-entities?ruleSystemCode=ESP&ruleEntityTypeCode=EMPLOYEE_CONTACT_TYPE`
- `GET /rule-entities?ruleSystemCode=ESP&ruleEntityTypeCode=EMPLOYEE_CONTACT_TYPE&code=EMAIL`

This is consistent with the already agreed rulesystem API direction in the
project context.

---

# 8. Seed Strategy

## 8.1. Current Accepted Strategy

When a new entity type such as `EMPLOYEE_CONTACT_TYPE` is introduced, initial
migration scripts may seed values for every existing `rule_system`.

This is valid and accepted.

## 8.2. Known Limitation

This approach only guarantees bootstrap for rule systems that already exist at
migration time.

It does not automatically solve what happens when a new `rule_system` is created
later.

## 8.3. Known Architectural Debt

A follow-up mechanism must eventually be chosen for new rule systems, for example:

- application service that bootstraps default entities when a rule system is created
- explicit operational script
- administrative endpoint
- other controlled bootstrap process

This debt must remain visible, but it does not invalidate the current model.

---

# 9. Ownership of Catalog Semantics

The metamodel is owned by the `rulesystem` bounded context.

Other bounded contexts such as `employee` consume the metamodel through business
codes and validation ports.

This means:

- `employee` does not redefine the semantics of catalog storage
- `rulesystem` remains the canonical owner of catalog configuration
- verticals such as `employee.contact` only declare which entity type they depend on

This is aligned with the updated employee resource catalog, where `employee.contact`
declares `contact_type_code` as catalog-backed and immutable. fileciteturn8file2

---

# 10. Naming Conventions

The following naming conventions are preferred:

- `ruleSystemCode`
- `ruleEntityTypeCode`
- `code`

Avoid introducing parallel alternative names for the same functional meaning
unless there is a very strong domain reason.

Within verticals, constants such as:

- `EMPLOYEE_CONTACT_TYPE`

should be defined once and reused.

---

# 11. Design Rules for Copilot

When implementing features that interact with the metamodel, Copilot must follow
these rules:

1. Treat `rule_system`, `rule_entity_type` and `rule_entity` as business resources.
2. Use business codes in APIs and use cases.
3. Do not expose technical IDs in public contracts.
4. Validate catalog-backed domain fields with:
   - rule system
   - entity type
   - code
5. Do not invent extra abstraction layers such as global entities or catalog inheritance.
6. Accept duplicated values across rule systems as valid and intentional.
7. Keep seed logic explicit and visible.
8. Do not move catalog semantics into random `shared` utility packages.
9. Keep the metamodel inside the `rulesystem` bounded context.
10. When a vertical uses a catalog code, document the associated `ruleEntityTypeCode`.

---

# 12. Consequences

From this ADR onwards:

- the rule entity metamodel is a first-class strategic mechanism of the project
- duplication of catalog values across rule systems is intentional
- APIs for metamodel resources must use functional business codes
- verticals must validate configurable codes through the metamodel
- future work may improve bootstrap for new rule systems, but without introducing
  hidden hierarchy levels prematurely

This ADR must be used together with:

- ADR-001 — vertical architecture and API identity rules
- ADR-002 — employee.contact vertical
