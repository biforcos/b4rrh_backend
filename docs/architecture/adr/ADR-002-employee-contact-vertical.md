# ADR-002 — Employee Contact Vertical

## Status
Accepted

## Context

The B4RRHH project models employee-related information as a set of vertical resources
inside the `employee` bounded context.

Each resource represents a distinct functional aspect of the employee domain
and follows the architectural rules defined in:

ADR-001 — Vertical architecture and API identity rules.

The `employee.contact` vertical represents the contact channels currently associated
with an employee.

Typical examples include:

- email
- phone
- mobile
- company mobile
- internal extension

These contact types are configurable through the metamodel
(`rulesystem.rule_entity`) using the entity type:

EMPLOYEE_CONTACT_TYPE

---

# Functional Definition

`employee.contact` represents **current contact channels of an employee**.

This resource is **not historized**.

It behaves as a **set of slots per contact type**.

Each employee may have **at most one contact per contact type**.

Example:

| employee | type | value |
|--------|------|------|
| EMP 0001 | EMAIL | john@corp.com |
| EMP 0001 | MOBILE | 600123123 |
| EMP 0001 | EXTENSION | 1234 |

Invalid:

| employee | type | value |
|--------|------|------|
| EMP 0001 | EMAIL | john@corp.com |
| EMP 0001 | EMAIL | john.personal@gmail.com |

---

# Structural Properties

| Property | Value |
|--------|------|
| historized | false |
| occurrence_type | MULTIPLE |
| simultaneous_occurrences | MULTIPLE |
| lifecycle_strategy | DELETE |
| delete_policy | PHYSICAL |

---

# Functional Identity

The functional identity of a contact is:

employee + contactTypeCode

Where employee identity is:

ruleSystemCode + employeeTypeCode + employeeNumber

Therefore the full functional identity is conceptually:

ruleSystemCode + employeeTypeCode + employeeNumber + contactTypeCode

The contact **is not identified by a technical ID**.

---

# Mutability Rules

| Field | Mutable |
|-----|------|
| contactTypeCode | ❌ No |
| contactValue | ✔ Yes |

Changing the contact type is not allowed.

If a different type is needed:

1. delete existing contact
2. create new contact

---

# Persistence Model

Typical persistence structure:

employee.contact

Columns:

| column | description |
|------|-------------|
| id | technical surrogate key |
| employee_id | FK to employee.employee |
| contact_type_code | contact type |
| contact_value | contact data |
| created_at | timestamp |
| updated_at | timestamp |

Database constraint:

unique(employee_id, contact_type_code)

The `id` column is **technical only**.

It must not define the public identity of the resource.

---

# Catalog Validation

`contact_type_code` must be validated against:

rulesystem.rule_entity

Using:

rule_entity_type_code = EMPLOYEE_CONTACT_TYPE

Validation must ensure:

- rule system matches employee rule system
- entity exists
- entity is active
- entity is within validity period

---

# REST API Identity

APIs must use **business keys only**.

Employee identity:

ruleSystemCode  
employeeTypeCode  
employeeNumber

Contact identity:

contactTypeCode

---

# REST Endpoints

POST   /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts  
GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts  
GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}  
PUT    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}  
DELETE /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}

Technical identifiers must not appear in the API.

---

# DTO Design

### CreateContactRequest

contactTypeCode  
contactValue

### UpdateContactRequest

contactValue

`contactTypeCode` must not be mutable.

---

# Error Conditions

Typical errors include:

- employee not found
- contact type not found
- contact type not valid for rule system
- contact already exists for employee
- contact not found
- invalid contact value

---

# Validation Rules

Examples:

### EMAIL

- must contain '@'
- reasonable length
- trimmed

### PHONE / MOBILE

- digits and allowed characters
- normalized format
- trimmed

### EXTENSION

- numeric
- short length

Validation should remain **lightweight** and not attempt to fully validate
international phone formats.

---

# Relationship With Other Verticals

`employee.contact` is independent from:

- `employee.presence`
- `employee.contract`
- `employee.address`

It represents **current communication channels**, not employment history.

---

# Migration Note

Initial implementations exposed technical IDs in API paths.

This ADR establishes the transition to business-key based APIs.

Existing endpoints may temporarily coexist during migration.

---

# Role in the Architecture

This vertical serves as the **reference implementation** for:

- vertical-first architecture
- hexagonal layering inside verticals
- API identity based on domain keys
- metamodel catalog validation

Future verticals in the `employee` bounded context should follow this pattern.