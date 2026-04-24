# Nómina — Gestión de Recibos Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `/nomina/recibos` screen to the B4RRHH backoffice that lists calculated payrolls with filters, renders them as a Spanish recibo de nómina, and supports Invalidar / Validar / Recalcular state transitions.

**Architecture:** Backend-first: extend the OpenAPI spec, implement missing endpoints (search + recalculate), then regenerate the Angular API client and build the frontend feature following the existing work-center master/detail pattern (client → gateway → mapper → models → store → UI components). No arithmetic in the frontend — all amounts come from the backend.

**Tech Stack:** Java 21 / Spring Boot / JPA (backend); Angular 21 standalone components, PrimeNG, signals (frontend); OpenAPI 3.0 contract as source of truth.

---

## File Map

### Backend — new files
| File | Responsibility |
|---|---|
| `src/main/java/com/b4rrhh/payroll/infrastructure/web/dto/PayrollSummaryResponse.java` | DTO for list items (no concept lines) |
| `src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsQuery.java` | Query record |
| `src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsUseCase.java` | Port interface |
| `src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsService.java` | Implementation |
| `src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollCommand.java` | Command record |
| `src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollUseCase.java` | Port interface |
| `src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollService.java` | Implementation |

### Backend — modified files
| File | Change |
|---|---|
| `openapi/personnel-administration-api.yaml` | Add `GET /payrolls`, `POST /payrolls/{key}/recalculate`, `PayrollSummaryResponse` schema |
| `src/main/java/com/b4rrhh/payroll/domain/port/PayrollRepository.java` | Add `findByFilters()` |
| `src/main/java/com/b4rrhh/payroll/infrastructure/persistence/SpringDataPayrollRepository.java` | Add JPQL query |
| `src/main/java/com/b4rrhh/payroll/infrastructure/persistence/PayrollPersistenceAdapter.java` | Implement `findByFilters()` |
| `src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollController.java` | Add 2 endpoints + constructor arg |
| `src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandler.java` | Handle `PayrollRecalculationNotAllowedException` |

### Backend — test files
| File | What it tests |
|---|---|
| `src/test/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsServiceTest.java` | Filter combinations, empty results |
| `src/test/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollServiceTest.java` | NOT_VALID guard, delegates to CalculatePayrollUnitUseCase |
| `src/test/java/com/b4rrhh/payroll/infrastructure/web/PayrollControllerTest.java` | 2 new test methods (search, recalculate) |

### Frontend — new files
| File | Responsibility |
|---|---|
| `src/app/features/nomina/recibos/recibos.routes.ts` | Lazy route definition |
| `src/app/features/nomina/recibos/client/recibos.client.ts` | Thin wrapper around generated PayrollService |
| `src/app/features/nomina/recibos/gateway/recibos.gateway.ts` | Maps API responses → domain models |
| `src/app/features/nomina/recibos/mapper/recibos.mapper.ts` | Pure mapping functions |
| `src/app/features/nomina/recibos/mapper/recibos.mapper.spec.ts` | Unit tests for mapper |
| `src/app/features/nomina/recibos/models/payroll-business-key.model.ts` | Business key type |
| `src/app/features/nomina/recibos/models/payroll-summary.model.ts` | List item domain model + PayrollStatus type |
| `src/app/features/nomina/recibos/models/payroll-concept.model.ts` | Concept line domain model |
| `src/app/features/nomina/recibos/models/recibos-filters.model.ts` | Filter form value |
| `src/app/features/nomina/recibos/store/recibos.store.ts` | Signals store |
| `src/app/features/nomina/recibos/store/recibos.store.spec.ts` | Store unit tests |
| `src/app/features/nomina/recibos/ui/recibos-page.component.ts` | Master/detail shell |
| `src/app/features/nomina/recibos/ui/recibos-list.component.ts` | Left panel (filters + list) |
| `src/app/features/nomina/recibos/ui/recibos-detail.component.ts` | Right panel (action bar + folio) |
| `src/app/features/nomina/recibos/ui/recibos-folio.component.ts` | The payslip table |

### Frontend — modified files
| File | Change |
|---|---|
| `src/app/app.routes.ts` | Add `nomina/recibos` child route |
| `src/app/core/i18n/app-texts.ts` | Add `sectionPayroll`, `sectionRecibos` |
| `src/app/core/layout/app-shell/app-shell.component.ts` | Add Nómina nav section |

---

## Task 1: OpenAPI spec — search + recalculate endpoints

**Files:**
- Modify: `openapi/personnel-administration-api.yaml`

- [ ] **Step 1: Add `PayrollSummaryResponse` schema**

Find the `components.schemas` section and add after the existing `PayrollResponse` schema:

```yaml
    PayrollSummaryResponse:
      type: object
      required:
        - ruleSystemCode
        - employeeTypeCode
        - employeeNumber
        - payrollPeriodCode
        - payrollTypeCode
        - presenceNumber
        - status
        - calculatedAt
      properties:
        ruleSystemCode:
          type: string
        employeeTypeCode:
          type: string
        employeeNumber:
          type: string
        payrollPeriodCode:
          type: string
        payrollTypeCode:
          type: string
        presenceNumber:
          type: integer
        status:
          type: string
          enum: [NOT_VALID, CALCULATED, EXPLICIT_VALIDATED, DEFINITIVE]
        calculatedAt:
          type: string
          format: date-time
```

- [ ] **Step 2: Add `GET /payrolls` path**

In the `paths` section, add a new path entry (keep paths alphabetical):

```yaml
  /payrolls:
    get:
      summary: Search payrolls
      operationId: searchPayrolls
      tags:
        - Payroll
      parameters:
        - name: payrollPeriodCode
          in: query
          required: false
          schema:
            type: string
        - name: employeeNumber
          in: query
          required: false
          schema:
            type: string
        - name: status
          in: query
          required: false
          schema:
            type: string
            enum: [NOT_VALID, CALCULATED, EXPLICIT_VALIDATED, DEFINITIVE]
      responses:
        '200':
          description: List of matching payrolls
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/PayrollSummaryResponse'
```

- [ ] **Step 3: Add `POST /payrolls/{key}/recalculate` path**

In `paths`, add after the existing `/payrolls/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/{payrollPeriodCode}/{payrollTypeCode}/{presenceNumber}/validate` entry:

```yaml
  /payrolls/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/{payrollPeriodCode}/{payrollTypeCode}/{presenceNumber}/recalculate:
    post:
      summary: Recalculate a payroll (only allowed from NOT_VALID)
      operationId: recalculatePayroll
      tags:
        - Payroll
      parameters:
        - name: ruleSystemCode
          in: path
          required: true
          schema: { type: string }
        - name: employeeTypeCode
          in: path
          required: true
          schema: { type: string }
        - name: employeeNumber
          in: path
          required: true
          schema: { type: string }
        - name: payrollPeriodCode
          in: path
          required: true
          schema: { type: string }
        - name: payrollTypeCode
          in: path
          required: true
          schema: { type: string }
        - name: presenceNumber
          in: path
          required: true
          schema: { type: integer }
      responses:
        '200':
          description: Recalculated payroll
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PayrollResponse'
        '409':
          description: Payroll is not in NOT_VALID status
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PayrollErrorResponse'
```

- [ ] **Step 4: Commit**

```bash
git add openapi/personnel-administration-api.yaml
git commit -m "feat(openapi): add search payrolls and recalculate endpoints"
```

---

## Task 2: Backend — search payrolls use case

