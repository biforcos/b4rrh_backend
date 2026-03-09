# B4RRHH - Resource Derivation Rules

## Purpose

This document defines how backend artifacts are derived from the domain metamodel.

The goal is to ensure that every new resource is designed consistently across:

- Database
- Domain
- Application
- Persistence
- REST API
- Validation rules

This document must be used together with:

- `docs/architecture/domain-metamodel.md`
- `docs/domain/employee-resource-catalog.md`

---

# Core Principle

Development does not start from endpoints or tables.

Development starts from a **resource definition**.

For each resource, the following structural properties must be known first:

- `object`
- `resource`
- `parent_resource`
- `historized`
- `occurrence_type`
- `simultaneous_occurrences`
- `mandatory`
- `lifecycle_strategy`
- `business_key_fields`
- `delete_policy`

From those properties, the rest of the implementation is derived.

---

# Artifacts Derived from a Resource

Every resource may generate the following artifacts:

## Documentation
- Resource catalog entry
- Field definitions
- Invariants and notes

## Database
- Schema
- Table
- Primary key
- Business key
- Temporal fields if historized
- Constraints

## Domain
- Domain model class
- Value objects if needed
- Domain services if needed

## Application
- Use cases
- Commands
- Queries
- Validation services

## Persistence
- JPA entity
- Spring Data repository
- Persistence adapter
- Mappers

## API
- Request DTOs
- Response DTOs
- Controller endpoints
- Error mappings

---

# Resource Families

Resources are classified by structural behavior.

---

## 1. Single Non-Historized

### Definition

```text
historized = false
occurrence_type = SINGLE
```

**Typical Examples:**
- `employee.employee`

**Typical Operations:**
- Create
- Read
- Update
- Delete (if explicitly defined by `delete_policy`)

**Typical Database Shape:**
- One row per parent object
- No `start_date`
- No `end_date`

**Typical Backend Artifacts:**
- Create use case
- Get use case
- Update use case
- One controller resource
- Repository port
- Persistence adapter

---

## 2. Multiple Non-Historized

### Definition

```text
historized = false
occurrence_type = MULTIPLE
```

**Typical Examples:**
- `employee.contact_method`
- `employee.identity_document`

**Typical Operations:**
- Add
- List
- Get
- Update
- Delete (if allowed by `delete_policy`)

**Typical Database Shape:**
- Multiple rows per parent object
- No temporal fields

**Typical Backend Artifacts:**
- Add use case
- List use case
- Get use case
- Update use case
- Delete use case (if allowed)

---

## 3. Historized Single-Active

### Definition

```text
historized = true
occurrence_type = MULTIPLE
simultaneous_occurrences = SINGLE_ACTIVE
```

**Typical Examples:**
- `employee.presence`
- `employee.contract`
- `employee.work_center_assignment`
- `employee.tax_information`

**Mandatory Temporal Fields:**
- `start_date`
- `end_date`

**Typical Operations:**
- Add occurrence
- List occurrences
- Get occurrence
- Close occurrence

**Optional Operations:**
- Correct occurrence
- Update occurrence (if allowed by business rules)

**Delete:**
- Delete is normally restricted. Prefer `close` over physical deletion.

**Typical Database Shape:**
- Multiple rows per parent object
- Business key often includes parent ID and `start_date`

**Typical Validations:**
- No overlapping active periods in the defined scope
- `start_date < end_date` when `end_date` is not null
- Uniqueness of business key
- Only one active occurrence at a time in scope

---

## 4. Historized Multi-Active

### Definition

```text
historized = true
occurrence_type = MULTIPLE
simultaneous_occurrences = MULTIPLE_ACTIVE
```

**Typical Examples:**
- `employee.cost_center_assignment`
- `employee.address`
- `employee.bank_account`

**Mandatory Temporal Fields:**
- `start_date`
- `end_date`

**Typical Operations:**
- Add occurrence
- List occurrences
- Get occurrence
- Update or correct occurrence
- Close occurrence

**Delete:**
- Delete depends on `delete_policy`, but physical deletion should remain exceptional.

**Typical Validations:**
- Custom set-level validations may apply
- Overlaps may be allowed
- Additional rules may exist, such as:
  - Percentage totals
  - Uniqueness by code and period
  - Mandatory combination rules

---

# Delete Policy

Delete behavior is not derived automatically from the resource family. Every resource must define an explicit `delete_policy`.

**Allowed Values:**
- `FORBIDDEN`: Physical delete is not allowed.
- `LOGICAL`: Delete is represented by a logical state change.
- `PHYSICAL`: Physical delete is allowed.
- `CASCADE_ROOT_ONLY`: The resource may only be physically deleted as part of deleting the full root object.

**Recommended Delete Policies:**

| Resource                     | Recommended Delete Policy       |
|------------------------------|----------------------------------|
| `employee.employee`          | `FORBIDDEN` or `CASCADE_ROOT_ONLY` |
| `employee.presence`          | `FORBIDDEN`                     |
| `employee.contract`          | `FORBIDDEN`                     |
| `employee.contact_method`    | `PHYSICAL`                      |
| `employee.identity_document` | `PHYSICAL` or `LOGICAL`         |
| `employee.cost_center_assignment` | `FORBIDDEN` or `LOGICAL`     |

---

# API Derivation Rules

REST endpoints are derived from the resource family and parent object.

**Examples:**

### Single Non-Historized
- `POST /employees`
- `GET /employees/{employeeId}`
- `PATCH /employees/{employeeId}`

### Multiple Non-Historized
- `POST /employees/{employeeId}/contact-methods`
- `GET /employees/{employeeId}/contact-methods`
- `GET /employees/{employeeId}/contact-methods/{contactMethodId}`
- `PATCH /employees/{employeeId}/contact-methods/{contactMethodId}`
- `DELETE /employees/{employeeId}/contact-methods/{contactMethodId}` (if allowed)

### Historized Single-Active
- `POST /employees/{employeeId}/presences`
- `GET /employees/{employeeId}/presences`
- `GET /employees/{employeeId}/presences/{presenceId}`
- `POST /employees/{employeeId}/presences/{presenceId}/close`

### Historized Multi-Active
- `POST /employees/{employeeId}/cost-center-assignments`
- `GET /employees/{employeeId}/cost-center-assignments`
- `GET /employees/{employeeId}/cost-center-assignments/{assignmentId}`
- `PATCH /employees/{employeeId}/cost-center-assignments/{assignmentId}`
- `POST /employees/{employeeId}/cost-center-assignments/{assignmentId}/close`

---

# Persistence Derivation Rules

For every resource:

### Domain
- Create one domain model class

### Port
- Create one repository port if persistence is needed

### Persistence
- Create one JPA entity
- Create one Spring Data repository
- Create one persistence adapter

### Application
- Create use cases according to the resource family.

---

# Temporal Derivation Rules

For every historized resource:

- `start_date` is mandatory
- `end_date` is mandatory as a field but nullable as a value
- `end_date = null` means open-ended
- `start_date` and `end_date` must follow common project conventions
- Overlap rules are derived from `simultaneous_occurrences`

---

# Root Object Deletion

Deleting a root object such as `employee` must never be assumed as a simple row deletion.

Deleting a root object may require:

- Validation of business rules
- Cascading behavior across resources
- Controlled deletion order
- Retention and audit decisions

Because of this, root object deletion must be treated as an explicit design decision, not as a default CRUD operation.

---

# Final Principle

A resource does not define only a table.

A resource defines:

- Lifecycle
- API behavior
- Persistence shape
- Validation model
- Operational semantics

Implementation must always follow the resource definition first.
