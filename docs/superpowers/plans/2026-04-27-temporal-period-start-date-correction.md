# Temporal Period Start-Date Correction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow users to correct the `startDate` of an existing temporal period (contract, labor classification, working time). When the date changes, the predecessor period's `endDate` is automatically adjusted to `newStartDate - 1 day` to maintain timeline continuity.

**Architecture:** The fix follows the existing layered pattern — OpenAPI spec first, then domain model, application service, infrastructure, frontend. Contract and labor classification reuse their existing `PUT /{startDate}` endpoints (path = old key, body = corrected value). Working time gets a new `PUT /{workingTimeNumber}` endpoint because the surrogate key does not change when the date moves.

**Tech Stack:** Java 21 records, Spring Boot, JPA, Spring Data, Angular 21 signals, generated OpenAPI client, Vitest, JUnit 5

**Worktrees:**
- Backend: `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_backend` (main branch)
- Frontend: `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_frontend\.worktrees\labor-redesign` (branch `feat/labor-management-redesign`)

---

## File Map

### Backend (new/modified)

| Action | File |
|--------|------|
| Modify | `openapi/personnel-administration-api.yaml` |
| Modify | `src/main/java/com/b4rrhh/employee/contract/domain/model/Contract.java` |
| Modify | `src/main/java/com/b4rrhh/employee/contract/application/command/UpdateContractCommand.java` |
| Modify | `src/main/java/com/b4rrhh/employee/contract/infrastructure/rest/dto/UpdateContractRequest.java` |
| Modify | `src/main/java/com/b4rrhh/employee/contract/infrastructure/rest/ContractController.java` |
| Modify | `src/main/java/com/b4rrhh/employee/contract/application/usecase/UpdateContractService.java` |
| Modify | `src/test/java/com/b4rrhh/employee/contract/application/usecase/UpdateContractServiceTest.java` |
| Modify | `src/main/java/com/b4rrhh/employee/labor_classification/domain/model/LaborClassification.java` |
| Modify | `src/main/java/com/b4rrhh/employee/labor_classification/application/command/UpdateLaborClassificationCommand.java` |
| Modify | `src/main/java/com/b4rrhh/employee/labor_classification/infrastructure/rest/dto/UpdateLaborClassificationRequest.java` |
| Modify | `src/main/java/com/b4rrhh/employee/labor_classification/infrastructure/rest/LaborClassificationController.java` |
| Modify | `src/main/java/com/b4rrhh/employee/labor_classification/application/usecase/UpdateLaborClassificationService.java` |
| Modify | `src/test/java/com/b4rrhh/employee/labor_classification/application/usecase/UpdateLaborClassificationServiceTest.java` |
| Modify | `src/main/java/com/b4rrhh/employee/working_time/domain/model/WorkingTime.java` |
| Modify | `src/main/java/com/b4rrhh/employee/working_time/domain/port/WorkingTimeRepository.java` |
| Modify | `src/main/java/com/b4rrhh/employee/working_time/infrastructure/persistence/SpringDataWorkingTimeRepository.java` |
| Create | `src/main/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeUseCase.java` |
| Create | `src/main/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeCommand.java` |
| Create | `src/main/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeService.java` |
| Create | `src/main/java/com/b4rrhh/employee/working_time/infrastructure/web/dto/UpdateWorkingTimeRequest.java` |
| Modify | `src/main/java/com/b4rrhh/employee/working_time/infrastructure/web/WorkingTimeController.java` |
| Create | `src/test/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeServiceTest.java` |

### Frontend (worktree `.worktrees/labor-redesign`)

All paths below are relative to `b4rrhh_frontend/.worktrees/labor-redesign`.

| Action | File |
|--------|------|
| Run | `npm run api:refresh` |
| Modify | `src/app/features/employee/data-access/employee-contract.mapper.ts` |
| Modify | `src/app/features/employee/presence/components/employee-contract-section.component.ts` |
| Modify | `src/app/features/employee/presence/components/employee-contract-section.component.html` |
| Modify | `src/app/features/employee/data-access/employee-labor-classification.mapper.ts` |
| Modify | `src/app/features/employee/presence/components/employee-labor-classification-section.component.ts` |
| Modify | `src/app/features/employee/presence/components/employee-labor-classification-section.component.html` |
| Modify | `src/app/features/employee/data-access/employee-working-time.mapper.ts` |
| Modify | `src/app/core/api/clients/employee-working-time-read.client.ts` |
| Modify | `src/app/features/employee/data-access/employee-working-time.gateway.ts` |
| Modify | `src/app/features/employee/data-access/employee-working-time.store.ts` |
| Modify | `src/app/features/employee/presence/components/employee-working-time-section.component.ts` |
| Modify | `src/app/features/employee/presence/components/employee-working-time-section.component.html` |

---

## Task 1: OpenAPI Spec — Add startDate Fields and UpdateWorkingTimeRequest

**Files:**
- Modify: `openapi/personnel-administration-api.yaml`

- [ ] **Step 1: Add optional `startDate` to `UpdateContractRequest` schema**

Open `openapi/personnel-administration-api.yaml`. The `UpdateContractRequest` schema starts at line 5750. Replace the schema body:

```yaml
    UpdateContractRequest:
      type: object
      additionalProperties: false
      required:
        - contractCode
        - contractSubtypeCode
      properties:
        startDate:
          type: string
          format: date
          nullable: true
          description: >-
            Corrected start date (yyyy-MM-dd). When provided and different from the
            path startDate, the predecessor period's endDate is automatically adjusted
            to newStartDate - 1 day. Must not overlap with other periods.
        contractCode:
          type: string
          minLength: 3
          maxLength: 3
        contractSubtypeCode:
          type: string
          minLength: 3
          maxLength: 3
```

- [ ] **Step 2: Add optional `startDate` to `UpdateLaborClassificationRequest` schema**

The `UpdateLaborClassificationRequest` schema starts at line 5854. Replace the schema body:

```yaml
    UpdateLaborClassificationRequest:
      type: object
      additionalProperties: false
      required:
        - agreementCode
        - agreementCategoryCode
      properties:
        startDate:
          type: string
          format: date
          nullable: true
          description: >-
            Corrected start date (yyyy-MM-dd). When provided and different from the
            path startDate, the predecessor period's endDate is automatically adjusted
            to newStartDate - 1 day. Must not overlap with other periods.
        agreementCode:
          type: string
          minLength: 3
          maxLength: 3
        agreementCategoryCode:
          type: string
          minLength: 3
          maxLength: 3
```

- [ ] **Step 3: Add `UpdateWorkingTimeRequest` schema near the working time schemas**

After the `CloseWorkingTimeRequest` schema (around line 5660), add:

```yaml
    UpdateWorkingTimeRequest:
      type: object
      additionalProperties: false
      required:
        - startDate
        - workingTimePercentage
      properties:
        startDate:
          type: string
          format: date
          description: >-
            Corrected start date (yyyy-MM-dd). When different from the current
            startDate, the predecessor period's endDate is automatically adjusted
            to newStartDate - 1 day.
        workingTimePercentage:
          type: number
          format: double
          minimum: 0
          exclusiveMinimum: true
          maximum: 100
```

- [ ] **Step 4: Add `PUT /{workingTimeNumber}` endpoint in the working times path**

Find the `/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/working-times/{workingTimeNumber}` path block. After the existing `post` (close) operation, add:

```yaml
      put:
        summary: Correct a working time period (startDate and/or percentage)
        operationId: updateWorkingTimeByBusinessKey
        tags:
          - Working Time
        parameters:
          - $ref: '#/components/parameters/ruleSystemCode'
          - $ref: '#/components/parameters/employeeTypeCode'
          - $ref: '#/components/parameters/employeeNumber'
          - name: workingTimeNumber
            in: path
            required: true
            schema:
              type: integer
        requestBody:
          required: true
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UpdateWorkingTimeRequest'
        responses:
          '200':
            description: Working time corrected successfully
            content:
              application/json:
                schema:
                  $ref: '#/components/schemas/WorkingTimeResponse'
          '404':
            description: Employee or working time not found
          '409':
            description: New start date overlaps with another working time period
```

- [ ] **Step 5: Verify backend still compiles (no Java changes yet)**

```bash
cd c:/Users/bifor/Documents/Proyectos/B4RRHH/b4rrhh_backend
mvn test -Dtest=UpdateContractServiceTest
```

Expected: Tests pass (nothing changed in Java yet).

- [ ] **Step 6: Commit**

```bash
git add openapi/personnel-administration-api.yaml
git commit -m "feat: add startDate correction to contract and labor classification update schemas; add UpdateWorkingTimeRequest and PUT endpoint"
```

