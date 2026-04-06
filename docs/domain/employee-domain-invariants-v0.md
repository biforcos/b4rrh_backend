# B4RRHH - Employee Domain Invariants v0

## Purpose

This document defines the initial business invariants for the employee domain in B4RRHH.

It is intentionally small and focused on the first personnel administration concepts:
- employee
- employee company presence
- employment contract

## Employee

- an employee belongs to exactly one rule system
- employee business identity is defined by:
  - rule_system_code
  - employee_number
- employee_number is unique only inside a rule system
- the same employee number may exist in different rule systems
- employee first name and surnames are stored directly in employee in v0
- preferred_name is optional

## Rule system references

- employee historical tables do not repeat rule_system_code
- the employee provides the rule system context
- business code fields such as company_code or contract_type_code are resolved using:
  - employee.rule_system_code
  - the semantic meaning of the field
- the semantic meaning of each code field is defined by the application dictionary

## Employee company presence

- employee company presence is a historical fact
- a presence belongs to exactly one employee
- presence_number is a functional sequential number per employee
- presence_number is unique per employee
- gaps in presence_number are acceptable in v0
- valid_from is mandatory
- valid_to is optional
- valid_to is exclusive by convention
- no two presence records for the same employee should overlap in time
- no two presence records for the same employee should start on the same valid_from date

## Employment contract

- employment contract is a historical fact
- a contract belongs to exactly one employee
- a contract may optionally reference a presence in v0
- company_code and contract_type_code are business codes resolved through the employee rule system
- valid_from is mandatory
- valid_to is optional
- valid_to is exclusive by convention
- contracts should be temporally consistent with a valid company presence
- exact non-overlap rules for contracts are still open for further refinement

## Temporal convention

For historical tables in v0:
- valid_from is inclusive
- valid_to is exclusive
- valid_to = null means open-ended

Queries and validations must respect this convention consistently.

## Out of scope for v0

The following are not yet enforced at database level in v0:
- semantic validation that company_code points to a COMPANY rule entity
- semantic validation that entry_reason_code points to an EMPLOYEE_PRESENCE_ENTRY_REASON rule entity
- semantic validation that exit_reason_code points to an EMPLOYEE_PRESENCE_EXIT_REASON rule entity
- semantic validation that contract_type_code points to a CONTRACT_TYPE rule entity
- temporal overlap prevention through PostgreSQL exclusion constraints
- the physical implementation of the business dictionary