# ADR-004 — Employee Business Key Strategy

## Status
Accepted

## Context

B4RRHH models the employee domain using functional business identity instead of
technical persistence identity as the primary public reference.

The project has already adopted these rules:

- APIs must use business keys, never technical IDs
- code is organized by vertical first
- employee-related resources live inside the `employee` bounded context
- child resources must inherit employee identity through the employee business key

The employee identity has evolved from:

    ruleSystemCode + employeeNumber

to:

    ruleSystemCode + employeeTypeCode + employeeNumber

This ADR formalizes that decision and its consequences.

---

# 1. Decision

The canonical functional identity of an employee in B4RRHH is:

    ruleSystemCode + employeeTypeCode + employeeNumber

This is the official employee business key for:

- public APIs
- domain logic
- lookups across verticals
- future integrations
- functional references between bounded contexts

Technical database IDs may still exist internally, but they are not part of the
public identity model.

---

# 2. Rationale

## 2.1. Avoid ambiguity

The same employee number may need to exist for different employee types inside
the same rule system.

Examples:

- ESP + EMP + 0001
- ESP + EXT + 0001
- ESP + JUB + 0001

If `employeeTypeCode` is omitted, these become ambiguous.

## 2.2. Preserve business meaning

The identity must reflect how the organization distinguishes employee populations.

`employeeTypeCode` is not decorative metadata. It is part of the business identity.

## 2.3. Support future scalability

This identity model scales better to:

- internal employees
- external collaborators
- retirees
- temporary populations
- country-specific employee classes

---

# 3. Scope

This strategy applies to all resources that reference an employee functionally.

That includes at least:

- `employee.employee`
- `employee.presence`
- `employee.contact`
- future employee verticals such as:
  - address
  - contract
  - assignment
  - compensation
  - document
  - absence

Whenever an API needs to identify an employee, it must use the 3-part business key.

---

# 4. API Rule

Public APIs must identify an employee using:

- `ruleSystemCode`
- `employeeTypeCode`
- `employeeNumber`

Examples:

    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}
    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts
    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/presences

The following are not valid as canonical public identity:

- `employeeId`
- technical UUIDs
- 2-part employee key without employeeTypeCode

---

# 5. Persistence Rule

The persistence layer may still use a technical surrogate key such as:

- `employee.id`

This is allowed for:

- foreign keys
- joins
- performance
- indexing
- adapter internals

However, the database must also enforce the functional uniqueness of the employee
through a unique constraint based on:

    (rule_system_code, employee_type_code, employee_number)

---

# 6. Child Resource Rule

Every employee child resource must conceptually inherit employee identity through
the employee business key.

Examples:

## 6.1. Contact

Functional identity:

    employee + contactTypeCode

Expanded:

    ruleSystemCode + employeeTypeCode + employeeNumber + contactTypeCode

## 6.2. Presence

Functional identity:

    employee + presenceNumber

Expanded:

    ruleSystemCode + employeeTypeCode + employeeNumber + presenceNumber

## 6.3. Future verticals

    employee + addressNumber
    employee + contractNumber
    employee + assignmentNumber
    employee + absenceNumber

This prevents mixed semantics such as:

- parent by business key
- child by technical ID

That pattern is explicitly rejected.

---

# 7. Integration Rule

Whenever another bounded context or external integration references an employee,
the preferred functional reference must be the 3-part business key.

If internal systems need technical IDs, those may exist as local persistence
concerns, but they must not replace the canonical business identity model.

---

# 8. Migration Guidance

When migrating legacy resources:

1. add `employeeTypeCode` to domain model
2. update unique constraints
3. update repository business-key lookups
4. update controllers and OpenAPI paths
5. update child vertical lookup adapters
6. update tests
7. remove or deprecate 2-part key endpoints

This is the expected migration path for verticals such as `presence`.

---

# 9. Design Rules for Copilot

When Copilot implements or refactors employee-related code, it must follow these rules:

1. Treat `ruleSystemCode + employeeTypeCode + employeeNumber` as the employee identity.
2. Do not design public APIs around `employeeId`.
3. Do not implement new employee child resources using only the old 2-part key.
4. Ensure child resources derive their identity from the employee business key.
5. Keep technical IDs inside persistence/adapters only.
6. Update OpenAPI, tests and migrations consistently when the employee key is involved.

---

# 10. Consequences

From this ADR onwards:

- the employee 3-part key is the canonical identity model
- any remaining 2-part employee-key APIs are transitional debt
- any employee API using technical IDs is non-canonical
- all new employee verticals must be designed around the 3-part key

This ADR must be used together with:

- ADR-001 — vertical architecture and API identity rules
- ADR-002 — employee.contact vertical
- ADR-003 — rule entity metamodel strategy