---

## Task 2: Contract Backend — Domain, Command, DTO, Service

**Files:**
- Modify: `src/main/java/com/b4rrhh/employee/contract/domain/model/Contract.java`
- Modify: `src/main/java/com/b4rrhh/employee/contract/application/command/UpdateContractCommand.java`
- Modify: `src/main/java/com/b4rrhh/employee/contract/infrastructure/rest/dto/UpdateContractRequest.java`
- Modify: `src/main/java/com/b4rrhh/employee/contract/infrastructure/rest/ContractController.java`
- Modify: `src/main/java/com/b4rrhh/employee/contract/application/usecase/UpdateContractService.java`
- Test: `src/test/java/com/b4rrhh/employee/contract/application/usecase/UpdateContractServiceTest.java`

- [ ] **Step 1: Write failing tests for the cascade and rejection behaviors**

Add to `src/test/java/com/b4rrhh/employee/contract/application/usecase/UpdateContractServiceTest.java`:

```java
@Test
void whenNewStartDateDiffers_predecessorEndDateIsCascaded() {
    // Arrange
    Contract predecessor = Contract.rehydrate(
            1L, EMPLOYEE_ID, "CTR", "ORD",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
    );
    Contract current = Contract.rehydrate(
            2L, EMPLOYEE_ID, "CTR", "ORD",
            LocalDate.of(2025, 1, 1), null
    );
    when(contractRepository.findByEmployeeIdAndStartDate(EMPLOYEE_ID, LocalDate.of(2025, 1, 1)))
            .thenReturn(Optional.of(current));
    when(contractRepository.findByEmployeeIdOrderByStartDate(EMPLOYEE_ID))
            .thenReturn(List.of(predecessor, current));
    when(contractRepository.existsOverlappingPeriod(any(), any(), any(), any()))
            .thenReturn(false);

    UpdateContractCommand command = new UpdateContractCommand(
            RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
            LocalDate.of(2025, 1, 1),   // path key (old startDate)
            LocalDate.of(2025, 2, 1),   // newStartDate
            "CTR", "ORD"
    );

    // Act
    service.update(command);

    // Assert: predecessor endDate cascaded to newStartDate - 1 = 2025-01-31
    ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
    verify(contractRepository, times(2)).update(captor.capture());
    List<Contract> updated = captor.getAllValues();
    Contract updatedPredecessor = updated.stream()
            .filter(c -> c.getStartDate().equals(LocalDate.of(2024, 1, 1)))
            .findFirst().orElseThrow();
    Contract updatedCurrent = updated.stream()
            .filter(c -> c.getStartDate().equals(LocalDate.of(2025, 2, 1)))
            .findFirst().orElseThrow();
    assertThat(updatedPredecessor.getEndDate()).isEqualTo(LocalDate.of(2025, 1, 31));
    assertThat(updatedCurrent.getStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
}

@Test
void whenNewStartDateDiffers_andNoPredecessor_onlyCurrentIsUpdated() {
    Contract current = Contract.rehydrate(
            1L, EMPLOYEE_ID, "CTR", "ORD",
            LocalDate.of(2025, 1, 1), null
    );
    when(contractRepository.findByEmployeeIdAndStartDate(EMPLOYEE_ID, LocalDate.of(2025, 1, 1)))
            .thenReturn(Optional.of(current));
    when(contractRepository.findByEmployeeIdOrderByStartDate(EMPLOYEE_ID))
            .thenReturn(List.of(current));
    when(contractRepository.existsOverlappingPeriod(any(), any(), any(), any()))
            .thenReturn(false);

    UpdateContractCommand command = new UpdateContractCommand(
            RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 2, 1),
            "CTR", "ORD"
    );

    service.update(command);

    verify(contractRepository, times(1)).update(any());
}
```

Note: these tests use `Contract.rehydrate(id, employeeId, ...)` which does not exist yet — the test will fail to compile.

- [ ] **Step 2: Run to verify compilation failure**

```bash
mvn test -Dtest=UpdateContractServiceTest 2>&1 | tail -20
```

Expected: COMPILATION ERROR — `rehydrate` method not found, `UpdateContractCommand` constructor arity mismatch, `newStartDate` field not found.

- [ ] **Step 3: Add `correctStartDate`, `adjustEndDate`, and `rehydrate` to `Contract`**

Open `src/main/java/com/b4rrhh/employee/contract/domain/model/Contract.java`. Add the following methods (do not change existing ones):

```java
// Rehydrate from persistence — does not enforce business invariants
public static Contract rehydrate(
        Long id,
        Long employeeId,
        String contractCode,
        String contractSubtypeCode,
        LocalDate startDate,
        LocalDate endDate
) {
    return new Contract(id, employeeId, contractCode, contractSubtypeCode, startDate, endDate);
}

// Corrects the startDate — does not check isActive so it can be used on any period
public Contract correctStartDate(LocalDate newStartDate) {
    return new Contract(id, employeeId, contractCode, contractSubtypeCode, newStartDate, endDate);
}

// Adjusts endDate without the isActive guard — used for predecessor cascade
public Contract adjustEndDate(LocalDate newEndDate) {
    return new Contract(id, employeeId, contractCode, contractSubtypeCode, startDate, newEndDate);
}
```

Check `Contract`'s constructor — if it's the canonical record constructor or an all-args constructor, match the parameter order: `(id, employeeId, contractCode, contractSubtypeCode, startDate, endDate)`.

Also verify whether `Contract` already has a `rehydrate` static factory or if the current persistence adapter uses a different approach. If `Contract` is a class (not a record), it has a private constructor you'll call directly.

- [ ] **Step 4: Add `newStartDate` to `UpdateContractCommand`**

Replace `src/main/java/com/b4rrhh/employee/contract/application/command/UpdateContractCommand.java`:

```java
package com.b4rrhh.employee.contract.application.command;

import java.time.LocalDate;

public record UpdateContractCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate startDate,        // path key — identifies the period to correct
        LocalDate newStartDate,     // nullable — null means "keep current startDate"
        String contractCode,
        String contractSubtypeCode
) {
}
```

- [ ] **Step 5: Add nullable `startDate` to `UpdateContractRequest` DTO**

Replace `src/main/java/com/b4rrhh/employee/contract/infrastructure/rest/dto/UpdateContractRequest.java`:

```java
package com.b4rrhh.employee.contract.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record UpdateContractRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        String contractCode,
        String contractSubtypeCode
) {
}
```

- [ ] **Step 6: Pass `newStartDate` through in `ContractController`**

In `ContractController.java`, find the PUT handler. Change the command construction to include `request.startDate()`:

```java
@PutMapping("/{startDate}")
public ResponseEntity<ContractResponse> update(
        @PathVariable String ruleSystemCode,
        @PathVariable String employeeTypeCode,
        @PathVariable String employeeNumber,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestBody UpdateContractRequest request
) {
    Contract updated = updateContractUseCase.update(
            new UpdateContractCommand(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    startDate,
                    request.startDate(),    // NEW — may be null
                    request.contractCode(),
                    request.contractSubtypeCode()
            )
    );
    return ResponseEntity.ok(contractResponseAssembler.toResponse(updated));
}
```

- [ ] **Step 7: Implement cascade branch in `UpdateContractService`**

Replace the `update()` method body in `UpdateContractService.java` (keep the class structure and helper methods unchanged):

