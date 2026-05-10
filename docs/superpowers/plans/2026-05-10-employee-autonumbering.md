# Employee Auto-Numbering Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate manual employee number entry on hire — the backend generates it from a per-rule-system counter; frontend and workforce loader stop sending it.

**Architecture:** New vertical `rulesystem.employeenumbering` owns config CRUD (GET + PUT endpoints). A `NextEmployeeNumberPort` in `employee.lifecycle` calls a `NextEmployeeNumberAdapter` that does `SELECT FOR UPDATE` inside the existing hire transaction to atomically read, format, and advance the counter. `HireEmployeeCommand` loses its `employeeNumber` field; `HireEmployeeService` calls the port first and threads the generated string to all downstream commands.

**Tech Stack:** Java 21, Spring Boot, JPA/Hibernate, Flyway (H2 for tests), Angular 21, PrimeNG, TypeScript.

---

## File Structure

### Backend — new
| File | Purpose |
|------|---------|
| `src/main/resources/db/migration/V98__create_employee_numbering_config.sql` | Table DDL |
| `src/main/resources/db/migration/V99__seed_employee_numbering_config_esp.sql` | ESP seed |
| `…/rulesystem/employeenumbering/domain/model/EmployeeNumberingConfig.java` | Domain record |
| `…/rulesystem/employeenumbering/domain/port/EmployeeNumberingConfigRepository.java` | Domain port |
| `…/rulesystem/employeenumbering/domain/exception/EmployeeNumberingConfigNotFoundException.java` | 422 when missing |
| `…/rulesystem/employeenumbering/domain/exception/EmployeeNumberingExhaustedException.java` | 409 when counter full |
| `…/rulesystem/employeenumbering/domain/exception/EmployeeNumberingConfigInvalidException.java` | 422 on bad length |
| `…/rulesystem/employeenumbering/application/usecase/GetEmployeeNumberingConfigUseCase.java` | Interface |
| `…/rulesystem/employeenumbering/application/usecase/GetEmployeeNumberingConfigService.java` | Implementation |
| `…/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigUseCase.java` | Interface |
| `…/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigCommand.java` | Command record |
| `…/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigService.java` | Implementation |
| `…/rulesystem/employeenumbering/infrastructure/persistence/EmployeeNumberingConfigEntity.java` | JPA entity |
| `…/rulesystem/employeenumbering/infrastructure/persistence/SpringDataEmployeeNumberingConfigRepository.java` | Spring Data + FOR UPDATE query |
| `…/rulesystem/employeenumbering/infrastructure/persistence/EmployeeNumberingConfigPersistenceAdapter.java` | Adapter |
| `…/rulesystem/employeenumbering/infrastructure/web/dto/EmployeeNumberingConfigResponse.java` | Response DTO |
| `…/rulesystem/employeenumbering/infrastructure/web/dto/UpsertEmployeeNumberingConfigRequest.java` | Request DTO |
| `…/rulesystem/employeenumbering/infrastructure/web/EmployeeNumberingConfigController.java` | REST endpoints |
| `…/rulesystem/employeenumbering/infrastructure/web/EmployeeNumberingConfigExceptionHandler.java` | Exception → HTTP |
| `…/employee/lifecycle/application/port/NextEmployeeNumberPort.java` | Secondary port |
| `…/employee/lifecycle/infrastructure/NextEmployeeNumberAdapter.java` | SELECT FOR UPDATE impl |

### Backend — modified
| File | Change |
|------|--------|
| `…/employee/lifecycle/application/command/HireEmployeeCommand.java` | Remove `employeeNumber` field |
| `…/employee/lifecycle/infrastructure/rest/dto/HireEmployeeRequest.java` | Remove `employeeNumber` field |
| `…/employee/lifecycle/infrastructure/rest/HireEmployeeWebMapper.java` | Remove `employeeNumber` from `toCommand()` |
| `…/employee/lifecycle/application/usecase/HireEmployeeService.java` | Inject port, generate number in `hire()` |
| `openapi/personnel-administration-api.yaml` | Remove `employeeNumber` from hire request body |

### Backend — tests
| File | Type |
|------|------|
| `…/rulesystem/employeenumbering/domain/model/EmployeeNumberingConfigTest.java` | Unit |
| `…/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigServiceTest.java` | Unit (Mockito) |
| `…/employee/lifecycle/infrastructure/NextEmployeeNumberAdapterIntegrationTest.java` | Integration (H2) |
| `…/employee/lifecycle/application/usecase/HireEmployeeServiceTest.java` | Update existing |

### Frontend — new
| File | Purpose |
|------|---------|
| `src/app/core/api/clients/employee-numbering-config.client.ts` | HTTP client |
| `src/app/features/company/ui/employee-numbering-config-card.component.ts` | Card component |
| `src/app/features/company/ui/employee-numbering-config-card.component.html` | Template |
| `src/app/features/company/ui/employee-numbering-config-card.component.scss` | Styles |

### Frontend — modified
| File | Change |
|------|--------|
| `src/app/features/company/ui/company-page.component.html` | Add numbering card |
| `src/app/features/employee/models/employee-hiring.model.ts` | Remove `employeeNumber` from draft |
| `src/app/features/employee/data-access/employee-hiring.mapper.ts` | Remove from request |
| `src/app/features/employee/lifecycle/hire/pages/hire-employee-page.component.ts` | Remove form control |
| `src/app/features/employee/lifecycle/hire/pages/hire-employee-page.component.html` | Remove input field |

### Workforce Loader — modified
| File | Change |
|------|--------|
| `…/workforceloader/infrastructure/api/dto/HireEmployeeRequest.java` | Remove `employeeNumber` |
| `…/workforceloader/infrastructure/api/dto/HireEmployeeResponse.java` | Add `employeeNumber` |
| `…/workforceloader/domain/model/SyntheticEmployee.java` | Add `withEmployeeNumber()` method |
| `…/workforceloader/application/RunLifecycleSimulationService.java` | Capture number from response, rebind `employee` |
| `…/workforceloader/application/HireReferenceDataResolverTest.java` | No `employeeNumber` in assertions |

---

## Task 1: Database Migrations

**Files:**
- Create: `src/main/resources/db/migration/V98__create_employee_numbering_config.sql`
- Create: `src/main/resources/db/migration/V99__seed_employee_numbering_config_esp.sql`

- [ ] **Step 1: Create V98 migration**

```sql
-- V98__create_employee_numbering_config.sql
CREATE TABLE rulesystem.employee_numbering_config (
    id                  BIGSERIAL    NOT NULL,
    rule_system_code    VARCHAR(20)  NOT NULL,
    prefix              VARCHAR(14)  NOT NULL DEFAULT '',
    numeric_part_length INT          NOT NULL,
    step                INT          NOT NULL DEFAULT 1,
    next_value          BIGINT       NOT NULL DEFAULT 1,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT pk_employee_numbering_config PRIMARY KEY (id),
    CONSTRAINT uk_employee_numbering_config_rs UNIQUE (rule_system_code),
    CONSTRAINT fk_employee_numbering_config_rs
        FOREIGN KEY (rule_system_code) REFERENCES rulesystem.rule_system(code),
    CONSTRAINT chk_employee_numbering_config_length
        CHECK (LENGTH(prefix) + numeric_part_length <= 15),
    CONSTRAINT chk_employee_numbering_config_part_min
        CHECK (numeric_part_length >= 1),
    CONSTRAINT chk_employee_numbering_config_step_min
        CHECK (step >= 1),
    CONSTRAINT chk_employee_numbering_config_next_min
        CHECK (next_value >= 1)
);
```

- [ ] **Step 2: Create V99 seed migration**

```sql
-- V99__seed_employee_numbering_config_esp.sql
INSERT INTO rulesystem.employee_numbering_config
    (rule_system_code, prefix, numeric_part_length, step, next_value, created_at, updated_at)
VALUES
    ('ESP', 'EMP', 6, 1, 1, NOW(), NOW());
-- Generates: EMP000001 … EMP999999 (1,000,000 employees)
```

