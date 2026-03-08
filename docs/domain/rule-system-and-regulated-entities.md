# B4RRHH - Rule System and Regulated Entities

## Purpose

This document defines the initial concept of rule systems and regulated entities in B4RRHH.

The goal is to provide a scalable alternative to creating a separate master-data structure for every business concept such as work centers, cost centers, collective agreements, or categories.

## Core idea

A rule system is the macro configuration context in which employees, contracts, and regulated entities coexist.

A regulated entity is a parametrized business entity identified by:
- the rule system it belongs to
- its entity type
- its business code

Examples of regulated entities:
- work center
- cost center
- collective agreement
- collective agreement category
- legal entity
- organizational unit

This approach allows multiple business concepts to share a common structural model instead of requiring one completely separate master-data model per concept.

## Principles

- `employee` remains a separate root concept
- `employment_contract` remains a separate root concept
- regulated entities belong to a `rule_system`
- regulated entities are differentiated by `rule_entity_type`
- regulated entities may be assigned to employees or contracts
- relationships between regulated entities should be modeled explicitly
- avoid creating speculative generic attribute models in v0
- avoid executable rule engines in v0

## Core tables

### rule_system
Defines the macro configuration context.

Suggested fields:
- id
- code
- name
- country_code
- description
- active
- created_at
- updated_at

### rule_entity_type
Defines the category of regulated entity.

Examples:
- WORK_CENTER
- COST_CENTER
- COLLECTIVE_AGREEMENT
- AGREEMENT_CATEGORY
- LEGAL_ENTITY
- ORG_UNIT

Suggested fields:
- id
- code
- name
- description
- active

### rule_entity
Represents a concrete regulated entity inside a rule system.

Suggested fields:
- id
- rule_system_id
- rule_entity_type_id
- code
- name
- description
- active
- valid_from
- valid_to
- created_at
- updated_at

Business identity concept:
- rule_system
- entity type
- business code

### rule_entity_relation
Represents explicit relationships between regulated entities.

Suggested fields:
- id
- rule_system_id
- parent_rule_entity_id
- child_rule_entity_id
- relation_type
- valid_from
- valid_to
- created_at
- updated_at

Example relation types:
- BELONGS_TO
- PART_OF
- ASSOCIATED_WITH
- DEFAULT_FOR

## Employee and contract integration

### employee
Employees belong to a rule system.

Suggested relevant fields:
- id
- employee_number
- rule_system_id
- status
- created_at
- updated_at

### employment_contract
Contracts remain independent domain entities and may carry assignments to regulated entities.

### contract_rule_entity_assignment
Associates a contract with regulated entities in explicit roles.

Suggested fields:
- id
- employment_contract_id
- rule_entity_id
- assignment_role
- is_primary
- valid_from
- valid_to
- created_at
- updated_at

Possible assignment roles:
- WORK_CENTER
- COST_CENTER
- COLLECTIVE_AGREEMENT
- AGREEMENT_CATEGORY

## What is intentionally out of scope in v0

- executable rule engines
- generic EAV attribute systems
- dynamic rule expressions
- speculative hierarchy frameworks without business use
- payroll logic

## Benefits

- reduces explosion of master tables
- provides a common structure for regulated business entities
- supports temporal modeling
- supports future growth
- keeps employee and contract as clear domain anchors

## Risks

- too much abstraction may reduce domain clarity
- validation rules must be explicit
- naming and documentation discipline are essential

## Initial recommendation

For v0, implement:
- rule_system
- rule_entity_type
- rule_entity
- rule_entity_relation
- employee
- employee_personal_data
- employment_contract
- contract_rule_entity_assignment

Do not implement generic attribute tables unless a real need appears.