```java
@Override
@Transactional
public Contract update(UpdateContractCommand command) {
    String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
    String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
    String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
    LocalDate normalizedStartDate = normalizeStartDate(command.startDate());

    EmployeeContractContext employee = employeeContractLookupPort
            .findByBusinessKeyForUpdate(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber
            )
            .orElseThrow(() -> new ContractEmployeeNotFoundException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber
            ));

    Contract existing = contractRepository
            .findByEmployeeIdAndStartDate(employee.employeeId(), normalizedStartDate)
            .orElseThrow(() -> new ContractNotFoundException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    normalizedStartDate
            ));

    if (!existing.isActive()) {
        throw new ContractAlreadyClosedException(existing.getStartDate());
    }

    String normalizedContractCode = contractCatalogValidator
            .normalizeRequiredCode("contractCode", command.contractCode());
    String normalizedContractSubtypeCode = contractCatalogValidator
            .normalizeRequiredCode("contractSubtypeCode", command.contractSubtypeCode());

    contractCatalogValidator.validateContractCode(
            normalizedRuleSystemCode, normalizedContractCode, existing.getStartDate());
    contractCatalogValidator.validateContractSubtypeCode(
            normalizedRuleSystemCode, normalizedContractSubtypeCode, existing.getStartDate());
    contractSubtypeRelationValidator.validateContractSubtypeRelation(
            normalizedRuleSystemCode, normalizedContractCode, normalizedContractSubtypeCode,
            existing.getStartDate());

    LocalDate effectiveStartDate = (command.newStartDate() != null)
            ? command.newStartDate()
            : normalizedStartDate;

    Contract updated = existing
            .correctStartDate(effectiveStartDate)
            .updateContract(normalizedContractCode, normalizedContractSubtypeCode);

    List<Contract> fullHistory = contractRepository
            .findByEmployeeIdOrderByStartDate(employee.employeeId());

    // Cascade: if startDate changed, find predecessor and adjust its endDate
    Contract cascadedPredecessor = null;
    if (!effectiveStartDate.equals(normalizedStartDate)) {
        LocalDate expectedPredecessorEnd = normalizedStartDate.minusDays(1);
        cascadedPredecessor = fullHistory.stream()
                .filter(c -> expectedPredecessorEnd.equals(c.getEndDate()))
                .map(c -> c.adjustEndDate(effectiveStartDate.minusDays(1)))
                .findFirst()
                .orElse(null);
        if (cascadedPredecessor != null) {
            contractRepository.update(cascadedPredecessor);
        }
    }

    if (contractRepository.existsOverlappingPeriod(
            employee.employeeId(),
            updated.getStartDate(),
            updated.getEndDate(),
            normalizedStartDate  // exclude old startDate
    )) {
        throw new ContractOverlapException(
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber,
                updated.getStartDate(),
                updated.getEndDate()
        );
    }

    contractPresenceCoverageValidator.validatePeriodWithinPresence(
            employee.employeeId(),
            updated.getStartDate(),
            updated.getEndDate(),
            normalizedRuleSystemCode,
            normalizedEmployeeTypeCode,
            normalizedEmployeeNumber
    );

    List<Contract> projectedHistory = buildProjectedHistory(
            fullHistory, cascadedPredecessor, updated, normalizedStartDate);

    contractPresenceCoverageValidator.validateFullCoverage(
            employee.employeeId(),
            projectedHistory,
            normalizedRuleSystemCode,
            normalizedEmployeeTypeCode,
            normalizedEmployeeNumber
    );

    contractRepository.update(updated);
    return updated;
}

private List<Contract> buildProjectedHistory(
        List<Contract> history,
        Contract cascadedPredecessor,
        Contract updated,
        LocalDate oldStartDate
) {
    List<Contract> projected = new ArrayList<>(history.size());
    for (Contract contract : history) {
        if (contract.getStartDate().equals(oldStartDate)) {
            projected.add(updated);
        } else if (cascadedPredecessor != null
                && contract.getStartDate().equals(cascadedPredecessor.getStartDate())) {
            projected.add(cascadedPredecessor);
        } else {
            projected.add(contract);
        }
    }
    return projected;
}
```

Remove the old `replaceByStartDate` method since `buildProjectedHistory` replaces it.

- [ ] **Step 8: Run failing tests**

```bash
mvn test -Dtest=UpdateContractServiceTest
```

Expected: Tests fail with assertion errors (cascade not wired yet if domain methods are missing, or pass if all is wired).

Fix any compilation errors and run again until all tests pass:

```bash
mvn test -Dtest=UpdateContractServiceTest
```

Expected: `BUILD SUCCESS`, all tests pass.

- [ ] **Step 9: Run full test suite**

```bash
mvn test
```

Expected: `BUILD SUCCESS`. If `ContractController` test fails due to command constructor change, fix it by adding the `newStartDate = null` argument.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/contract/ \
        src/test/java/com/b4rrhh/employee/contract/
git commit -m "feat: add startDate correction and predecessor cascade to contract update"
```

---

## Task 3: Labor Classification Backend — Domain, Command, DTO, Service

**Files:**
- Modify: `src/main/java/com/b4rrhh/employee/labor_classification/domain/model/LaborClassification.java`
- Modify: `src/main/java/com/b4rrhh/employee/labor_classification/application/command/UpdateLaborClassificationCommand.java`
- Modify: `src/main/java/com/b4rrhh/employee/labor_classification/infrastructure/rest/dto/UpdateLaborClassificationRequest.java`
- Modify: `src/main/java/com/b4rrhh/employee/labor_classification/infrastructure/rest/LaborClassificationController.java`
- Modify: `src/main/java/com/b4rrhh/employee/labor_classification/application/usecase/UpdateLaborClassificationService.java`
- Test: `src/test/java/com/b4rrhh/employee/labor_classification/application/usecase/UpdateLaborClassificationServiceTest.java`

This task is structurally identical to Task 2. Apply the same changes for the labor classification vertical.

- [ ] **Step 1: Write failing tests**

Add to `src/test/java/com/b4rrhh/employee/labor_classification/application/usecase/UpdateLaborClassificationServiceTest.java`:

```java
@Test
void whenNewStartDateDiffers_predecessorEndDateIsCascaded() {
    LaborClassification predecessor = LaborClassification.rehydrate(
            1L, EMPLOYEE_ID, "AGR", "CAT",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
    );
    LaborClassification current = LaborClassification.rehydrate(
            2L, EMPLOYEE_ID, "AGR", "CAT",
            LocalDate.of(2025, 1, 1), null
    );
    when(laborClassificationRepository.findByEmployeeIdAndStartDate(EMPLOYEE_ID, LocalDate.of(2025, 1, 1)))
            .thenReturn(Optional.of(current));
    when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(EMPLOYEE_ID))
            .thenReturn(List.of(predecessor, current));
    when(laborClassificationRepository.existsOverlappingPeriod(any(), any(), any(), any()))
            .thenReturn(false);

    UpdateLaborClassificationCommand command = new UpdateLaborClassificationCommand(
            RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
            LocalDate.of(2025, 1, 1),   // path key
            LocalDate.of(2025, 2, 1),   // newStartDate
            "AGR", "CAT"
    );

    service.update(command);

    ArgumentCaptor<LaborClassification> captor = ArgumentCaptor.forClass(LaborClassification.class);
    verify(laborClassificationRepository, times(2)).update(captor.capture());
    List<LaborClassification> saved = captor.getAllValues();
    LaborClassification updatedPredecessor = saved.stream()
            .filter(c -> c.getStartDate().equals(LocalDate.of(2024, 1, 1)))
            .findFirst().orElseThrow();
    assertThat(updatedPredecessor.getEndDate()).isEqualTo(LocalDate.of(2025, 1, 31));
}

@Test
void whenNewStartDateDiffers_andNoPredecessor_onlyCurrentIsUpdated() {
    LaborClassification current = LaborClassification.rehydrate(
            1L, EMPLOYEE_ID, "AGR", "CAT",
            LocalDate.of(2025, 1, 1), null
    );
    when(laborClassificationRepository.findByEmployeeIdAndStartDate(EMPLOYEE_ID, LocalDate.of(2025, 1, 1)))
            .thenReturn(Optional.of(current));
    when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(EMPLOYEE_ID))
            .thenReturn(List.of(current));
    when(laborClassificationRepository.existsOverlappingPeriod(any(), any(), any(), any()))
            .thenReturn(false);

    UpdateLaborClassificationCommand command = new UpdateLaborClassificationCommand(
            RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), "AGR", "CAT"
    );

    service.update(command);

    verify(laborClassificationRepository, times(1)).update(any());
}
```

- [ ] **Step 2: Run to verify compilation failure**

```bash
mvn test -Dtest=UpdateLaborClassificationServiceTest 2>&1 | tail -20
```

Expected: COMPILATION ERROR — `rehydrate` and `newStartDate` not yet defined.

- [ ] **Step 3: Add `rehydrate`, `correctStartDate`, `adjustEndDate` to `LaborClassification`**

In `src/main/java/com/b4rrhh/employee/labor_classification/domain/model/LaborClassification.java`:

```java
public static LaborClassification rehydrate(
        Long id,
        Long employeeId,
        String agreementCode,
        String agreementCategoryCode,
        LocalDate startDate,
        LocalDate endDate
) {
    return new LaborClassification(id, employeeId, agreementCode, agreementCategoryCode, startDate, endDate);
}

public LaborClassification correctStartDate(LocalDate newStartDate) {
    return new LaborClassification(id, employeeId, agreementCode, agreementCategoryCode, newStartDate, endDate);
}

public LaborClassification adjustEndDate(LocalDate newEndDate) {
    return new LaborClassification(id, employeeId, agreementCode, agreementCategoryCode, startDate, newEndDate);
}
```

- [ ] **Step 4: Add `newStartDate` to `UpdateLaborClassificationCommand`**

```java
package com.b4rrhh.employee.labor_classification.application.command;