- [ ] **Step 3: Verify migrations run cleanly**

```bash
cd b4rrhh_backend
mvn test -Dtest=EmployeeNumberingConfigTest -pl . 2>&1 | tail -5
```

Expected: BUILD SUCCESS (or "No tests to run for EmployeeNumberingConfigTest" — that's fine; Flyway ran).

Actually: run any existing test to confirm Flyway runs the two new migrations without error.

```bash
mvn test -Dtest=HireEmployeeServiceTest
```

Expected: `BUILD SUCCESS` — confirms H2 migrations apply cleanly.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/db/migration/V98__create_employee_numbering_config.sql
git add src/main/resources/db/migration/V99__seed_employee_numbering_config_esp.sql
git commit -m "feat(autonumbering): add DB migrations V98 (table) and V99 (ESP seed)"
```

---

## Task 2: Domain Model + Exceptions

**Files:**
- Create: `src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/model/EmployeeNumberingConfig.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/exception/EmployeeNumberingConfigNotFoundException.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/exception/EmployeeNumberingExhaustedException.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/exception/EmployeeNumberingConfigInvalidException.java`
- Create (test): `src/test/java/com/b4rrhh/rulesystem/employeenumbering/domain/model/EmployeeNumberingConfigTest.java`

- [ ] **Step 1: Write the failing tests for `EmployeeNumberingConfig`**

```java
// src/test/java/com/b4rrhh/rulesystem/employeenumbering/domain/model/EmployeeNumberingConfigTest.java
package com.b4rrhh.rulesystem.employeenumbering.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeNumberingConfigTest {

    @Test
    void formatsNumberWithPrefixAndPadding() {
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "EMP", 6, 1, 1L);
        assertEquals("EMP000001", config.formatNumber());
    }

    @Test
    void formatsNumberWithEmptyPrefixAndPadding() {
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "", 8, 1, 42L);
        assertEquals("00000042", config.formatNumber());
    }

    @Test
    void detectsOverflowWhenNextValueExceedsMax() {
        // numericPartLength=3 → max=999; nextValue=1000 overflows
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "EMP", 3, 1, 1000L);
        assertTrue(config.isExhausted());
    }

    @Test
    void doesNotOverflowWhenNextValueEqualsMax() {
        // numericPartLength=3 → max=999; nextValue=999 is fine
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "EMP", 3, 1, 999L);
        assertFalse(config.isExhausted());
    }

    @Test
    void advancedByStepReturnsNewConfig() {
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "EMP", 6, 3, 1L);
        EmployeeNumberingConfig advanced = config.advance();
        assertEquals(4L, advanced.nextValue());
    }
}
```

- [ ] **Step 2: Run tests to confirm failure**

```bash
mvn test -Dtest=EmployeeNumberingConfigTest
```

Expected: `BUILD FAILURE` — `EmployeeNumberingConfig` not found.

- [ ] **Step 3: Create domain model**

```java
// src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/model/EmployeeNumberingConfig.java
package com.b4rrhh.rulesystem.employeenumbering.domain.model;

public record EmployeeNumberingConfig(
        String ruleSystemCode,
        String prefix,
        int numericPartLength,
        int step,
        long nextValue
) {
    public String formatNumber() {
        return prefix + String.format("%0" + numericPartLength + "d", nextValue);
    }

    public boolean isExhausted() {
        long max = (long) Math.pow(10, numericPartLength) - 1;
        return nextValue > max;
    }

    public EmployeeNumberingConfig advance() {
        return new EmployeeNumberingConfig(ruleSystemCode, prefix, numericPartLength, step, nextValue + step);
    }
}
```

- [ ] **Step 4: Create the three domain exceptions**

```java
// src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/exception/EmployeeNumberingConfigNotFoundException.java
package com.b4rrhh.rulesystem.employeenumbering.domain.exception;

public class EmployeeNumberingConfigNotFoundException extends RuntimeException {
    public EmployeeNumberingConfigNotFoundException(String ruleSystemCode) {
        super("No employee numbering config for rule system: " + ruleSystemCode);
    }
}
```

```java
// src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/exception/EmployeeNumberingExhaustedException.java
package com.b4rrhh.rulesystem.employeenumbering.domain.exception;

public class EmployeeNumberingExhaustedException extends RuntimeException {
    public EmployeeNumberingExhaustedException(String ruleSystemCode) {
        super("Employee numbering counter exhausted for rule system: " + ruleSystemCode);
    }
}
```

```java
// src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/exception/EmployeeNumberingConfigInvalidException.java
package com.b4rrhh.rulesystem.employeenumbering.domain.exception;

public class EmployeeNumberingConfigInvalidException extends RuntimeException {
    public EmployeeNumberingConfigInvalidException(String message) {
        super(message);
    }
}
```

- [ ] **Step 5: Run tests to confirm pass**

```bash
mvn test -Dtest=EmployeeNumberingConfigTest
```

Expected: `BUILD SUCCESS`, 5 tests passing.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/
git add src/test/java/com/b4rrhh/rulesystem/employeenumbering/domain/
git commit -m "feat(autonumbering): add EmployeeNumberingConfig domain model and exceptions"
```

---

## Task 3: Domain Port + Persistence Layer

**Files:**
- Create: `…/rulesystem/employeenumbering/domain/port/EmployeeNumberingConfigRepository.java`
- Create: `…/rulesystem/employeenumbering/infrastructure/persistence/EmployeeNumberingConfigEntity.java`
- Create: `…/rulesystem/employeenumbering/infrastructure/persistence/SpringDataEmployeeNumberingConfigRepository.java`
- Create: `…/rulesystem/employeenumbering/infrastructure/persistence/EmployeeNumberingConfigPersistenceAdapter.java`

