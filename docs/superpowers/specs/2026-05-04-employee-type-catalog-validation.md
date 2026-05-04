# Employee Type Code — Catalog Validation & Semantic Default

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the placeholder `"EMP"` employee type code with the semantically correct `"INTERNAL"`, and add catalog-backed validation so that `employeeTypeCode` is verified against the `EMPLOYEE_TYPE` rule entity — just like `contractTypeCode`, `agreementCode`, and other catalog fields.

**Architecture:** New `EmployeeTypeCatalogValidator @Component` following the existing validator pattern (`ContractCatalogValidator`, `PresenceCatalogValidator`). The default in both backend and frontend changes from `"EMP"` to `"INTERNAL"`. A new migration seeds the `resource_field_catalog_binding` entry to register `EMPLOYEE_TYPE` in the metamodel.

**Tech Stack:** Java 21 / Spring Boot, JUnit 5 / Mockito, Angular 21 / Vitest, Flyway PostgreSQL migrations.

---

## Context

`employee_type_code` is part of the business key `(rule_system_code, employee_type_code, employee_number)`. The DB already seeds two valid values for `ESP`: `INTERNAL` (V49) and `EXTERNAL` (V49). However:

- Backend hardcodes `DEFAULT_EMPLOYEE_TYPE_CODE = "EMP"` — a value that does **not** exist in the catalog.
- Frontend hardcodes `employeeTypeCode: 'EMP'` — same problem.
- No catalog lookup validates the type at hire or rehire time.

The hire form always produces `INTERNAL` employees (no UI selector); `EXTERNAL` is reserved for future use.

---

## Scope

### Out of scope
- Exposing `employeeTypeCode` as a selectable field in the hire form.
- Per-type catalog restrictions (e.g. different available contracts per type).
- Creating an `EXTERNAL` hire flow.

---

## Layer-by-layer changes

### A. Default: `"EMP"` → `"INTERNAL"`

**Backend**
- `HireEmployeeDefaultValues.DEFAULT_EMPLOYEE_TYPE_CODE = "INTERNAL"`

**Frontend**
- `HIRE_EMPLOYEE_DEFAULTS.employeeTypeCode = 'INTERNAL'` in `hire-employee.defaults.ts`

---

### B. New `EmployeeTypeCatalogValidator`

**Package:** `com.b4rrhh.employee.employee.application.service`

```java
@Component
public class EmployeeTypeCatalogValidator {

    private final RuleEntityRepository ruleEntityRepository;

    public EmployeeTypeCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public void validateEmployeeTypeCode(
            String ruleSystemCode,
            String employeeTypeCode,
            LocalDate referenceDate
    ) {
        RuleEntity ruleEntity = ruleEntityRepository
                .findByBusinessKey(ruleSystemCode, "EMPLOYEE_TYPE", employeeTypeCode)
                .orElseThrow(() -> new EmployeeTypeInvalidException(employeeTypeCode));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new EmployeeTypeInvalidException(employeeTypeCode);
        }
    }

    private boolean isDateApplicable(RuleEntity ruleEntity, LocalDate referenceDate) {
        if (referenceDate == null) return true;
        boolean starts = !referenceDate.isBefore(ruleEntity.getStartDate());
        boolean ends = ruleEntity.getEndDate() == null || !referenceDate.isAfter(ruleEntity.getEndDate());
        return starts && ends;
    }
}
```

**New exception:** `com.b4rrhh.employee.employee.domain.exception.EmployeeTypeInvalidException`

```java
public class EmployeeTypeInvalidException extends RuntimeException {
    public EmployeeTypeInvalidException(String code) {
        super("Invalid employeeTypeCode: '" + code + "'");
    }
}
```

---

### C. Wire into `HireEmployeeService`

1. Inject `EmployeeTypeCatalogValidator` as a constructor dependency.
2. Call `employeeTypeCatalogValidator.validateEmployeeTypeCode(ruleSystemCode, employeeTypeCode, hireDate)` **before** the duplicate-employee check.
3. Add `EmployeeTypeInvalidException` to the existing catalog-value catch block → `HireEmployeeCatalogValueInvalidException`.

---

### D. Wire into `RehireEmployeeService`

Same as C: inject validator, call before rehire logic, add `EmployeeTypeInvalidException` to the existing catalog-value catch block → `RehireEmployeeCatalogValueInvalidException`.

---

### E. New migration `V93__seed_employee_type_catalog_binding.sql`

Registers the `EMPLOYEE_TYPE` entity type in `resource_field_catalog_binding` so the metamodel is consistent with other catalog-backed fields:

```sql
insert into rulesystem.resource_field_catalog_binding (
    rule_system_code, resource_code, field_code, rule_entity_type_code
)
select rs.code, 'EMPLOYEE', 'employeeTypeCode', 'EMPLOYEE_TYPE'
from rulesystem.rule_system rs
where rs.code = 'ESP'
on conflict do nothing;
```

> Note: `V92` was already used for the contract subtype length fix. This migration is `V93`.

---

### F. Test updates

**Backend**
- New `EmployeeTypeCatalogValidatorTest` (unit) with:
  - Valid code → no exception
  - Code not in catalog → `EmployeeTypeInvalidException`
  - Inactive entity → `EmployeeTypeInvalidException`
- `HireEmployeeServiceTest` (if exists): add mock for `EmployeeTypeCatalogValidator`, verify it's called with the right args.
- Any test that passes `"EMP"` as `employeeTypeCode` (search and replace with `"INTERNAL"`).

**Frontend**
- Tests in `employee-hiring.mapper.spec.ts`, `employee-rehire.mapper.spec.ts`, `hire-employee-page.component.spec.ts` that assert `employeeTypeCode === 'EMP'` → change to `'INTERNAL'`.

---

## Error propagation

| Exception | Catch block | HTTP response |
|---|---|---|
| `EmployeeTypeInvalidException` | `HireEmployeeCatalogValueInvalidException` | 422 `INVALID_CATALOG_VALUE` |
| same in rehire | `RehireEmployeeCatalogValueInvalidException` | 422 `INVALID_CATALOG_VALUE` |

---

## What does NOT change

- The business key shape `(ruleSystemCode, employeeTypeCode, employeeNumber)` — unchanged.
- No new UI fields in the hire form.
- No per-type catalog restrictions.
- `EXTERNAL` seeded in DB but not used by any flow.
- All other lifecycle endpoints (terminate, etc.) already receive `employeeTypeCode` as a path variable — they don't create employees so no catalog validation needed there.
