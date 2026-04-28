# Employee Payroll Input — Design Spec

## Goal

Allow HR to register per-employee, per-period quantity inputs (overtime hours, transport bonus units, etc.) that the payroll engine consumes at calculation time via a new `EMPLOYEE_INPUT` calculation type.

## Context

The payroll engine resolves a directed acyclic concept graph. Existing calculation types (RATE_BY_QUANTITY, PERCENTAGE, AGGREGATE) operate on other concept results. `DIRECT_AMOUNT` and `JAVA_PROVIDED` inject fixed or hard-coded values. There is currently no way to feed variable, per-employee quantities — the gap this feature closes.

---

## Component 1: `employee.payroll_input` Vertical

### Domain Model — `EmployeePayrollInput`

| Field | Type | Notes |
|---|---|---|
| `ruleSystemCode` | String | Part of business key |
| `employeeTypeCode` | String | Part of business key |
| `employeeNumber` | String | Part of business key |
| `conceptCode` | String | Part of business key; references the EMPLOYEE_INPUT concept that consumes this value |
| `period` | int | Part of business key; format `yyyyMM` (e.g. 202604) |
| `quantity` | BigDecimal | The value the engine will read |

Business key: `(ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period)` — unique constraint enforced at DB and domain level.

No separate input-type catalog. The `conceptCode` IS the identifier. If a concept is renamed, its inputs must be updated — acceptable given concept codes are stable.

### Use Cases

| Use case | Command | Service |
|---|---|---|
| `CreateEmployeePayrollInputUseCase` | `CreateEmployeePayrollInputCommand` | `CreateEmployeePayrollInputService` |
| `UpdateEmployeePayrollInputUseCase` | `UpdateEmployeePayrollInputCommand` | `UpdateEmployeePayrollInputService` |
| `DeleteEmployeePayrollInputUseCase` | `DeleteEmployeePayrollInputCommand` | `DeleteEmployeePayrollInputService` |
| `ListEmployeePayrollInputsUseCase` | `ListEmployeePayrollInputsCommand` | `ListEmployeePayrollInputsService` |

`CreateEmployeePayrollInputService` throws `EmployeePayrollInputAlreadyExistsException` on duplicate business key.

`UpdateEmployeePayrollInputService` throws `EmployeePayrollInputNotFoundException` if the record does not exist.

### REST API

All paths follow the employee business-key convention.

```
POST   /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs
GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs?period=202604
PUT    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs/{conceptCode}?period=202604
DELETE /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs/{conceptCode}?period=202604
```

**POST body** (`CreateEmployeePayrollInputRequest`):
```json
{ "conceptCode": "HE_QTY", "period": 202604, "quantity": 40.00 }
```

**PUT body** (`UpdateEmployeePayrollInputRequest`):
```json
{ "quantity": 35.00 }
```

**GET response** (`EmployeePayrollInputsResponse`):
```json
{
  "period": 202604,
  "inputs": [
    { "conceptCode": "HE_QTY", "quantity": 40.00 },
    { "conceptCode": "TRANSP_QTY", "quantity": 1.00 }
  ]
}
```

### Database Migration

New table `employee_payroll_input`:

```sql
CREATE TABLE employee_payroll_input (
    rule_system_code   VARCHAR(10)    NOT NULL,
    employee_type_code VARCHAR(10)    NOT NULL,
    employee_number    VARCHAR(20)    NOT NULL,
    concept_code       VARCHAR(20)    NOT NULL,
    period             INTEGER        NOT NULL,
    quantity           NUMERIC(14,4)  NOT NULL,
    CONSTRAINT pk_employee_payroll_input
        PRIMARY KEY (rule_system_code, employee_type_code, employee_number, concept_code, period)
);
```

### Package Structure

```
com.b4rrhh.employee.payroll_input
  domain.model.EmployeePayrollInput
  domain.port.EmployeePayrollInputRepository
  domain.exception.EmployeePayrollInputAlreadyExistsException
  domain.exception.EmployeePayrollInputNotFoundException
  application.usecase.CreateEmployeePayrollInputUseCase
  application.usecase.CreateEmployeePayrollInputCommand
  application.usecase.CreateEmployeePayrollInputService
  application.usecase.UpdateEmployeePayrollInputUseCase
  application.usecase.UpdateEmployeePayrollInputCommand
  application.usecase.UpdateEmployeePayrollInputService
  application.usecase.DeleteEmployeePayrollInputUseCase
  application.usecase.DeleteEmployeePayrollInputCommand
  application.usecase.DeleteEmployeePayrollInputService
  application.usecase.ListEmployeePayrollInputsUseCase
  application.usecase.ListEmployeePayrollInputsCommand
  application.usecase.ListEmployeePayrollInputsService
  infrastructure.persistence.EmployeePayrollInputEntity
  infrastructure.persistence.SpringDataEmployeePayrollInputRepository
  infrastructure.persistence.EmployeePayrollInputPersistenceAdapter
  infrastructure.web.EmployeePayrollInputController
  infrastructure.web.dto.CreateEmployeePayrollInputRequest
  infrastructure.web.dto.UpdateEmployeePayrollInputRequest
  infrastructure.web.dto.EmployeePayrollInputResponse
  infrastructure.web.dto.EmployeePayrollInputsResponse
  infrastructure.web.dto.EmployeePayrollInputErrorResponse
  infrastructure.web.assembler.EmployeePayrollInputResponseAssembler
  infrastructure.web.EmployeePayrollInputExceptionHandler
```

