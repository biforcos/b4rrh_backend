# Employee Resource Catalog (v1)

This document defines the resources composing the employee domain object.

Resources are defined according to the metamodel described in:

[docs/architecture/domain-metamodel.md](../architecture/domain-metamodel.md)

Each resource specifies:

- Structural behavior
- Temporal behavior
- Lifecycle rules
- Deletion policy
- Key fields

The catalog serves as the source of truth for implementing:

- Database schema
- Backend services
- REST endpoints
- Validation logic

---

## Root Domain Object: `employee`

The `employee` object represents a person registered in a specific rule system.

The `employee` object is composed of multiple resources.

---

## Resource List

| Resource                     | Historized | Occurrence | Simultaneous       | Lifecycle | Delete Policy |
|------------------------------|------------|------------|--------------------|-----------|---------------|
| `employee.employee`          | No         | Single     | N/A                | Update    | Forbidden     |
| `employee.presence`          | Yes        | Multiple   | Single Active      | Close     | Forbidden     |
| `employee.contract`          | Yes        | Multiple   | Single Active      | Close     | Forbidden     |
| `employee.work_center_assignment` | Yes    | Multiple   | Single Active      | Close     | Forbidden     |
| `employee.cost_center_assignment` | Yes    | Multiple   | Multiple Active    | Close     | Forbidden     |
| `employee.contact_method`    | No         | Multiple   | Multiple           | Delete    | Physical      |
| `employee.address`           | Yes        | Multiple   | Multiple Active    | Close     | Forbidden     |
| `employee.bank_account`      | Yes        | Multiple   | Multiple Active    | Close     | Forbidden     |
| `employee.identity_document` | No         | Multiple   | Multiple           | Delete    | Physical      |
| `employee.tax_information`   | Yes        | Multiple   | Single Active      | Close     | Forbidden     |

---

### 1. `employee.employee`

Root resource of the `employee` object. Represents the basic identity of the employee inside a rule system.

**Structural Properties:**
- `historized = false`
- `occurrence_type = SINGLE`
- `mandatory = true`
- `lifecycle_strategy = UPDATE`
- `delete_policy = FORBIDDEN`

**Fields:**
- `rule_system_code`
- `employee_number`
- `first_name`
- `last_name_1`
- `last_name_2`
- `preferred_name`
- `status`
- `created_at`
- `updated_at`

**Business Key:**
- `(rule_system_code, employee_number)`

---

### 2. `employee.presence`

Represents the periods during which an employee is associated with a company.

**Typical Interpretation:**
- Employment entry/exit

**Structural Properties:**
- `historized = true`
- `occurrence_type = MULTIPLE`
- `simultaneous_occurrences = SINGLE_ACTIVE`
- `lifecycle_strategy = CLOSE`
- `delete_policy = FORBIDDEN`

**Fields:**
- `presence_number`
- `company_code`
- `entry_reason_code`
- `exit_reason_code`
- `start_date`
- `end_date`

**Business Key:**
- `(employee_id, start_date)`

**Rules:**
- An employee cannot have overlapping presences.
- Only one presence may be active at a time.

---

### 3. `employee.contract`

Represents employment contracts.

**Structural Properties:**
- `historized = true`
- `occurrence_type = MULTIPLE`
- `simultaneous_occurrences = SINGLE_ACTIVE`
- `lifecycle_strategy = CLOSE`
- `delete_policy = FORBIDDEN`

**Fields:**
- `contract_code`
- `company_code`
- `contract_type_code`
- `start_date`
- `end_date`

**Business Key:**
- `(employee_id, start_date)`

**Rules:**
- An employee cannot have overlapping active contracts.

---

### 4. `employee.work_center_assignment`

Defines the work center assigned to the employee.

**Structural Properties:**
- `historized = true`
- `occurrence_type = MULTIPLE`
- `simultaneous_occurrences = SINGLE_ACTIVE`
- `lifecycle_strategy = CLOSE`
- `delete_policy = FORBIDDEN`

