# TerminateEmployeeService — Participant Pattern Refactor (ADR-047 Phase 2)

## Goal

Replace `TerminateEmployeeService` (13 dependencies, ~400 lines) with a 3-dependency orchestrator that delegates each vertical's closure logic to a `TerminationParticipant`. Mirrors the structure established in Phase 1 (HireEmployeeService).

## Architecture

The service becomes a pure orchestrator. Each vertical owns its own participant, which lists its domain records and closes the active one. All closures execute within the service's single `@Transactional` boundary — no change to transaction semantics.

### New files

**Ports and model:**
- `lifecycle/application/port/TerminationParticipant.java`
- `lifecycle/application/model/TerminationContext.java`
- `lifecycle/application/service/TerminationPreConditionValidator.java`

**Participants (one per vertical):**
- `lifecycle/application/participant/WorkingTimeTerminationParticipant.java` (order=10)
- `lifecycle/application/participant/WorkCenterTerminationParticipant.java` (order=20)
- `lifecycle/application/participant/CostCenterTerminationParticipant.java` (order=30)
- `lifecycle/application/participant/ContractTerminationParticipant.java` (order=40)
- `lifecycle/application/participant/LaborClassificationTerminationParticipant.java` (order=50)
- `lifecycle/application/participant/PresenceTerminationParticipant.java` (order=60)

**Modified:**
- `lifecycle/application/usecase/TerminateEmployeeService.java` — full rewrite
- `lifecycle/application/usecase/TerminateEmployeeServiceTest.java` — full rewrite
- `lifecycle/application/usecase/TerminateEmployeeServiceRollbackIntegrationTest.java` — TestConfiguration updated

---

## TerminationParticipant interface

```java
package com.b4rrhh.employee.lifecycle.application.port;

public interface TerminationParticipant {
    int order();
    void participate(TerminationContext ctx);
}
```

---

## TerminationContext

Mutable data bag carrying normalized inputs (final), the pre-loaded `Employee`, mutable participant results, and the idempotency state.

```java
public class TerminationContext {

    // Inputs — set once by TerminationPreConditionValidator
    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;
    private final LocalDate terminationDate;
    private final String exitReasonCode;
    private final Employee employee;

    // Idempotency
    private final boolean alreadyTerminated;
    private final TerminateEmployeeResult idempotentResult; // non-null only when alreadyTerminated=true

    // Participant results (mutable, written by participants)
    private Presence closedPresence;           // mandatory — must be set by PresenceTerminationParticipant
    private WorkCenter closedWorkCenter;       // optional
    private Contract closedContract;           // optional
    private LaborClassification closedLaborClassification; // optional
    private WorkingTime closedWorkingTime;     // optional
    // CostCenter has no domain result — CloseActiveCostCenterDistributionAtTerminationUseCase handles it internally
}
```

**Key methods:**
- `isAlreadyTerminated()` — returns the idempotency flag
- `reconstructIdempotentResult()` — returns the pre-built `idempotentResult`; throws `IllegalStateException` if not in idempotent state
- `assertNoActivePresence()` — throws `TerminateEmployeeConflictException` if `closedPresence` is null after all participants run
- `terminatedEmployee()` — returns a new `Employee` instance with `status="TERMINATED"` and `updatedAt=LocalDateTime.now()`
- `toResult()` — builds `TerminateEmployeeResult`; requires `closedPresence` non-null

---

## TerminationPreConditionValidator

Dependencies: `GetEmployeeByBusinessKeyUseCase`, `ListEmployeePresencesUseCase`

Steps:
1. Validate `ruleSystemCode`, `employeeTypeCode`, `employeeNumber` — required, normalize to trimmed uppercase. Throw `TerminateEmployeeRequestInvalidException` if missing.
2. Validate `terminationDate` — required. Throw `TerminateEmployeeRequestInvalidException` if null.
3. Validate `exitReasonCode` — required, normalize to trimmed uppercase. Throw `TerminateEmployeeRequestInvalidException` if missing.
4. Call `GetEmployeeByBusinessKeyUseCase` — throw `TerminateEmployeeEmployeeNotFoundException` if not found.
5. If `employee.getStatus().equals("TERMINATED")`:
   - Call the necessary `List*UseCase` ports to fetch history for all domain objects that appear in `TerminateEmployeeResult` (at minimum: Presence; and any optional domain that the result includes — Contract, LaborClassification, WorkCenter, WorkingTime)
   - Filter each history list for records where `endDate.equals(terminationDate)` (and exitReasonCode matches for Presence)
   - Reconstruct `TerminateEmployeeResult` from those closed records
   - Return `TerminationContext` with `alreadyTerminated=true`, `idempotentResult=<reconstructed>`
   - The exact set of `List*UseCase` dependencies is determined by what `TerminateEmployeeResult` requires — read the current service's idempotency path to enumerate them precisely
6. If employee is active: return `TerminationContext` with `alreadyTerminated=false`

---

