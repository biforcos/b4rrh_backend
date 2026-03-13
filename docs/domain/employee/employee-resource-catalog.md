# Employee Resource Catalog

This document defines the canonical resources of the `employee` bounded context.

The catalog acts as the **source of truth** for:

- data model
- business keys
- resource semantics
- vertical boundaries

The design order is:

1. resource definition
2. domain model
3. persistence model
4. application services
5. public API contract

---

# 1. `employee.employee`

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

## Functional Business Key

(rule_system_code, employee_type_code, employee_number)

## Persistence Identity

- technical primary key: `id`
- unique functional constraint:
  `(rule_system_code, employee_type_code, employee_number)`

---

# 2. `employee.presence`

Represents employment or presence periods of an employee.

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

## Functional Business Key

(employee, presence_number)

Expanded functional identity:

(rule_system_code, employee_type_code, employee_number, presence_number)

## Persistence Identity

- technical primary key: `id`
- unique internal ownership key:
  `(employee_id, presence_number)`

## Rules

- presence belongs to exactly one employee
- presence APIs must use employee business key + presence_number
- no technical presence id in public API
- start_date and end_date are period attributes, not public identity
- periods must not overlap according to domain rules
- close is a domain action, not generic delete/update semantics

---

# 3. `employee.contact`

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

## Functional Business Key

(employee, contact_type_code)

Expanded functional identity:

(rule_system_code, employee_type_code, employee_number, contact_type_code)

## Persistence Identity

- technical primary key: `id`
- unique internal ownership key:
  `(employee_id, contact_type_code)`

## Rules

- only one contact per type per employee
- `contact_type_code` must exist in catalog
- `contact_value` is required
- `contact_type_code` is immutable
- no technical contact id in public API

---

# 4. `employee.address`

Represents employee addresses.

## Structural Properties

- historized = true
- occurrence_type = MULTIPLE
- simultaneous_occurrences = MULTIPLE
- lifecycle_strategy = CLOSE
- delete_policy = FORBIDDEN

## Fields

- address_number
- address_type_code
- street
- city
- postal_code
- region_code
- country_code
- start_date
- end_date

## Functional Business Key

(employee, address_number)

---

# 5. `employee.contract`

Represents the contractual relationship between the employee and the employer.

## Structural Properties

- historized = true
- occurrence_type = MULTIPLE
- simultaneous_occurrences = SINGLE_ACTIVE
- lifecycle_strategy = CLOSE
- delete_policy = FORBIDDEN

## Fields

- contract_number
- contract_type_code
- working_time_code
- company_code
- start_date
- end_date

## Functional Business Key

(employee, contract_number)

---

# 6. `employee.assignment`

Represents the organizational assignment of the employee.

## Structural Properties

- historized = true
- occurrence_type = MULTIPLE
- simultaneous_occurrences = SINGLE_ACTIVE
- lifecycle_strategy = CLOSE
- delete_policy = FORBIDDEN

## Fields

- assignment_number
- department_code
- job_code
- manager_employee_number
- cost_center_code
- location_code
- start_date
- end_date

## Functional Business Key

(employee, assignment_number)

---

# 7. `employee.compensation`

Represents compensation conditions.

## Structural Properties

- historized = true
- occurrence_type = MULTIPLE
- simultaneous_occurrences = SINGLE_ACTIVE
- lifecycle_strategy = CLOSE
- delete_policy = FORBIDDEN

## Fields

- compensation_number
- salary_amount
- salary_type_code
- currency_code
- bonus_eligibility
- start_date
- end_date

## Functional Business Key

(employee, compensation_number)

---

# 8. `employee.work_schedule`

Represents working schedule conditions.

## Structural Properties

- historized = true
- occurrence_type = MULTIPLE
- simultaneous_occurrences = SINGLE_ACTIVE
- lifecycle_strategy = CLOSE
- delete_policy = FORBIDDEN

## Fields

- schedule_number
- schedule_type_code
- hours_per_week
- shift_code
- start_date
- end_date

## Functional Business Key

(employee, schedule_number)

---

# 9. `employee.document`

Represents employee documents.

## Structural Properties

- historized = true
- occurrence_type = MULTIPLE
- simultaneous_occurrences = MULTIPLE
- lifecycle_strategy = CLOSE
- delete_policy = FORBIDDEN

## Fields

- document_number
- document_type_code
- document_identifier
- issuing_country_code
- start_date
- end_date

## Functional Business Key

(employee, document_number)

---

# 10. `employee.absence`

Represents absences such as vacation, sickness or leave.

## Structural Properties

- historized = true
- occurrence_type = MULTIPLE
- simultaneous_occurrences = MULTIPLE
- lifecycle_strategy = CLOSE
- delete_policy = FORBIDDEN

## Fields

- absence_number
- absence_type_code
- approval_status_code
- start_date
- end_date

## Functional Business Key

(employee, absence_number)
