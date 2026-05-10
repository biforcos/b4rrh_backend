# Employee Auto-Numbering Design

**Goal:** Eliminate manual entry of employee number (matrícula) during hire by generating it automatically from a configurable counter per rule system.

**Date:** 2026-05-10

---

## Problem

`HireEmployeeCommand` currently requires `employeeNumber` to be provided by the caller. This means the frontend hire form, the workforce loader, and any API consumer must invent a unique number — a source of errors and friction. The system should own this responsibility.

---

## Decisions

| # | Decision |
|---|----------|
| 1 | Scope: one config per `ruleSystemCode` (not per company, not per employee type) |
| 2 | Server always auto-generates: `employeeNumber` removed from hire request entirely |
| 3 | Architecture: new vertical `rulesystem.employeenumbering`, following `CompanyProfile`/`DisplayNameFormat` pattern |
| 4 | Atomicity: pessimistic lock (`SELECT … FOR UPDATE`) within the hire transaction |
| 5 | Frontend: numbering config as a tab in the rule system detail page |

---

## Config Model

**Domain entity:** `EmployeeNumberingConfig`

| Field | Type | Constraints |
|-------|------|-------------|
| `ruleSystemCode` | String | Logical PK, FK to rule_system |
| `prefix` | String | May be empty `""`, max 14 chars |
| `numericPartLength` | int | ≥ 1 |
| `step` | int | ≥ 1 |
| `nextValue` | long | ≥ 1 |

**Invariant (validated on upsert):** `prefix.length() + numericPartLength <= 15`
— enforced because `employee.employee_number` is `varchar(15)`.
Violation → HTTP 422.

**Number format:** `prefix + leftPad(nextValue, numericPartLength, '0')`

Examples:
- `prefix="EMP"`, `numericPartLength=6`, `nextValue=1` → `EMP000001`
- `prefix=""`, `numericPartLength=8`, `nextValue=42` → `00000042`

**Overflow:** if `nextValue > 10^numericPartLength - 1`, hire throws `EmployeeNumberingExhaustedException` → HTTP 409. The operator must update the config (increase `numericPartLength` or reset `nextValue`).

---

## Architecture

### New vertical: `rulesystem.employeenumbering`

```
com.b4rrhh.rulesystem.employeenumbering
  domain/model/         EmployeeNumberingConfig.java
  domain/port/          EmployeeNumberingConfigRepository.java
  application/usecase/  GetEmployeeNumberingConfigUseCase.java
                        GetEmployeeNumberingConfigService.java
                        UpsertEmployeeNumberingConfigUseCase.java
                        UpsertEmployeeNumberingConfigCommand.java
                        UpsertEmployeeNumberingConfigService.java
  infrastructure/
    persistence/        EmployeeNumberingConfigEntity.java
                        SpringDataEmployeeNumberingConfigRepository.java
                        EmployeeNumberingConfigPersistenceAdapter.java
    web/
      dto/              EmployeeNumberingConfigResponse.java
                        UpsertEmployeeNumberingConfigRequest.java
      EmployeeNumberingConfigController.java
      EmployeeNumberingConfigExceptionHandler.java
```

### Port + adapter in `employee.lifecycle`

```
com.b4rrhh.employee.lifecycle
  application/port/     NextEmployeeNumberPort.java
  infrastructure/       NextEmployeeNumberAdapter.java   ← SELECT FOR UPDATE
```

`NextEmployeeNumberPort` interface:
```java
String consumeNext(String ruleSystemCode);
// Throws EmployeeNumberingConfigNotFoundException if no config exists
// Throws EmployeeNumberingExhaustedException if counter would overflow
```

`NextEmployeeNumberAdapter` implementation (runs inside the hire transaction):
```java
@Transactional(propagation = MANDATORY)
public String consumeNext(String ruleSystemCode) {
    EmployeeNumberingConfigEntity config = repository.findByRuleSystemCodeForUpdate(ruleSystemCode)
        .orElseThrow(() -> new EmployeeNumberingConfigNotFoundException(ruleSystemCode));
    long value = config.getNextValue();
    long max = (long) Math.pow(10, config.getNumericPartLength()) - 1;
    if (value > max) throw new EmployeeNumberingExhaustedException(ruleSystemCode);
    String number = config.getPrefix() + String.format("%0" + config.getNumericPartLength() + "d", value);
    config.setNextValue(value + config.getStep());
    repository.save(config);
    return number;
}
```

