# B4RRHH – Data Dictionary Concept

This document describes the conceptual approach for the B4RRHH business dictionary.

The dictionary is not implemented in v0 but defines the architectural direction.

---

## Motivation

Many HR fields reference parameterized entities.

Examples:

- `company_code`
- `contract_type_code`
- `entry_reason_code`
- `cost_center_code`

These fields share common characteristics:

- They reference entities from the rule system
- They use business codes
- They have specific validation rules
- They often have fixed code lengths

Managing this information purely through database constraints becomes rigid and difficult to evolve.

Instead, B4RRHH introduces the concept of a **business dictionary**.

---

## Core Idea

Each business field referencing a rule entity is defined through dictionary metadata.

### Example 1: `employee_company_presence.company_code`

Dictionary definition:

- `entity_type = COMPANY`
- `max_length = 4`
- `uppercase = true`
- `nullable = false`

### Example 2: `employment_contract.contract_type_code`

Dictionary definition:

- `entity_type = CONTRACT_TYPE`
- `max_length = 5`
- `uppercase = true`
- `nullable = false`

---

## Benefits

The dictionary allows:

- Centralized validation rules
- Field semantics independent from database structure
- Flexible evolution of HR configuration
- Simpler database schema

---

## Relationship with Rule Entities

Dictionary entries reference rule entity types.

### Examples:

- `company_code` → `COMPANY`
- `entry_reason_code` → `EMPLOYEE_PRESENCE_ENTRY_REASON`
- `exit_reason_code` → `EMPLOYEE_PRESENCE_EXIT_REASON`
- `contract_type_code` → `CONTRACT_TYPE`

### Resolution Process:

1. `employee.rule_system_code`
2. `+ dictionary.entity_type`
3. `+ field_code`

This combination uniquely identifies the corresponding rule entity.

---

## Validation

Validation logic will eventually ensure:

- Code exists in `rule_entity`
- Entity type matches dictionary definition
- Rule system matches employee rule system
- Code length matches dictionary configuration

---

## Implementation Strategy

The dictionary will initially exist only conceptually and in documentation.

Later versions may introduce:

- A metadata table
- Application configuration
- Code-based definitions

The design deliberately keeps the database schema simple while allowing the application layer to evolve validation logic over time.