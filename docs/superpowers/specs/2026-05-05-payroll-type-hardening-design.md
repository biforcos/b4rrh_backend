# Payroll Type Hardening — Design Spec

**Date:** 2026-05-05
**Status:** Approved

---

## Goal

Restrict `payrollTypeCode` to exactly two valid values — `NORMAL` and `EXTRA` — across the backend and frontend. Currently the field is a free-form `varchar(30)` with no constraint at any layer; all existing test data incorrectly uses `"MENSUAL"` (which is the accrual type, not the payroll type).

---

## Scope

This spec covers only `payrollTypeCode` in the `payroll` bounded context. It does NOT touch:

- `TipoNomina` (MENSUAL / DIARIO) — that is the accrual type used by `AgreementCategoryProfile` and is already constrained by its own CHECK constraint.
- The `rulesystem` module — no rule entity type is created; this is a lightweight, self-contained constraint.

---

## Approach: Lightweight (Option B)

- `payrollTypeCode` stays as `String` in domain, JPA entities, and all mappers — no enum type change.
- Enforcement via: DB CHECK constraints + domain constructor validation.
- No `PayrollTypeCatalogValidator`, no catalog lookup, no rule entity type.

---

## Backend

### 1. Database Migration — `V94__add_payroll_type_code_constraint.sql`

Applies to 4 tables in the `payroll` schema:
- `payroll.payroll`
- `payroll.calculation_run`
- `payroll.calculation_claim`
- `payroll.calculation_run_message`

Steps (in order):
1. UPDATE existing rows: `'MENSUAL'` → `'NORMAL'` in each table (cleans up incorrect test/dev data).
2. ADD CHECK constraint `payroll_type_code IN ('NORMAL', 'EXTRA')` to each table.

### 2. Domain Constants — `PayrollTypeCodes.java`

New file: `com.b4rrhh.payroll.domain.model.PayrollTypeCodes`

```java
public final class PayrollTypeCodes {
    public static final String NORMAL = "NORMAL";
    public static final String EXTRA  = "EXTRA";

    private static final Set<String> VALID = Set.of(NORMAL, EXTRA);

    private PayrollTypeCodes() {}

    public static boolean isValid(String code) {
        return code != null && VALID.contains(code);
    }
}
```

### 3. Domain Exception — `PayrollTypeInvalidException.java`

New file: `com.b4rrhh.payroll.domain.exception.PayrollTypeInvalidException`

```java
public class PayrollTypeInvalidException extends RuntimeException {
    public PayrollTypeInvalidException(String code) {
        super("Invalid payrollTypeCode: '" + code + "'");
    }
}
```

### 4. Domain Model Validation — `Payroll.java`

In the constructor (or factory method) of the `Payroll` aggregate, add:

```java
if (!PayrollTypeCodes.isValid(payrollTypeCode)) {
    throw new PayrollTypeInvalidException(payrollTypeCode);
}
```

Single enforcement point. No changes to services individually.

### 5. Exception Mapping

`PayrollTypeInvalidException` must be mapped to an appropriate HTTP error response (422 or 400) in the existing payroll exception handler.

### 6. Test Data Update

All test files in the `payroll` module that use `"MENSUAL"` as a `payrollTypeCode` value must be updated to `"NORMAL"`. This is a mechanical find-and-replace scoped to `src/test/java/com/b4rrhh/payroll/`.

---

## OpenAPI

### `openapi/payroll-api.yaml`

Add `enum: [NORMAL, EXTRA]` to the `payrollTypeCode` schema definition (path parameters and request body properties).

### `openapi/personnel-administration-api.yaml`

Same: add `enum: [NORMAL, EXTRA]` wherever `payrollTypeCode` appears as a schema property.

---

## Frontend

### API Client Regeneration

After OpenAPI changes: run `npm run api:refresh` in `b4rrhh_frontend`. This regenerates `src/app/core/api/generated/` — auto-generated models will reflect the new enum constraint.

### Operaciones Page

**File:** `src/app/features/nomina/operaciones/store/operaciones.store.ts`
- Change default: `payrollTypeCodeState = signal('MENSUAL')` → `signal('NORMAL')`

**File:** `src/app/features/nomina/operaciones/ui/operaciones-page.component.html`
- Replace the free-text `<input>` for "Tipo de nómina" with a `<p-dropdown>` bound to options `[{ value: 'NORMAL', label: 'Normal' }, { value: 'EXTRA', label: 'Extra' }]`.

---

## What Does NOT Change

- `payroll_type_code` column type stays `varchar(30)` — no ALTER COLUMN needed.
- JPA entities (`PayrollEntity`, `CalculationRunEntity`, `CalculationClaimEntity`, `CalculationRunMessageEntity`) — no changes.
- Persistence adapters and mappers — no changes.
- Service method signatures — no changes.
- `TipoNomina` enum and `AgreementCategoryProfile` — untouched.

---

## Test Strategy

| Layer | Test |
|-------|------|
| Domain | Unit test `PayrollTypeCodes.isValid()` and `Payroll` constructor rejection |
| Services | Update existing service tests: replace `"MENSUAL"` → `"NORMAL"` |
| Exception handler | Unit test that `PayrollTypeInvalidException` → correct HTTP response |

No new integration tests required.

---

## Migration Ordering

V94 is the next available version. No dependencies on V93.
