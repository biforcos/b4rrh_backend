# B4RRHH — Naming Guide for Bounded Contexts, Verticals and APIs

## Purpose

This document defines naming conventions for the B4RRHH project so that new
verticals, APIs, services and resources remain consistent.

---

# 1. Bounded Context Naming

Use singular bounded context names based on business domain.

Examples:

- `employee`
- `rulesystem`
- `shared`

Avoid vague technical names such as:

- `core`
- `common`
- `misc`
- `utils`

unless the meaning is truly transversal and unavoidable.

---

# 2. Vertical Naming

Inside a bounded context, verticals should be singular, short and business-oriented.

Examples inside `employee`:

- `employee`
- `presence`
- `contact`
- `address`
- `contract`
- `assignment`
- `compensation`
- `work_schedule`
- `document`
- `absence`

Prefer stable nouns.

Avoid names that describe implementation details, such as:

- `employeeData`
- `employeeManagement`
- `contactModule`
- `presenceCrud`

---

# 3. Package Naming

Pattern:

    com.b4rrhh.<bounded-context>.<vertical>.<layer>

Examples:

    com.b4rrhh.employee.contact.application
    com.b4rrhh.employee.contact.domain
    com.b4rrhh.employee.contact.infrastructure

Use lowercase package names.

For multi-word verticals, prefer snake_case only if the domain term is clearly
composed and readability is improved, such as:

    work_schedule

---

# 4. Use Case Naming

Use cases should be named by business action.

Good examples:

- `CreateContactUseCase`
- `UpdateContactUseCase`
- `DeleteContactUseCase`
- `ListEmployeeContactsUseCase`
- `GetEmployeeByBusinessKeyUseCase`
- `ResolveEmployeePresenceByBusinessKeyUseCase`

Avoid technical-id-centric names when business identity exists.

Avoid:

- `GetContactByIdUseCase`
- `DeleteEmployeeByIdUseCase`

unless the use case is strictly internal and not part of public domain semantics.

---

# 5. Service Naming

Service implementations should mirror the use case and remain business-oriented.

Examples:

- `CreateContactService`
- `GetContactByBusinessKeyService`
- `ClosePresenceService`

Avoid generic or vague names:

- `ContactManager`
- `EmployeeProcessor`
- `GeneralService`

---

# 6. Repository Naming

Repositories represent domain persistence contracts.

Pattern:

    <AggregateOrResource>Repository

Examples:

- `EmployeeRepository`
- `ContactRepository`
- `PresenceRepository`
- `RuleEntityRepository`

Avoid implementation detail in domain port names.

---

# 7. Controller Naming

Controllers should reflect the identity style of the public API.

Examples:

- `EmployeeBusinessKeyController`
- `ContactController`
- `PresenceBusinessKeyController`

If there are both canonical and legacy controllers, naming must make that visible.

Examples:

- `EmployeeBusinessKeyController`
- `LegacyEmployeeIdController`

Do not hide transitional semantics behind generic controller names.

---

# 8. DTO Naming

Use suffixes consistently:

- `Create...Request`
- `Update...Request`
- `...Response`
- `...ErrorResponse`

Examples:

- `CreateContactRequest`
- `UpdateContactRequest`
- `ContactResponse`
- `PresenceErrorResponse`

Avoid transport names that leak implementation detail.

---

# 9. Exception Naming

Domain exceptions should explain the business problem.

Examples:

- `ContactAlreadyExistsException`
- `ContactTypeMutationNotAllowedException`
- `PresenceOverlapException`
- `EmployeeAlreadyExistsException`

Avoid low-signal names such as:

- `InvalidStateException`
- `BusinessException`
- `GeneralValidationException`

unless part of a broader shared strategy with clear semantics.

---

# 10. API Path Naming

Use plural resource collections in paths.

Examples:

    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}
    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts
    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/presences

Use business codes in path variables:

- `ruleSystemCode`
- `employeeTypeCode`
- `employeeNumber`
- `contactTypeCode`
- `presenceNumber`

Avoid:

- `id`
- `employeeId`
- `contactId`
- `presenceId`

in canonical public APIs.

---

# 11. Catalog Naming

For metamodel-backed catalogs:

- entity type constants must be uppercase snake case
- codes should be domain-oriented and stable

Examples:

- `EMPLOYEE_CONTACT_TYPE`
- `EMPLOYEE_ENTRY_REASON`
- `EMPLOYEE_EXIT_REASON`

Catalog-backed fields in resources should use `...Code` suffix:

- `contactTypeCode`
- `entryReasonCode`
- `exitReasonCode`
- `employeeTypeCode`

---

# 12. Naming Principle Summary

When naming something in B4RRHH, prefer:

1. business meaning over technical detail
2. explicit semantics over brevity
3. functional identity over surrogate identity
4. stable nouns for resources
5. action verbs for use cases

If a name reinforces a wrong identity model, rename it.
