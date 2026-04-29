# Employee Payroll Input Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow HR to register per-employee, per-period quantity inputs (e.g. overtime hours) that the payroll engine consumes at calculation time via a new `EMPLOYEE_INPUT` calculation type.

**Architecture:** New `employee.payroll_input` vertical with standard hexagonal CRUD. A new `EmployeePayrollInputLookupPort` in the `payroll` context allows the `CalculatePayrollUnitService` to load inputs before the execution loop and resolve `EMPLOYEE_INPUT` concepts as a map lookup. `SegmentCalculationContext` gains an `employeeInputs` map so the PoC executor path also supports the new type.

**Tech Stack:** Java 21, Spring Boot, Spring Data JPA, Flyway, JUnit 5, Mockito, React/TypeScript (designer minor update).

**Repos involved:**
- `b4rrhh_backend` — all backend tasks
- `b4rrhh_designer` — Task 10 only

**Reference patterns:**
- Employee vertical pattern: `com.b4rrhh.employee.working_time`
- Payroll lookup port pattern: `com.b4rrhh.payroll.application.port.PayrollEmployeePresenceLookupPort`
- OpenAPI pattern: see existing `/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/working-times` block

---

## File Map

**New — employee.payroll_input vertical:**
```
src/main/java/com/b4rrhh/employee/payroll_input/
  domain/model/EmployeePayrollInput.java
  domain/port/EmployeePayrollInputRepository.java
  domain/exception/EmployeePayrollInputAlreadyExistsException.java
  domain/exception/EmployeePayrollInputNotFoundException.java
  application/usecase/CreateEmployeePayrollInputUseCase.java
  application/usecase/CreateEmployeePayrollInputCommand.java
  application/usecase/CreateEmployeePayrollInputService.java
  application/usecase/UpdateEmployeePayrollInputUseCase.java
  application/usecase/UpdateEmployeePayrollInputCommand.java
  application/usecase/UpdateEmployeePayrollInputService.java
  application/usecase/DeleteEmployeePayrollInputUseCase.java
  application/usecase/DeleteEmployeePayrollInputCommand.java
  application/usecase/DeleteEmployeePayrollInputService.java
  application/usecase/ListEmployeePayrollInputsUseCase.java
  application/usecase/ListEmployeePayrollInputsCommand.java
  application/usecase/ListEmployeePayrollInputsService.java
  infrastructure/persistence/EmployeePayrollInputEntity.java
  infrastructure/persistence/SpringDataEmployeePayrollInputRepository.java
  infrastructure/persistence/EmployeePayrollInputPersistenceAdapter.java
  infrastructure/web/EmployeePayrollInputController.java
  infrastructure/web/dto/CreateEmployeePayrollInputRequest.java
  infrastructure/web/dto/UpdateEmployeePayrollInputRequest.java
  infrastructure/web/dto/EmployeePayrollInputResponse.java
  infrastructure/web/dto/EmployeePayrollInputsResponse.java
  infrastructure/web/dto/EmployeePayrollInputErrorResponse.java
  infrastructure/web/assembler/EmployeePayrollInputResponseAssembler.java
  infrastructure/web/EmployeePayrollInputExceptionHandler.java
```

**New — payroll lookup integration:**
```
src/main/java/com/b4rrhh/payroll/application/port/EmployeePayrollInputLookupPort.java
src/main/java/com/b4rrhh/payroll/infrastructure/persistence/EmployeePayrollInputLookupAdapter.java
```

**New — Flyway migration:**
```
src/main/resources/db/migration/V79__create_employee_payroll_input_table.sql
```

**New — tests:**
```
src/test/java/com/b4rrhh/employee/payroll_input/application/usecase/CreateEmployeePayrollInputServiceTest.java
src/test/java/com/b4rrhh/employee/payroll_input/application/usecase/UpdateEmployeePayrollInputServiceTest.java
src/test/java/com/b4rrhh/employee/payroll_input/application/usecase/DeleteEmployeePayrollInputServiceTest.java
src/test/java/com/b4rrhh/payroll_engine/execution/application/service/DefaultSegmentExecutionEngineEmployeeInputTest.java
```

**Modified:**
```
src/main/java/com/b4rrhh/payroll_engine/concept/domain/model/CalculationType.java
src/main/java/com/b4rrhh/payroll_engine/segment/domain/model/SegmentCalculationContext.java
src/main/java/com/b4rrhh/payroll_engine/execution/application/service/DefaultSegmentExecutionEngine.java
src/main/java/com/b4rrhh/payroll_engine/execution/application/service/DefaultPayrollEnginePocExecutor.java
src/main/java/com/b4rrhh/payroll_engine/planning/application/service/DefaultEligiblePayrollExecutor.java
src/main/java/com/b4rrhh/payroll/application/usecase/CalculatePayrollUnitService.java
openapi/personnel-administration-api.yaml
b4rrhh_designer/src/app/canvas/types.ts
```

---

## Task 1: OpenAPI Contract

Per CLAUDE.md, all API changes start with the OpenAPI spec.

**Files:**
- Modify: `openapi/personnel-administration-api.yaml`

- [ ] **Step 1: Add payroll-inputs endpoints to the OpenAPI spec**

Find the block for `/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/working-times` (around line 1246) and add the following block **after** all working-time and cost-center entries, before the `components:` section:

```yaml
  /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs:
    post:
      operationId: createEmployeePayrollInputByBusinessKey
      summary: Register a payroll input quantity for an employee and period
      tags:
        - Employee Payroll Input
      parameters:
        - in: path
          name: ruleSystemCode
          required: true
          schema:
            type: string
            maxLength: 10
        - in: path
          name: employeeTypeCode
          required: true
          schema:
            type: string
            maxLength: 10
        - in: path
          name: employeeNumber
          required: true
          schema:
            type: string
            maxLength: 20
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/CreateEmployeePayrollInputRequest"
      responses:
        "201":
          description: Payroll input created
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/EmployeePayrollInputResponse"
        "400":
          description: Invalid request
        "409":
          description: Payroll input already exists for this concept and period
    get:
      operationId: listEmployeePayrollInputsByBusinessKey
      summary: List payroll inputs for an employee and period
      tags:
        - Employee Payroll Input
      parameters:
        - in: path
          name: ruleSystemCode
          required: true
          schema:
            type: string
            maxLength: 10
        - in: path
          name: employeeTypeCode
          required: true
          schema:
            type: string
            maxLength: 10
        - in: path
          name: employeeNumber
          required: true
          schema:
            type: string
            maxLength: 20
        - in: query
          name: period
          required: true
          schema:
            type: integer
            description: "Format yyyyMM, e.g. 202604"
      responses:
        "200":
          description: Payroll inputs list
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/EmployeePayrollInputsResponse"

  /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs/{conceptCode}:
    put:
      operationId: updateEmployeePayrollInputByBusinessKey
      summary: Update the quantity of a payroll input
      tags:
        - Employee Payroll Input
      parameters:
        - in: path
          name: ruleSystemCode
          required: true
          schema:
            type: string
        - in: path
          name: employeeTypeCode
          required: true
          schema:
            type: string
        - in: path
          name: employeeNumber
          required: true
          schema:
            type: string
        - in: path
          name: conceptCode
          required: true
          schema:
            type: string
        - in: query
          name: period
          required: true
          schema:
            type: integer
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/UpdateEmployeePayrollInputRequest"
      responses:
        "200":
          description: Payroll input updated
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/EmployeePayrollInputResponse"
        "400":
          description: Invalid request
        "404":
          description: Payroll input not found
    delete:
      operationId: deleteEmployeePayrollInputByBusinessKey
      summary: Delete a payroll input for an employee, concept, and period
      tags:
        - Employee Payroll Input
      parameters:
        - in: path
          name: ruleSystemCode
          required: true
          schema:
            type: string
        - in: path
          name: employeeTypeCode
          required: true
          schema:
            type: string
        - in: path
          name: employeeNumber
          required: true
          schema:
            type: string
        - in: path
          name: conceptCode
          required: true
          schema:
            type: string
        - in: query
          name: period
          required: true
          schema:
            type: integer
      responses:
        "204":
          description: Payroll input deleted
        "404":
          description: Payroll input not found
```

