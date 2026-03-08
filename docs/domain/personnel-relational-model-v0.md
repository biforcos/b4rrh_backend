# B4RRHH – Personnel Relational Model (v0)

This document describes the initial relational model for the Personnel Administration module.

The goal of this model is to provide a clean foundation for:

- Employee lifecycle
- Rule-system driven parameterization
- Temporal HR data

The model intentionally avoids premature complexity and focuses on a minimal but coherent structure.

---

## Core Principles

The design follows these principles:

- Business identity is defined by **business codes**
- Technical IDs are used only for persistence
- Rule system context is inherited from the employee
- Most HR data is **temporal**
- Database constraints enforce structural integrity
- Business semantics are handled by application logic

---

## Rule System

Represents a regulatory or organizational rule context.

Examples:

- ESP (Spain)
- FRA (France)

Each employee belongs to exactly one rule system.

### Table: `rule_system`

Key fields:

- `code`
- `name`
- `country_code`

**Business identity:** `rule_system.code`

---

## Rule Entity Type

Defines types of parameterized entities.

Examples:

- COMPANY
- ENTRY_REASON
- EXIT_REASON
- CONTRACT_TYPE
- COST_CENTER

### Table: `rule_entity_type`

**Business identity:** `rule_entity_type.code`

---

## Rule Entity

Represents parameterized entities belonging to a rule system.

Examples:

- ESP + COMPANY + 009
- ESP + CONTRACT_TYPE + IND

### Table: `rule_entity`

**Logical identity:**

- `rule_system_code`
- `rule_entity_type_code`
- `code`

Example: `ESP-COMPANY-009`

Entities are not versioned through multiple rows. Instead, they use:

- `valid_from`
- `valid_to`

to represent lifecycle.

---

## Employee

Represents a person in the system.

### Table: `employee`

**Business identity:** `rule_system_code + employee_number`

Examples:

- ESP + 00001
- FRA + 00001

Employee stores stable identity information such as:

- First name
- Surnames
- Preferred name

Employee does not store temporal employment information.

---

## Employee Company Presence

Represents periods where an employee is linked to a company.

### Table: `employee_company_presence`

Fields include:

- `company_code`
- `entry_reason_code`
- `exit_reason_code`
- `valid_from`
- `valid_to`

Presence number is a sequential identifier per employee.

**Uniqueness rules:**

- `employee_id + presence_number`
- `employee_id + valid_from`

Temporal overlap validation is handled by application logic.

---

## Employment Contract

Represents the employee contract during a given period.

### Table: `employment_contract`

Important fields:

- `company_code`
- `contract_type_code`
- `valid_from`
- `valid_to`

**Uniqueness rule:** `employee_id + valid_from`

Contracts may optionally reference a presence.

---

## Temporal Convention

The system follows the rule:

- `valid_from` inclusive
- `valid_to` exclusive

Meaning:

- `valid_from <= date < valid_to`

If `valid_to` is `NULL`, the record is considered open-ended.

---

## Future Extensions

Planned future extensions include:

- Work center assignments
- Cost center assignments
- Collective agreement
- Job category
- HR dictionary system