---

## API

### Config endpoints

```
GET  /rule-systems/{ruleSystemCode}/employee-numbering-config
     → 200 EmployeeNumberingConfigResponse | 404 if not configured

PUT  /rule-systems/{ruleSystemCode}/employee-numbering-config
     body: UpsertEmployeeNumberingConfigRequest
     → 200 EmployeeNumberingConfigResponse
     → 422 if prefix.length() + numericPartLength > 15
     → 404 if ruleSystemCode does not exist
```

### Hire endpoint changes

`POST /employees/hire` request body: **remove `employeeNumber` field**.

The generated number is returned in the existing `employeeNumber` field of `HireEmployeeResponse` — no change to the response contract.

---

## Hire flow change

**Before:**
```
HireEmployeeRequest(employeeNumber, ...) → HireEmployeeCommand(employeeNumber, ...) → HireEmployeeService
```

**After:**
```
HireEmployeeRequest(...) → HireEmployeeService
  → nextEmployeeNumberPort.consumeNext(ruleSystemCode)  ← atomic, inside @Transactional
  → HireEmployeeCommand(generatedNumber, ...)
```

`RehireEmployeeRequest/Command`: no change — rehire does not assign a new number.

---

## Error cases

| Situation | Exception | HTTP |
|-----------|-----------|------|
| No config for ruleSystemCode | `EmployeeNumberingConfigNotFoundException` | 422 |
| Counter exhausted (`nextValue > 10^numericPartLength - 1`) | `EmployeeNumberingExhaustedException` | 409 |
| Upsert with invalid length | `EmployeeNumberingConfigInvalidException` | 422 |

---

## Database migration

**V98** — create table:
```sql
CREATE TABLE employee_numbering_config (
    rule_system_code    varchar(20)  NOT NULL,
    prefix              varchar(14)  NOT NULL DEFAULT '',
    numeric_part_length int          NOT NULL,
    step                int          NOT NULL DEFAULT 1,
    next_value          bigint       NOT NULL DEFAULT 1,
    CONSTRAINT pk_employee_numbering_config PRIMARY KEY (rule_system_code),
    CONSTRAINT fk_employee_numbering_config_rs
        FOREIGN KEY (rule_system_code) REFERENCES rule_system(code),
    CONSTRAINT chk_employee_numbering_config_length
        CHECK (length(prefix) + numeric_part_length <= 15),
    CONSTRAINT chk_employee_numbering_config_part_min
        CHECK (numeric_part_length >= 1),
    CONSTRAINT chk_employee_numbering_config_step_min
        CHECK (step >= 1),
    CONSTRAINT chk_employee_numbering_config_next_min
        CHECK (next_value >= 1)
);
```

**V99** — seed for ESP:
```sql
INSERT INTO employee_numbering_config (rule_system_code, prefix, numeric_part_length, step, next_value)
VALUES ('ESP', 'EMP', 6, 1, 1);
-- Generates: EMP000001 … EMP999999 (1,000,000 employees)
```

---

## Frontend changes

### Rule system detail page
- Add **"Numeración"** tab alongside existing tabs (Perfil, Formato nombre, etc.)
- Tab content: form with `prefix`, `numericPartLength`, `step`, `nextValue` fields
- **Live preview**: computed field showing the next number as user types (e.g., `EMP000001`)
- On save: calls `PUT /rule-systems/{code}/employee-numbering-config`
- On 404: shows "Sin configurar" with a save button pre-filled with defaults

### Hire form
- Remove the `employeeNumber` input field entirely
- The hired employee's number comes back in the response and is displayed as read-only confirmation

---

## Workforce loader changes

- Remove `employeeNumber` from the hire request DTO and all places that build it
- The loader no longer generates or tracks employee numbers — the backend owns the sequence

---

## Tests

### Backend
- `EmployeeNumberingConfigTest` — unit: format, overflow guard, length invariant
- `UpsertEmployeeNumberingConfigServiceTest` — unit: validates `prefix.length() + numericPartLength <= 15`
- `NextEmployeeNumberAdapterIntegrationTest` — H2 integration: two consecutive calls return different numbers, `nextValue` advances correctly; missing config throws correct exception
- `HireEmployeeServiceTest` — update: remove `employeeNumber` from command, verify port is called

### Workforce loader
- Update `HireReferenceDataResolverTest` to reflect that hire requests no longer carry `employeeNumber`