**Fields:**
- `work_center_code`
- `start_date`
- `end_date`

**Business Key:**
- `(employee_id, start_date)`

---

### 5. `employee.cost_center_assignment`

Defines the cost center allocation of the employee. Multiple cost centers may be active simultaneously.

**Example:**
- 60% cost center A
- 40% cost center B

**Structural Properties:**
- `historized = true`
- `occurrence_type = MULTIPLE`
- `simultaneous_occurrences = MULTIPLE_ACTIVE`
- `lifecycle_strategy = CLOSE`
- `delete_policy = FORBIDDEN`

**Fields:**
- `cost_center_code`
- `percentage`
- `start_date`
- `end_date`

**Business Key:**
- `(employee_id, cost_center_code, start_date)`

**Rules:**
- `sum(percentage) = 100`

---

### 6. `employee.contact_method`

Stores contact information.

**Examples:**
- Phone
- Email

**Structural Properties:**
- `historized = false`
- `occurrence_type = MULTIPLE`
- `lifecycle_strategy = DELETE`
- `delete_policy = PHYSICAL`

**Fields:**
- `contact_type`
- `contact_value`

**Business Key:**
- `(employee_id, contact_type, contact_value)`

---

### 7. `employee.address`

Stores employee addresses.

**Examples:**
- Residence address
- Fiscal address

**Structural Properties:**
- `historized = true`
- `occurrence_type = MULTIPLE`
- `simultaneous_occurrences = MULTIPLE_ACTIVE`
- `lifecycle_strategy = CLOSE`
- `delete_policy = FORBIDDEN`

**Fields:**
- `address_type`
- `street`
- `city`
- `postal_code`
- `country_code`
- `start_date`
- `end_date`

**Business Key:**
- `(employee_id, address_type, start_date)`

---

### 8. `employee.bank_account`

Stores employee bank accounts.

**Structural Properties:**
- `historized = true`
- `occurrence_type = MULTIPLE`
- `simultaneous_occurrences = MULTIPLE_ACTIVE`
- `lifecycle_strategy = CLOSE`
- `delete_policy = FORBIDDEN`

**Fields:**
- `iban`
- `bank_code`
- `start_date`
- `end_date`

**Business Key:**
- `(employee_id, iban, start_date)`

---

### 9. `employee.identity_document`

Stores identity documents.

**Examples:**
- DNI
- NIE
- Passport

**Structural Properties:**
- `historized = false`
- `occurrence_type = MULTIPLE`
- `lifecycle_strategy = DELETE`
- `delete_policy = PHYSICAL`

**Fields:**
- `document_type`
- `document_number`
- `country_code`

**Business Key:**
- `(employee_id, document_type)`

---

### 10. `employee.tax_information`

Represents tax configuration of the employee.

**Examples:**
- Tax regime
- Tax residence

**Structural Properties:**
- `historized = true`
- `occurrence_type = MULTIPLE`
- `simultaneous_occurrences = SINGLE_ACTIVE`
- `lifecycle_strategy = CLOSE`
- `delete_policy = FORBIDDEN`

**Fields:**
- `tax_regime_code`
- `tax_residence_country_code`
- `start_date`
- `end_date`

**Business Key:**
- `(employee_id, start_date)`

---

## Key Design Principles

The `employee` object is not a single table. It is a composition of resources.

Each resource defines:

- Data structure
- Temporal behavior
- Lifecycle operations
- API behavior
- Validation rules

Resources are implemented consistently following the metamodel.

---

## Next Evolution

Future versions of this catalog may include:

- Field-level metadata
- Reference entity mappings
- Validation rules
- Derived API definitions
- Automatic code generation rules

---

## Important Rule

Implementation must always follow the resource definition.

The order of design is:

```
resource definition
→ domain model
→ database schema
→ application services
→ REST endpoints
```

Not the other way around.