import java.time.LocalDate;

public record UpdateLaborClassificationCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate startDate,
        LocalDate newStartDate,
        String agreementCode,
        String agreementCategoryCode
) {
}
```

- [ ] **Step 5: Add nullable `startDate` to `UpdateLaborClassificationRequest` DTO**

```java
package com.b4rrhh.employee.labor_classification.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record UpdateLaborClassificationRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        String agreementCode,
        String agreementCategoryCode
) {
}
```

- [ ] **Step 6: Pass `newStartDate` through in `LaborClassificationController`**

Find the PUT handler method in `LaborClassificationController.java`. Update the command construction:

```java
new UpdateLaborClassificationCommand(
        ruleSystemCode,
        employeeTypeCode,
        employeeNumber,
        startDate,
        request.startDate(),    // NEW
        request.agreementCode(),
        request.agreementCategoryCode()
)
```

- [ ] **Step 7: Implement cascade branch in `UpdateLaborClassificationService`**

Apply the same cascade pattern as in Task 2's Step 7. The service structure is identical — replace `Contract` with `LaborClassification`, `contractRepository` with `laborClassificationRepository`, `contractCatalogValidator` with `laborClassificationCatalogValidator`, etc.

The cascade branch and `buildProjectedHistory` method are copy-paste equivalent. Ensure `findByEmployeeIdOrderByStartDate` and `update` exist on `LaborClassificationRepository` (they do — same as ContractRepository).

- [ ] **Step 8: Run tests**

```bash
mvn test -Dtest=UpdateLaborClassificationServiceTest
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 9: Run full test suite**

```bash
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/labor_classification/ \
        src/test/java/com/b4rrhh/employee/labor_classification/
git commit -m "feat: add startDate correction and predecessor cascade to labor classification update"
```

---

## Task 4: Working Time Backend — New Update Vertical

**Files:**
- Modify: `src/main/java/com/b4rrhh/employee/working_time/domain/model/WorkingTime.java`
- Modify: `src/main/java/com/b4rrhh/employee/working_time/domain/port/WorkingTimeRepository.java`
- Modify: `src/main/java/com/b4rrhh/employee/working_time/infrastructure/persistence/SpringDataWorkingTimeRepository.java`
- Create: `src/main/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeUseCase.java`
- Create: `src/main/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeCommand.java`
- Create: `src/main/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeService.java`
- Create: `src/main/java/com/b4rrhh/employee/working_time/infrastructure/web/dto/UpdateWorkingTimeRequest.java`
- Modify: `src/main/java/com/b4rrhh/employee/working_time/infrastructure/web/WorkingTimeController.java`
- Create: `src/test/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeServiceTest.java`

- [ ] **Step 1: Write failing test file**

Create `src/test/java/com/b4rrhh/employee/working_time/application/usecase/UpdateWorkingTimeServiceTest.java`:

```java
package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.AgreementAnnualHoursLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.application.service.WorkingTimePresenceConsistencyValidator;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeEmployeeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOverlapException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.port.WorkingTimeRepository;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateWorkingTimeServiceTest {

    private static final Long EMPLOYEE_ID = 1L;
    private static final String RULE_SYSTEM_CODE = "RSC";
    private static final String EMPLOYEE_TYPE_CODE = "ETC";
    private static final String EMPLOYEE_NUMBER = "EMP001";
    private static final BigDecimal ANNUAL_HOURS = BigDecimal.valueOf(1800);
    private static final BigDecimal PERCENTAGE = BigDecimal.valueOf(100);

    private WorkingTimeRepository workingTimeRepository;
    private EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;
    private EmployeeAgreementContextLookupPort agreementContextLookupPort;
    private AgreementAnnualHoursLookupPort agreementAnnualHoursLookupPort;
    private WorkingTimePresenceConsistencyValidator presenceConsistencyValidator;
    private WorkingTimeDerivationPolicy workingTimeDerivationPolicy;
    private UpdateWorkingTimeService service;

    @BeforeEach
    void setUp() {
        workingTimeRepository = mock(WorkingTimeRepository.class);
        employeeWorkingTimeLookupPort = mock(EmployeeWorkingTimeLookupPort.class);
        agreementContextLookupPort = mock(EmployeeAgreementContextLookupPort.class);
        agreementAnnualHoursLookupPort = mock(AgreementAnnualHoursLookupPort.class);
        presenceConsistencyValidator = mock(WorkingTimePresenceConsistencyValidator.class);
        workingTimeDerivationPolicy = mock(WorkingTimeDerivationPolicy.class);

        service = new UpdateWorkingTimeService(
                workingTimeRepository,
                employeeWorkingTimeLookupPort,
                agreementContextLookupPort,
                agreementAnnualHoursLookupPort,
                presenceConsistencyValidator,
                workingTimeDerivationPolicy
        );

        EmployeeWorkingTimeContext employee = mock(EmployeeWorkingTimeContext.class);
        when(employee.employeeId()).thenReturn(EMPLOYEE_ID);
        when(employeeWorkingTimeLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employee));

        EmployeeAgreementContext agreementContext = mock(EmployeeAgreementContext.class);
        when(agreementContext.ruleSystemCode()).thenReturn(RULE_SYSTEM_CODE);
        when(agreementContext.agreementCode()).thenReturn("AGR");
        when(agreementContextLookupPort.resolveContext(any(), any()))
                .thenReturn(agreementContext);
        when(agreementAnnualHoursLookupPort.resolveAnnualHours(any(), any()))
                .thenReturn(ANNUAL_HOURS);

        WorkingTimeDerivedHours derived = new WorkingTimeDerivedHours(
                BigDecimal.valueOf(40), BigDecimal.valueOf(8), BigDecimal.valueOf(150));
        when(workingTimeDerivationPolicy.derive(any(), any())).thenReturn(derived);
    }

    @Test
    void whenStartDateChanges_predecessorEndDateIsCascaded() {
        WorkingTime predecessor = WorkingTime.rehydrate(
                1L, EMPLOYEE_ID, 1,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                PERCENTAGE,
                BigDecimal.valueOf(40), BigDecimal.valueOf(8), BigDecimal.valueOf(150)
        );
        WorkingTime current = WorkingTime.rehydrate(
                2L, EMPLOYEE_ID, 2,
                LocalDate.of(2025, 1, 1), null,
                PERCENTAGE,
                BigDecimal.valueOf(40), BigDecimal.valueOf(8), BigDecimal.valueOf(150)
        );
        when(workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(EMPLOYEE_ID, 2))
                .thenReturn(Optional.of(current));
        when(workingTimeRepository.findByEmployeeIdOrderByStartDate(EMPLOYEE_ID))
                .thenReturn(List.of(predecessor, current));
        when(workingTimeRepository.existsOverlappingPeriodExcluding(any(), any(), any(), any()))
                .thenReturn(false);

        UpdateWorkingTimeCommand command = new UpdateWorkingTimeCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
                2, LocalDate.of(2025, 2, 1), PERCENTAGE
        );

        service.update(command);

        ArgumentCaptor<WorkingTime> captor = ArgumentCaptor.forClass(WorkingTime.class);
        verify(workingTimeRepository, times(2)).save(captor.capture());
        List<WorkingTime> saved = captor.getAllValues();
        WorkingTime updatedPredecessor = saved.stream()
                .filter(w -> w.getWorkingTimeNumber() == 1)
                .findFirst().orElseThrow();
        WorkingTime updatedCurrent = saved.stream()
                .filter(w -> w.getWorkingTimeNumber() == 2)
                .findFirst().orElseThrow();
        assertThat(updatedPredecessor.getEndDate()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(updatedCurrent.getStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    void whenEmployeeNotFound_throwsWorkingTimeEmployeeNotFoundException() {
        when(employeeWorkingTimeLookupPort.findByBusinessKeyForUpdate(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(new UpdateWorkingTimeCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
                1, LocalDate.of(2025, 1, 1), PERCENTAGE)))
                .isInstanceOf(WorkingTimeEmployeeNotFoundException.class);
    }

    @Test
    void whenWorkingTimeNotFound_throwsWorkingTimeNotFoundException() {
        when(workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(EMPLOYEE_ID, 99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(new UpdateWorkingTimeCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
                99, LocalDate.of(2025, 1, 1), PERCENTAGE)))
                .isInstanceOf(WorkingTimeNotFoundException.class);
    }

    @Test
    void whenNewStartDateOverlaps_throwsWorkingTimeOverlapException() {
        WorkingTime current = WorkingTime.rehydrate(
                1L, EMPLOYEE_ID, 1,
                LocalDate.of(2025, 1, 1), null,
                PERCENTAGE,
                BigDecimal.valueOf(40), BigDecimal.valueOf(8), BigDecimal.valueOf(150)
        );
        when(workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(EMPLOYEE_ID, 1))
                .thenReturn(Optional.of(current));
        when(workingTimeRepository.findByEmployeeIdOrderByStartDate(EMPLOYEE_ID))
                .thenReturn(List.of(current));
        when(workingTimeRepository.existsOverlappingPeriodExcluding(any(), any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.update(new UpdateWorkingTimeCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
                1, LocalDate.of(2025, 3, 1), PERCENTAGE)))
                .isInstanceOf(WorkingTimeOverlapException.class);
    }
}
```