Also add to the `components.schemas` section:

```yaml
    CreateEmployeePayrollInputRequest:
      type: object
      required: [conceptCode, period, quantity]
      properties:
        conceptCode:
          type: string
          maxLength: 20
        period:
          type: integer
          description: "Format yyyyMM"
        quantity:
          type: number
          format: double

    UpdateEmployeePayrollInputRequest:
      type: object
      required: [quantity]
      properties:
        quantity:
          type: number
          format: double

    EmployeePayrollInputResponse:
      type: object
      properties:
        conceptCode:
          type: string
        period:
          type: integer
        quantity:
          type: number

    EmployeePayrollInputsResponse:
      type: object
      properties:
        period:
          type: integer
        inputs:
          type: array
          items:
            $ref: "#/components/schemas/EmployeePayrollInputResponse"

    EmployeePayrollInputErrorResponse:
      type: object
      properties:
        code:
          type: string
        message:
          type: string
        details:
          type: object
```

Also update the `CalculationType` enum at line 7387:
```yaml
          enum: [DIRECT_AMOUNT, RATE_BY_QUANTITY, PERCENTAGE, AGGREGATE, JAVA_PROVIDED, EMPLOYEE_INPUT]
```

- [ ] **Step 2: Commit**

```bash
git add openapi/personnel-administration-api.yaml
git commit -m "feat: add employee payroll-inputs endpoints to OpenAPI contract"
```

---

## Task 2: Flyway Migration

**Files:**
- Create: `src/main/resources/db/migration/V79__create_employee_payroll_input_table.sql`

- [ ] **Step 1: Create migration file**

```sql
CREATE TABLE employee.employee_payroll_input (
    id                 BIGSERIAL       NOT NULL,
    rule_system_code   VARCHAR(10)     NOT NULL,
    employee_type_code VARCHAR(10)     NOT NULL,
    employee_number    VARCHAR(20)     NOT NULL,
    concept_code       VARCHAR(20)     NOT NULL,
    period             INTEGER         NOT NULL,
    quantity           NUMERIC(14,4)   NOT NULL,
    CONSTRAINT pk_employee_payroll_input PRIMARY KEY (id),
    CONSTRAINT uq_employee_payroll_input_bk
        UNIQUE (rule_system_code, employee_type_code, employee_number, concept_code, period)
);
```

- [ ] **Step 2: Verify migration runs**

Start PostgreSQL if not running:
```bash
cd docker/postgres && docker compose up -d
```

Start the backend — Flyway runs automatically:
```bash
mvn spring-boot:run
```

Expected: startup logs show `Successfully applied 1 migration to schema "employee"` and table `employee.employee_payroll_input` exists.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V79__create_employee_payroll_input_table.sql
git commit -m "feat: add employee_payroll_input table migration V79"
```

---

## Task 3: Domain Model and Exceptions

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/domain/model/EmployeePayrollInput.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/domain/exception/EmployeePayrollInputAlreadyExistsException.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/domain/exception/EmployeePayrollInputNotFoundException.java`

- [ ] **Step 1: Create `EmployeePayrollInput.java`**

```java
package com.b4rrhh.employee.payroll_input.domain.model;

import java.math.BigDecimal;

public class EmployeePayrollInput {

    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;
    private final String conceptCode;
    private final int period;
    private BigDecimal quantity;

    private EmployeePayrollInput(String ruleSystemCode, String employeeTypeCode,
                                  String employeeNumber, String conceptCode,
                                  int period, BigDecimal quantity) {
        this.ruleSystemCode = ruleSystemCode;
        this.employeeTypeCode = employeeTypeCode;
        this.employeeNumber = employeeNumber;
        this.conceptCode = conceptCode;
        this.period = period;
        this.quantity = quantity;
    }

    public static EmployeePayrollInput create(String ruleSystemCode, String employeeTypeCode,
                                               String employeeNumber, String conceptCode,
                                               int period, BigDecimal quantity) {
        requireNonBlank(ruleSystemCode, "ruleSystemCode");
        requireNonBlank(employeeTypeCode, "employeeTypeCode");
        requireNonBlank(employeeNumber, "employeeNumber");
        requireNonBlank(conceptCode, "conceptCode");
        requireValidPeriod(period);
        requireNonNegative(quantity, "quantity");
        return new EmployeePayrollInput(ruleSystemCode, employeeTypeCode, employeeNumber,
                conceptCode, period, quantity);
    }

    public static EmployeePayrollInput rehydrate(String ruleSystemCode, String employeeTypeCode,
                                                  String employeeNumber, String conceptCode,
                                                  int period, BigDecimal quantity) {
        return new EmployeePayrollInput(ruleSystemCode, employeeTypeCode, employeeNumber,
                conceptCode, period, quantity);
    }

    public void updateQuantity(BigDecimal newQuantity) {
        requireNonNegative(newQuantity, "quantity");
        this.quantity = newQuantity;
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireValidPeriod(int period) {
        int year = period / 100;
        int month = period % 100;
        if (year < 2000 || year > 9999 || month < 1 || month > 12) {
            throw new IllegalArgumentException("period must be yyyyMM between 200001 and 999912, got: " + period);
        }
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " must be >= 0");
        }
    }

    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getConceptCode() { return conceptCode; }
    public int getPeriod() { return period; }
    public BigDecimal getQuantity() { return quantity; }
}
```

- [ ] **Step 2: Create `EmployeePayrollInputAlreadyExistsException.java`**