---

## Component 2: Engine Integration

### New Calculation Type

Add `EMPLOYEE_INPUT` to `CalculationType` enum in `payroll_engine.concept.domain.model`.

A concept with `calculationType = EMPLOYEE_INPUT` has:
- No operands
- No feeds
- No new fields on `PayrollConcept`

Its `conceptCode` is the key used to look up the employee's registered quantity for the period.

### `SegmentCalculationContext` extension

Add field `Map<String, BigDecimal> employeeInputs` (conceptCode → quantity).

Constructor validates: field must not be null (empty map is allowed — means no inputs registered for this employee+period).

This follows the same pattern as `monthlySalaryAmount` — a monthly value passed once and shared across all segments.

### Execution engine — `DefaultSegmentExecutionEngine`

Add case to the switch expression:

```java
case EMPLOYEE_INPUT -> context.employeeInputs()
    .getOrDefault(entry.identity().conceptCode(), BigDecimal.ZERO);
```

No exception is thrown for missing inputs: returning zero is correct because a concept like `HE_QTY` may be assigned to the employee but have zero hours in a given month.

`ConceptExecutionPlanEntry` does not change — `EMPLOYEE_INPUT` entries use the same empty collections as `DIRECT_AMOUNT`.

### Lookup Port — `EmployeePayrollInputLookupPort`

Defined in `payroll.application.port`, following the pattern of `PayrollEmployeePresenceLookupPort`:

```java
public interface EmployeePayrollInputLookupPort {
    Map<String, BigDecimal> findInputsByPeriod(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        int period
    );
}
```

Context DTO: `EmployeePayrollInputContext` (wraps the map).

### Lookup Adapter

`EmployeePayrollInputLookupAdapter` in `payroll.infrastructure.persistence` implements the port by querying `employee_payroll_input` directly.

### `CalculatePayrollUnitService` wiring

When building `SegmentCalculationContext`, derive the period from `periodStart`:
```java
int period = periodStart.getYear() * 100 + periodStart.getMonthValue();
```

Call the port, add the resulting map to the context:
```java
Map<String, BigDecimal> employeeInputs = employeePayrollInputLookupPort
    .findInputsByPeriod(ruleSystemCode, employeeTypeCode, employeeNumber, period);
// pass to SegmentCalculationContext constructor
```

The same `employeeInputs` map is reused for all segments of the same period (inputs are monthly, not segment-specific).

---

## Component 3: Designer Update (minor)

In `b4rrhh_designer`:

- Add `EMPLOYEE_INPUT` to `CalculationType` union type in `types.ts`
- Add `EMPLOYEE_INPUT` to `INPUT_PORTS` map with empty port list (`[]`) — the concept has no input handles, only the `out` source handle
- Add badge color and label for `EMPLOYEE_INPUT` in `ConceptNode` and `CanvasLegend`

---

## OpenAPI Contract

Per project convention, all API changes start with the OpenAPI spec. The four new endpoints must be added to `b4rrhh_backend/openapi/personnel-administration-api.yaml` before implementation:

- `POST /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs`
- `GET /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs`
- `PUT /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs/{conceptCode}`
- `DELETE /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs/{conceptCode}`

Schemas: `CreateEmployeePayrollInputRequest`, `UpdateEmployeePayrollInputRequest`, `EmployeePayrollInputsResponse`, `EmployeePayrollInputErrorResponse`.

---

## Error Handling

| Condition | Exception | HTTP |
|---|---|---|
| Duplicate payroll input | `EmployeePayrollInputAlreadyExistsException` | 409 |
| Input not found | `EmployeePayrollInputNotFoundException` | 404 |
| Unknown concept code (validation optional) | — | Not enforced at this layer; decoupled by design |

---

## Testing

- Unit tests for `CreateEmployeePayrollInputService` (duplicate check)
- Unit tests for `DefaultSegmentExecutionEngine` EMPLOYEE_INPUT case (value present, value absent → zero)
- Integration test for `CalculatePayrollUnitService` with a mock `EMPLOYEE_INPUT` concept in the graph
- API-level tests for all four endpoints