- [ ] **Step 2: Run to verify compilation failure**

```bash
mvn test -Dtest=UpdateWorkingTimeServiceTest 2>&1 | tail -20
```

Expected: COMPILATION ERROR — `UpdateWorkingTimeService`, `UpdateWorkingTimeCommand`, `WorkingTime.rehydrate`, `existsOverlappingPeriodExcluding` not found.

- [ ] **Step 3: Add `rehydrate`, `correct`, `adjustEndDate` to `WorkingTime`**

In `src/main/java/com/b4rrhh/employee/working_time/domain/model/WorkingTime.java`, add:

```java
public static WorkingTime rehydrate(
        Long id,
        Long employeeId,
        Integer workingTimeNumber,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal workingTimePercentage,
        BigDecimal weeklyHours,
        BigDecimal dailyHours,
        BigDecimal monthlyHours
) {
    return new WorkingTime(id, employeeId, workingTimeNumber, startDate, endDate,
            workingTimePercentage, weeklyHours, dailyHours, monthlyHours, null, null);
}

public WorkingTime correct(
        LocalDate newStartDate,
        BigDecimal newPercentage,
        WorkingTimeDerivedHours newDerivedHours
) {
    return new WorkingTime(id, employeeId, workingTimeNumber, newStartDate, endDate,
            newPercentage,
            newDerivedHours.weeklyHours(),
            newDerivedHours.dailyHours(),
            newDerivedHours.monthlyHours(),
            createdAt, updatedAt);
}

public WorkingTime adjustEndDate(LocalDate newEndDate) {
    return new WorkingTime(id, employeeId, workingTimeNumber, startDate, newEndDate,
            workingTimePercentage, weeklyHours, dailyHours, monthlyHours, createdAt, updatedAt);
}
```

Match the exact field names from the existing private constructor. If `createdAt`/`updatedAt` are not accessible (private fields), use `null` for both in `rehydrate` and `correct`.

Check whether `WorkingTimeDerivedHours` is a record with `.weeklyHours()`, `.dailyHours()`, `.monthlyHours()` accessors — it is, based on `CreateWorkingTimeService` usage.

- [ ] **Step 4: Add `existsOverlappingPeriodExcluding` to `WorkingTimeRepository`**

In `src/main/java/com/b4rrhh/employee/working_time/domain/port/WorkingTimeRepository.java`, add:

```java
boolean existsOverlappingPeriodExcluding(
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate,
        Integer excludeWorkingTimeNumber
);
```

- [ ] **Step 5: Implement the JPQL query in `SpringDataWorkingTimeRepository`**

In `src/main/java/com/b4rrhh/employee/working_time/infrastructure/persistence/SpringDataWorkingTimeRepository.java`, add:

```java
@Query("""
        select case when count(w) > 0 then true else false end
        from WorkingTimeEntity w
        where w.employeeId = :employeeId
          and w.startDate <= :effectiveEndDate
          and :startDate <= coalesce(w.endDate, :maxDate)
          and w.workingTimeNumber <> :excludeWorkingTimeNumber
        """)
boolean existsOverlappingPeriodExcluding(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("effectiveEndDate") LocalDate effectiveEndDate,
        @Param("maxDate") LocalDate maxDate,
        @Param("excludeWorkingTimeNumber") Integer excludeWorkingTimeNumber
);
```

Note: The repository port method signature above passes `endDate` but the JPQL adapter needs to convert `null` endDate to `maxDate`. The persistence adapter (not the Spring Data interface) handles this mapping. Check `WorkingTimePersistenceAdapter` for how the existing `existsOverlappingPeriod` is called — it likely passes `LocalDate.of(9999, 12, 31)` as `maxDate` when `endDate` is null. The new method in the port must have the same signature convention as the existing one. If the Spring Data repo method takes `effectiveEndDate` and `maxDate` as separate params, update accordingly.

If the repository port uses a different adapter pattern (i.e., `WorkingTimePersistenceAdapter` wraps the Spring Data interface), add the method to both the port interface and the adapter.

- [ ] **Step 6: Create `UpdateWorkingTimeCommand`**

```java
package com.b4rrhh.employee.working_time.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateWorkingTimeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer workingTimeNumber,
        LocalDate startDate,
        BigDecimal workingTimePercentage
) {
}
```

- [ ] **Step 7: Create `UpdateWorkingTimeUseCase`**

```java
package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.domain.model.WorkingTime;

public interface UpdateWorkingTimeUseCase {
    WorkingTime update(UpdateWorkingTimeCommand command);
}
```

- [ ] **Step 8: Create `UpdateWorkingTimeService`**

```java
package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.AgreementAnnualHoursLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.application.service.WorkingTimePresenceConsistencyValidator;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeEmployeeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOverlapException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.port.WorkingTimeRepository;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class UpdateWorkingTimeService implements UpdateWorkingTimeUseCase {

    private final WorkingTimeRepository workingTimeRepository;
    private final EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;
    private final EmployeeAgreementContextLookupPort employeeAgreementContextLookupPort;
    private final AgreementAnnualHoursLookupPort agreementAnnualHoursLookupPort;
    private final WorkingTimePresenceConsistencyValidator workingTimePresenceConsistencyValidator;
    private final WorkingTimeDerivationPolicy workingTimeDerivationPolicy;

    public UpdateWorkingTimeService(
            WorkingTimeRepository workingTimeRepository,
            EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort,
            EmployeeAgreementContextLookupPort employeeAgreementContextLookupPort,
            AgreementAnnualHoursLookupPort agreementAnnualHoursLookupPort,
            WorkingTimePresenceConsistencyValidator workingTimePresenceConsistencyValidator,
            WorkingTimeDerivationPolicy workingTimeDerivationPolicy
    ) {
        this.workingTimeRepository = workingTimeRepository;
        this.employeeWorkingTimeLookupPort = employeeWorkingTimeLookupPort;
        this.employeeAgreementContextLookupPort = employeeAgreementContextLookupPort;
        this.agreementAnnualHoursLookupPort = agreementAnnualHoursLookupPort;
        this.workingTimePresenceConsistencyValidator = workingTimePresenceConsistencyValidator;
        this.workingTimeDerivationPolicy = workingTimeDerivationPolicy;
    }

    @Override
    @Transactional
    public WorkingTime update(UpdateWorkingTimeCommand command) {
        String normalizedRuleSystemCode = normalize("ruleSystemCode", command.ruleSystemCode()).toUpperCase();
        String normalizedEmployeeTypeCode = normalize("employeeTypeCode", command.employeeTypeCode()).toUpperCase();
        String normalizedEmployeeNumber = normalize("employeeNumber", command.employeeNumber());
        Integer normalizedWorkingTimeNumber = normalizePositive(command.workingTimeNumber());

        EmployeeWorkingTimeContext employee = employeeWorkingTimeLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new WorkingTimeEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        WorkingTime existing = workingTimeRepository
                .findByEmployeeIdAndWorkingTimeNumber(
                        employee.employeeId(), normalizedWorkingTimeNumber)
                .orElseThrow(() -> new WorkingTimeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedWorkingTimeNumber
                ));

        // Resolve derived hours for the (possibly new) percentage at the (possibly new) startDate
        EmployeeAgreementContext agreementContext = employeeAgreementContextLookupPort
                .resolveContext(employee.employeeId(), command.startDate());
        BigDecimal annualHours = agreementAnnualHoursLookupPort
                .resolveAnnualHours(agreementContext.ruleSystemCode(), agreementContext.agreementCode());
        WorkingTimeDerivedHours derivedHours = workingTimeDerivationPolicy
                .derive(command.workingTimePercentage(), annualHours);

        WorkingTime corrected = existing.correct(
                command.startDate(),
                command.workingTimePercentage(),
                derivedHours
        );

        List<WorkingTime> fullHistory = workingTimeRepository
                .findByEmployeeIdOrderByStartDate(employee.employeeId());

        // Cascade: if startDate changed, find predecessor and adjust its endDate
        WorkingTime cascadedPredecessor = null;
        if (!command.startDate().equals(existing.getStartDate())) {
            LocalDate expectedPredecessorEnd = existing.getStartDate().minusDays(1);
            cascadedPredecessor = fullHistory.stream()
                    .filter(w -> expectedPredecessorEnd.equals(w.getEndDate()))
                    .map(w -> w.adjustEndDate(command.startDate().minusDays(1)))
                    .findFirst()
                    .orElse(null);
            if (cascadedPredecessor != null) {
                workingTimeRepository.save(cascadedPredecessor);
            }
        }

        if (workingTimeRepository.existsOverlappingPeriodExcluding(
                employee.employeeId(),
                corrected.getStartDate(),
                corrected.getEndDate(),
                normalizedWorkingTimeNumber
        )) {
            throw new WorkingTimeOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    corrected.getStartDate(),
                    corrected.getEndDate()
            );
        }

        workingTimePresenceConsistencyValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                corrected.getStartDate(),
                corrected.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        return workingTimeRepository.save(corrected);
    }

    private String normalize(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private Integer normalizePositive(Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("workingTimeNumber must be a positive integer");
        }
        return value;
    }
}
```