No dedicated unit test for the persistence adapter (it's thin mapping code exercised by the integration test in Task 7).

- [ ] **Step 1: Create domain port**

```java
// src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/port/EmployeeNumberingConfigRepository.java
package com.b4rrhh.rulesystem.employeenumbering.domain.port;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import java.util.Optional;

public interface EmployeeNumberingConfigRepository {
    Optional<EmployeeNumberingConfig> findByRuleSystemCode(String ruleSystemCode);
    EmployeeNumberingConfig save(EmployeeNumberingConfig config);
}
```

- [ ] **Step 2: Create JPA entity**

```java
// src/main/java/com/b4rrhh/rulesystem/employeenumbering/infrastructure/persistence/EmployeeNumberingConfigEntity.java
package com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lock;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "employee_numbering_config",
        schema = "rulesystem",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_numbering_config_rs",
                columnNames = "rule_system_code"
        )
)
public class EmployeeNumberingConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_system_code", nullable = false, length = 20)
    private String ruleSystemCode;

    @Column(name = "prefix", nullable = false, length = 14)
    private String prefix;

    @Column(name = "numeric_part_length", nullable = false)
    private int numericPartLength;

    @Column(name = "step", nullable = false)
    private int step;

    @Column(name = "next_value", nullable = false)
    private long nextValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public void setRuleSystemCode(String ruleSystemCode) { this.ruleSystemCode = ruleSystemCode; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public int getNumericPartLength() { return numericPartLength; }
    public void setNumericPartLength(int numericPartLength) { this.numericPartLength = numericPartLength; }
    public int getStep() { return step; }
    public void setStep(int step) { this.step = step; }
    public long getNextValue() { return nextValue; }
    public void setNextValue(long nextValue) { this.nextValue = nextValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

- [ ] **Step 3: Create Spring Data repository (includes FOR UPDATE query)**

```java
// src/main/java/com/b4rrhh/rulesystem/employeenumbering/infrastructure/persistence/SpringDataEmployeeNumberingConfigRepository.java
package com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SpringDataEmployeeNumberingConfigRepository
        extends JpaRepository<EmployeeNumberingConfigEntity, Long> {

    Optional<EmployeeNumberingConfigEntity> findByRuleSystemCode(String ruleSystemCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmployeeNumberingConfigEntity e WHERE e.ruleSystemCode = :code")
    Optional<EmployeeNumberingConfigEntity> findByRuleSystemCodeForUpdate(@Param("code") String code);
}
```

- [ ] **Step 4: Create persistence adapter**

```java
// src/main/java/com/b4rrhh/rulesystem/employeenumbering/infrastructure/persistence/EmployeeNumberingConfigPersistenceAdapter.java
package com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.domain.port.EmployeeNumberingConfigRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class EmployeeNumberingConfigPersistenceAdapter
        implements EmployeeNumberingConfigRepository {

    private final SpringDataEmployeeNumberingConfigRepository springDataRepo;

    public EmployeeNumberingConfigPersistenceAdapter(
            SpringDataEmployeeNumberingConfigRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<EmployeeNumberingConfig> findByRuleSystemCode(String ruleSystemCode) {
        return springDataRepo.findByRuleSystemCode(ruleSystemCode).map(this::toDomain);
    }

    @Override
    public EmployeeNumberingConfig save(EmployeeNumberingConfig config) {
        EmployeeNumberingConfigEntity entity = springDataRepo
                .findByRuleSystemCode(config.ruleSystemCode())
                .orElseGet(EmployeeNumberingConfigEntity::new);

        entity.setRuleSystemCode(config.ruleSystemCode());
        entity.setPrefix(config.prefix());
        entity.setNumericPartLength(config.numericPartLength());
        entity.setStep(config.step());
        entity.setNextValue(config.nextValue());

        return toDomain(springDataRepo.save(entity));
    }

    private EmployeeNumberingConfig toDomain(EmployeeNumberingConfigEntity e) {
        return new EmployeeNumberingConfig(
                e.getRuleSystemCode(),
                e.getPrefix(),
                e.getNumericPartLength(),
                e.getStep(),
                e.getNextValue()
        );
    }
}
```

- [ ] **Step 5: Verify it compiles**

```bash
mvn test -Dtest=EmployeeNumberingConfigTest
```

Expected: `BUILD SUCCESS` — persistence layer compiles and existing test still passes.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/employeenumbering/domain/port/
git add src/main/java/com/b4rrhh/rulesystem/employeenumbering/infrastructure/persistence/
git commit -m "feat(autonumbering): add domain port and persistence layer"
```

---

## Task 4: Get + Upsert Use Cases with Tests

**Files:**
- Create: `…/rulesystem/employeenumbering/application/usecase/GetEmployeeNumberingConfigUseCase.java`
- Create: `…/rulesystem/employeenumbering/application/usecase/GetEmployeeNumberingConfigService.java`
- Create: `…/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigUseCase.java`
- Create: `…/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigCommand.java`
- Create: `…/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigService.java`
- Create (test): `…/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigServiceTest.java`

- [ ] **Step 1: Write failing tests for upsert**

```java
// src/test/java/com/b4rrhh/rulesystem/employeenumbering/application/usecase/UpsertEmployeeNumberingConfigServiceTest.java
package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigInvalidException;
import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.domain.port.EmployeeNumberingConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpsertEmployeeNumberingConfigServiceTest {

    @Mock
    private EmployeeNumberingConfigRepository configRepository;
    @Mock
    private RuleSystemRepository ruleSystemRepository;

    private UpsertEmployeeNumberingConfigService service;

    @BeforeEach
    void setUp() {
        service = new UpsertEmployeeNumberingConfigService(configRepository, ruleSystemRepository);
        when(ruleSystemRepository.findByCode("ESP"))
                .thenReturn(Optional.of(new RuleSystem("ESP", "Spain", null)));
    }

    @Test
    void savesValidConfig() {
        when(configRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpsertEmployeeNumberingConfigCommand command =
                new UpsertEmployeeNumberingConfigCommand("ESP", "EMP", 6, 1, 1L);

        EmployeeNumberingConfig result = service.upsert(command);

        assertEquals("ESP", result.ruleSystemCode());
        assertEquals("EMP", result.prefix());
        assertEquals(6, result.numericPartLength());
        verify(configRepository).save(any());
    }

    @Test
    void rejectsWhenPrefixPlusLengthExceedsFifteen() {
        // prefix "EMP" (3) + numericPartLength 13 = 16 > 15
        UpsertEmployeeNumberingConfigCommand command =
                new UpsertEmployeeNumberingConfigCommand("ESP", "EMP", 13, 1, 1L);

        assertThrows(EmployeeNumberingConfigInvalidException.class, () -> service.upsert(command));
        verify(configRepository, never()).save(any());
    }

    @Test
    void rejectsWhenRuleSystemDoesNotExist() {
        when(ruleSystemRepository.findByCode("NOPE")).thenReturn(Optional.empty());
        UpsertEmployeeNumberingConfigCommand command =
                new UpsertEmployeeNumberingConfigCommand("NOPE", "", 8, 1, 1L);

        assertThrows(IllegalArgumentException.class, () -> service.upsert(command));
        verify(configRepository, never()).save(any());
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
mvn test -Dtest=UpsertEmployeeNumberingConfigServiceTest
```

Expected: `BUILD FAILURE` — classes not found.

- [ ] **Step 3: Create use case interfaces and command**

```java
// GetEmployeeNumberingConfigUseCase.java
package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import java.util.Optional;

public interface GetEmployeeNumberingConfigUseCase {
    Optional<EmployeeNumberingConfig> getByRuleSystemCode(String ruleSystemCode);
}
```

```java
// UpsertEmployeeNumberingConfigUseCase.java
package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;

public interface UpsertEmployeeNumberingConfigUseCase {
    EmployeeNumberingConfig upsert(UpsertEmployeeNumberingConfigCommand command);
}
```

```java
// UpsertEmployeeNumberingConfigCommand.java
package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

public record UpsertEmployeeNumberingConfigCommand(
        String ruleSystemCode,
        String prefix,
        int numericPartLength,
        int step,
        long nextValue
) {}
```

- [ ] **Step 4: Create service implementations**

```java
// GetEmployeeNumberingConfigService.java
package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.domain.port.EmployeeNumberingConfigRepository;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.Optional;

@Service
public class GetEmployeeNumberingConfigService implements GetEmployeeNumberingConfigUseCase {

    private final EmployeeNumberingConfigRepository repository;

    public GetEmployeeNumberingConfigService(EmployeeNumberingConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EmployeeNumberingConfig> getByRuleSystemCode(String ruleSystemCode) {
        return repository.findByRuleSystemCode(ruleSystemCode.trim().toUpperCase(Locale.ROOT));
    }
}
```

```java
// UpsertEmployeeNumberingConfigService.java
package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigInvalidException;
import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.domain.port.EmployeeNumberingConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class UpsertEmployeeNumberingConfigService implements UpsertEmployeeNumberingConfigUseCase {

    private final EmployeeNumberingConfigRepository configRepository;
    private final RuleSystemRepository ruleSystemRepository;

    public UpsertEmployeeNumberingConfigService(
            EmployeeNumberingConfigRepository configRepository,
            RuleSystemRepository ruleSystemRepository) {
        this.configRepository = configRepository;
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    @Transactional
    public EmployeeNumberingConfig upsert(UpsertEmployeeNumberingConfigCommand command) {
        String ruleSystemCode = command.ruleSystemCode().trim().toUpperCase(Locale.ROOT);

        ruleSystemRepository.findByCode(ruleSystemCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rule system not found: " + ruleSystemCode));

        if (command.prefix().length() + command.numericPartLength() > 15) {
            throw new EmployeeNumberingConfigInvalidException(
                    "prefix.length() + numericPartLength must be <= 15 (employee_number is varchar(15)); " +
                    "got prefix=\"" + command.prefix() + "\" (" + command.prefix().length() +
                    ") + numericPartLength=" + command.numericPartLength());
        }

        return configRepository.save(new EmployeeNumberingConfig(
                ruleSystemCode,
                command.prefix(),
                command.numericPartLength(),
                command.step(),
                command.nextValue()
        ));
    }
}
```

- [ ] **Step 5: Check if `RuleSystem` constructor in test is correct**

Look at how other tests construct `RuleSystem` (e.g., `GetEmployeeDisplayNameFormatService` tests or grep for `new RuleSystem`). Adjust the `setUp()` mock accordingly. The constructor used in the test above is `new RuleSystem("ESP", "Spain", null)` — verify the actual constructor signature matches.

```bash
grep -r "new RuleSystem(" src/test --include="*.java" | head -3
```

If the constructor is different, update the test's `setUp()` mock accordingly.

- [ ] **Step 6: Run tests to confirm pass**

```bash
mvn test -Dtest=UpsertEmployeeNumberingConfigServiceTest
```

Expected: `BUILD SUCCESS`, 3 tests passing.

- [ ] **Step 7: Run all tests to confirm nothing broken**

```bash
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/employeenumbering/application/
git add src/test/java/com/b4rrhh/rulesystem/employeenumbering/application/
git commit -m "feat(autonumbering): add Get and Upsert use cases with tests"
```

---

## Task 5: REST Layer (Controller + DTOs + Exception Handler)

**Files:**
- Create: `…/rulesystem/employeenumbering/infrastructure/web/dto/EmployeeNumberingConfigResponse.java`
- Create: `…/rulesystem/employeenumbering/infrastructure/web/dto/UpsertEmployeeNumberingConfigRequest.java`
- Create: `…/rulesystem/employeenumbering/infrastructure/web/EmployeeNumberingConfigController.java`
- Create: `…/rulesystem/employeenumbering/infrastructure/web/EmployeeNumberingConfigExceptionHandler.java`

- [ ] **Step 1: Create DTOs**

```java
// EmployeeNumberingConfigResponse.java
package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto;

public record EmployeeNumberingConfigResponse(
        String ruleSystemCode,
        String prefix,
        int numericPartLength,
        int step,
        long nextValue,
        String nextNumberPreview
) {}
```

```java
// UpsertEmployeeNumberingConfigRequest.java
package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto;

public record UpsertEmployeeNumberingConfigRequest(
        String prefix,
        int numericPartLength,
        int step,
        long nextValue
) {}
```

- [ ] **Step 2: Create controller**

```java
// EmployeeNumberingConfigController.java
package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web;

import com.b4rrhh.rulesystem.employeenumbering.application.usecase.GetEmployeeNumberingConfigUseCase;
import com.b4rrhh.rulesystem.employeenumbering.application.usecase.UpsertEmployeeNumberingConfigCommand;
import com.b4rrhh.rulesystem.employeenumbering.application.usecase.UpsertEmployeeNumberingConfigUseCase;
import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto.EmployeeNumberingConfigResponse;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto.UpsertEmployeeNumberingConfigRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rule-systems/{ruleSystemCode}/employee-numbering-config")
public class EmployeeNumberingConfigController {

    private final GetEmployeeNumberingConfigUseCase getUseCase;
    private final UpsertEmployeeNumberingConfigUseCase upsertUseCase;

    public EmployeeNumberingConfigController(
            GetEmployeeNumberingConfigUseCase getUseCase,
            UpsertEmployeeNumberingConfigUseCase upsertUseCase) {
        this.getUseCase = getUseCase;
        this.upsertUseCase = upsertUseCase;
    }

    @GetMapping
    public ResponseEntity<EmployeeNumberingConfigResponse> get(
            @PathVariable String ruleSystemCode) {
        return getUseCase.getByRuleSystemCode(ruleSystemCode)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<EmployeeNumberingConfigResponse> upsert(
            @PathVariable String ruleSystemCode,
            @RequestBody UpsertEmployeeNumberingConfigRequest request) {
        EmployeeNumberingConfig config = upsertUseCase.upsert(new UpsertEmployeeNumberingConfigCommand(
                ruleSystemCode,
                request.prefix() != null ? request.prefix() : "",
                request.numericPartLength(),
                request.step(),
                request.nextValue()
        ));
        return ResponseEntity.ok(toResponse(config));
    }

    private EmployeeNumberingConfigResponse toResponse(EmployeeNumberingConfig config) {
        return new EmployeeNumberingConfigResponse(
                config.ruleSystemCode(),
                config.prefix(),
                config.numericPartLength(),
                config.step(),
                config.nextValue(),
                config.formatNumber()
        );
    }
}
```

- [ ] **Step 3: Create exception handler**

```java
// EmployeeNumberingConfigExceptionHandler.java
package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web;

import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigInvalidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EmployeeNumberingConfigExceptionHandler {

    @ExceptionHandler(EmployeeNumberingConfigInvalidException.class)
    public ResponseEntity<String> handleInvalid(EmployeeNumberingConfigInvalidException ex) {
        return ResponseEntity.unprocessableEntity().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.notFound().build();
    }
}
```

- [ ] **Step 4: Verify compilation and all tests pass**

```bash
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/employeenumbering/infrastructure/
git commit -m "feat(autonumbering): add REST controller, DTOs, and exception handler"
```

---

## Task 6: OpenAPI Spec Update

**Files:**
- Modify: `openapi/personnel-administration-api.yaml`

- [ ] **Step 1: Remove `employeeNumber` from the hire request schema**

Search for the `HireEmployeeRequest` schema object in the file. It looks like:

```yaml
HireEmployeeRequest:
  type: object
  required:
    - ruleSystemCode
    - employeeTypeCode
    - employeeNumber    # ← REMOVE this line
    - firstName
    ...
  properties:
    ruleSystemCode:
      ...
    employeeTypeCode:
      ...
    employeeNumber:     # ← REMOVE this entire property block
      type: string
      maxLength: 15
    firstName:
      ...
```

Remove the `employeeNumber` line from `required:` and the entire `employeeNumber:` property block.

- [ ] **Step 2: Verify the YAML is valid**

```bash
mvn test -Dtest=HireEmployeeServiceTest
```

Expected: `BUILD SUCCESS` — YAML is parsed by Spring Boot context tests without error.

- [ ] **Step 3: Commit**

```bash
git add openapi/personnel-administration-api.yaml
git commit -m "feat(autonumbering): remove employeeNumber from hire request OpenAPI schema"
```

---

## Task 7: NextEmployeeNumberPort + Adapter + Integration Test

**Files:**
- Create: `…/employee/lifecycle/application/port/NextEmployeeNumberPort.java`
- Create: `…/employee/lifecycle/infrastructure/NextEmployeeNumberAdapter.java`
- Create (test): `…/employee/lifecycle/infrastructure/NextEmployeeNumberAdapterIntegrationTest.java`

- [ ] **Step 1: Write failing integration test**

```java
// src/test/java/com/b4rrhh/employee/lifecycle/infrastructure/NextEmployeeNumberAdapterIntegrationTest.java
package com.b4rrhh.employee.lifecycle.infrastructure;

import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigNotFoundException;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingExhaustedException;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence.EmployeeNumberingConfigEntity;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence.SpringDataEmployeeNumberingConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NextEmployeeNumberAdapterIntegrationTest {

    @Autowired
    private NextEmployeeNumberAdapter adapter;

    @Autowired
    private SpringDataEmployeeNumberingConfigRepository configRepo;

    @Test
    void consumeNextReturnsDifferentNumbersOnConsecutiveCalls() {
        insertConfig("TEST_RS", "EMP", 6, 1, 1L);

        String first = adapter.consumeNext("TEST_RS");
        String second = adapter.consumeNext("TEST_RS");

        assertEquals("EMP000001", first);
        assertEquals("EMP000002", second);
    }

    @Test
    void consumeNextAdvancesByStep() {
        insertConfig("TEST_RS", "X", 4, 5, 10L);

        String first = adapter.consumeNext("TEST_RS");
        String second = adapter.consumeNext("TEST_RS");

        assertEquals("X0010", first);
        assertEquals("X0015", second);
    }

    @Test
    void consumeNextThrowsWhenConfigMissing() {
        assertThrows(EmployeeNumberingConfigNotFoundException.class,
                () -> adapter.consumeNext("NO_SUCH_RS"));
    }

    @Test
    void consumeNextThrowsWhenCounterExhausted() {
        // numericPartLength=2 → max=99; nextValue=100 overflows
        insertConfig("TEST_RS", "", 2, 1, 100L);

        assertThrows(EmployeeNumberingExhaustedException.class,
                () -> adapter.consumeNext("TEST_RS"));
    }

    private void insertConfig(String code, String prefix, int length, int step, long next) {
        EmployeeNumberingConfigEntity e = new EmployeeNumberingConfigEntity();
        e.setRuleSystemCode(code);
        e.setPrefix(prefix);
        e.setNumericPartLength(length);
        e.setStep(step);
        e.setNextValue(next);
        configRepo.save(e);
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
mvn test -Dtest=NextEmployeeNumberAdapterIntegrationTest
```

Expected: `BUILD FAILURE` — `NextEmployeeNumberAdapter` not found.

- [ ] **Step 3: Create port interface**

```java
// src/main/java/com/b4rrhh/employee/lifecycle/application/port/NextEmployeeNumberPort.java
package com.b4rrhh.employee.lifecycle.application.port;

public interface NextEmployeeNumberPort {
    /**
     * Atomically reads the next employee number for the given rule system,
     * advances the counter, and returns the formatted string.
     * Must be called within an existing transaction (MANDATORY propagation).
     *
     * @throws com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigNotFoundException if no config exists
     * @throws com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingExhaustedException if counter would overflow
     */
    String consumeNext(String ruleSystemCode);
}
```

- [ ] **Step 4: Create adapter implementation**

```java
// src/main/java/com/b4rrhh/employee/lifecycle/infrastructure/NextEmployeeNumberAdapter.java
package com.b4rrhh.employee.lifecycle.infrastructure;

import com.b4rrhh.employee.lifecycle.application.port.NextEmployeeNumberPort;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigNotFoundException;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingExhaustedException;
import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence.EmployeeNumberingConfigEntity;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence.SpringDataEmployeeNumberingConfigRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NextEmployeeNumberAdapter implements NextEmployeeNumberPort {

    private final SpringDataEmployeeNumberingConfigRepository configRepo;

    public NextEmployeeNumberAdapter(SpringDataEmployeeNumberingConfigRepository configRepo) {
        this.configRepo = configRepo;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public String consumeNext(String ruleSystemCode) {
        EmployeeNumberingConfigEntity entity = configRepo
                .findByRuleSystemCodeForUpdate(ruleSystemCode)
                .orElseThrow(() -> new EmployeeNumberingConfigNotFoundException(ruleSystemCode));

        EmployeeNumberingConfig config = new EmployeeNumberingConfig(
                entity.getRuleSystemCode(),
                entity.getPrefix(),
                entity.getNumericPartLength(),
                entity.getStep(),
                entity.getNextValue()
        );

        if (config.isExhausted()) {
            throw new EmployeeNumberingExhaustedException(ruleSystemCode);
        }

        String number = config.formatNumber();
        entity.setNextValue(entity.getNextValue() + entity.getStep());
        configRepo.save(entity);
        return number;
    }
}
```

- [ ] **Step 5: Run integration test**

```bash
mvn test -Dtest=NextEmployeeNumberAdapterIntegrationTest
```

Expected: `BUILD SUCCESS`, 4 tests passing.

**Note on H2:** `@Lock(PESSIMISTIC_WRITE)` degrades gracefully in H2 — the tests verify functional behavior (number generation and counter advance), not the locking behavior, which is a PostgreSQL concern.

- [ ] **Step 6: Run full test suite**

```bash
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/lifecycle/application/port/
git add src/main/java/com/b4rrhh/employee/lifecycle/infrastructure/NextEmployeeNumberAdapter.java
git add src/test/java/com/b4rrhh/employee/lifecycle/infrastructure/
git commit -m "feat(autonumbering): add NextEmployeeNumberPort and adapter with SELECT FOR UPDATE"
```

---

## Task 8: Hire Flow Changes (Backend)

**Files:**
- Modify: `…/employee/lifecycle/application/command/HireEmployeeCommand.java`
- Modify: `…/employee/lifecycle/infrastructure/rest/dto/HireEmployeeRequest.java`
- Modify: `…/employee/lifecycle/infrastructure/rest/HireEmployeeWebMapper.java`
- Modify: `…/employee/lifecycle/application/usecase/HireEmployeeService.java`
- Modify (test): `…/employee/lifecycle/application/usecase/HireEmployeeServiceTest.java`

The changes must be done together since `HireEmployeeCommand` is a record — removing a field from it causes cascading compilation failures in the mapper, service, and tests.

- [ ] **Step 1: Update `HireEmployeeCommand` — remove `employeeNumber`**

Current:
```java
public record HireEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,   // ← REMOVE
        String firstName,
        ...
```

After removal:
```java
public record HireEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        LocalDate hireDate,
        String entryReasonCode,
        String companyCode,
        String workCenterCode,
        HireEmployeeContractCommand contract,
        HireEmployeeLaborClassificationCommand laborClassification,
        HireEmployeeCostCenterDistributionCommand costCenterDistribution,
        HireEmployeeWorkingTimeCommand workingTime
) { /* inner records unchanged */ }
```

- [ ] **Step 2: Update `HireEmployeeRequest` — remove `employeeNumber`**

Current:
```java
public record HireEmployeeRequest(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,   // ← REMOVE
        String firstName,
        ...
```

After removal:
```java
public record HireEmployeeRequest(
        String ruleSystemCode,
        String employeeTypeCode,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        LocalDate hireDate,
        String entryReasonCode,
        String companyCode,
        String workCenterCode,
        HireEmployeeCostCenterDistributionRequest costCenterDistribution,
        HireContractRequest contract,
        HireLaborClassificationRequest laborClassification,
        HireEmployeeWorkingTimeRequest workingTime
) { /* inner records unchanged */ }
```

- [ ] **Step 3: Update `HireEmployeeWebMapper.toCommand()` — remove `employeeNumber` argument**

Current line 29:
```java
return new HireEmployeeCommand(
        normalizeCode(request.ruleSystemCode()),
        normalizeEmployeeTypeCode(request.employeeTypeCode()),
        request.employeeNumber(),    // ← REMOVE this line
        request.firstName(),
        ...
```

After:
```java
return new HireEmployeeCommand(
        normalizeCode(request.ruleSystemCode()),
        normalizeEmployeeTypeCode(request.employeeTypeCode()),
        request.firstName(),
        request.lastName1(),
        request.lastName2(),
        request.preferredName(),
        request.hireDate(),
        request.entryReasonCode(),
        request.companyCode(),
        request.workCenterCode(),
        toContractCommand(request),
        toLaborClassificationCommand(request),
        request.costCenterDistribution() != null ? new HireEmployeeCommand.HireEmployeeCostCenterDistributionCommand(
                request.costCenterDistribution().items().stream()
                        .map(item -> new HireEmployeeCommand.HireEmployeeCostCenterItemCommand(
                                item.costCenterCode(),
                                item.allocationPercentage()
                        ))
                        .collect(Collectors.toList())
        ) : null,
        toWorkingTimeCommand(request.workingTime())
);
```

- [ ] **Step 4: Update `HireEmployeeService` — inject port, generate number, remove normalizeRequiredText call**

**4a.** Add `NextEmployeeNumberPort` field and constructor parameter. The service currently has 10 constructor parameters. Add `NextEmployeeNumberPort nextEmployeeNumberPort` as the 11th (add at the end or in a logical position — recommend at the end to minimize diff):

In the field declarations, add:
```java
private final NextEmployeeNumberPort nextEmployeeNumberPort;
```

In the constructor, add the parameter:
```java
public HireEmployeeService(
        EmployeeRepository employeeRepository,
        CreateEmployeeUseCase createEmployeeUseCase,
        CreatePresenceUseCase createPresenceUseCase,
        CreateLaborClassificationUseCase createLaborClassificationUseCase,
        CreateContractUseCase createContractUseCase,
        CreateWorkCenterUseCase createWorkCenterUseCase,
        CreateCostCenterDistributionUseCase createCostCenterDistributionUseCase,
        CreateWorkingTimeUseCase createWorkingTimeUseCase,
        WorkCenterCompanyValidator workCenterCompanyValidator,
        EmployeeTypeCatalogValidator employeeTypeCatalogValidator,
        NextEmployeeNumberPort nextEmployeeNumberPort        // new
) {
    // ... existing assignments ...
    this.nextEmployeeNumberPort = nextEmployeeNumberPort;
}
```

Add the import:
```java
import com.b4rrhh.employee.lifecycle.application.port.NextEmployeeNumberPort;
```

**4b.** In `hire()`, replace the `normalizeRequiredText("employeeNumber", command.employeeNumber())` line (currently line 107) with a call to the port. The port call must happen **inside** the `@Transactional` method, **before** the try block that runs the downstream create commands.

Find the line:
```java
String employeeNumber = normalizeRequiredText("employeeNumber", command.employeeNumber());
```

Replace it with:
```java
String employeeNumber = nextEmployeeNumberPort.consumeNext(ruleSystemCode);
```

(At this point `ruleSystemCode` has already been normalized by a prior `normalizeRequiredText` call in `hire()`.)

**Also add these imports:**
```java
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigNotFoundException;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingExhaustedException;
```

**4c.** The port's exceptions (`EmployeeNumberingConfigNotFoundException`, `EmployeeNumberingExhaustedException`) must be mapped to lifecycle exceptions in the multi-catch at the top of the try block (or before the try block if the port call is outside it). Since the port call is **outside** the try block, these exceptions propagate as-is. Add an exception handler in `EmployeeNumberingConfigExceptionHandler` to catch them and return HTTP 422/409 respectively — OR wrap them in `HireEmployeeRequestInvalidException` / `HireEmployeeConflictException` inside `hire()`:

After the `consumeNext` call, the exceptions propagate uncaught through the service. The existing `EmployeeNumberingConfigExceptionHandler` in the `rulesystem.employeenumbering` package already handles them. Since `HireEmployeeService` is not in the web layer, the exceptions will bubble up through the controller and be caught by the exception handler. **No additional wrapping needed.**

- [ ] **Step 5: Update `HireEmployeeServiceTest`**

**5a.** Add the mock field:
```java
@Mock
private NextEmployeeNumberPort nextEmployeeNumberPort;
```

**5b.** Add it to the `service = new HireEmployeeService(...)` constructor call in `setUp()`:
```java
service = new HireEmployeeService(
        employeeRepository,
        createEmployeeUseCase,
        createPresenceUseCase,
        createLaborClassificationUseCase,
        createContractUseCase,
        createWorkCenterUseCase,
        createCostCenterDistributionUseCase,
        createWorkingTimeUseCase,
        workCenterCompanyValidator,
        employeeTypeCatalogValidator,
        nextEmployeeNumberPort           // new
);
```

**5c.** Add the import:
```java
import com.b4rrhh.employee.lifecycle.application.port.NextEmployeeNumberPort;
```

**5d.** In `hiresEmployeeAndPropagatesHireDateToAllInitialRecords()`, add a stub for the port before the existing stubs:
```java
when(nextEmployeeNumberPort.consumeNext("ESP")).thenReturn("EMP001");
```

**5e.** Update `validCommand()` — remove the `"EMP001"` argument (3rd position):

Current:
```java
return new HireEmployeeCommand(
        "ESP",
        "INTERNAL",
        "EMP001",     // ← REMOVE
        "Ana",
        ...
```

After:
```java
return new HireEmployeeCommand(
        "ESP",
        "INTERNAL",
        "Ana",
        "Lopez",
        null,
        "Ani",
        LocalDate.of(2026, 3, 23),
        "HIRE",
        "COMP",
        "WC1",
        new HireEmployeeCommand.HireEmployeeContractCommand("CON", "SUB"),
        new HireEmployeeCommand.HireEmployeeLaborClassificationCommand("AGR", "CAT"),
        null,
        new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(new BigDecimal("75"))
);
```

**5f.** For tests that test failure scenarios (e.g., `failsWhenEmployeeAlreadyExists`, `failsFastWhenWorkCenterDoesNotBelongToCompany`, `mapsInvalidEmployeeTypeToLifecycleException`), the port must be stubbed so the test doesn't fail on a missing stub. Add `lenient()` stub in `setUp()`:
```java
lenient().when(nextEmployeeNumberPort.consumeNext(any())).thenReturn("EMP001");
```

**5g.** Check other test methods (lines 200–420) for any other `HireEmployeeCommand` constructors with 15 args (was: ruleSystemCode, employeeTypeCode, employeeNumber, …). Remove `employeeNumber` from each one.

- [ ] **Step 6: Run tests**

```bash
mvn test -Dtest=HireEmployeeServiceTest
```

Expected: `BUILD SUCCESS`, all tests passing.

- [ ] **Step 7: Run full test suite**

```bash
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/lifecycle/
git add src/test/java/com/b4rrhh/employee/lifecycle/application/usecase/HireEmployeeServiceTest.java
git commit -m "feat(autonumbering): wire NextEmployeeNumberPort into hire flow, remove manual employeeNumber"
```

---

## Task 9: Frontend — Regenerate API Client + Numbering Config Card

**Working directory:** `b4rrhh_frontend`

**Files:**
- Run: `npm run api:refresh`
- Create: `src/app/core/api/clients/employee-numbering-config.client.ts`
- Create: `src/app/features/company/ui/employee-numbering-config-card.component.ts`
- Create: `src/app/features/company/ui/employee-numbering-config-card.component.html`
- Create: `src/app/features/company/ui/employee-numbering-config-card.component.scss`
- Modify: `src/app/features/company/ui/company-page.component.html`

- [ ] **Step 1: Regenerate the OpenAPI client**

```bash
cd b4rrhh_frontend
npm run api:refresh
```

Expected: the generated files in `src/app/core/api/generated/` are updated. `HireEmployeeRequest` in generated models no longer has `employeeNumber`.

- [ ] **Step 2: Create the API client**

```typescript
// src/app/core/api/clients/employee-numbering-config.client.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EmployeeNumberingConfig {
  ruleSystemCode: string;
  prefix: string;
  numericPartLength: number;
  step: number;
  nextValue: number;
  nextNumberPreview: string;
}

export interface UpsertEmployeeNumberingConfigRequest {
  prefix: string;
  numericPartLength: number;
  step: number;
  nextValue: number;
}

@Injectable({ providedIn: 'root' })
export class EmployeeNumberingConfigClient {
  private readonly http = inject(HttpClient);

  get(ruleSystemCode: string): Observable<EmployeeNumberingConfig> {
    return this.http.get<EmployeeNumberingConfig>(
      `/api/rule-systems/${ruleSystemCode}/employee-numbering-config`
    );
  }

  upsert(
    ruleSystemCode: string,
    request: UpsertEmployeeNumberingConfigRequest
  ): Observable<EmployeeNumberingConfig> {
    return this.http.put<EmployeeNumberingConfig>(
      `/api/rule-systems/${ruleSystemCode}/employee-numbering-config`,
      request
    );
  }
}
```

- [ ] **Step 3: Create the card component TypeScript**

```typescript
// src/app/features/company/ui/employee-numbering-config-card.component.ts
import {
  Component, computed, input, OnChanges, signal, SimpleChanges, inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import {
  EmployeeNumberingConfigClient,
  EmployeeNumberingConfig,
  UpsertEmployeeNumberingConfigRequest
} from '../../../core/api/clients/employee-numbering-config.client';

@Component({
  selector: 'app-employee-numbering-config-card',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, InputTextModule, InputNumberModule],
  templateUrl: './employee-numbering-config-card.component.html',
  styleUrl: './employee-numbering-config-card.component.scss'
})
export class EmployeeNumberingConfigCardComponent implements OnChanges {
  readonly ruleSystemCode = input.required<string>();

  private readonly client = inject(EmployeeNumberingConfigClient);

  readonly config = signal<EmployeeNumberingConfig | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly notConfigured = signal(false);

  // Draft form fields
  readonly draftPrefix = signal('');
  readonly draftNumericPartLength = signal(6);
  readonly draftStep = signal(1);
  readonly draftNextValue = signal(1);

  readonly preview = computed(() => {
    const prefix = this.draftPrefix();
    const length = this.draftNumericPartLength();
    const next = this.draftNextValue();
    if (length < 1 || length > 15 || prefix.length + length > 15) return '—';
    return prefix + String(next).padStart(length, '0');
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ruleSystemCode']) {
      this.load();
    }
  }

  private load(): void {
    this.loading.set(true);
    this.client.get(this.ruleSystemCode()).subscribe({
      next: (cfg) => {
        this.config.set(cfg);
        this.notConfigured.set(false);
        this.syncDraftFromConfig(cfg);
        this.loading.set(false);
      },
      error: () => {
        this.config.set(null);
        this.notConfigured.set(true);
        this.loading.set(false);
      }
    });
  }

  private syncDraftFromConfig(cfg: EmployeeNumberingConfig): void {
    this.draftPrefix.set(cfg.prefix);
    this.draftNumericPartLength.set(cfg.numericPartLength);
    this.draftStep.set(cfg.step);
    this.draftNextValue.set(cfg.nextValue);
  }

  save(): void {
    this.saving.set(true);
    const request: UpsertEmployeeNumberingConfigRequest = {
      prefix: this.draftPrefix(),
      numericPartLength: this.draftNumericPartLength(),
      step: this.draftStep(),
      nextValue: this.draftNextValue()
    };
    this.client.upsert(this.ruleSystemCode(), request).subscribe({
      next: (cfg) => {
        this.config.set(cfg);
        this.notConfigured.set(false);
        this.syncDraftFromConfig(cfg);
        this.saving.set(false);
      },
      error: () => {
        this.saving.set(false);
      }
    });
  }
}
```

- [ ] **Step 4: Create the card template**

```html
<!-- src/app/features/company/ui/employee-numbering-config-card.component.html -->
<div class="card">
  <h3 class="mt-0 mb-3">Numeración de matrícula</h3>

  @if (loading()) {
    <p class="text-color-secondary">Cargando...</p>
  } @else {
    @if (notConfigured()) {
      <p class="text-color-secondary mb-3">Sin configurar — introduce los valores para activar la autonumeración.</p>
    }

    <div class="grid gap-3">
      <div class="field col-12 sm:col-6 md:col-3">
        <label class="block mb-1">Prefijo</label>
        <input pInputText [ngModel]="draftPrefix()" (ngModelChange)="draftPrefix.set($event)"
               placeholder="EMP" class="w-full" maxlength="14" />
      </div>

      <div class="field col-12 sm:col-6 md:col-3">
        <label class="block mb-1">Dígitos numéricos</label>
        <p-inputNumber [ngModel]="draftNumericPartLength()"
                       (ngModelChange)="draftNumericPartLength.set($event)"
                       [min]="1" [max]="14" class="w-full" />
      </div>

      <div class="field col-12 sm:col-6 md:col-3">
        <label class="block mb-1">Incremento</label>
        <p-inputNumber [ngModel]="draftStep()"
                       (ngModelChange)="draftStep.set($event)"
                       [min]="1" class="w-full" />
      </div>

      <div class="field col-12 sm:col-6 md:col-3">
        <label class="block mb-1">Siguiente valor</label>
        <p-inputNumber [ngModel]="draftNextValue()"
                       (ngModelChange)="draftNextValue.set($event)"
                       [min]="1" class="w-full" />
      </div>
    </div>

    <div class="flex align-items-center gap-3 mt-2">
      <div class="preview-badge">
        Siguiente matrícula: <strong>{{ preview() }}</strong>
      </div>
      <p-button label="Guardar" [loading]="saving()" (onClick)="save()" />
    </div>
  }
</div>
```

- [ ] **Step 5: Create styles**

```scss
// src/app/features/company/ui/employee-numbering-config-card.component.scss
.preview-badge {
  background: var(--indigo-50);
  color: var(--indigo-700);
  border-radius: 4px;
  padding: 4px 12px;
  font-size: 0.875rem;
}
```

- [ ] **Step 6: Add the card to the company page**

In `src/app/features/company/ui/company-page.component.html`, find where `<app-display-name-format-card>` is rendered (around line 38) and add the numbering card below it, following the same pattern:

```html
<app-employee-numbering-config-card
  [ruleSystemCode]="store.selectedKey()!.ruleSystemCode"
/>
```

In `company-page.component.ts`, add the import:
```typescript
import { EmployeeNumberingConfigCardComponent } from './employee-numbering-config-card.component';
```

And add `EmployeeNumberingConfigCardComponent` to the `imports` array of the component decorator.

- [ ] **Step 7: Build to verify no TypeScript errors**

```bash
npm run build 2>&1 | tail -20
```

Expected: no errors.

- [ ] **Step 8: Commit**

```bash
git add src/app/core/api/clients/employee-numbering-config.client.ts
git add src/app/features/company/ui/employee-numbering-config-card.component.*
git add src/app/features/company/ui/company-page.component.*
git add src/app/core/api/generated/
git commit -m "feat(autonumbering): add numbering config card component and regenerate API client"
```

---

## Task 10: Frontend — Remove `employeeNumber` from Hire Form

**Working directory:** `b4rrhh_frontend`

**Files:**
- Modify: `src/app/features/employee/models/employee-hiring.model.ts`
- Modify: `src/app/features/employee/data-access/employee-hiring.mapper.ts`
- Modify: `src/app/features/employee/lifecycle/hire/pages/hire-employee-page.component.ts`
- Modify: `src/app/features/employee/lifecycle/hire/pages/hire-employee-page.component.html`

- [ ] **Step 1: Remove `employeeNumber` from `HireEmployeeDraft` model**

In `employee-hiring.model.ts`, find `employeeNumber: string` in the `HireEmployeeDraft` interface and remove it.

- [ ] **Step 2: Remove `employeeNumber` from mapper**

In `employee-hiring.mapper.ts`, `mapDraftToHireRequest()` has a line like `employeeNumber: draft.employeeNumber`. Remove it.

The `mapResponseToResult()` method reads `response.employeeNumber` — this remains unchanged since the response still contains the generated number.

- [ ] **Step 3: Remove the form control from the hire component**

In `hire-employee-page.component.ts`, in `ngOnInit` or wherever the `FormGroup` is constructed, find:

```typescript
employeeNumber: ['', Validators.required],
```

Remove this line.

- [ ] **Step 4: Remove the input field from the hire form HTML**

In `hire-employee-page.component.html`, remove the block:

```html
<div class="flex flex-column gap-2">
  <label for="employeeNumber">{{ texts.detailHeaderEmployeeNumberLabel }} *</label>
  <input pInputText id="employeeNumber" formControlName="employeeNumber" class="w-full" />
</div>
```

- [ ] **Step 5: Build and check for leftover references**

```bash
npm run build 2>&1 | tail -20
```

Expected: no errors. If any `employeeNumber` references remain in the hire feature, the build will report them.

- [ ] **Step 6: Commit**

```bash
git add src/app/features/employee/models/employee-hiring.model.ts
git add src/app/features/employee/data-access/employee-hiring.mapper.ts
git add src/app/features/employee/lifecycle/hire/pages/hire-employee-page.component.*
git commit -m "feat(autonumbering): remove employeeNumber from frontend hire form"
```

---

## Task 11: Workforce Loader

**Working directory:** `b4rrhh_workforce_loader`

**Files:**
- Modify: `…/workforceloader/infrastructure/api/dto/HireEmployeeRequest.java`
- Modify: `…/workforceloader/infrastructure/api/dto/HireEmployeeResponse.java`
- Modify: `…/workforceloader/domain/model/SyntheticEmployee.java`
- Modify: `…/workforceloader/application/RunLifecycleSimulationService.java`
- Modify (test): `…/workforceloader/application/HireReferenceDataResolverTest.java`

- [ ] **Step 1: Remove `employeeNumber` from the loader's `HireEmployeeRequest`**

Current (line 9):
```java
public record HireEmployeeRequest(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,   // ← REMOVE
        ...
```

After:
```java
public record HireEmployeeRequest(
        String ruleSystemCode,
        String employeeTypeCode,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        LocalDate hireDate,
        String entryReasonCode,
        String companyCode,
        String workCenterCode,
        Contract contract,
        LaborClassification laborClassification,
        WorkingTime workingTime,
        CostCenterDistribution costCenterDistribution
) { /* inner records unchanged */ }
```

- [ ] **Step 2: Add `employeeNumber` to the loader's `HireEmployeeResponse`**

Current:
```java
public record HireEmployeeResponse(
        String status,
        String message
) {}
```

After:
```java
public record HireEmployeeResponse(
        String status,
        String message,
        String employeeNumber
) {}
```

- [ ] **Step 3: Add `withEmployeeNumber()` to `SyntheticEmployee`**

`SyntheticEmployee` is a Java record (immutable). Add a custom method that returns a new instance with the given number:

```java
public record SyntheticEmployee(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        LocalDate hireDate,
        BigDecimal workingTimePercentage
) {
    public SyntheticEmployee withEmployeeNumber(String number) {
        return new SyntheticEmployee(
                ruleSystemCode, employeeTypeCode, number,
                firstName, lastName1, lastName2, preferredName,
                hireDate, workingTimePercentage
        );
    }
}
```

- [ ] **Step 4: Update `RunLifecycleSimulationService`**

**4a.** In `toHireRequest()`, remove `employee.employeeNumber()` (line 238):

Current:
```java
return new HireEmployeeRequest(
        normalizeCode(employee.ruleSystemCode()),
        normalizeCode(employee.employeeTypeCode()),
        employee.employeeNumber(),    // ← REMOVE
        employee.firstName(),
        ...
```

After:
```java
return new HireEmployeeRequest(
        normalizeCode(employee.ruleSystemCode()),
        normalizeCode(employee.employeeTypeCode()),
        employee.firstName(),
        employee.lastName1(),
        employee.lastName2(),
        employee.preferredName(),
        event.effectiveDate(),
        normalizeCode(resolvedHireData.entryReasonCode()),
        normalizeCode(resolvedHireData.companyCode()),
        normalizeCode(resolvedHireData.workCenterCode()),
        new HireEmployeeRequest.Contract(
                normalizeCode(resolvedHireData.contractTypeCode()),
                normalizeCode(resolvedHireData.contractSubtypeCode())
        ),
        new HireEmployeeRequest.LaborClassification(
                normalizeCode(resolvedHireData.agreementCode()),
                normalizeCode(resolvedHireData.agreementCategoryCode())
        ),
        buildHireWorkingTime(employee, resolvedHireData),
        buildHireCostCenterDistribution(employee)
);
```

**4b.** Change `executeHire()` to return `HireEmployeeResponse` instead of `EventOutcome`, and expose the response for the caller:

Current:
```java
private EventOutcome executeHire(HireEmployeeRequest request) {
    if (properties.getRun().isDryRun()) {
        return EventOutcome.success("DRY-RUN payload -> " + summarizeHirePayload(request));
    }
    try {
        HireEmployeeResponse response = b4rrhhLifecycleClient.hire(request);
        return EventOutcome.success(summarizeApiResponse("Hire", response.status(), response.message()));
    } catch (Exception ex) {
        return EventOutcome.failure(ex.getMessage());
    }
}
```

Add a small inner record to carry both the outcome and the employee number:

```java
private record HireResult(EventOutcome outcome, String employeeNumber) {}
```

Replace `executeHire` with:

```java
private HireResult executeHire(HireEmployeeRequest request) {
    if (properties.getRun().isDryRun()) {
        return new HireResult(
                EventOutcome.success("DRY-RUN payload -> " + summarizeHirePayload(request)),
                null
        );
    }
    try {
        HireEmployeeResponse response = b4rrhhLifecycleClient.hire(request);
        return new HireResult(
                EventOutcome.success(summarizeApiResponse("Hire", response.status(), response.message())),
                response.employeeNumber()
        );
    } catch (Exception ex) {
        return new HireResult(EventOutcome.failure(ex.getMessage()), null);
    }
}
```

**4c.** Update the HIRE branch in the event loop to capture the employee number and rebind `employee`:

Current (around lines 103–119):
```java
case HIRE -> {
    hiresRequested++;
    try {
        HireEmployeeRequest request = toHireRequest(employee, event, employeeResolvedHireData);
        outcome = executeHire(request);
        if (outcome.success()) {
            hiresSuccess++;
            applyInitialHireState(executionState, employeeResolvedHireData, event.effectiveDate(), request.costCenterDistribution());
        } else {
            hiresFailed++;
            scenarioCanContinue = false;
        }
    } catch (IllegalArgumentException ex) {
        hiresFailed++;
        outcome = EventOutcome.failure(ex.getMessage());
        scenarioCanContinue = false;
    }
}
```

After:
```java
case HIRE -> {
    hiresRequested++;
    try {
        HireEmployeeRequest request = toHireRequest(employee, event, employeeResolvedHireData);
        HireResult hireResult = executeHire(request);
        outcome = hireResult.outcome();
        if (outcome.success()) {
            hiresSuccess++;
            if (hireResult.employeeNumber() != null) {
                employee = employee.withEmployeeNumber(hireResult.employeeNumber());
            }
            applyInitialHireState(executionState, employeeResolvedHireData, event.effectiveDate(), request.costCenterDistribution());
        } else {
            hiresFailed++;
            scenarioCanContinue = false;
        }
    } catch (IllegalArgumentException ex) {
        hiresFailed++;
        outcome = EventOutcome.failure(ex.getMessage());
        scenarioCanContinue = false;
    }
}
```

**Note:** `employee` is now `var` or must be declared as a non-final local variable. In Java, range variables from for-each loops can be re-bound. Verify `SyntheticEmployee employee = scenario.syntheticEmployee();` (line 88) is a plain local variable (not `final`) — it is, so re-binding works.

- [ ] **Step 5: Fix `HireReferenceDataResolverTest` if needed**

Run:
```bash
cd b4rrhh_workforce_loader
mvn test -Dtest=HireReferenceDataResolverTest
```

If the test constructs a `HireEmployeeRequest` directly with `employeeNumber`, remove that argument. The test at line 60 uses `newResolver()` and calls `preloadPools()` — it does not call `toHireRequest()` — so it likely doesn't reference `employeeNumber` directly. If it does fail, remove the argument.

- [ ] **Step 6: Run all loader tests**

```bash
cd b4rrhh_workforce_loader
mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit**

```bash
cd b4rrhh_workforce_loader
git add src/
git commit -m "feat(autonumbering): remove employeeNumber from hire request, capture from response"
```

---

## Final Verification

- [ ] **Backend: full test suite**

```bash
cd b4rrhh_backend
mvn test
```

Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Frontend: build**

```bash
cd b4rrhh_frontend
npm run build
```

Expected: no errors.

- [ ] **Workforce Loader: full test suite**

```bash
cd b4rrhh_workforce_loader
mvn test
```

Expected: `BUILD SUCCESS`.