**Files:**
- Create: `src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsQuery.java`
- Create: `src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsUseCase.java`
- Create: `src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsService.java`
- Create: `src/test/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsServiceTest.java`
- Modify: `src/main/java/com/b4rrhh/payroll/domain/port/PayrollRepository.java`
- Modify: `src/main/java/com/b4rrhh/payroll/infrastructure/persistence/SpringDataPayrollRepository.java`
- Modify: `src/main/java/com/b4rrhh/payroll/infrastructure/persistence/PayrollPersistenceAdapter.java`

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsServiceTest.java
package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollStatus;
import com.b4rrhh.payroll.domain.port.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchPayrollsServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    private SearchPayrollsService service;

    @BeforeEach
    void setUp() {
        service = new SearchPayrollsService(payrollRepository);
    }

    @Test
    void returnsMatchingPayrollsForPeriodFilter() {
        Payroll payroll = minimalPayroll("MAS000001", "202604", PayrollStatus.CALCULATED);
        when(payrollRepository.findByFilters(eq("202604"), eq(null), eq(null)))
                .thenReturn(List.of(payroll));

        List<Payroll> result = service.search(new SearchPayrollsQuery("202604", null, null));

        assertEquals(1, result.size());
        assertEquals("MAS000001", result.get(0).getEmployeeNumber());
    }

    @Test
    void returnsEmptyListWhenNoMatch() {
        when(payrollRepository.findByFilters(eq("202605"), eq(null), eq(null)))
                .thenReturn(List.of());

        List<Payroll> result = service.search(new SearchPayrollsQuery("202605", null, null));

        assertEquals(0, result.size());
    }

    @Test
    void passesAllFiltersToRepository() {
        when(payrollRepository.findByFilters(eq("202604"), eq("MAS000001"), eq(PayrollStatus.CALCULATED)))
                .thenReturn(List.of());

        service.search(new SearchPayrollsQuery("202604", "MAS000001", PayrollStatus.CALCULATED));

        // no exception = filters were forwarded correctly
    }

    private Payroll minimalPayroll(String employeeNumber, String periodCode, PayrollStatus status) {
        return Payroll.rehydrate(
                1L, "MAS", "EMP", employeeNumber, periodCode, "MENSUAL", 1,
                status, null,
                LocalDateTime.now(), "ENGINE_001", "1.0",
                List.of(), List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=SearchPayrollsServiceTest -pl . 2>&1 | tail -20
```
Expected: FAIL — `SearchPayrollsService`, `SearchPayrollsQuery` not found.

- [ ] **Step 3: Create `SearchPayrollsQuery`**

```java
// src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsQuery.java
package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.PayrollStatus;

public record SearchPayrollsQuery(
        String payrollPeriodCode,
        String employeeNumber,
        PayrollStatus status
) {}
```

- [ ] **Step 4: Create `SearchPayrollsUseCase`**

```java
// src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsUseCase.java
package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.Payroll;

import java.util.List;

public interface SearchPayrollsUseCase {
    List<Payroll> search(SearchPayrollsQuery query);
}
```

- [ ] **Step 5: Add `findByFilters` to `PayrollRepository`**

```java
// Add to existing PayrollRepository interface:
List<Payroll> findByFilters(String payrollPeriodCode, String employeeNumber, PayrollStatus status);
```

- [ ] **Step 6: Create `SearchPayrollsService`**

```java
// src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsService.java
package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.port.PayrollRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchPayrollsService implements SearchPayrollsUseCase {

    private final PayrollRepository payrollRepository;

    public SearchPayrollsService(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    @Override
    public List<Payroll> search(SearchPayrollsQuery query) {
        return payrollRepository.findByFilters(
                query.payrollPeriodCode(),
                query.employeeNumber(),
                query.status()
        );
    }
}
```

- [ ] **Step 7: Add JPQL query to `SpringDataPayrollRepository`**

```java
// Add to SpringDataPayrollRepository:
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Query("SELECT p FROM PayrollEntity p WHERE " +
       "(:payrollPeriodCode IS NULL OR p.payrollPeriodCode = :payrollPeriodCode) AND " +
       "(:employeeNumber IS NULL OR p.employeeNumber = :employeeNumber) AND " +
       "(:status IS NULL OR p.status = :status) " +
       "ORDER BY p.calculatedAt DESC")
List<PayrollEntity> findByFilters(
    @Param("payrollPeriodCode") String payrollPeriodCode,
    @Param("employeeNumber") String employeeNumber,
    @Param("status") PayrollStatus status
);
```

- [ ] **Step 8: Implement `findByFilters` in `PayrollPersistenceAdapter`**

```java
// Add to PayrollPersistenceAdapter:
import java.util.List;

@Override
public List<Payroll> findByFilters(String payrollPeriodCode, String employeeNumber, PayrollStatus status) {
    return springDataPayrollRepository
            .findByFilters(payrollPeriodCode, employeeNumber, status)
            .stream()
            .map(this::toDomain)
            .toList();
}
```

- [ ] **Step 9: Run tests to verify they pass**

```bash
mvn test -Dtest=SearchPayrollsServiceTest -pl . 2>&1 | tail -10
```
Expected: `BUILD SUCCESS`, 3 tests passed.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsQuery.java \
        src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsUseCase.java \
        src/main/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsService.java \
        src/main/java/com/b4rrhh/payroll/domain/port/PayrollRepository.java \
        src/main/java/com/b4rrhh/payroll/infrastructure/persistence/SpringDataPayrollRepository.java \
        src/main/java/com/b4rrhh/payroll/infrastructure/persistence/PayrollPersistenceAdapter.java \
        src/test/java/com/b4rrhh/payroll/application/usecase/SearchPayrollsServiceTest.java
git commit -m "feat(payroll): add search payrolls use case with period/employee/status filters"
```

---

## Task 3: Backend — recalculate payroll use case

**Files:**
- Create: `src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollCommand.java`
- Create: `src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollUseCase.java`
- Create: `src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollService.java`
- Create: `src/test/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollServiceTest.java
package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.exception.PayrollNotFoundException;
import com.b4rrhh.payroll.domain.exception.PayrollRecalculationNotAllowedException;
import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollStatus;
import com.b4rrhh.payroll.domain.port.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecalculatePayrollServiceTest {

    @Mock
    private PayrollRepository payrollRepository;
    @Mock
    private CalculatePayrollUnitUseCase calculatePayrollUnitUseCase;

    private RecalculatePayrollService service;

    @BeforeEach
    void setUp() {
        service = new RecalculatePayrollService(payrollRepository, calculatePayrollUnitUseCase);
    }

    @Test
    void delegatesToCalculateUnitWhenPayrollIsNotValid() {
        RecalculatePayrollCommand command = command("MAS", "EMP", "MAS000001", "202604", "MENSUAL", 1);
        Payroll notValidPayroll = payroll("MAS000001", "202604", PayrollStatus.NOT_VALID, "ENGINE_001", "1.0");
        Payroll recalculated = payroll("MAS000001", "202604", PayrollStatus.CALCULATED, "ENGINE_001", "1.0");

        when(payrollRepository.findByBusinessKey("MAS", "EMP", "MAS000001", "202604", "MENSUAL", 1))
                .thenReturn(Optional.of(notValidPayroll));
        when(calculatePayrollUnitUseCase.calculate(any())).thenReturn(recalculated);

        Payroll result = service.recalculate(command);

        assertEquals(PayrollStatus.CALCULATED, result.getStatus());

        ArgumentCaptor<CalculatePayrollUnitCommand> captor = ArgumentCaptor.forClass(CalculatePayrollUnitCommand.class);
        verify(calculatePayrollUnitUseCase).calculate(captor.capture());
        CalculatePayrollUnitCommand sent = captor.getValue();
        assertEquals("MAS000001", sent.employeeNumber());
        assertEquals("202604", sent.payrollPeriodCode());
        assertEquals("ENGINE_001", sent.calculationEngineCode());
        assertEquals("1.0", sent.calculationEngineVersion());
    }

    @Test
    void throwsWhenPayrollNotFound() {
        when(payrollRepository.findByBusinessKey(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () ->
                service.recalculate(command("MAS", "EMP", "MAS000001", "202604", "MENSUAL", 1)));
    }

    @Test
    void throwsWhenPayrollIsNotInNotValidState() {
        when(payrollRepository.findByBusinessKey("MAS", "EMP", "MAS000001", "202604", "MENSUAL", 1))
                .thenReturn(Optional.of(payroll("MAS000001", "202604", PayrollStatus.CALCULATED, "ENGINE_001", "1.0")));

        assertThrows(PayrollRecalculationNotAllowedException.class, () ->
                service.recalculate(command("MAS", "EMP", "MAS000001", "202604", "MENSUAL", 1)));
    }

    @Test
    void derivesPeriodDatesFrom6DigitPeriodCode() {
        when(payrollRepository.findByBusinessKey(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(payroll("MAS000001", "202604", PayrollStatus.NOT_VALID, "ENG", "1")));
        when(calculatePayrollUnitUseCase.calculate(any())).thenReturn(
                payroll("MAS000001", "202604", PayrollStatus.CALCULATED, "ENG", "1"));

        service.recalculate(command("MAS", "EMP", "MAS000001", "202604", "MENSUAL", 1));

        ArgumentCaptor<CalculatePayrollUnitCommand> captor = ArgumentCaptor.forClass(CalculatePayrollUnitCommand.class);
        verify(calculatePayrollUnitUseCase).calculate(captor.capture());
        assertEquals(1, captor.getValue().periodStart().getDayOfMonth());
        assertEquals(4, captor.getValue().periodStart().getMonthValue());
        assertEquals(2026, captor.getValue().periodStart().getYear());
        assertEquals(30, captor.getValue().periodEnd().getDayOfMonth());
    }

    private RecalculatePayrollCommand command(String rsc, String etc, String en, String ppc, String ptc, int pn) {
        return new RecalculatePayrollCommand(rsc, etc, en, ppc, ptc, pn);
    }

    private Payroll payroll(String employeeNumber, String periodCode, PayrollStatus status, String engCode, String engVer) {
        return Payroll.rehydrate(
                1L, "MAS", "EMP", employeeNumber, periodCode, "MENSUAL", 1,
                status, null, LocalDateTime.now(), engCode, engVer,
                List.of(), List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=RecalculatePayrollServiceTest -pl . 2>&1 | tail -10
```
Expected: FAIL — `RecalculatePayrollService`, `RecalculatePayrollCommand` not found.

- [ ] **Step 3: Create `RecalculatePayrollCommand`**

```java
// src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollCommand.java
package com.b4rrhh.payroll.application.usecase;

public record RecalculatePayrollCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber
) {}
```

- [ ] **Step 4: Create `RecalculatePayrollUseCase`**

```java
// src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollUseCase.java
package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.Payroll;

public interface RecalculatePayrollUseCase {
    Payroll recalculate(RecalculatePayrollCommand command);
}
```

- [ ] **Step 5: Create `RecalculatePayrollService`**

```java
// src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollService.java
package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.exception.PayrollNotFoundException;
import com.b4rrhh.payroll.domain.exception.PayrollRecalculationNotAllowedException;
import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.port.PayrollRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class RecalculatePayrollService implements RecalculatePayrollUseCase {

    private final PayrollRepository payrollRepository;
    private final CalculatePayrollUnitUseCase calculatePayrollUnitUseCase;

    public RecalculatePayrollService(
            PayrollRepository payrollRepository,
            CalculatePayrollUnitUseCase calculatePayrollUnitUseCase
    ) {
        this.payrollRepository = payrollRepository;
        this.calculatePayrollUnitUseCase = calculatePayrollUnitUseCase;
    }

    @Override
    public Payroll recalculate(RecalculatePayrollCommand command) {
        Payroll payroll = payrollRepository.findByBusinessKey(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.payrollPeriodCode(),
                command.payrollTypeCode(),
                command.presenceNumber()
        ).orElseThrow(() -> new PayrollNotFoundException(
                command.ruleSystemCode(), command.employeeTypeCode(), command.employeeNumber(),
                command.payrollPeriodCode(), command.payrollTypeCode(), command.presenceNumber()
        ));

        if (!payroll.canBeRecalculated()) {
            throw new PayrollRecalculationNotAllowedException(
                    command.ruleSystemCode(), command.employeeTypeCode(), command.employeeNumber(),
                    command.payrollPeriodCode(), command.payrollTypeCode(), command.presenceNumber(),
                    payroll.getStatus()
            );
        }

        LocalDate periodStart = parsePeriodStart(command.payrollPeriodCode());
        return calculatePayrollUnitUseCase.calculate(new CalculatePayrollUnitCommand(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.payrollPeriodCode(),
                command.payrollTypeCode(),
                command.presenceNumber(),
                periodStart,
                periodStart.withDayOfMonth(periodStart.lengthOfMonth()),
                payroll.getCalculationEngineCode(),
                payroll.getCalculationEngineVersion()
        ));
    }

    private LocalDate parsePeriodStart(String periodCode) {
        int year = Integer.parseInt(periodCode.substring(0, 4));
        int month = Integer.parseInt(periodCode.substring(4, 6));
        return LocalDate.of(year, month, 1);
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**

```bash
mvn test -Dtest=RecalculatePayrollServiceTest -pl . 2>&1 | tail -10
```
Expected: `BUILD SUCCESS`, 4 tests passed.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollCommand.java \
        src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollUseCase.java \
        src/main/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollService.java \
        src/test/java/com/b4rrhh/payroll/application/usecase/RecalculatePayrollServiceTest.java
git commit -m "feat(payroll): add recalculate payroll use case"
```

---

## Task 4: Backend — controller endpoints + DTO + exception handler

**Files:**
- Create: `src/main/java/com/b4rrhh/payroll/infrastructure/web/dto/PayrollSummaryResponse.java`
- Modify: `src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollController.java`
- Modify: `src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandler.java`
- Modify: `src/test/java/com/b4rrhh/payroll/infrastructure/web/PayrollControllerTest.java`

- [ ] **Step 1: Write the failing controller tests**

Add these two test methods to `PayrollControllerTest.java`. First add the new mocks and constructor at the `@BeforeEach` level (the existing controller constructor call will need updating):

```java
// Add at top of PayrollControllerTest:
@Mock
private SearchPayrollsUseCase searchPayrollsUseCase;
@Mock
private RecalculatePayrollUseCase recalculatePayrollUseCase;
```

Update the `setUp()` method to pass the new use cases:

```java
@BeforeEach
void setUp() {
    controller = new PayrollController(
            calculatePayrollUseCase,
            getPayrollByBusinessKeyUseCase,
            invalidatePayrollUseCase,
            validatePayrollUseCase,
            finalizePayrollUseCase,
            bulkInvalidatePayrollUseCase,
            searchPayrollsUseCase,
            recalculatePayrollUseCase,
            new PayrollResponseAssembler()
    );
}
```

Add the new test methods:

```java
@Test
void searchesPayrollsByFilters() {
    Payroll payroll = payroll(PayrollStatus.CALCULATED, null);
    when(searchPayrollsUseCase.search(any(SearchPayrollsQuery.class))).thenReturn(List.of(payroll));

    ResponseEntity<List<PayrollSummaryResponse>> response = controller.search("202604", "MAS000001", "CALCULATED");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
}

@Test
void recalculatesPayroll() {
    Payroll recalculated = payroll(PayrollStatus.CALCULATED, null);
    when(recalculatePayrollUseCase.recalculate(any(RecalculatePayrollCommand.class))).thenReturn(recalculated);

    ResponseEntity<PayrollResponse> response = controller.recalculate(
            "MAS", "EMP", "MAS000001", "202604", "MENSUAL", 1
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("CALCULATED", response.getBody().status());
}
```

- [ ] **Step 2: Run to verify tests fail**

```bash
mvn test -Dtest=PayrollControllerTest -pl . 2>&1 | tail -20
```
Expected: compile error — `searchPayrollsUseCase` and `recalculatePayrollUseCase` not in constructor.

- [ ] **Step 3: Create `PayrollSummaryResponse`**

```java
// src/main/java/com/b4rrhh/payroll/infrastructure/web/dto/PayrollSummaryResponse.java
package com.b4rrhh.payroll.infrastructure.web.dto;

import java.time.LocalDateTime;

public record PayrollSummaryResponse(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String payrollPeriodCode,
        String payrollTypeCode,
        Integer presenceNumber,
        String status,
        LocalDateTime calculatedAt
) {}
```

- [ ] **Step 4: Add search + recalculate endpoints to `PayrollController`**

Add new constructor parameters and inject them:

```java
// Add to imports:
import com.b4rrhh.payroll.application.usecase.RecalculatePayrollCommand;
import com.b4rrhh.payroll.application.usecase.RecalculatePayrollUseCase;
import com.b4rrhh.payroll.application.usecase.SearchPayrollsQuery;
import com.b4rrhh.payroll.application.usecase.SearchPayrollsUseCase;
import com.b4rrhh.payroll.domain.model.PayrollStatus;
import com.b4rrhh.payroll.infrastructure.web.dto.PayrollSummaryResponse;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
```

Add fields (after `bulkInvalidatePayrollUseCase`):
```java
private final SearchPayrollsUseCase searchPayrollsUseCase;
private final RecalculatePayrollUseCase recalculatePayrollUseCase;
```

Update constructor to include both (add at end of parameter list):
```java
SearchPayrollsUseCase searchPayrollsUseCase,
RecalculatePayrollUseCase recalculatePayrollUseCase,
```

Add in constructor body:
```java
this.searchPayrollsUseCase = searchPayrollsUseCase;
this.recalculatePayrollUseCase = recalculatePayrollUseCase;
```

Add endpoints at the end of the class:

```java
@GetMapping
public ResponseEntity<List<PayrollSummaryResponse>> search(
        @RequestParam(required = false) String payrollPeriodCode,
        @RequestParam(required = false) String employeeNumber,
        @RequestParam(required = false) String status
) {
    PayrollStatus parsedStatus = status != null ? PayrollStatus.valueOf(status) : null;
    List<PayrollSummaryResponse> body = searchPayrollsUseCase
            .search(new SearchPayrollsQuery(payrollPeriodCode, employeeNumber, parsedStatus))
            .stream()
            .map(p -> new PayrollSummaryResponse(
                    p.getRuleSystemCode(), p.getEmployeeTypeCode(), p.getEmployeeNumber(),
                    p.getPayrollPeriodCode(), p.getPayrollTypeCode(), p.getPresenceNumber(),
                    p.getStatus().name(), p.getCalculatedAt()
            ))
            .toList();
    return ResponseEntity.ok(body);
}

@PostMapping("/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/{payrollPeriodCode}/{payrollTypeCode}/{presenceNumber}/recalculate")
public ResponseEntity<PayrollResponse> recalculate(
        @PathVariable String ruleSystemCode,
        @PathVariable String employeeTypeCode,
        @PathVariable String employeeNumber,
        @PathVariable String payrollPeriodCode,
        @PathVariable String payrollTypeCode,
        @PathVariable Integer presenceNumber
) {
    Payroll payroll = recalculatePayrollUseCase.recalculate(new RecalculatePayrollCommand(
            ruleSystemCode, employeeTypeCode, employeeNumber,
            payrollPeriodCode, payrollTypeCode, presenceNumber
    ));
    return ResponseEntity.ok(payrollResponseAssembler.toResponse(payroll));
}
```

- [ ] **Step 5: Add `PayrollRecalculationNotAllowedException` handler to `PayrollExceptionHandler`**

```java
// Add to PayrollExceptionHandler:
import com.b4rrhh.payroll.domain.exception.PayrollRecalculationNotAllowedException;
import org.springframework.http.HttpStatus;

@ExceptionHandler(PayrollRecalculationNotAllowedException.class)
public ResponseEntity<PayrollErrorResponse> handleRecalculationNotAllowed(PayrollRecalculationNotAllowedException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new PayrollErrorResponse(ex.getMessage()));
}
```

- [ ] **Step 6: Run tests to verify they pass**

```bash
mvn test -Dtest=PayrollControllerTest -pl . 2>&1 | tail -10
```
Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Run all payroll tests**

```bash
mvn test -Dtest="com.b4rrhh.payroll.**" -pl . 2>&1 | tail -15
```
Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/b4rrhh/payroll/infrastructure/web/dto/PayrollSummaryResponse.java \
        src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollController.java \
        src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandler.java \
        src/test/java/com/b4rrhh/payroll/infrastructure/web/PayrollControllerTest.java
git commit -m "feat(payroll): expose search and recalculate REST endpoints"
```

---

## Task 5: Regenerate frontend API client

**Files:**
- Regenerate: `src/app/core/api/generated/` (entire directory, auto-generated)

- [ ] **Step 1: Ensure backend is running (needed for api:pull)**

```bash
# In b4rrhh_backend directory:
mvn spring-boot:run
# Wait for "Started Application" in logs
```

- [ ] **Step 2: Regenerate the API client**

```bash
# In b4rrhh_frontend directory:
npm run api:refresh
```

Expected output: pulls `personnel-administration-api.yaml` from the running backend and generates TypeScript models + services under `src/app/core/api/generated/`.

- [ ] **Step 3: Verify new service methods exist**

```bash
grep -n "searchPayrolls\|recalculatePayroll\|PayrollSummaryResponse" src/app/core/api/generated/**/*.ts
```

Expected: at least one hit for each name.

- [ ] **Step 4: Commit**

```bash
git add src/app/core/api/generated/
git commit -m "chore(frontend): regenerate API client with search and recalculate payroll"
```
*(Run this commit from `b4rrhh_frontend` directory.)*

---

## Task 6: Frontend domain models + mapper

**Files:**
- Create: `src/app/features/nomina/recibos/models/payroll-business-key.model.ts`
- Create: `src/app/features/nomina/recibos/models/payroll-summary.model.ts`
- Create: `src/app/features/nomina/recibos/models/payroll-concept.model.ts`
- Create: `src/app/features/nomina/recibos/models/recibos-filters.model.ts`
- Create: `src/app/features/nomina/recibos/mapper/recibos.mapper.ts`
- Create: `src/app/features/nomina/recibos/mapper/recibos.mapper.spec.ts`

- [ ] **Step 1: Write the failing mapper tests**

```typescript
// src/app/features/nomina/recibos/mapper/recibos.mapper.spec.ts
import { mapPayrollSummaryResponseToModel, mapPayrollConceptResponseToModel } from './recibos.mapper';
import { PayrollSummaryResponse } from '../../../core/api/generated/model/payroll-summary-response';
import { PayrollConceptResponse } from '../../../core/api/generated/model/payroll-concept-response';

describe('recibos.mapper', () => {
  describe('mapPayrollSummaryResponseToModel', () => {
    it('maps all fields correctly', () => {
      const response: PayrollSummaryResponse = {
        ruleSystemCode: 'MAS',
        employeeTypeCode: 'EMP',
        employeeNumber: 'MAS000001',
        payrollPeriodCode: '202604',
        payrollTypeCode: 'MENSUAL',
        presenceNumber: 1,
        status: 'CALCULATED',
        calculatedAt: '2026-04-24T12:00:00',
      };

      const model = mapPayrollSummaryResponseToModel(response);

      expect(model.ruleSystemCode).toBe('MAS');
      expect(model.employeeNumber).toBe('MAS000001');
      expect(model.payrollPeriodCode).toBe('202604');
      expect(model.status).toBe('CALCULATED');
    });
  });

  describe('mapPayrollConceptResponseToModel', () => {
    it('maps amount as null when undefined', () => {
      const response: PayrollConceptResponse = {
        lineNumber: 1,
        conceptCode: '001',
        conceptLabel: 'Salario base',
        amount: undefined,
        quantity: undefined,
        rate: undefined,
        conceptNatureCode: 'EARNING',
        originPeriodCode: '202604',
        displayOrder: 10,
      };

      const model = mapPayrollConceptResponseToModel(response);

      expect(model.amount).toBeNull();
      expect(model.quantity).toBeNull();
      expect(model.rate).toBeNull();
      expect(model.conceptNatureCode).toBe('EARNING');
    });

    it('maps numeric values when present', () => {
      const response: PayrollConceptResponse = {
        lineNumber: 1,
        conceptCode: '001',
        conceptLabel: 'Salario base',
        amount: 2100,
        quantity: 30,
        rate: 70,
        conceptNatureCode: 'EARNING',
        originPeriodCode: '202604',
        displayOrder: 10,
      };

      const model = mapPayrollConceptResponseToModel(response);

      expect(model.amount).toBe(2100);
      expect(model.quantity).toBe(30);
      expect(model.rate).toBe(70);
    });
  });
});
```

- [ ] **Step 2: Run to verify tests fail**

```bash
npm run test -- --run --reporter=verbose 2>&1 | grep -A5 "recibos.mapper"
```
Expected: FAIL — mapper file not found.

- [ ] **Step 3: Create model files**

```typescript
// src/app/features/nomina/recibos/models/payroll-business-key.model.ts
export interface PayrollBusinessKey {
  ruleSystemCode: string;
  employeeTypeCode: string;
  employeeNumber: string;
  payrollPeriodCode: string;
  payrollTypeCode: string;
  presenceNumber: number;
}
```

```typescript
// src/app/features/nomina/recibos/models/payroll-summary.model.ts
import { PayrollBusinessKey } from './payroll-business-key.model';

export type PayrollStatus = 'NOT_VALID' | 'CALCULATED' | 'EXPLICIT_VALIDATED' | 'DEFINITIVE';

export interface PayrollSummaryModel extends PayrollBusinessKey {
  status: PayrollStatus;
  calculatedAt: string;
}
```

```typescript
// src/app/features/nomina/recibos/models/payroll-concept.model.ts
export interface PayrollConceptModel {
  lineNumber: number;
  conceptCode: string;
  conceptLabel: string;
  amount: number | null;
  quantity: number | null;
  rate: number | null;
  conceptNatureCode: string;
  originPeriodCode: string | null;
  displayOrder: number;
}
```

```typescript
// src/app/features/nomina/recibos/models/recibos-filters.model.ts
import { PayrollStatus } from './payroll-summary.model';

export interface RecibosFilters {
  payrollPeriodCode: string;
  employeeNumber: string;
  status: PayrollStatus | '';
}
```

- [ ] **Step 4: Create mapper**

```typescript
// src/app/features/nomina/recibos/mapper/recibos.mapper.ts
import { PayrollSummaryResponse } from '../../../core/api/generated/model/payroll-summary-response';
import { PayrollConceptResponse } from '../../../core/api/generated/model/payroll-concept-response';
import { PayrollSummaryModel } from '../models/payroll-summary.model';
import { PayrollConceptModel } from '../models/payroll-concept.model';

export function mapPayrollSummaryResponseToModel(response: PayrollSummaryResponse): PayrollSummaryModel {
  return {
    ruleSystemCode: response.ruleSystemCode,
    employeeTypeCode: response.employeeTypeCode,
    employeeNumber: response.employeeNumber,
    payrollPeriodCode: response.payrollPeriodCode,
    payrollTypeCode: response.payrollTypeCode,
    presenceNumber: response.presenceNumber,
    status: response.status as PayrollSummaryModel['status'],
    calculatedAt: response.calculatedAt,
  };
}

export function mapPayrollConceptResponseToModel(response: PayrollConceptResponse): PayrollConceptModel {
  return {
    lineNumber: response.lineNumber ?? 0,
    conceptCode: response.conceptCode ?? '',
    conceptLabel: response.conceptLabel ?? '',
    amount: response.amount ?? null,
    quantity: response.quantity ?? null,
    rate: response.rate ?? null,
    conceptNatureCode: response.conceptNatureCode ?? '',
    originPeriodCode: response.originPeriodCode ?? null,
    displayOrder: response.displayOrder ?? 0,
  };
}
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
npm run test -- --run --reporter=verbose 2>&1 | grep -A5 "recibos.mapper"
```
Expected: all 3 tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/app/features/nomina/
git commit -m "feat(nomina): add domain models and mapper for recibos feature"
```

---

## Task 7: Frontend — client + gateway

**Files:**
- Create: `src/app/features/nomina/recibos/client/recibos.client.ts`
- Create: `src/app/features/nomina/recibos/gateway/recibos.gateway.ts`

- [ ] **Step 1: Create `RecibosClient`**

```typescript
// src/app/features/nomina/recibos/client/recibos.client.ts
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { PayrollService } from '../../../core/api/generated/api/payroll.service';
import { PayrollSummaryResponse } from '../../../core/api/generated/model/payroll-summary-response';
import { PayrollResponse } from '../../../core/api/generated/model/payroll-response';
import { PayrollBusinessKey } from '../models/payroll-business-key.model';
import { RecibosFilters } from '../models/recibos-filters.model';

@Injectable({ providedIn: 'root' })
export class RecibosClient {
  private readonly api = inject(PayrollService);

  search(filters: RecibosFilters): Observable<Array<PayrollSummaryResponse>> {
    return this.api.searchPayrolls({
      payrollPeriodCode: filters.payrollPeriodCode || undefined,
      employeeNumber: filters.employeeNumber || undefined,
      status: filters.status || undefined,
    });
  }

  getByBusinessKey(key: PayrollBusinessKey): Observable<PayrollResponse> {
    return this.api.getPayrollByBusinessKey({
      ruleSystemCode: key.ruleSystemCode,
      employeeTypeCode: key.employeeTypeCode,
      employeeNumber: key.employeeNumber,
      payrollPeriodCode: key.payrollPeriodCode,
      payrollTypeCode: key.payrollTypeCode,
      presenceNumber: key.presenceNumber,
    });
  }

  invalidate(key: PayrollBusinessKey): Observable<PayrollResponse> {
    return this.api.invalidatePayroll({
      ruleSystemCode: key.ruleSystemCode,
      employeeTypeCode: key.employeeTypeCode,
      employeeNumber: key.employeeNumber,
      payrollPeriodCode: key.payrollPeriodCode,
      payrollTypeCode: key.payrollTypeCode,
      presenceNumber: key.presenceNumber,
      invalidatePayrollRequest: { statusReasonCode: 'MANUAL_INVALIDATION' },
    });
  }

  validate(key: PayrollBusinessKey): Observable<PayrollResponse> {
    return this.api.validatePayroll({
      ruleSystemCode: key.ruleSystemCode,
      employeeTypeCode: key.employeeTypeCode,
      employeeNumber: key.employeeNumber,
      payrollPeriodCode: key.payrollPeriodCode,
      payrollTypeCode: key.payrollTypeCode,
      presenceNumber: key.presenceNumber,
    });
  }

  recalculate(key: PayrollBusinessKey): Observable<PayrollResponse> {
    return this.api.recalculatePayroll({
      ruleSystemCode: key.ruleSystemCode,
      employeeTypeCode: key.employeeTypeCode,
      employeeNumber: key.employeeNumber,
      payrollPeriodCode: key.payrollPeriodCode,
      payrollTypeCode: key.payrollTypeCode,
      presenceNumber: key.presenceNumber,
    });
  }
}
```

- [ ] **Step 2: Create `RecibosGateway`**

```typescript
// src/app/features/nomina/recibos/gateway/recibos.gateway.ts
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { RecibosClient } from '../client/recibos.client';
import {
  mapPayrollSummaryResponseToModel,
  mapPayrollConceptResponseToModel,
} from '../mapper/recibos.mapper';
import { PayrollBusinessKey } from '../models/payroll-business-key.model';
import { PayrollConceptModel } from '../models/payroll-concept.model';
import { PayrollSummaryModel } from '../models/payroll-summary.model';
import { RecibosFilters } from '../models/recibos-filters.model';

@Injectable({ providedIn: 'root' })
export class RecibosGateway {
  private readonly client = inject(RecibosClient);

  search(filters: RecibosFilters): Observable<ReadonlyArray<PayrollSummaryModel>> {
    return this.client
      .search(filters)
      .pipe(map((items) => items.map(mapPayrollSummaryResponseToModel)));
  }

  getConcepts(key: PayrollBusinessKey): Observable<ReadonlyArray<PayrollConceptModel>> {
    return this.client.getByBusinessKey(key).pipe(
      map((response) =>
        (response.concepts ?? [])
          .map(mapPayrollConceptResponseToModel)
          .sort((a, b) => a.displayOrder - b.displayOrder),
      ),
    );
  }

  invalidate(key: PayrollBusinessKey): Observable<PayrollSummaryModel> {
    return this.client
      .invalidate(key)
      .pipe(map((r) => mapPayrollSummaryResponseToModel({ ...r, status: r.status ?? 'NOT_VALID' })));
  }

  validate(key: PayrollBusinessKey): Observable<PayrollSummaryModel> {
    return this.client
      .validate(key)
      .pipe(map((r) => mapPayrollSummaryResponseToModel({ ...r, status: r.status ?? 'EXPLICIT_VALIDATED' })));
  }

  recalculate(key: PayrollBusinessKey): Observable<PayrollSummaryModel> {
    return this.client
      .recalculate(key)
      .pipe(map((r) => mapPayrollSummaryResponseToModel({ ...r, status: r.status ?? 'CALCULATED' })));
  }
}
```

- [ ] **Step 3: Compile check**

```bash
npm run build -- --configuration=development 2>&1 | tail -20
```
Expected: no TypeScript errors.

- [ ] **Step 4: Commit**

```bash
git add src/app/features/nomina/recibos/client/ src/app/features/nomina/recibos/gateway/
git commit -m "feat(nomina): add recibos client and gateway"
```

---

## Task 8: Frontend — store

**Files:**
- Create: `src/app/features/nomina/recibos/store/recibos.store.ts`
- Create: `src/app/features/nomina/recibos/store/recibos.store.spec.ts`

- [ ] **Step 1: Write the failing store tests**

```typescript
// src/app/features/nomina/recibos/store/recibos.store.spec.ts
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RecibosStore } from './recibos.store';
import { RecibosGateway } from '../gateway/recibos.gateway';
import { PayrollSummaryModel } from '../models/payroll-summary.model';
import { PayrollConceptModel } from '../models/payroll-concept.model';
import { PayrollBusinessKey } from '../models/payroll-business-key.model';

const MOCK_KEY: PayrollBusinessKey = {
  ruleSystemCode: 'MAS', employeeTypeCode: 'EMP', employeeNumber: 'MAS000001',
  payrollPeriodCode: '202604', payrollTypeCode: 'MENSUAL', presenceNumber: 1,
};

const MOCK_SUMMARY: PayrollSummaryModel = {
  ...MOCK_KEY, status: 'CALCULATED', calculatedAt: '2026-04-24T10:00:00',
};

describe('RecibosStore', () => {
  let store: RecibosStore;
  let gateway: jest.Mocked<RecibosGateway>;

  beforeEach(() => {
    const gatewayMock: jest.Mocked<RecibosGateway> = {
      search: jest.fn(),
      getConcepts: jest.fn(),
      invalidate: jest.fn(),
      validate: jest.fn(),
      recalculate: jest.fn(),
    } as any;

    TestBed.configureTestingModule({
      providers: [RecibosStore, { provide: RecibosGateway, useValue: gatewayMock }],
    });

    store = TestBed.inject(RecibosStore);
    gateway = TestBed.inject(RecibosGateway) as jest.Mocked<RecibosGateway>;
  });

  it('initialises with empty state', () => {
    expect(store.payrolls()).toEqual([]);
    expect(store.selectedKey()).toBeNull();
    expect(store.concepts()).toEqual([]);
    expect(store.listLoading()).toBe(false);
  });

  it('loads payrolls on search', () => {
    gateway.search.mockReturnValue(of([MOCK_SUMMARY]));

    store.search({ payrollPeriodCode: '202604', employeeNumber: '', status: '' });

    expect(store.payrolls()).toHaveLength(1);
    expect(store.payrolls()[0].employeeNumber).toBe('MAS000001');
  });

  it('sets listError on search failure', () => {
    gateway.search.mockReturnValue(throwError(() => new Error('fail')));

    store.search({ payrollPeriodCode: '', employeeNumber: '', status: '' });

    expect(store.listError()).toBe('request-failed');
  });

  it('loads concepts when selecting a payroll', () => {
    const concept: PayrollConceptModel = {
      lineNumber: 1, conceptCode: '001', conceptLabel: 'Salario base',
      amount: 2100, quantity: 30, rate: 70, conceptNatureCode: 'EARNING',
      originPeriodCode: '202604', displayOrder: 10,
    };
    gateway.getConcepts.mockReturnValue(of([concept]));

    store.selectPayroll(MOCK_KEY);

    expect(store.selectedKey()).toEqual(MOCK_KEY);
    expect(store.concepts()).toHaveLength(1);
  });
});
```

- [ ] **Step 2: Run to verify tests fail**

```bash
npm run test -- --run --reporter=verbose 2>&1 | grep -A5 "RecibosStore"
```
Expected: FAIL — `RecibosStore` not found.

- [ ] **Step 3: Create `RecibosStore`**

```typescript
// src/app/features/nomina/recibos/store/recibos.store.ts
import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { take } from 'rxjs';

import { RecibosGateway } from '../gateway/recibos.gateway';
import { PayrollBusinessKey } from '../models/payroll-business-key.model';
import { PayrollConceptModel } from '../models/payroll-concept.model';
import { PayrollSummaryModel } from '../models/payroll-summary.model';
import { RecibosFilters } from '../models/recibos-filters.model';

export type RecibosErrorCode = 'request-failed' | 'not-found' | 'transition-failed';

@Injectable({ providedIn: 'root' })
export class RecibosStore {
  private readonly gateway = inject(RecibosGateway);

  private readonly payrollsState = signal<ReadonlyArray<PayrollSummaryModel>>([]);
  private readonly listLoadingState = signal(false);
  private readonly listErrorState = signal<RecibosErrorCode | null>(null);

  private readonly selectedKeyState = signal<PayrollBusinessKey | null>(null);
  private readonly conceptsState = signal<ReadonlyArray<PayrollConceptModel>>([]);
  private readonly conceptsLoadingState = signal(false);

  private readonly transitioningState = signal(false);
  private readonly transitionErrorState = signal<string | null>(null);

  readonly payrolls = this.payrollsState.asReadonly();
  readonly listLoading = this.listLoadingState.asReadonly();
  readonly listError = this.listErrorState.asReadonly();
  readonly selectedKey = this.selectedKeyState.asReadonly();
  readonly concepts = this.conceptsState.asReadonly();
  readonly conceptsLoading = this.conceptsLoadingState.asReadonly();
  readonly transitioning = this.transitioningState.asReadonly();
  readonly transitionError = this.transitionErrorState.asReadonly();

  readonly selectedPayroll = computed(() => {
    const key = this.selectedKeyState();
    if (!key) return null;
    return this.payrollsState().find(
      (p) =>
        p.ruleSystemCode === key.ruleSystemCode &&
        p.employeeTypeCode === key.employeeTypeCode &&
        p.employeeNumber === key.employeeNumber &&
        p.payrollPeriodCode === key.payrollPeriodCode &&
        p.payrollTypeCode === key.payrollTypeCode &&
        p.presenceNumber === key.presenceNumber,
    ) ?? null;
  });

  search(filters: RecibosFilters): void {
    this.listLoadingState.set(true);
    this.listErrorState.set(null);

    this.gateway.search(filters).pipe(take(1)).subscribe({
      next: (payrolls) => {
        this.payrollsState.set(payrolls);
        this.listLoadingState.set(false);
      },
      error: () => {
        this.listLoadingState.set(false);
        this.listErrorState.set('request-failed');
      },
    });
  }

  selectPayroll(key: PayrollBusinessKey): void {
    this.selectedKeyState.set(key);
    this.transitionErrorState.set(null);
    this.loadConcepts(key);
  }

  invalidate(key: PayrollBusinessKey): void {
    if (this.transitioningState()) return;
    this.transitioningState.set(true);
    this.transitionErrorState.set(null);

    this.gateway.invalidate(key).pipe(take(1)).subscribe({
      next: (updated) => {
        this.updatePayrollInList(updated);
        this.transitioningState.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.transitioningState.set(false);
        this.transitionErrorState.set(this.mapTransitionError(err));
      },
    });
  }

  validate(key: PayrollBusinessKey): void {
    if (this.transitioningState()) return;
    this.transitioningState.set(true);
    this.transitionErrorState.set(null);

    this.gateway.validate(key).pipe(take(1)).subscribe({
      next: (updated) => {
        this.updatePayrollInList(updated);
        this.transitioningState.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.transitioningState.set(false);
        this.transitionErrorState.set(this.mapTransitionError(err));
      },
    });
  }

  recalculate(key: PayrollBusinessKey): void {
    if (this.transitioningState()) return;
    this.transitioningState.set(true);
    this.transitionErrorState.set(null);

    this.gateway.recalculate(key).pipe(take(1)).subscribe({
      next: (updated) => {
        this.updatePayrollInList(updated);
        this.transitioningState.set(false);
        this.loadConcepts(key);
      },
      error: (err: HttpErrorResponse) => {
        this.transitioningState.set(false);
        this.transitionErrorState.set(this.mapTransitionError(err));
      },
    });
  }

  private loadConcepts(key: PayrollBusinessKey): void {
    this.conceptsLoadingState.set(true);
    this.conceptsState.set([]);

    this.gateway.getConcepts(key).pipe(take(1)).subscribe({
      next: (concepts) => {
        this.conceptsState.set(concepts);
        this.conceptsLoadingState.set(false);
      },
      error: () => {
        this.conceptsLoadingState.set(false);
      },
    });
  }

  private updatePayrollInList(updated: PayrollSummaryModel): void {
    this.payrollsState.update((list) =>
      list.map((p) =>
        p.employeeNumber === updated.employeeNumber &&
        p.payrollPeriodCode === updated.payrollPeriodCode &&
        p.payrollTypeCode === updated.payrollTypeCode
          ? updated
          : p,
      ),
    );
  }

  private mapTransitionError(err: HttpErrorResponse): string {
    if (err.status === 409) return err.error?.message ?? 'Transición no permitida en el estado actual.';
    if (err.status === 404) return 'Nómina no encontrada.';
    return 'Error al cambiar el estado. Inténtalo de nuevo.';
  }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
npm run test -- --run --reporter=verbose 2>&1 | grep -A10 "RecibosStore"
```
Expected: all 4 store tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/app/features/nomina/recibos/store/
git commit -m "feat(nomina): add recibos signals store with search, select, and state transitions"
```

---

## Task 9: Frontend — UI components

**Files:**
- Create: `src/app/features/nomina/recibos/ui/recibos-folio.component.ts`
- Create: `src/app/features/nomina/recibos/ui/recibos-detail.component.ts`
- Create: `src/app/features/nomina/recibos/ui/recibos-list.component.ts`
- Create: `src/app/features/nomina/recibos/ui/recibos-page.component.ts`

- [ ] **Step 1: Create `RecibosFolioComponent`**

This component renders the payslip table. It receives `concepts` as input and renders purely from the list — no arithmetic.

```typescript
// src/app/features/nomina/recibos/ui/recibos-folio.component.ts
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollConceptModel } from '../models/payroll-concept.model';

@Component({
  selector: 'app-recibos-folio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="folio">
      <!-- Header (placeholder until employee/company bounded context is integrated) -->
      <div class="folio-header">
        <div class="header-company">
          <div class="company-name">EMPRESA EJEMPLO S.L.</div>
          <div class="company-meta">CIF: B-12345678 · C/ Ejemplo, 1 · 28001 Madrid</div>
        </div>
        <div class="header-title">
          <div class="payslip-title">Recibo de Salarios</div>
          <div class="payslip-period">Período: {{ payrollPeriodCode }}</div>
        </div>
      </div>

      <div class="header-employee">
        <span class="label">Trabajador: </span><strong>— (pendiente integración)</strong>
        <span class="label" style="margin-left:16px">Nº emp.: </span><strong>{{ employeeNumber }}</strong>
      </div>

      <!-- Concept table -->
      <table class="concept-table">
        <thead>
          <tr>
            <th class="col-period">Período</th>
            <th class="col-code">Clave</th>
            <th class="col-label">Concepto</th>
            <th class="col-qty">Cantidad</th>
            <th class="col-rate">Tarifa/Base</th>
            <th class="col-earning">Devengos</th>
            <th class="col-deduction">Deducciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let concept of concepts" [class.row-total]="isTotal(concept)">
            <td>{{ concept.originPeriodCode ?? '—' }}</td>
            <td>{{ concept.conceptCode }}</td>
            <td>{{ concept.conceptLabel }}</td>
            <td class="text-right">{{ concept.quantity != null ? formatNum(concept.quantity) : '—' }}</td>
            <td class="text-right">{{ concept.rate != null ? formatNum(concept.rate) : '—' }}</td>
            <td class="text-right amount-earning">
              {{ isEarning(concept) && concept.amount != null ? formatNum(concept.amount) : '—' }}
            </td>
            <td class="text-right amount-deduction">
              {{ isDeduction(concept) && concept.amount != null ? formatNum(concept.amount) : '—' }}
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Net pay footer — rendered only when a NET_PAY concept is present -->
      <div *ngIf="netPayConcept" class="net-pay-footer">
        <span class="net-pay-label">Líquido total a percibir</span>
        <span class="net-pay-amount">{{ formatNum(netPayConcept.amount!) }} €</span>
      </div>
    </div>
  `,
  styles: [`
    .folio { background: white; padding: 24px 28px; box-shadow: 0 2px 12px rgba(0,0,0,0.18); max-width: 780px; }
    .folio-header { display: flex; justify-content: space-between; border-bottom: 2px solid #212529; padding-bottom: 12px; margin-bottom: 10px; }
    .company-name { font-weight: 700; font-size: 14px; color: #212529; }
    .company-meta { color: #6c757d; font-size: 10px; margin-top: 2px; }
    .payslip-title { font-weight: 700; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; color: #212529; }
    .payslip-period { color: #6c757d; font-size: 11px; margin-top: 2px; }
    .header-employee { font-size: 11px; padding: 8px 0 12px; border-bottom: 1px solid #dee2e6; margin-bottom: 12px; }
    .label { color: #6c757d; }
    .concept-table { width: 100%; border-collapse: collapse; font-size: 11px; }
    .concept-table th { background: #343a40; color: white; padding: 6px 8px; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
    .concept-table td { padding: 5px 8px; border-bottom: 1px solid #f1f3f5; color: #212529; }
    .text-right { text-align: right; }
    .amount-earning, .amount-deduction { font-weight: 600; }
    .row-total td { font-weight: 700; border-top: 2px solid #adb5bd; background: #f8f9fa; }
    .net-pay-footer { background: #212529; color: white; padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; margin-top: 0; }
    .net-pay-label { font-size: 10px; text-transform: uppercase; letter-spacing: 0.6px; color: #adb5bd; }
    .net-pay-amount { font-size: 26px; font-weight: 700; color: #a6e3a1; }
    .col-period { width: 8%; }
    .col-code { width: 7%; }
    .col-label { width: 35%; }
    .col-qty, .col-rate { width: 10%; }
    .col-earning, .col-deduction { width: 15%; }
  `],
})
export class RecibosFolioComponent {
  @Input() concepts: ReadonlyArray<PayrollConceptModel> = [];
  @Input() employeeNumber = '';
  @Input() payrollPeriodCode = '';

  get netPayConcept(): PayrollConceptModel | null {
    return this.concepts.find((c) => c.conceptNatureCode === 'NET_PAY') ?? null;
  }

  isEarning(concept: PayrollConceptModel): boolean {
    return concept.conceptNatureCode === 'EARNING' || concept.conceptNatureCode === 'TOTAL_EARNING';
  }

  isDeduction(concept: PayrollConceptModel): boolean {
    return concept.conceptNatureCode === 'DEDUCTION' || concept.conceptNatureCode === 'TOTAL_DEDUCTION';
  }

  isTotal(concept: PayrollConceptModel): boolean {
    return concept.conceptNatureCode === 'TOTAL_EARNING' || concept.conceptNatureCode === 'TOTAL_DEDUCTION';
  }

  formatNum(value: number): string {
    return new Intl.NumberFormat('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value);
  }
}
```

- [ ] **Step 2: Create `RecibosDetailComponent`**

```typescript
// src/app/features/nomina/recibos/ui/recibos-detail.component.ts
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RecibosStore } from '../store/recibos.store';
import { RecibosFolioComponent } from './recibos-folio.component';

const STATUS_LABELS: Record<string, string> = {
  CALCULATED: 'CALCULADA',
  NOT_VALID: 'INVÁLIDA',
  EXPLICIT_VALIDATED: 'VALIDADA',
  DEFINITIVE: 'DEFINITIVA',
};

@Component({
  selector: 'app-recibos-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule, RecibosFolioComponent],
  template: `
    @if (store.selectedPayroll(); as payroll) {
      <!-- Action bar -->
      <div class="action-bar">
        <div class="action-bar-info">
          <span class="payroll-key">{{ payroll.employeeNumber }} · Período {{ payroll.payrollPeriodCode }}</span>
          <span class="status-badge" [class]="'badge-' + payroll.status.toLowerCase()">
            {{ statusLabel(payroll.status) }}
          </span>
        </div>
        <div class="action-bar-buttons">
          @if (payroll.status === 'CALCULATED') {
            <button class="btn btn-invalidar" [disabled]="store.transitioning()" (click)="invalidate()">Invalidar</button>
            <button class="btn btn-validar" [disabled]="store.transitioning()" (click)="validate()">Validar</button>
          }
          @if (payroll.status === 'NOT_VALID') {
            <button class="btn btn-recalcular" [disabled]="store.transitioning()" (click)="recalculate()">Recalcular</button>
          }
        </div>
      </div>

      @if (store.transitionError()) {
        <div class="transition-error">{{ store.transitionError() }}</div>
      }

      <!-- Folio -->
      <div class="folio-wrapper">
        @if (store.conceptsLoading()) {
          <div class="loading-msg">Cargando conceptos...</div>
        } @else {
          <app-recibos-folio
            [concepts]="store.concepts()"
            [employeeNumber]="payroll.employeeNumber"
            [payrollPeriodCode]="payroll.payrollPeriodCode"
          />
        }
      </div>
    } @else {
      <div class="no-selection">Selecciona una nómina de la lista para ver el detalle.</div>
    }
  `,
  styles: [`
    :host { display: flex; flex-direction: column; flex: 1; overflow-y: auto; background: #d1d5db; padding: 16px 20px; gap: 12px; }
    .action-bar { background: #1e1e2e; padding: 8px 14px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; max-width: 780px; }
    .payroll-key { color: #89b4fa; font-size: 12px; font-weight: 600; }
    .status-badge { margin-left: 10px; padding: 2px 8px; border-radius: 10px; font-size: 10px; font-weight: 600; color: #1e1e2e; }
    .badge-calculated { background: #a6e3a1; }
    .badge-not_valid { background: #f38ba8; }
    .badge-explicit_validated { background: #89b4fa; }
    .badge-definitive { background: #cba6f7; }
    .action-bar-buttons { display: flex; gap: 8px; }
    .btn { border: none; padding: 5px 14px; border-radius: 4px; font-size: 11px; font-weight: 700; cursor: pointer; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-invalidar { background: #f38ba8; color: #1e1e2e; }
    .btn-validar { background: #89b4fa; color: #1e1e2e; }
    .btn-recalcular { background: #fab387; color: #1e1e2e; }
    .transition-error { max-width: 780px; background: #f38ba8; color: #1e1e2e; padding: 8px 14px; border-radius: 4px; font-size: 11px; }
    .folio-wrapper { max-width: 780px; }
    .loading-msg, .no-selection { color: #6c757d; font-size: 12px; padding: 20px; }
  `],
})
export class RecibosDetailComponent {
  protected readonly store = inject(RecibosStore);

  statusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
  }

  invalidate(): void {
    const key = this.store.selectedKey();
    if (key) this.store.invalidate(key);
  }

  validate(): void {
    const key = this.store.selectedKey();
    if (key) this.store.validate(key);
  }

  recalculate(): void {
    const key = this.store.selectedKey();
    if (key) this.store.recalculate(key);
  }
}
```

- [ ] **Step 3: Create `RecibosListComponent`**

```typescript
// src/app/features/nomina/recibos/ui/recibos-list.component.ts
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RecibosStore } from '../store/recibos.store';
import { RecibosFilters } from '../models/recibos-filters.model';
import { PayrollSummaryModel } from '../models/payroll-summary.model';

const STATUS_LABELS: Record<string, string> = {
  CALCULATED: 'CALCULADA',
  NOT_VALID: 'INVÁLIDA',
  EXPLICIT_VALIDATED: 'VALIDADA',
  DEFINITIVE: 'DEFINITIVA',
};

@Component({
  selector: 'app-recibos-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="list-panel">
      <!-- Filters -->
      <div class="filters">
        <div class="filter-row">
          <div class="filter-field">
            <label>PERÍODO</label>
            <input [(ngModel)]="filters().payrollPeriodCode" (ngModelChange)="patchFilter('payrollPeriodCode', $event)" placeholder="202604" />
          </div>
          <div class="filter-field">
            <label>ESTADO</label>
            <select [(ngModel)]="filters().status" (ngModelChange)="patchFilter('status', $event)">
              <option value="">Todos</option>
              <option value="CALCULATED">CALCULADA</option>
              <option value="NOT_VALID">INVÁLIDA</option>
              <option value="EXPLICIT_VALIDATED">VALIDADA</option>
              <option value="DEFINITIVE">DEFINITIVA</option>
            </select>
          </div>
        </div>
        <div class="filter-field">
          <label>EMPLEADO</label>
          <input [(ngModel)]="filters().employeeNumber" (ngModelChange)="patchFilter('employeeNumber', $event)" placeholder="Número o nombre..." />
        </div>
        <button class="search-btn" (click)="search()">Buscar</button>
      </div>

      <!-- Results -->
      <div class="results">
        @for (payroll of store.payrolls(); track trackPayroll(payroll)) {
          <div
            class="payroll-row"
            [class.selected]="isSelected(payroll)"
            (click)="select(payroll)"
          >
            <div class="row-top">
              <span class="employee-number" [class.bold]="isSelected(payroll)">{{ payroll.employeeNumber }}</span>
              <span class="status-badge" [class]="'badge-' + payroll.status.toLowerCase()">{{ statusLabel(payroll.status) }}</span>
            </div>
            <div class="row-sub">{{ payroll.payrollPeriodCode }} · {{ payroll.payrollTypeCode }}</div>
          </div>
        }
        @if (store.listLoading()) {
          <div class="list-msg">Buscando...</div>
        }
        @if (store.listError()) {
          <div class="list-msg error">Error al cargar las nóminas.</div>
        }
      </div>

      <!-- Footer -->
      <div class="list-footer">{{ store.payrolls().length }} nóminas encontradas</div>
    </div>
  `,
  styles: [`
    .list-panel { width: 300px; border-right: 1px solid #313244; background: #181825; display: flex; flex-direction: column; flex-shrink: 0; }
    .filters { padding: 10px; border-bottom: 1px solid #313244; background: #1e1e2e; }
    .filter-row { display: flex; gap: 6px; margin-bottom: 6px; }
    .filter-field { display: flex; flex-direction: column; flex: 1; margin-bottom: 6px; }
    label { color: #6c7086; font-size: 10px; margin-bottom: 2px; }
    input, select { background: #313244; padding: 4px 8px; border-radius: 4px; color: #cdd6f4; font-size: 11px; border: none; }
    .search-btn { background: #89b4fa; color: #1e1e2e; padding: 5px; border-radius: 4px; text-align: center; font-weight: 600; font-size: 11px; border: none; cursor: pointer; width: 100%; }
    .results { overflow-y: auto; flex: 1; }
    .payroll-row { padding: 8px 10px; border-bottom: 1px solid #313244; border-left: 3px solid transparent; cursor: pointer; }
    .payroll-row.selected { background: #313244; border-left-color: #89b4fa; }
    .row-top { display: flex; justify-content: space-between; align-items: center; }
    .employee-number { color: #cdd6f4; font-size: 11px; }
    .employee-number.bold { font-weight: 600; }
    .row-sub { color: #6c7086; font-size: 10px; margin-top: 2px; }
    .status-badge { padding: 1px 7px; border-radius: 10px; font-size: 10px; font-weight: 600; color: #1e1e2e; }
    .badge-calculated { background: #a6e3a1; }
    .badge-not_valid { background: #f38ba8; }
    .badge-explicit_validated { background: #89b4fa; }
    .badge-definitive { background: #cba6f7; }
    .list-msg { padding: 12px; color: #6c7086; font-size: 11px; }
    .list-msg.error { color: #f38ba8; }
    .list-footer { padding: 8px 10px; border-top: 1px solid #313244; color: #6c7086; font-size: 10px; background: #1e1e2e; }
  `],
})
export class RecibosListComponent {
  protected readonly store = inject(RecibosStore);

  protected readonly filters = signal<RecibosFilters>({
    payrollPeriodCode: '', employeeNumber: '', status: '',
  });

  patchFilter<K extends keyof RecibosFilters>(key: K, value: RecibosFilters[K]): void {
    this.filters.update((f) => ({ ...f, [key]: value }));
  }

  search(): void {
    this.store.search(this.filters());
  }

  select(payroll: PayrollSummaryModel): void {
    this.store.selectPayroll(payroll);
  }

  isSelected(payroll: PayrollSummaryModel): boolean {
    const key = this.store.selectedKey();
    return key?.employeeNumber === payroll.employeeNumber &&
           key?.payrollPeriodCode === payroll.payrollPeriodCode &&
           key?.payrollTypeCode === payroll.payrollTypeCode;
  }

  statusLabel(status: string): string {
    return STATUS_LABELS[status] ?? status;
  }

  trackPayroll(payroll: PayrollSummaryModel): string {
    return `${payroll.employeeNumber}-${payroll.payrollPeriodCode}-${payroll.payrollTypeCode}`;
  }
}
```

- [ ] **Step 4: Create `RecibosPageComponent`**

```typescript
// src/app/features/nomina/recibos/ui/recibos-page.component.ts
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RecibosListComponent } from './recibos-list.component';
import { RecibosDetailComponent } from './recibos-detail.component';

@Component({
  selector: 'app-recibos-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [RecibosListComponent, RecibosDetailComponent],
  template: `
    <div class="page-layout">
      <app-recibos-list />
      <app-recibos-detail />
    </div>
  `,
  styles: [`
    .page-layout { display: flex; height: 100%; overflow: hidden; font-family: 'Segoe UI', sans-serif; font-size: 12px; }
  `],
})
export class RecibosPageComponent {}
```

- [ ] **Step 5: Compile check**

```bash
npm run build -- --configuration=development 2>&1 | tail -20
```
Expected: no errors.

- [ ] **Step 6: Commit**

```bash
git add src/app/features/nomina/recibos/ui/
git commit -m "feat(nomina): add recibos UI components (folio, detail, list, page)"
```

---

## Task 10: Wiring — route + nav menu

**Files:**
- Create: `src/app/features/nomina/recibos/recibos.routes.ts`
- Modify: `src/app/app.routes.ts`
- Modify: `src/app/core/i18n/app-texts.ts`
- Modify: `src/app/core/layout/app-shell/app-shell.component.ts`

- [ ] **Step 1: Create `recibos.routes.ts`**

```typescript
// src/app/features/nomina/recibos/recibos.routes.ts
import { Routes } from '@angular/router';

export const recibosRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./ui/recibos-page.component').then((m) => m.RecibosPageComponent),
  },
];
```

- [ ] **Step 2: Add route in `app.routes.ts`**

Add before the `'employees'` redirect entry:

```typescript
{
  path: 'nomina/recibos',
  loadChildren: () =>
    import('./features/nomina/recibos/recibos.routes').then((m) => m.recibosRoutes),
},
```

- [ ] **Step 3: Add text keys to `app-texts.ts`**

```typescript
// Add to the appTexts object:
sectionPayroll: 'Nómina',
sectionRecibos: 'Recibos',
```

- [ ] **Step 4: Add nav section in `app-shell.component.ts`**

Add a new `MenuItem` entry to `sideNavItems` after the `sectionSettings` block:

```typescript
{
  label: this.texts.sectionPayroll,
  icon: 'pi pi-file',
  expanded: true,
  items: [
    { label: this.texts.sectionRecibos, icon: 'pi pi-receipt', routerLink: '/nomina/recibos' },
  ],
},
```

- [ ] **Step 5: Build and verify**

```bash
npm run build -- --configuration=development 2>&1 | tail -20
```
Expected: `Build at:` timestamp, no errors.

- [ ] **Step 6: Manual smoke test**

```bash
npm start
```

1. Navigate to `http://localhost:4200`
2. Verify "Nómina → Recibos" appears in the left sidebar
3. Click "Recibos" — the page loads with the left filter panel and an empty right panel
4. Enter period `202604` and click "Buscar" — 3 payrolls appear (MAS000001 CALCULADA, MAS000002 INVÁLIDA, MAS000004 INVÁLIDA)
5. Click MAS000001 — folio appears in the right panel, "Invalidar" and "Validar" buttons visible
6. Click "Invalidar" — status badge changes to INVÁLIDA, "Recalcular" button appears
7. Click MAS000002 (NOT_VALID) — only "Recalcular" button is visible

- [ ] **Step 7: Commit**

```bash
git add src/app/features/nomina/recibos/recibos.routes.ts \
        src/app/app.routes.ts \
        src/app/core/i18n/app-texts.ts \
        src/app/core/layout/app-shell/app-shell.component.ts
git commit -m "feat(nomina): wire recibos route and nav menu entry"
```

---

## Self-Review Checklist

| Spec requirement | Covered by |
|---|---|
| Route `/nomina/recibos` | Task 10 |
| Left panel filters (period, status, employee) | Task 9 `RecibosListComponent` |
| Results list with status badges + row count | Task 9 `RecibosListComponent` |
| 7-column folio table (Período, Clave, Concepto, Cantidad, Tarifa/Base, Devengos, Deducciones) | Task 9 `RecibosFolioComponent` |
| Frontend performs no arithmetic | `RecibosFolioComponent` — no computation |
| `originPeriodCode` → Período column | Task 9, Task 6 mapper |
| `conceptNatureCode` routing to Devengos/Deducciones | `isEarning()` / `isDeduction()` in `RecibosFolioComponent` |
| TOTAL row style (bold, thick border) | `.row-total` CSS class |
| NET_PAY footer | `netPayConcept` getter |
| Status buttons — CALCULATED: Invalidar + Validar | `RecibosDetailComponent` `@if` |
| Status buttons — NOT_VALID: Recalcular only | `RecibosDetailComponent` `@if` |
| Status buttons — EXPLICIT_VALIDATED/DEFINITIVE: none | covered by `@if` conditions |
| Backend search endpoint | Task 2 + 4 |
| Backend recalculate endpoint | Task 3 + 4 |
| Nav menu entry | Task 10 |