- [ ] **Step 9: Create `UpdateWorkingTimeRequest` DTO**

```java
package com.b4rrhh.employee.working_time.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateWorkingTimeRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        BigDecimal workingTimePercentage
) {
}
```

- [ ] **Step 10: Add `PUT /{workingTimeNumber}` to `WorkingTimeController`**

Add the following imports at the top of `WorkingTimeController.java`:

```java
import com.b4rrhh.employee.working_time.application.usecase.UpdateWorkingTimeCommand;
import com.b4rrhh.employee.working_time.application.usecase.UpdateWorkingTimeUseCase;
import com.b4rrhh.employee.working_time.infrastructure.web.dto.UpdateWorkingTimeRequest;
import org.springframework.web.bind.annotation.PutMapping;
```

Add the field to the constructor:

```java
private final UpdateWorkingTimeUseCase updateWorkingTimeUseCase;

public WorkingTimeController(
        CreateWorkingTimeUseCase createWorkingTimeUseCase,
        ListEmployeeWorkingTimesUseCase listEmployeeWorkingTimesUseCase,
        GetWorkingTimeByBusinessKeyUseCase getWorkingTimeByBusinessKeyUseCase,
        CloseWorkingTimeUseCase closeWorkingTimeUseCase,
        UpdateWorkingTimeUseCase updateWorkingTimeUseCase,       // NEW
        WorkingTimeResponseAssembler workingTimeResponseAssembler
) {
    this.createWorkingTimeUseCase = createWorkingTimeUseCase;
    this.listEmployeeWorkingTimesUseCase = listEmployeeWorkingTimesUseCase;
    this.getWorkingTimeByBusinessKeyUseCase = getWorkingTimeByBusinessKeyUseCase;
    this.closeWorkingTimeUseCase = closeWorkingTimeUseCase;
    this.updateWorkingTimeUseCase = updateWorkingTimeUseCase;   // NEW
    this.workingTimeResponseAssembler = workingTimeResponseAssembler;
}
```

Add the endpoint method before the closing brace:

```java
@PutMapping("/{workingTimeNumber}")
public ResponseEntity<WorkingTimeResponse> update(
        @PathVariable String ruleSystemCode,
        @PathVariable String employeeTypeCode,
        @PathVariable String employeeNumber,
        @PathVariable Integer workingTimeNumber,
        @RequestBody UpdateWorkingTimeRequest request
) {
    WorkingTime updated = updateWorkingTimeUseCase.update(
            new UpdateWorkingTimeCommand(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    workingTimeNumber,
                    request.startDate(),
                    request.workingTimePercentage()
            )
    );
    return ResponseEntity.ok(workingTimeResponseAssembler.toResponse(updated));
}
```

- [ ] **Step 11: Run tests**

```bash
mvn test -Dtest=UpdateWorkingTimeServiceTest
```

Expected: `BUILD SUCCESS`, all tests pass.

If `existsOverlappingPeriodExcluding` fails because the persistence adapter doesn't route to the Spring Data method, check `WorkingTimePersistenceAdapter.java` and add the delegation there.

- [ ] **Step 12: Run full test suite**

```bash
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 13: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/working_time/ \
        src/test/java/com/b4rrhh/employee/working_time/
git commit -m "feat: add UpdateWorkingTimeService with startDate correction and predecessor cascade"
```

---

## Task 5: Frontend — Regenerate API Client

**Context:** The frontend lives in `.worktrees/labor-redesign`. After the backend OpenAPI changes in Task 1, the generated client must be regenerated before the Angular code can reference `UpdateWorkingTimeRequest`.

- [ ] **Step 1: Run api:refresh**

```bash
cd c:/Users/bifor/Documents/Proyectos/B4RRHH/b4rrhh_frontend/.worktrees/labor-redesign
npm run api:refresh
```

Expected: Prints "Done" or equivalent. No error.

- [ ] **Step 2: Verify new generated types exist**

```bash
grep -l "updateWorkingTimeByBusinessKey\|UpdateWorkingTimeRequest" src/app/core/api/generated/**/*
```

Expected: At least one `.ts` file listed (the generated service and/or model file).

- [ ] **Step 3: Verify TypeScript compiles**

```bash
npx tsc --noEmit
```

Expected: No errors.

- [ ] **Step 4: Commit**

```bash
git add src/app/core/api/generated/
git commit -m "chore: regenerate API client with UpdateWorkingTimeRequest and startDate fields"
```

---

## Task 6: Contract Frontend — Start-Date Field and Cascade Warning

**Context:** The contract section component already has a `'create' | 'edit' | 'close'` modal mode. The `correctOccurrence` gateway call already exists. Only the mapper, component signals, and HTML template need updating.

**Files (all relative to `.worktrees/labor-redesign`):**
- Modify: `src/app/features/employee/data-access/employee-contract.mapper.ts`
- Modify: `src/app/features/employee/presence/components/employee-contract-section.component.ts`
- Modify: `src/app/features/employee/presence/components/employee-contract-section.component.html`

- [ ] **Step 1: Extend `ContractCorrectDraft` and mapper in `employee-contract.mapper.ts`**

Find `ContractCorrectDraft` and `mapContractCorrectDraftToRequest`. Replace with:

```typescript
export interface ContractCorrectDraft {
  startDate?: string;       // optional — null/undefined means "keep current"
  contractCode: string;
  contractSubtypeCode: string;
}

export function mapContractCorrectDraftToRequest(
  draft: ContractCorrectDraft,
): UpdateContractRequest {
  return {
    startDate: draft.startDate ? draft.startDate : null,
    contractCode: draft.contractCode.trim().toUpperCase(),
    contractSubtypeCode: draft.contractSubtypeCode.trim().toUpperCase(),
  };
}
```

Also update `createEmptyContractCorrectDraft()` if it exists:

```typescript
export function createEmptyContractCorrectDraft(): ContractCorrectDraft {
  return { startDate: undefined, contractCode: '', contractSubtypeCode: '' };
}
```

- [ ] **Step 2: Add `startDateDraft`, `editingOriginalStartDate`, `showCascadeWarning` signals to `employee-contract-section.component.ts`**

Add these three signals alongside the existing ones (after `endDateDraft`):

```typescript
protected readonly startDateDraft = signal('');
protected readonly editingOriginalStartDate = signal<string | null>(null);
protected readonly showCascadeWarning = computed(
  () => this.modalMode() === 'edit'
    && !!this.startDateDraft()
    && this.startDateDraft() !== this.editingOriginalStartDate(),
);
```

- [ ] **Step 3: Initialize `startDateDraft` in `openEdit()`**

In `openEdit(index: number)`, after `this.editingStartDate.set(row.startDate)`, add:

```typescript
this.startDateDraft.set(row.startDate);
this.editingOriginalStartDate.set(row.startDate);
```

- [ ] **Step 4: Include `startDate` in the draft passed to `correctOccurrence` in `submit()`**

In the `mode === 'edit'` branch of `submit()`, change:

```typescript
this.contractStore.correctOccurrence(key, this.editingStartDate()!, {
  startDate: this.startDateDraft() !== this.editingOriginalStartDate()
    ? this.startDateDraft()
    : undefined,
  contractCode: this.contractCodeDraft(),
  contractSubtypeCode: this.contractSubtypeCodeDraft(),
});
```

- [ ] **Step 5: Add date input and cascade warning to the HTML template**

