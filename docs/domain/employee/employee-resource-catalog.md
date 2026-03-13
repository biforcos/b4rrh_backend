# Employee Resource Catalog (Updated)

This document defines the canonical resources of the `employee` bounded context.

The catalog acts as the **source of truth** for:

- data model
- business keys
- resource semantics
- vertical boundaries

---

# 1. employee.employee

Root resource of the employee domain.

Represents the identity of a person inside a rule system.

## Structural Properties

- historized = false
- occurrence_type = SINGLE
- lifecycle_strategy = UPDATE
- delete_policy = FORBIDDEN

## Fields

- rule_system_code
- employee_type_code
- employee_number
- first_name
- last_name_1
- last_name_2
- preferred_name
- status
- created_at
- updated_at

## Business Key

(rule_system_code, employee_type_code, employee_number)

---

# 2. employee.presence

Represents employment periods.

## Structural Properties

- historized = true
- occurrence_type = MULTIPLE
- simultaneous_occurrences = SINGLE_ACTIVE
- lifecycle_strategy = CLOSE
- delete_policy = FORBIDDEN

## Fields

- presence_number
- company_code
- entry_reason_code
- exit_reason_code
- start_date
- end_date

## Business Key

(employee_id, start_date)

---

# 3. employee.contact

Represents contact channels associated with an employee.

Examples:

- email
- phone
- mobile
- company mobile
- extension

## Structural Properties

- historized = false
- occurrence_type = MULTIPLE
- simultaneous_occurrences = MULTIPLE
- lifecycle_strategy = DELETE
- delete_policy = PHYSICAL

## Fields

- contact_type_code
- contact_value

## Business Key

(employee_id, contact_type_code)

## Rules

- Only one contact per type per employee
- contact_type_code must exist in catalog
- contact_value required
- contact_type_code immutable