```java
package com.b4rrhh.employee.payroll_input.domain.exception;

public class EmployeePayrollInputAlreadyExistsException extends RuntimeException {

    public EmployeePayrollInputAlreadyExistsException(String conceptCode, int period) {
        super("Ya existe un input de nómina para el concepto '" + conceptCode +
                "' en el período " + period);
    }
}
```

- [ ] **Step 3: Create `EmployeePayrollInputNotFoundException.java`**

```java
package com.b4rrhh.employee.payroll_input.domain.exception;

public class EmployeePayrollInputNotFoundException extends RuntimeException {

    public EmployeePayrollInputNotFoundException(String conceptCode, int period) {
        super("No existe un input de nómina para el concepto '" + conceptCode +
                "' en el período " + period);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/payroll_input/domain/
git commit -m "feat: add EmployeePayrollInput domain model and exceptions"
```

---

## Task 4: Repository Port and Create Use Case

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/domain/port/EmployeePayrollInputRepository.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/CreateEmployeePayrollInputUseCase.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/CreateEmployeePayrollInputCommand.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/CreateEmployeePayrollInputService.java`
- Test: `src/test/java/com/b4rrhh/employee/payroll_input/application/usecase/CreateEmployeePayrollInputServiceTest.java`

- [ ] **Step 1: Create `EmployeePayrollInputRepository.java`**

```java
package com.b4rrhh.employee.payroll_input.domain.port;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;

import java.util.List;
import java.util.Optional;

public interface EmployeePayrollInputRepository {

    boolean existsByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                 String employeeNumber, String conceptCode, int period);

    Optional<EmployeePayrollInput> findByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                                      String employeeNumber, String conceptCode, int period);

    List<EmployeePayrollInput> findByEmployeeAndPeriod(String ruleSystemCode, String employeeTypeCode,
                                                        String employeeNumber, int period);

    EmployeePayrollInput save(EmployeePayrollInput input);

    void deleteByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                              String employeeNumber, String conceptCode, int period);
}
```

- [ ] **Step 2: Create `CreateEmployeePayrollInputCommand.java`**

```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import java.math.BigDecimal;

public record CreateEmployeePayrollInputCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String conceptCode,
        int period,
        BigDecimal quantity
) {}
```

- [ ] **Step 3: Create `CreateEmployeePayrollInputUseCase.java`**

```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;