Open `employee-contract-section.component.html`. In the `@if (modalMode() === 'edit')` block, add `app-ui-date-input` **before** the contract type selector:

```html
<app-ui-date-input
  label="Fecha de inicio"
  [value]="startDateDraft()"
  (valueChanged)="startDateDraft.set($event)" />

@if (showCascadeWarning()) {
  <p class="contract-section__cascade-hint">
    El período anterior se ajustará automáticamente.
  </p>
}
```

- [ ] **Step 6: Add the CSS class**

In `employee-contract-section.component.scss` (or `.css`), add:

```scss
.contract-section__cascade-hint {
  font-size: 12px;
  color: #713f12;
  background: #fefce8;
  border: 1px solid #fde047;
  border-radius: 6px;
  padding: 8px 12px;
  margin: 0;
}
```

- [ ] **Step 7: Verify TypeScript compiles**

```bash
npx tsc --noEmit
```

Expected: No errors.

- [ ] **Step 8: Run tests**

```bash
npm run test -- --run
```

Expected: All tests pass (existing contract tests still pass).

- [ ] **Step 9: Commit**

```bash
git add src/app/features/employee/data-access/employee-contract.mapper.ts \
        src/app/features/employee/presence/components/employee-contract-section.component.ts \
        src/app/features/employee/presence/components/employee-contract-section.component.html \
        src/app/features/employee/presence/components/employee-contract-section.component.scss
git commit -m "feat: add startDate correction and cascade warning to contract edit modal"
```

---

## Task 7: Labor Classification Frontend — Start-Date Field and Cascade Warning

**Context:** Identical pattern to Task 6. Apply the same changes for the labor classification vertical.

**Files (all relative to `.worktrees/labor-redesign`):**
- Modify: `src/app/features/employee/data-access/employee-labor-classification.mapper.ts`
- Modify: `src/app/features/employee/presence/components/employee-labor-classification-section.component.ts`
- Modify: `src/app/features/employee/presence/components/employee-labor-classification-section.component.html`

- [ ] **Step 1: Extend `LaborClassificationCorrectDraft` and mapper**

Find `LaborClassificationCorrectDraft` (or equivalent — check the mapper file for the existing edit/correct draft type name). Add `startDate?: string` and update the mapper function:

```typescript
export interface LaborClassificationCorrectDraft {
  startDate?: string;
  agreementCode: string;
  agreementCategoryCode: string;
}

export function mapLaborClassificationCorrectDraftToRequest(
  draft: LaborClassificationCorrectDraft,
): UpdateLaborClassificationRequest {
  return {
    startDate: draft.startDate ? draft.startDate : null,
    agreementCode: draft.agreementCode.trim().toUpperCase(),
    agreementCategoryCode: draft.agreementCategoryCode.trim().toUpperCase(),
  };
}
```

- [ ] **Step 2: Add signals to the labor classification section component**

Add these signals (same pattern as Task 6, Step 2):

```typescript
protected readonly startDateDraft = signal('');
protected readonly editingOriginalStartDate = signal<string | null>(null);
protected readonly showCascadeWarning = computed(
  () => this.modalMode() === 'edit'
    && !!this.startDateDraft()
    && this.startDateDraft() !== this.editingOriginalStartDate(),
);
```

- [ ] **Step 3: Initialize in `openEdit()` and update `submit()`**

Same as Task 6 Steps 3–4, substituting labor classification field names (`agreementCode`, `agreementCategoryCode`).

- [ ] **Step 4: Add date input and cascade warning to the HTML template**

Same structure as Task 6 Step 5. Use class `.labor-classification-section__cascade-hint` for the paragraph.

- [ ] **Step 5: Add the CSS class to the labor classification SCSS file**

Same as Task 6 Step 6, using the labor classification class name.

- [ ] **Step 6: Verify TypeScript compiles and tests pass**

```bash
npx tsc --noEmit
npm run test -- --run
```

Expected: No errors, all tests pass.

- [ ] **Step 7: Commit**

```bash
git add src/app/features/employee/data-access/employee-labor-classification.mapper.ts \
        src/app/features/employee/presence/components/employee-labor-classification-section.component.ts \
        src/app/features/employee/presence/components/employee-labor-classification-section.component.html \
        src/app/features/employee/presence/components/employee-labor-classification-section.component.scss
git commit -m "feat: add startDate correction and cascade warning to labor classification edit modal"
```

---

## Task 8: Working Time Frontend — Full Edit Mode

**Context:** Unlike contract and labor classification (which have existing `'edit'` modal modes), working time currently has only `'create'` and `'close'` modes. The `openEdit()` method currently opens the `'close'` modal — this must change. A new `'edit'` mode is added that shows `startDate` and `percentage` inputs with a cascade warning.

**Files (all relative to `.worktrees/labor-redesign`):**
- Modify: `src/app/features/employee/data-access/employee-working-time.mapper.ts`
- Modify: `src/app/core/api/clients/employee-working-time-read.client.ts`
- Modify: `src/app/features/employee/data-access/employee-working-time.gateway.ts`
- Modify: `src/app/features/employee/data-access/employee-working-time.store.ts`
- Modify: `src/app/features/employee/presence/components/employee-working-time-section.component.ts`
- Modify: `src/app/features/employee/presence/components/employee-working-time-section.component.html`

- [ ] **Step 1: Add `WorkingTimeCorrectDraft` and mapper to `employee-working-time.mapper.ts`**

```typescript
export interface WorkingTimeCorrectDraft {
  startDate: string;
  workingTimePercentage: number;
}

export function mapWorkingTimeCorrectDraftToRequest(
  draft: WorkingTimeCorrectDraft,
): UpdateWorkingTimeRequest {
  return {
    startDate: draft.startDate,
    workingTimePercentage: draft.workingTimePercentage,
  };
}
```

Add `UpdateWorkingTimeRequest` to the imports from `'../../../core/api/generated/model/models'`.

- [ ] **Step 2: Add `updateWorkingTimeByBusinessKey` to `EmployeeWorkingTimeReadClient`**

In `src/app/core/api/clients/employee-working-time-read.client.ts`, add a method that calls the generated `updateWorkingTimeByBusinessKey`:

```typescript
updateWorkingTimeByBusinessKey(
  key: EmployeeBusinessKey,
  workingTimeNumber: number,
  request: UpdateWorkingTimeRequest,
): Observable<WorkingTimeResponse> {
  return this.employeeWorkingTimeService.updateWorkingTimeByBusinessKey(
    key.ruleSystemCode,
    key.employeeTypeCode,
    key.employeeNumber,
    workingTimeNumber,
    request,
  );
}
```

Import `UpdateWorkingTimeRequest` from the generated models.

- [ ] **Step 3: Add `correctEmployeeWorkingTime` to `EmployeeWorkingTimeGateway`**

In `src/app/features/employee/data-access/employee-working-time.gateway.ts`, add:

```typescript
correctEmployeeWorkingTime(
  key: EmployeeBusinessKey,
  workingTimeNumber: number,
  draft: WorkingTimeCorrectDraft,
): Observable<void> {
  return this.employeeWorkingTimeReadClient
    .updateWorkingTimeByBusinessKey(key, workingTimeNumber, mapWorkingTimeCorrectDraftToRequest(draft))
    .pipe(map(() => undefined));
}
```

Import `WorkingTimeCorrectDraft`, `mapWorkingTimeCorrectDraftToRequest` from the mapper.

- [ ] **Step 4: Add `correctWorkingTime` to `EmployeeWorkingTimeStore`**

First, extend the `successState` type:

```typescript
private readonly successState = signal<'created' | 'closed' | 'corrected' | null>(null);
```

Update the `readonly success` accessor type accordingly:

```typescript
readonly success: Signal<'created' | 'closed' | 'corrected' | null> = this.successState.asReadonly();
```

Also update the import in the store to include `WorkingTimeCorrectDraft`:

```typescript
import { WorkingTimeCreateDraft, WorkingTimeCloseDraft, WorkingTimeCorrectDraft } from './employee-working-time.mapper';
```

Add the method:

```typescript
correctWorkingTime(
  employeeKey: EmployeeBusinessKey,
  workingTimeNumber: number,
  draft: WorkingTimeCorrectDraft,
): void {
  if (this.mutatingState()) {
    return;
  }

  const normalizedEmployeeKey = toEmployeeBusinessKey(employeeKey);

  this.mutatingState.set(true);
  this.errorState.set(null);
  this.successState.set(null);

  this.employeeWorkingTimeGateway
    .correctEmployeeWorkingTime(normalizedEmployeeKey, workingTimeNumber, draft)
    .pipe(take(1))
    .subscribe({
      next: () => {
        this.mutatingState.set(false);
        this.successState.set('corrected');
        this.loadWorkingTimesByBusinessKeyInternal(normalizedEmployeeKey, true);
      },
      error: (error) => {
        this.mutatingState.set(false);
        this.errorState.set(mapEmployeeWorkingTimeErrorCode(error));
      },
    });
}
```

