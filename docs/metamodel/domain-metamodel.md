# Domain Metamodel

## Purpose

This document defines the domain metamodel used in B4RRHH.

The metamodel describes how domain information is structured independently of any specific implementation such as:

- Database schema
- API endpoints
- Service classes
- Persistence mechanisms

Instead of designing features directly as tables or endpoints, the system is designed around domain objects and resources with defined structural behavior.

From this model we derive:

- Database structure
- REST resources
- Validation rules
- Lifecycle operations
- Temporal behavior

This approach ensures that the system remains consistent, predictable, and extensible as the domain grows.

## Core Concepts

The domain is described using three structural layers:

- **Domain Object**
  - **Resource**
    - **Field**

Each layer has a specific responsibility.

### 1. Domain Object

A Domain Object represents a high-level concept in the domain.

**Examples:**

- `employee`
- `rule_system`
- `rule_entity`

Domain objects represent business concepts, not database tables. A domain object may contain multiple resources.

**Example:**

```
employee
 ├── employee
 ├── presence
 ├── contract
 ├── cost_center_assignment
 └── contact_method
```

Each resource represents a distinct structural aspect of the object.

### 2. Resource

A Resource is the structural representation of domain information. A resource usually corresponds to a database table and a REST collection. Resources belong to a domain object.

**Example resource identifiers:**

- `employee.employee`
- `employee.presence`
- `employee.contract`
- `employee.cost_center_assignment`

Naming follows the convention:

```
<object>.<resource>
```

#### Resource Persistence

In the database, resources are stored using:

- **Schema** = object
- **Table** = resource

**Example:**

- `employee.presence`
- `employee.contract`
- `employee.cost_center_assignment`

This provides:

- Clear domain grouping
- Predictable naming
- Natural alignment with the metamodel

#### Resource Structural Properties

Every resource must declare the following structural properties:

- **`object`**: The domain object the resource belongs to.
  - **Example:** `object = employee`
- **`resource`**: The resource name inside the object.
  - **Example:** `resource = presence`
- **`parent_resource` (optional)**: Some resources may depend on other resources instead of the root object.
  - **Example:** `employee.contract_clause` → `parent_resource = contract`
- **`historized`**: Indicates whether the resource represents information that changes over time.
  - **Values:** `true | false`
  - If `historized = true`, the resource must contain:
    - `start_date`
    - `end_date`

#### Temporal Convention

All historized resources must follow this rule:

```
start_date DATE NOT NULL
end_date   DATE NULL
```

**Meaning:**

```
start_date <= reference_date
AND (end_date IS NULL OR end_date >= reference_date)
```

This convention ensures:

- Consistent queries
- Reusable validation logic
- Predictable temporal behavior

- **`occurrence_type`**: Defines how many occurrences may exist for a resource.
  - **Values:** `SINGLE | MULTIPLE`
  - **Examples:**
    - `employee.employee` → `SINGLE`
    - `employee.presence` → `MULTIPLE`
- **`simultaneous_occurrences`**: Defines whether multiple active occurrences may exist at the same time.
  - **Values:** `SINGLE_ACTIVE | MULTIPLE_ACTIVE`
  - **Examples:**
    - `employee.presence` → `SINGLE_ACTIVE`
    - `employee.cost_center_assignment` → `MULTIPLE_ACTIVE`
- **`mandatory`**: Indicates whether the resource must exist for the domain object.
  - **Values:** `true | false`
  - **Examples:**
    - `employee.employee` → `true`
    - `employee.presence` → `false`
- **`lifecycle_strategy`**: Defines how records evolve over time.
  - **Values:** `UPDATE | CLOSE | DELETE | IMMUTABLE`
  - **Examples:**
    - `employee.employee` → `UPDATE`
    - `employee.presence` → `CLOSE`
    - `employee.contact_method` → `DELETE`
- **`business_key_fields`**: Defines the logical identity of a resource occurrence.
  - **Example:**
    - `employee.presence` → `business_key = [employee_id, start_date]`

### 3. Field

Fields define the attributes of a resource. Fields belong to a resource and are identified as:

```
<object>.<resource>.<field>
```

**Example:**

- `employee.presence.company_code`
- `employee.presence.start_date`
- `employee.presence.end_date`

#### Field Properties

Each field may declare the following metadata:

- **`type`**: Data type.
  - **Examples:** `string`, `integer`, `decimal`, `date`, `timestamp`, `boolean`
- **`length / precision`**: Defines storage constraints.
  - **Example:** `length = 30`, `precision = 10`, `scale = 2`
- **`required`**: Indicates whether the field is mandatory.
  - **Values:** `true | false`
- **`reference_entity_type`**: Indicates the master entity type used for validation.
  - **Examples:** `COMPANY`, `CONTRACT_TYPE`, `COST_CENTER`
- **`format_rules`**: Optional formatting rules.
  - **Examples:** `uppercase = true`, `trim = true`, `pattern = [A-Z0-9]+`
- **`temporal_role`**: Fields may participate in temporal semantics.
  - **Values:** `start_date | end_date`

### Naming Conventions

#### Resources

```
<object>.<resource>
```

**Examples:**

- `employee.employee`
- `employee.presence`
- `employee.contract`
- `employee.cost_center_assignment`

#### Tables

Tables follow the same structure:

- **Schema** = object
- **Table** = resource

**Example:**

- `employee.presence`
- `employee.contract`

#### Fields

Fields are always:

```
snake_case
```

**Examples:**

- `start_date`
- `end_date`
- `company_code`
- `contract_type_code`

### REST Derivation

REST endpoints are derived from the resource model.

**Example:**

- `employee.presence`

**Derives:**

- `POST   /employees/{employeeId}/presences`
- `GET    /employees/{employeeId}/presences`
- `GET    /employees/{employeeId}/presences/{presenceId}`
- `POST   /employees/{employeeId}/presences/{presenceId}/close`

The API design must remain consistent with the resource behavior type.

## Design Philosophy

The system is designed around domain structure first, not endpoints. Development follows this sequence:

1. Define domain object
2. Define resource
3. Define resource structural properties
4. Define fields
5. Derive persistence model
6. Derive API behavior
7. Implement application logic

This approach ensures that the domain model remains the source of truth for the system.

## Long-Term Vision

The metamodel may later evolve into a formal metadata layer that describes:

- Resources
- Fields
- Reference entities
- Validation rules

However, at this stage, it serves as a design contract for the architecture.

## Final Principle

B4RRHH is not designed as:

```
tables + endpoints
```

but as:

```
domain objects
    → resources
        → fields
```

From this structure, the rest of the system is derived.