public interface CreateEmployeePayrollInputUseCase {
    EmployeePayrollInput create(CreateEmployeePayrollInputCommand command);
}
```

- [ ] **Step 4: Write failing test**

```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputAlreadyExistsException;
import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEmployeePayrollInputServiceTest {

    @Mock
    private EmployeePayrollInputRepository repository;

    private CreateEmployeePayrollInputService service;

    @BeforeEach
    void setUp() {
        service = new CreateEmployeePayrollInputService(repository);
    }

    @Test
    void create_savesInput_whenBusinessKeyIsNew() {
        var command = new CreateEmployeePayrollInputCommand("ESP", "GEN", "00001", "HE_QTY", 202604, BigDecimal.valueOf(40));
        when(repository.existsByBusinessKey("ESP", "GEN", "00001", "HE_QTY", 202604)).thenReturn(false);
        var expected = EmployeePayrollInput.create("ESP", "GEN", "00001", "HE_QTY", 202604, BigDecimal.valueOf(40));
        when(repository.save(any())).thenReturn(expected);

        EmployeePayrollInput result = service.create(command);

        assertThat(result.getConceptCode()).isEqualTo("HE_QTY");
        assertThat(result.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(40));
        verify(repository).save(any());
    }

    @Test
    void create_throwsAlreadyExists_whenDuplicateBusinessKey() {
        var command = new CreateEmployeePayrollInputCommand("ESP", "GEN", "00001", "HE_QTY", 202604, BigDecimal.valueOf(40));
        when(repository.existsByBusinessKey("ESP", "GEN", "00001", "HE_QTY", 202604)).thenReturn(true);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(EmployeePayrollInputAlreadyExistsException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void create_normalizesRuleSystemCodeToUpperCase() {
        var command = new CreateEmployeePayrollInputCommand("esp", "gen", "00001", "he_qty", 202604, BigDecimal.valueOf(10));
        when(repository.existsByBusinessKey("ESP", "GEN", "00001", "HE_QTY", 202604)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmployeePayrollInput result = service.create(command);

        assertThat(result.getRuleSystemCode()).isEqualTo("ESP");
        assertThat(result.getConceptCode()).isEqualTo("HE_QTY");
    }
}
```

- [ ] **Step 5: Run test to verify it fails**

```bash
mvn test -Dtest=CreateEmployeePayrollInputServiceTest -pl . 2>&1 | tail -15
```

Expected: FAIL — `CreateEmployeePayrollInputService` does not exist.

- [ ] **Step 6: Create `CreateEmployeePayrollInputService.java`**

```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputAlreadyExistsException;
import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEmployeePayrollInputService implements CreateEmployeePayrollInputUseCase {

    private final EmployeePayrollInputRepository repository;

    public CreateEmployeePayrollInputService(EmployeePayrollInputRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public EmployeePayrollInput create(CreateEmployeePayrollInputCommand command) {
        String rsc = command.ruleSystemCode().trim().toUpperCase();
        String etc = command.employeeTypeCode().trim().toUpperCase();
        String en  = command.employeeNumber().trim();
        String cc  = command.conceptCode().trim().toUpperCase();

        if (repository.existsByBusinessKey(rsc, etc, en, cc, command.period())) {
            throw new EmployeePayrollInputAlreadyExistsException(cc, command.period());
        }

        EmployeePayrollInput input = EmployeePayrollInput.create(rsc, etc, en, cc,
                command.period(), command.quantity());
        return repository.save(input);
    }
}
```

- [ ] **Step 7: Run tests to verify they pass**

```bash
mvn test -Dtest=CreateEmployeePayrollInputServiceTest -pl . 2>&1 | tail -10
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/payroll_input/
git add src/test/java/com/b4rrhh/employee/payroll_input/
git commit -m "feat: add EmployeePayrollInputRepository port and CreateEmployeePayrollInputService"
```

---

## Task 5: Update, Delete, and List Use Cases

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/UpdateEmployeePayrollInputUseCase.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/UpdateEmployeePayrollInputCommand.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/UpdateEmployeePayrollInputService.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/DeleteEmployeePayrollInputUseCase.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/DeleteEmployeePayrollInputCommand.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/DeleteEmployeePayrollInputService.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/ListEmployeePayrollInputsUseCase.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/ListEmployeePayrollInputsCommand.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/application/usecase/ListEmployeePayrollInputsService.java`
- Test: `src/test/java/com/b4rrhh/employee/payroll_input/application/usecase/UpdateEmployeePayrollInputServiceTest.java`
- Test: `src/test/java/com/b4rrhh/employee/payroll_input/application/usecase/DeleteEmployeePayrollInputServiceTest.java`

- [ ] **Step 1: Create Update command, interface, and test**

`UpdateEmployeePayrollInputCommand.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import java.math.BigDecimal;

public record UpdateEmployeePayrollInputCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String conceptCode,
        int period,
        BigDecimal quantity
) {}
```

`UpdateEmployeePayrollInputUseCase.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;

public interface UpdateEmployeePayrollInputUseCase {
    EmployeePayrollInput update(UpdateEmployeePayrollInputCommand command);
}
```

`UpdateEmployeePayrollInputServiceTest.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputNotFoundException;
import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateEmployeePayrollInputServiceTest {

    @Mock
    private EmployeePayrollInputRepository repository;

    private UpdateEmployeePayrollInputService service;

    @BeforeEach
    void setUp() {
        service = new UpdateEmployeePayrollInputService(repository);
    }

    @Test
    void update_updatesQuantity_whenInputExists() {
        var existing = EmployeePayrollInput.rehydrate("ESP", "GEN", "00001", "HE_QTY", 202604, BigDecimal.valueOf(40));
        when(repository.findByBusinessKey("ESP", "GEN", "00001", "HE_QTY", 202604))
                .thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateEmployeePayrollInputCommand("ESP", "GEN", "00001", "HE_QTY", 202604, BigDecimal.valueOf(35));
        EmployeePayrollInput result = service.update(command);

        assertThat(result.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(35));
    }

    @Test
    void update_throwsNotFound_whenInputDoesNotExist() {
        when(repository.findByBusinessKey("ESP", "GEN", "00001", "HE_QTY", 202604))
                .thenReturn(Optional.empty());

        var command = new UpdateEmployeePayrollInputCommand("ESP", "GEN", "00001", "HE_QTY", 202604, BigDecimal.valueOf(35));

        assertThatThrownBy(() -> service.update(command))
                .isInstanceOf(EmployeePayrollInputNotFoundException.class);
    }
}
```

- [ ] **Step 2: Run update test — verify it fails**

```bash
mvn test -Dtest=UpdateEmployeePayrollInputServiceTest 2>&1 | tail -5
```

Expected: FAIL

- [ ] **Step 3: Create `UpdateEmployeePayrollInputService.java`**

```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputNotFoundException;
import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEmployeePayrollInputService implements UpdateEmployeePayrollInputUseCase {

    private final EmployeePayrollInputRepository repository;

    public UpdateEmployeePayrollInputService(EmployeePayrollInputRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public EmployeePayrollInput update(UpdateEmployeePayrollInputCommand command) {
        String rsc = command.ruleSystemCode().trim().toUpperCase();
        String etc = command.employeeTypeCode().trim().toUpperCase();
        String en  = command.employeeNumber().trim();
        String cc  = command.conceptCode().trim().toUpperCase();

        EmployeePayrollInput input = repository
                .findByBusinessKey(rsc, etc, en, cc, command.period())
                .orElseThrow(() -> new EmployeePayrollInputNotFoundException(cc, command.period()));

        input.updateQuantity(command.quantity());
        return repository.save(input);
    }
}
```

- [ ] **Step 4: Create Delete command, interface, test, and service**

`DeleteEmployeePayrollInputCommand.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

public record DeleteEmployeePayrollInputCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String conceptCode,
        int period
) {}
```

`DeleteEmployeePayrollInputUseCase.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

public interface DeleteEmployeePayrollInputUseCase {
    void delete(DeleteEmployeePayrollInputCommand command);
}
```

`DeleteEmployeePayrollInputServiceTest.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputNotFoundException;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteEmployeePayrollInputServiceTest {

    @Mock
    private EmployeePayrollInputRepository repository;

    private DeleteEmployeePayrollInputService service;

    @BeforeEach
    void setUp() {
        service = new DeleteEmployeePayrollInputService(repository);
    }

    @Test
    void delete_callsRepository_whenInputExists() {
        when(repository.existsByBusinessKey("ESP", "GEN", "00001", "HE_QTY", 202604)).thenReturn(true);

        service.delete(new DeleteEmployeePayrollInputCommand("ESP", "GEN", "00001", "HE_QTY", 202604));

        verify(repository).deleteByBusinessKey("ESP", "GEN", "00001", "HE_QTY", 202604);
    }

    @Test
    void delete_throwsNotFound_whenInputDoesNotExist() {
        when(repository.existsByBusinessKey("ESP", "GEN", "00001", "HE_QTY", 202604)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(
                new DeleteEmployeePayrollInputCommand("ESP", "GEN", "00001", "HE_QTY", 202604)))
                .isInstanceOf(EmployeePayrollInputNotFoundException.class);
    }
}
```

`DeleteEmployeePayrollInputService.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputNotFoundException;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteEmployeePayrollInputService implements DeleteEmployeePayrollInputUseCase {

    private final EmployeePayrollInputRepository repository;

    public DeleteEmployeePayrollInputService(EmployeePayrollInputRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void delete(DeleteEmployeePayrollInputCommand command) {
        String rsc = command.ruleSystemCode().trim().toUpperCase();
        String etc = command.employeeTypeCode().trim().toUpperCase();
        String en  = command.employeeNumber().trim();
        String cc  = command.conceptCode().trim().toUpperCase();

        if (!repository.existsByBusinessKey(rsc, etc, en, cc, command.period())) {
            throw new EmployeePayrollInputNotFoundException(cc, command.period());
        }
        repository.deleteByBusinessKey(rsc, etc, en, cc, command.period());
    }
}
```

- [ ] **Step 5: Create List command, interface, and service (no test needed — trivial delegation)**

`ListEmployeePayrollInputsCommand.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

public record ListEmployeePayrollInputsCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        int period
) {}
```

`ListEmployeePayrollInputsUseCase.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;

import java.util.List;

public interface ListEmployeePayrollInputsUseCase {
    List<EmployeePayrollInput> listByEmployeeAndPeriod(ListEmployeePayrollInputsCommand command);
}
```

`ListEmployeePayrollInputsService.java`:
```java
package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListEmployeePayrollInputsService implements ListEmployeePayrollInputsUseCase {

    private final EmployeePayrollInputRepository repository;

    public ListEmployeePayrollInputsService(EmployeePayrollInputRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeePayrollInput> listByEmployeeAndPeriod(ListEmployeePayrollInputsCommand command) {
        String rsc = command.ruleSystemCode().trim().toUpperCase();
        String etc = command.employeeTypeCode().trim().toUpperCase();
        String en  = command.employeeNumber().trim();
        return repository.findByEmployeeAndPeriod(rsc, etc, en, command.period());
    }
}
```

- [ ] **Step 6: Run all new tests**

```bash
mvn test -Dtest="UpdateEmployeePayrollInputServiceTest,DeleteEmployeePayrollInputServiceTest" 2>&1 | tail -10
```

Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/payroll_input/application/
git add src/test/java/com/b4rrhh/employee/payroll_input/
git commit -m "feat: add Update, Delete, List payroll input use cases"
```

---

## Task 6: Persistence Layer

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/persistence/EmployeePayrollInputEntity.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/persistence/SpringDataEmployeePayrollInputRepository.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/persistence/EmployeePayrollInputPersistenceAdapter.java`

- [ ] **Step 1: Create `EmployeePayrollInputEntity.java`**

```java
package com.b4rrhh.employee.payroll_input.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "employee_payroll_input", schema = "employee")
public class EmployeePayrollInputEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_system_code", nullable = false, length = 10)
    private String ruleSystemCode;

    @Column(name = "employee_type_code", nullable = false, length = 10)
    private String employeeTypeCode;

    @Column(name = "employee_number", nullable = false, length = 20)
    private String employeeNumber;

    @Column(name = "concept_code", nullable = false, length = 20)
    private String conceptCode;

    @Column(name = "period", nullable = false)
    private int period;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 4)
    private BigDecimal quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public void setRuleSystemCode(String ruleSystemCode) { this.ruleSystemCode = ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public void setEmployeeTypeCode(String employeeTypeCode) { this.employeeTypeCode = employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getConceptCode() { return conceptCode; }
    public void setConceptCode(String conceptCode) { this.conceptCode = conceptCode; }
    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
```

- [ ] **Step 2: Create `SpringDataEmployeePayrollInputRepository.java`**

```java
package com.b4rrhh.employee.payroll_input.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEmployeePayrollInputRepository
        extends JpaRepository<EmployeePayrollInputEntity, Long> {

    boolean existsByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
            String ruleSystemCode, String employeeTypeCode,
            String employeeNumber, String conceptCode, int period);

    Optional<EmployeePayrollInputEntity> findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
            String ruleSystemCode, String employeeTypeCode,
            String employeeNumber, String conceptCode, int period);

    List<EmployeePayrollInputEntity> findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndPeriodOrderByConceptCode(
            String ruleSystemCode, String employeeTypeCode,
            String employeeNumber, int period);

    void deleteByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
            String ruleSystemCode, String employeeTypeCode,
            String employeeNumber, String conceptCode, int period);
}
```

- [ ] **Step 3: Create `EmployeePayrollInputPersistenceAdapter.java`**

```java
package com.b4rrhh.employee.payroll_input.infrastructure.persistence;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeePayrollInputPersistenceAdapter implements EmployeePayrollInputRepository {

    private final SpringDataEmployeePayrollInputRepository springDataRepo;

    public EmployeePayrollInputPersistenceAdapter(
            SpringDataEmployeePayrollInputRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public boolean existsByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                        String employeeNumber, String conceptCode, int period) {
        return springDataRepo
                .existsByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
                        ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period);
    }

    @Override
    public Optional<EmployeePayrollInput> findByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                                             String employeeNumber, String conceptCode,
                                                             int period) {
        return springDataRepo
                .findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
                        ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period)
                .map(this::toDomain);
    }

    @Override
    public List<EmployeePayrollInput> findByEmployeeAndPeriod(String ruleSystemCode,
                                                               String employeeTypeCode,
                                                               String employeeNumber, int period) {
        return springDataRepo
                .findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndPeriodOrderByConceptCode(
                        ruleSystemCode, employeeTypeCode, employeeNumber, period)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public EmployeePayrollInput save(EmployeePayrollInput input) {
        EmployeePayrollInputEntity saved = springDataRepo.save(toEntity(input));
        return toDomain(saved);
    }

    @Override
    public void deleteByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                     String employeeNumber, String conceptCode, int period) {
        springDataRepo
                .deleteByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
                        ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period);
    }

    private EmployeePayrollInput toDomain(EmployeePayrollInputEntity entity) {
        return EmployeePayrollInput.rehydrate(
                entity.getRuleSystemCode(),
                entity.getEmployeeTypeCode(),
                entity.getEmployeeNumber(),
                entity.getConceptCode(),
                entity.getPeriod(),
                entity.getQuantity()
        );
    }

    private EmployeePayrollInputEntity toEntity(EmployeePayrollInput input) {
        EmployeePayrollInputEntity entity = new EmployeePayrollInputEntity();
        entity.setRuleSystemCode(input.getRuleSystemCode());
        entity.setEmployeeTypeCode(input.getEmployeeTypeCode());
        entity.setEmployeeNumber(input.getEmployeeNumber());
        entity.setConceptCode(input.getConceptCode());
        entity.setPeriod(input.getPeriod());
        entity.setQuantity(input.getQuantity());
        return entity;
    }
}
```

- [ ] **Step 4: Run full test suite to verify no regressions**

```bash
mvn test 2>&1 | tail -10
```

Expected: all tests pass (persistence adapter not covered by unit tests — tested implicitly via integration in Task 9).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/persistence/
git commit -m "feat: add payroll input JPA entity and persistence adapter"
```

---

## Task 7: Web Layer

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/web/dto/CreateEmployeePayrollInputRequest.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/web/dto/UpdateEmployeePayrollInputRequest.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/web/dto/EmployeePayrollInputResponse.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/web/dto/EmployeePayrollInputsResponse.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/web/dto/EmployeePayrollInputErrorResponse.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/web/assembler/EmployeePayrollInputResponseAssembler.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/web/EmployeePayrollInputController.java`
- Create: `src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/web/EmployeePayrollInputExceptionHandler.java`

- [ ] **Step 1: Create DTOs**

`CreateEmployeePayrollInputRequest.java`:
```java
package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.math.BigDecimal;

public class CreateEmployeePayrollInputRequest {

    private String conceptCode;
    private int period;
    private BigDecimal quantity;

    public String getConceptCode() { return conceptCode; }
    public void setConceptCode(String conceptCode) { this.conceptCode = conceptCode; }
    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("Unexpected field: " + fieldName);
    }
}
```

`UpdateEmployeePayrollInputRequest.java`:
```java
package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.math.BigDecimal;

public class UpdateEmployeePayrollInputRequest {

    private BigDecimal quantity;

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("Unexpected field: " + fieldName);
    }
}
```

`EmployeePayrollInputResponse.java`:
```java
package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import java.math.BigDecimal;

public record EmployeePayrollInputResponse(
        String conceptCode,
        int period,
        BigDecimal quantity
) {}
```

`EmployeePayrollInputsResponse.java`:
```java
package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import java.util.List;

public record EmployeePayrollInputsResponse(
        int period,
        List<EmployeePayrollInputResponse> inputs
) {}
```

`EmployeePayrollInputErrorResponse.java`:
```java
package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import java.util.Map;

public record EmployeePayrollInputErrorResponse(
        String code,
        String message,
        Map<String, Object> details
) {}
```

- [ ] **Step 2: Create `EmployeePayrollInputResponseAssembler.java`**

```java
package com.b4rrhh.employee.payroll_input.infrastructure.web.assembler;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputResponse;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputsResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeePayrollInputResponseAssembler {

    public EmployeePayrollInputResponse toResponse(EmployeePayrollInput input) {
        return new EmployeePayrollInputResponse(
                input.getConceptCode(),
                input.getPeriod(),
                input.getQuantity()
        );
    }

    public EmployeePayrollInputsResponse toListResponse(int period, List<EmployeePayrollInput> inputs) {
        List<EmployeePayrollInputResponse> items = inputs.stream()
                .map(this::toResponse)
                .toList();
        return new EmployeePayrollInputsResponse(period, items);
    }
}
```

- [ ] **Step 3: Create `EmployeePayrollInputController.java`**

```java
package com.b4rrhh.employee.payroll_input.infrastructure.web;

import com.b4rrhh.employee.payroll_input.application.usecase.CreateEmployeePayrollInputCommand;
import com.b4rrhh.employee.payroll_input.application.usecase.CreateEmployeePayrollInputUseCase;
import com.b4rrhh.employee.payroll_input.application.usecase.DeleteEmployeePayrollInputCommand;
import com.b4rrhh.employee.payroll_input.application.usecase.DeleteEmployeePayrollInputUseCase;
import com.b4rrhh.employee.payroll_input.application.usecase.ListEmployeePayrollInputsCommand;
import com.b4rrhh.employee.payroll_input.application.usecase.ListEmployeePayrollInputsUseCase;
import com.b4rrhh.employee.payroll_input.application.usecase.UpdateEmployeePayrollInputCommand;
import com.b4rrhh.employee.payroll_input.application.usecase.UpdateEmployeePayrollInputUseCase;
import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.infrastructure.web.assembler.EmployeePayrollInputResponseAssembler;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.CreateEmployeePayrollInputRequest;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputResponse;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputsResponse;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.UpdateEmployeePayrollInputRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs")
public class EmployeePayrollInputController {

    private final CreateEmployeePayrollInputUseCase createUseCase;
    private final UpdateEmployeePayrollInputUseCase updateUseCase;
    private final DeleteEmployeePayrollInputUseCase deleteUseCase;
    private final ListEmployeePayrollInputsUseCase listUseCase;
    private final EmployeePayrollInputResponseAssembler assembler;

    public EmployeePayrollInputController(
            CreateEmployeePayrollInputUseCase createUseCase,
            UpdateEmployeePayrollInputUseCase updateUseCase,
            DeleteEmployeePayrollInputUseCase deleteUseCase,
            ListEmployeePayrollInputsUseCase listUseCase,
            EmployeePayrollInputResponseAssembler assembler
    ) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.listUseCase = listUseCase;
        this.assembler = assembler;
    }

    @PostMapping
    public ResponseEntity<EmployeePayrollInputResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateEmployeePayrollInputRequest request
    ) {
        EmployeePayrollInput created = createUseCase.create(new CreateEmployeePayrollInputCommand(
                ruleSystemCode, employeeTypeCode, employeeNumber,
                request.getConceptCode(), request.getPeriod(), request.getQuantity()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<EmployeePayrollInputsResponse> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestParam int period
    ) {
        List<EmployeePayrollInput> inputs = listUseCase.listByEmployeeAndPeriod(
                new ListEmployeePayrollInputsCommand(ruleSystemCode, employeeTypeCode, employeeNumber, period));
        return ResponseEntity.ok(assembler.toListResponse(period, inputs));
    }

    @PutMapping("/{conceptCode}")
    public ResponseEntity<EmployeePayrollInputResponse> update(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String conceptCode,
            @RequestParam int period,
            @RequestBody UpdateEmployeePayrollInputRequest request
    ) {
        EmployeePayrollInput updated = updateUseCase.update(new UpdateEmployeePayrollInputCommand(
                ruleSystemCode, employeeTypeCode, employeeNumber,
                conceptCode, period, request.getQuantity()
        ));
        return ResponseEntity.ok(assembler.toResponse(updated));
    }

    @DeleteMapping("/{conceptCode}")
    public ResponseEntity<Void> delete(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String conceptCode,
            @RequestParam int period
    ) {
        deleteUseCase.delete(new DeleteEmployeePayrollInputCommand(
                ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period));
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 4: Create `EmployeePayrollInputExceptionHandler.java`**

```java
package com.b4rrhh.employee.payroll_input.infrastructure.web;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputAlreadyExistsException;
import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputNotFoundException;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmployeePayrollInputController.class)
public class EmployeePayrollInputExceptionHandler {

    @ExceptionHandler(EmployeePayrollInputNotFoundException.class)
    public ResponseEntity<EmployeePayrollInputErrorResponse> handleNotFound(
            EmployeePayrollInputNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new EmployeePayrollInputErrorResponse(
                        "PAYROLL_INPUT_NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(EmployeePayrollInputAlreadyExistsException.class)
    public ResponseEntity<EmployeePayrollInputErrorResponse> handleConflict(
            EmployeePayrollInputAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new EmployeePayrollInputErrorResponse(
                        "PAYROLL_INPUT_ALREADY_EXISTS", ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EmployeePayrollInputErrorResponse> handleBadRequest(
            IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new EmployeePayrollInputErrorResponse(
                        "PAYROLL_INPUT_BAD_REQUEST", ex.getMessage(), null));
    }
}
```

- [ ] **Step 5: Run full tests and verify the app compiles**

```bash
mvn test 2>&1 | tail -10
```

Expected: all tests pass.

- [ ] **Step 6: Manual smoke test (optional — requires running backend + DB)**

```bash
curl -X POST http://localhost:8080/employees/ESP/GEN/00001/payroll-inputs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"conceptCode":"HE_QTY","period":202604,"quantity":40}'
```

Expected: HTTP 201 with `{"conceptCode":"HE_QTY","period":202604,"quantity":40.0000}`

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/payroll_input/infrastructure/
git commit -m "feat: add payroll input web layer (controller, DTOs, exception handler)"
```

---

## Task 8: Engine Changes — CalculationType, SegmentCalculationContext, DefaultSegmentExecutionEngine

**Files:**
- Modify: `src/main/java/com/b4rrhh/payroll_engine/concept/domain/model/CalculationType.java`
- Modify: `src/main/java/com/b4rrhh/payroll_engine/segment/domain/model/SegmentCalculationContext.java`
- Modify: `src/main/java/com/b4rrhh/payroll_engine/execution/application/service/DefaultSegmentExecutionEngine.java`
- Modify: `src/main/java/com/b4rrhh/payroll_engine/execution/application/service/DefaultPayrollEnginePocExecutor.java`
- Modify: `src/main/java/com/b4rrhh/payroll_engine/planning/application/service/DefaultEligiblePayrollExecutor.java`
- Test: `src/test/java/com/b4rrhh/payroll_engine/execution/application/service/DefaultSegmentExecutionEngineEmployeeInputTest.java`

- [ ] **Step 1: Write failing test for DefaultSegmentExecutionEngine EMPLOYEE_INPUT**

```java
package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
import com.b4rrhh.payroll_engine.segment.domain.model.SegmentCalculationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSegmentExecutionEngineEmployeeInputTest {

    private DefaultSegmentExecutionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultSegmentExecutionEngine(
                new SegmentTechnicalValueResolver(),
                new RateByQuantityOperandResolver(),
                new PercentageConceptResolver(),
                List.of()
        );
    }

    private SegmentCalculationContext contextWith(Map<String, BigDecimal> inputs) {
        return new SegmentCalculationContext(
                "ESP", "GEN", "00001",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                true, true,
                30, 30,
                BigDecimal.ONE, BigDecimal.valueOf(2000),
                inputs
        );
    }

    @Test
    void employeeInput_returnsRegisteredQuantity_whenPresent() {
        ConceptNodeIdentity id = new ConceptNodeIdentity("ESP", "HE_QTY");
        ConceptExecutionPlanEntry entry = new ConceptExecutionPlanEntry(id, CalculationType.EMPLOYEE_INPUT, Map.of(), List.of());

        SegmentCalculationContext context = contextWith(Map.of("HE_QTY", BigDecimal.valueOf(40)));
        SegmentExecutionState state = engine.execute(List.of(entry), context);

        assertThat(state.getRequiredAmount(id)).isEqualByComparingTo(BigDecimal.valueOf(40));
    }

    @Test
    void employeeInput_returnsZero_whenNotPresent() {
        ConceptNodeIdentity id = new ConceptNodeIdentity("ESP", "HE_QTY");
        ConceptExecutionPlanEntry entry = new ConceptExecutionPlanEntry(id, CalculationType.EMPLOYEE_INPUT, Map.of(), List.of());

        SegmentCalculationContext context = contextWith(Map.of());
        SegmentExecutionState state = engine.execute(List.of(entry), context);

        assertThat(state.getRequiredAmount(id)).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=DefaultSegmentExecutionEngineEmployeeInputTest 2>&1 | tail -10
```

Expected: FAIL — `EMPLOYEE_INPUT` is not a valid enum value / `SegmentCalculationContext` doesn't accept `employeeInputs`.

- [ ] **Step 3: Add `EMPLOYEE_INPUT` to `CalculationType.java`**

Replace:
```java
public enum CalculationType {
    DIRECT_AMOUNT,
    RATE_BY_QUANTITY,
    PERCENTAGE,
    AGGREGATE,
    JAVA_PROVIDED
}
```

With:
```java
public enum CalculationType {
    DIRECT_AMOUNT,
    RATE_BY_QUANTITY,
    PERCENTAGE,
    AGGREGATE,
    JAVA_PROVIDED,
    EMPLOYEE_INPUT
}
```

- [ ] **Step 4: Add `employeeInputs` to `SegmentCalculationContext.java`**

The current constructor ends after `monthlySalaryAmount`. Add the new field and parameter. The full modified class:

Add field after `monthlySalaryAmount`:
```java
private final Map<String, BigDecimal> employeeInputs;
```

Add import at the top:
```java
import java.util.Map;
```

Change constructor signature — add parameter at end:
```java
public SegmentCalculationContext(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate segmentStart,
        LocalDate segmentEnd,
        boolean firstSegment,
        boolean lastSegment,
        long daysInPeriod,
        long daysInSegment,
        BigDecimal workingTimePercentage,
        BigDecimal monthlySalaryAmount,
        Map<String, BigDecimal> employeeInputs    // NEW
) {
```

Add validation after the existing validations (before assignments):
```java
requireNonNull(employeeInputs, "employeeInputs");
```

Add assignment (after `this.monthlySalaryAmount = monthlySalaryAmount;`):
```java
this.employeeInputs = employeeInputs;
```

Add getter at end of class:
```java
public Map<String, BigDecimal> getEmployeeInputs() { return employeeInputs; }
```

- [ ] **Step 5: Add `EMPLOYEE_INPUT` case to `DefaultSegmentExecutionEngine.java`**

In the `switch` expression, add before `default ->`:
```java
case EMPLOYEE_INPUT -> context.getEmployeeInputs()
        .getOrDefault(entry.identity().getConceptCode(), BigDecimal.ZERO);
```

- [ ] **Step 6: Fix `DefaultPayrollEnginePocExecutor.java` — pass `Map.of()` to updated constructor**

Find the `new SegmentCalculationContext(` call (around line 162) and add `Map.of()` as the last argument:

```java
SegmentCalculationContext context = new SegmentCalculationContext(
        request.getRuleSystemCode(),
        request.getEmployeeTypeCode(),
        request.getEmployeeNumber(),
        period.getPeriodStart(),
        period.getPeriodEnd(),
        segment.getSegmentStart(),
        segment.getSegmentEnd(),
        segment.isFirstSegment(),
        segment.isLastSegment(),
        daysInPeriod,
        segment.lengthInDaysInclusive(),
        workingTimePercentage,
        request.getMonthlySalaryAmount(),
        Map.of()   // employeeInputs — not used by PoC executor
);
```

Add import: `import java.util.Map;`

- [ ] **Step 7: Fix `DefaultEligiblePayrollExecutor.java` — pass `Map.of()` to updated constructor**

Same change — find the `new SegmentCalculationContext(` call (around line 126) and add `Map.of()` as the last argument:

```java
SegmentCalculationContext segmentContext = new SegmentCalculationContext(
        request.getRuleSystemCode(),
        request.getEmployeeTypeCode(),
        request.getEmployeeNumber(),
        period.getPeriodStart(),
        period.getPeriodEnd(),
        segment.getSegmentStart(),
        segment.getSegmentEnd(),
        segment.isFirstSegment(),
        segment.isLastSegment(),
        daysInPeriod,
        segment.lengthInDaysInclusive(),
        workingTimePercentage,
        request.getMonthlySalaryAmount(),
        Map.of()   // employeeInputs — not used by eligible executor
);
```

Add import: `import java.util.Map;`

- [ ] **Step 8: Run tests**

```bash
mvn test -Dtest=DefaultSegmentExecutionEngineEmployeeInputTest 2>&1 | tail -10
```

Expected: `Tests run: 2, Failures: 0`

- [ ] **Step 9: Run full test suite**

```bash
mvn test 2>&1 | tail -10
```

Expected: all tests pass.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/b4rrhh/payroll_engine/
git add src/test/java/com/b4rrhh/payroll_engine/
git commit -m "feat: add EMPLOYEE_INPUT calculation type and engine support"
```

---

## Task 9: Payroll Lookup Port, Adapter, and CalculatePayrollUnitService Wiring

**Files:**
- Create: `src/main/java/com/b4rrhh/payroll/application/port/EmployeePayrollInputLookupPort.java`
- Create: `src/main/java/com/b4rrhh/payroll/infrastructure/persistence/EmployeePayrollInputLookupAdapter.java`
- Modify: `src/main/java/com/b4rrhh/payroll/application/usecase/CalculatePayrollUnitService.java`

- [ ] **Step 1: Create `EmployeePayrollInputLookupPort.java`**

```java
package com.b4rrhh.payroll.application.port;

import java.math.BigDecimal;
import java.util.Map;

public interface EmployeePayrollInputLookupPort {

    Map<String, BigDecimal> findInputsByPeriod(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            int period
    );
}
```

- [ ] **Step 2: Create `EmployeePayrollInputLookupAdapter.java`**

This adapter queries `employee.employee_payroll_input` directly (cross-context persistence access — allowed by hexagonal architecture).

```java
package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.application.port.EmployeePayrollInputLookupPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EmployeePayrollInputLookupAdapter implements EmployeePayrollInputLookupPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, BigDecimal> findInputsByPeriod(String ruleSystemCode, String employeeTypeCode,
                                                       String employeeNumber, int period) {
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT concept_code, quantity FROM employee.employee_payroll_input " +
                "WHERE rule_system_code = :rsc AND employee_type_code = :etc " +
                "AND employee_number = :en AND period = :period"
        )
                .setParameter("rsc", ruleSystemCode)
                .setParameter("etc", employeeTypeCode)
                .setParameter("en", employeeNumber)
                .setParameter("period", period)
                .getResultList();

        return rows.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (BigDecimal) row[1]
        ));
    }
}
```

- [ ] **Step 3: Inject `EmployeePayrollInputLookupPort` into `CalculatePayrollUnitService`**

In `CalculatePayrollUnitService.java`:

3a. Add field declaration (with existing fields around line 58):
```java
private final EmployeePayrollInputLookupPort employeePayrollInputLookupPort;
```

3b. Add to constructor (with existing params around line 70):
```java
EmployeePayrollInputLookupPort employeePayrollInputLookupPort,
```

3c. Add assignment in constructor body:
```java
this.employeePayrollInputLookupPort = employeePayrollInputLookupPort;
```

3d. Add import:
```java
import com.b4rrhh.payroll.application.port.EmployeePayrollInputLookupPort;
import java.util.Map;
```

- [ ] **Step 4: Load employee inputs before the execution loop in `CalculatePayrollUnitService`**

After the `PayrollConceptExecutionContext calcContext = new PayrollConceptExecutionContext(...)` block (around line 172), and before the `firstAggIdx` computation, add:

```java
int period = command.periodStart().getYear() * 100 + command.periodStart().getMonthValue();
Map<String, BigDecimal> employeeInputsForPeriod = employeePayrollInputLookupPort.findInputsByPeriod(
        command.ruleSystemCode(),
        command.employeeTypeCode(),
        command.employeeNumber(),
        period
);
```

- [ ] **Step 5: Add `EMPLOYEE_INPUT` case to the switch in `CalculatePayrollUnitService`**

In the per-segment execution switch (around line 210, after the `JAVA_PROVIDED` case, before the closing brace of the switch):

```java
case EMPLOYEE_INPUT -> {
    amount = employeeInputsForPeriod.getOrDefault(conceptCode, BigDecimal.ZERO);
    log.info("[NÓMINA] [{}/{}] {} EMPLOYEE_INPUT → {}",
            step, total, conceptCode, amount);
}
```

- [ ] **Step 6: Run full test suite**

```bash
mvn test 2>&1 | tail -15
```

Expected: all tests pass. If any test creates `CalculatePayrollUnitService` directly, it will need the new constructor argument — add `mock(EmployeePayrollInputLookupPort.class)` returning `Map.of()` by default.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/b4rrhh/payroll/application/port/EmployeePayrollInputLookupPort.java
git add src/main/java/com/b4rrhh/payroll/infrastructure/persistence/EmployeePayrollInputLookupAdapter.java
git add src/main/java/com/b4rrhh/payroll/application/usecase/CalculatePayrollUnitService.java
git commit -m "feat: wire EmployeePayrollInputLookupPort into payroll execution engine"
```

---

## Task 10: Designer — types.ts Update

**Repo:** `b4rrhh_designer`

**Files:**
- Modify: `src/app/canvas/types.ts`

- [ ] **Step 1: Add `EMPLOYEE_INPUT` to all relevant type definitions in `types.ts`**

Replace:
```typescript
export type CalculationType = 'DIRECT_AMOUNT' | 'RATE_BY_QUANTITY' | 'PERCENTAGE' | 'AGGREGATE' | 'JAVA_PROVIDED'
```
With:
```typescript
export type CalculationType = 'DIRECT_AMOUNT' | 'RATE_BY_QUANTITY' | 'PERCENTAGE' | 'AGGREGATE' | 'JAVA_PROVIDED' | 'EMPLOYEE_INPUT'
```

In `INPUT_PORTS`, add:
```typescript
export const INPUT_PORTS: Record<CalculationType, string[]> = {
  DIRECT_AMOUNT:    [],
  JAVA_PROVIDED:    [],
  EMPLOYEE_INPUT:   [],   // NEW — no input handles; reads from employee data
  RATE_BY_QUANTITY: ['qty', 'rate'],
  PERCENTAGE:       ['base', 'pct'],
  AGGREGATE:        ['feed'],
}
```

In `TYPE_BADGE_COLORS`, add:
```typescript
export const TYPE_BADGE_COLORS: Record<CalculationType, string> = {
  DIRECT_AMOUNT:    'bg-slate-800 text-slate-400',
  JAVA_PROVIDED:    'bg-slate-800 text-slate-400',
  EMPLOYEE_INPUT:   'bg-teal-950 text-teal-400',   // NEW
  RATE_BY_QUANTITY: 'bg-sky-950 text-sky-400',
  PERCENTAGE:       'bg-violet-950 text-violet-400',
  AGGREGATE:        'bg-green-950 text-green-400',
}
```

- [ ] **Step 2: Run TypeScript check**

```bash
cd c:/Users/bifor/Documents/Proyectos/B4RRHH/b4rrhh_designer
npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit in the designer repo**

```bash
git add src/app/canvas/types.ts
git commit -m "feat: add EMPLOYEE_INPUT calculation type to designer canvas"
```

---

## Self-Review Checklist

After all tasks are complete, verify:

- [ ] `mvn test` passes in `b4rrhh_backend` with no failures
- [ ] `npx tsc --noEmit` passes in `b4rrhh_designer`
- [ ] POST `/employees/ESP/GEN/00001/payroll-inputs` creates a record (manual test)
- [ ] GET `/employees/ESP/GEN/00001/payroll-inputs?period=202604` returns the record
- [ ] Creating an `EMPLOYEE_INPUT` concept in the designer shows it with teal badge and no input ports
- [ ] A payroll recalculation for an employee with an `EMPLOYEE_INPUT` concept and a registered input reads the correct quantity