- [ ] **Step 5: Update `employee-working-time-section.component.ts`**

Add `'edit'` to the modal mode type and add required signals. Replace the entire component class with the following updated version:

```typescript
type WorkingTimeModalMode = 'create' | 'edit' | 'close';

// ... (keep existing imports and interface WorkingTimePeriodRow unchanged)

export class EmployeeWorkingTimeSectionComponent {
  readonly employeeBusinessKey = input<EmployeeBusinessKey | null>(null);

  private readonly workingTimeStore = inject(EmployeeWorkingTimeStore);

  protected readonly modalVisible = signal(false);
  protected readonly modalMode = signal<WorkingTimeModalMode>('create');
  protected readonly editingNumber = signal<number | null>(null);
  protected readonly startDateDraft = signal(currentLocalDate());
  protected readonly editingOriginalStartDate = signal<string | null>(null);   // NEW
  protected readonly percentageDraft = signal(100);
  protected readonly endDateDraft = signal('');

  protected readonly showCascadeWarning = computed(                            // NEW
    () => this.modalMode() === 'edit'
      && !!this.startDateDraft()
      && this.startDateDraft() !== this.editingOriginalStartDate(),
  );

  protected readonly texts = employeeTexts;

  protected readonly rows = computed<ReadonlyArray<WorkingTimePeriodRow>>(() =>
    this.workingTimeStore.workingTimes().map((wt: EmployeeWorkingTimeModel) => ({
      startDate: wt.startDate,
      endDate: wt.endDate,
      isActive: wt.isActive,
      canEdit: wt.isActive,
      canDelete: false,
      workingTimeNumber: wt.workingTimeNumber,
      workingTimePercentage: wt.workingTimePercentage,
      weeklyHours: wt.weeklyHours,
      dailyHours: wt.dailyHours,
    })),
  );

  protected readonly saving = computed(() => this.workingTimeStore.mutating());

  protected readonly modalTitle = computed(() => {
    if (this.modalMode() === 'create') return 'Nueva jornada';
    if (this.modalMode() === 'edit') return 'Editar jornada';
    return 'Cerrar período — Jornada';
  });

  protected readonly isSubmitEnabled = computed(() => {
    const mode = this.modalMode();
    if (mode === 'create') return !!this.startDateDraft();
    if (mode === 'edit') return !!this.startDateDraft();
    return !!this.endDateDraft();
  });

  constructor() {
    effect(() => {
      const key = this.employeeBusinessKey();
      untracked(() => this.workingTimeStore.loadWorkingTimesByBusinessKey(key));
    });

    effect(() => {
      const success = this.workingTimeStore.success();
      if (success)
        untracked(() => {
          if (this.modalVisible()) this.closeModal();
        });
    });
  }

  protected openCreate(): void {
    this.workingTimeStore.clearFeedback();
    this.modalMode.set('create');
    this.startDateDraft.set(currentLocalDate());
    this.percentageDraft.set(100);
    this.modalVisible.set(true);
  }

  protected openEdit(index: number): void {
    const row = this.rows()[index];
    if (!row || !row.isActive) return;
    this.workingTimeStore.clearFeedback();
    this.modalMode.set('edit');                              // was 'close'
    this.editingNumber.set(row.workingTimeNumber);
    this.startDateDraft.set(row.startDate);                  // NEW
    this.editingOriginalStartDate.set(row.startDate);        // NEW
    this.percentageDraft.set(row.workingTimePercentage);     // NEW
    this.modalVisible.set(true);
  }

  protected switchToClose(): void {                          // NEW — replaces direct close from edit btn
    this.modalMode.set('close');
    this.endDateDraft.set(currentLocalDate());
  }

  protected submit(): void {
    const key = this.employeeBusinessKey();
    if (!key || this.workingTimeStore.mutating()) return;

    if (this.modalMode() === 'create') {
      this.workingTimeStore.createWorkingTime(key, {
        startDate: this.startDateDraft(),
        workingTimePercentage: this.percentageDraft(),
      });
    } else if (this.modalMode() === 'edit') {
      this.workingTimeStore.correctWorkingTime(key, this.editingNumber()!, {
        startDate: this.startDateDraft(),
        workingTimePercentage: this.percentageDraft(),
      });
    } else {
      this.workingTimeStore.closeWorkingTime(key, this.editingNumber()!, {
        endDate: this.endDateDraft(),
      });
    }
  }

  protected closeModal(): void {
    this.modalVisible.set(false);
    this.workingTimeStore.clearFeedback();
  }
}
```

- [ ] **Step 6: Update `employee-working-time-section.component.html`**

Add an `@if (modalMode() === 'edit')` block. The template will have three conditional blocks: create, edit, close. The edit block:

```html
@if (modalMode() === 'edit') {
  <app-ui-date-input
    label="Fecha de inicio"
    [value]="startDateDraft()"
    (valueChanged)="startDateDraft.set($event)" />

  <app-ui-input-number
    label="Porcentaje de jornada"
    [value]="percentageDraft()"
    (valueChanged)="percentageDraft.set($event)" />

  @if (showCascadeWarning()) {
    <p class="working-time-section__cascade-hint">
      El período anterior se ajustará automáticamente.
    </p>
  }

  <button type="button" class="working-time-section__close-link" (click)="switchToClose()">
    Cerrar período
  </button>
}
```

Also ensure the `@if (modalMode() === 'close')` block still exists and renders the close form (endDate input).

- [ ] **Step 7: Add CSS**

In `employee-working-time-section.component.scss`:

```scss
.working-time-section__cascade-hint {
  font-size: 12px;
  color: #713f12;
  background: #fefce8;
  border: 1px solid #fde047;
  border-radius: 6px;
  padding: 8px 12px;
  margin: 0;
}

.working-time-section__close-link {
  background: none;
  border: none;
  color: #dc2626;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
}
```

- [ ] **Step 8: Verify TypeScript compiles**

```bash
npx tsc --noEmit
```

Expected: No errors.

- [ ] **Step 9: Run tests**

```bash
npm run test -- --run
```

Expected: All tests pass. Existing working time component tests that check `openEdit → mode === 'close'` will need updating — they should now expect `mode === 'edit'`. Fix those tests:

In `employee-working-time-section.component.spec.ts`, find tests asserting `modalMode() === 'close'` after `openEdit()` and change to `'edit'`. Also update tests that check `closeWorkingTime` is called on submit from `openEdit` — they should now assert `correctWorkingTime` is called. Add `correctWorkingTime: vi.fn()` to the `MockWorkingTimeStore`.

- [ ] **Step 10: Commit**

```bash
git add src/app/features/employee/data-access/employee-working-time.mapper.ts \
        src/app/core/api/clients/employee-working-time-read.client.ts \
        src/app/features/employee/data-access/employee-working-time.gateway.ts \
        src/app/features/employee/data-access/employee-working-time.store.ts \
        src/app/features/employee/presence/components/employee-working-time-section.component.ts \
        src/app/features/employee/presence/components/employee-working-time-section.component.html \
        src/app/features/employee/presence/components/employee-working-time-section.component.scss \
        src/app/features/employee/presence/components/employee-working-time-section.component.spec.ts
git commit -m "feat: add edit mode to working time section with startDate correction and predecessor cascade"
```

---

## Self-Review Checklist

After all tasks, verify:

- [ ] Backend: `mvn test` passes clean with 0 failures
- [ ] Frontend: `npm run test -- --run` passes clean with 0 failures
- [ ] `npx tsc --noEmit` passes in the labor-redesign worktree
- [ ] OpenAPI spec: `UpdateContractRequest` has optional `startDate`; `UpdateLaborClassificationRequest` has optional `startDate`; `UpdateWorkingTimeRequest` schema exists; `PUT /{workingTimeNumber}` endpoint for working times exists
- [ ] Cascade behavior verified: correcting `startDate` from `2025-01-01` to `2025-02-01` updates predecessor's `endDate` from `2024-12-31` to `2025-01-31`
- [ ] No cascade when there is no predecessor (first period)
- [ ] Overlap rejection works: moving a period to a date already covered by another period returns 409
- [ ] Working time `openEdit` now opens `'edit'` mode (not `'close'`)
- [ ] Cascade warning is visible in the UI when `startDate` is changed in the edit modal
- [ ] Cascade warning is NOT visible when `startDate` is unchanged
