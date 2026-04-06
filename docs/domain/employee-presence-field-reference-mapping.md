# Employee Presence Field Reference Mapping

This document defines the semantic reference mapping for the fields of `employee.presence`.

## Context source

The rule system context is taken from:

- `employee.employee.rule_system_code`

## Field mappings

### company_code
- reference entity type: `COMPANY`

### entry_reason_code
- reference entity type: `EMPLOYEE_PRESENCE_ENTRY_REASON`

### exit_reason_code
- reference entity type: `EMPLOYEE_PRESENCE_EXIT_REASON`

## Validation rule

For every reference field in `employee.presence`, the application must validate that:

- the referenced code exists in `rulesystem.rule_entity`
- the `rule_system_code` matches the employee rule system
- the `rule_entity_type_code` matches the field mapping