## Participant behaviour

### Common pattern for optional domain objects (WorkingTime, WorkCenter, Contract, LaborClassification)

Each participant:
1. Calls its `List*UseCase` to fetch all records for the employee
2. Filters for the active record (no endDate)
3. **`requireAtMostOneActive()`** — if more than one active record, throw `TerminateEmployeeConflictException`
4. **`resolveOptionalActive()`** — if the active record's `startDate` is strictly after `terminationDate`, skip (return without closing; ctx setter not called)
5. If no active record → return without closing
6. Close the active record via `Close*UseCase`
7. Store result in context via setter

Exception translation per participant:
- `AlreadyClosed*Exception`, `*NotFoundException`, `Invalid*DateRange`, `*OutsidePresencePeriod`, `CoverageIncomplete*` → `TerminateEmployeeConflictException`
- Catalog-related exceptions → `TerminateEmployeeCatalogValueInvalidException`

### CostCenterTerminationParticipant (order=30)

Special case — delegates entirely to `CloseActiveCostCenterDistributionAtTerminationUseCase.closeIfPresent(ruleSystemCode, employeeTypeCode, employeeNumber, terminationDate)`. No list, no result stored in context.

### PresenceTerminationParticipant (order=60)

Mandatory. Steps:
1. List all presences for the employee
2. Deduplicate by `presenceNumber` (prefer closed over active; prefer latest `endDate` when both closed)
3. Find the active presence — throw `TerminateEmployeeConflictException` if none found
4. Close via `ClosePresenceUseCase`
5. Store in `ctx.setClosedPresence(...)`

Exception translation: `PresenceCatalogValueInvalidException` → `TerminateEmployeeCatalogValueInvalidException`

---

## TerminateEmployeeService (refactored)

```java
@Service
public class TerminateEmployeeService implements TerminateEmployeeUseCase {

    private final TerminationPreConditionValidator validator;
    private final List<TerminationParticipant> participants;
    private final EmployeeRepository employeeRepository;

    public TerminateEmployeeService(
            TerminationPreConditionValidator validator,
            List<TerminationParticipant> participants,
            EmployeeRepository employeeRepository) {
        this.validator = validator;
        this.participants = participants.stream()
                .sorted(Comparator.comparingInt(TerminationParticipant::order))
                .toList();
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public TerminateEmployeeResult terminate(TerminateEmployeeCommand command) {
        TerminationContext ctx = validator.validateAndLookup(command);
        if (ctx.isAlreadyTerminated()) return ctx.reconstructIdempotentResult();
        participants.forEach(p -> p.participate(ctx));
        ctx.assertNoActivePresence();
        employeeRepository.save(ctx.terminatedEmployee());
        return ctx.toResult();
    }
}
```

`EmployeeRepository` is the only non-participant dependency because the employee status update does not belong to any vertical.

---

## Testing

### Per-participant unit tests (new files)

Each `*TerminationParticipantTest` covers:
- `orderIs<N>()` — verifies `order()` return value
- Happy path: verifies the `Close*Command` fields via `ArgumentCaptor`, verifies result stored in context
- Skip path: no active record → use case not called, context not set
- `startDate > terminationDate` skip: active record exists but is skipped
- Exception translation: domain exception → lifecycle exception

`PresenceTerminationParticipantTest` additionally covers:
- Missing active presence → `TerminateEmployeeConflictException`
- Deduplication: duplicate presenceNumbers resolved correctly

### TerminateEmployeeServiceTest (rewritten)

Four orchestration-only tests (same pattern as `HireEmployeeServiceTest`):
- `callsValidatorThenRunsParticipantsInOrder()` — verifies sequence using `inOrder()`
- `returnsIdempotentResultWhenAlreadyTerminated()` — validator returns `alreadyTerminated=true`, participants never called
- `runsPostConditionCheckAfterParticipants()` — `ctx.assertNoActivePresence()` called after forEach
- `savesTerminatedEmployeeStatusAfterParticipants()` — `employeeRepository.save()` called with terminated employee

All tests use `mock(TerminationContext.class)` to avoid coupling to `toResult()` implementation.

### TerminateEmployeeServiceRollbackIntegrationTest (updated)

`@TestConfiguration` provides:
- `TerminationPreConditionValidator` bean (needs `GetEmployeeByBusinessKeyUseCase` stub + `ListEmployeePresencesUseCase` stub)
- 6 `TerminationParticipant` beans wrapping stubs (Presence closes successfully; one optional participant throws for the rollback scenario)
- `EmployeeRepository` already provided by `EmployeePersistenceAdapter` in `@Import`

---

## What does NOT change

- `@Transactional` boundary — single transaction, same rollback behaviour
- All domain exceptions and their HTTP mappings (`TerminateEmployeeExceptionHandler`)
- `TerminateEmployeeController`, `TerminateEmployeeWebMapper`, DTOs
- `TerminateEmployeeCommand`, `TerminateEmployeeResult`
- Public API contract
