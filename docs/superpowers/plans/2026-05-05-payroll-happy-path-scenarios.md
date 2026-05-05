# Payroll Happy Path Scenarios Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 4 integration tests (`PayrollHappyPathIntegrationTest`) that validate the payroll calculation engine against realistic business scenarios, extracting the shared concept graph seed into `PayrollScenarioFixtures` and refactoring the existing E2E test to use it.

**Architecture:** Extract the inline seed from `LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest` into a plain helper class `PayrollScenarioFixtures(JdbcTemplate)`. The new test class `PayrollHappyPathIntegrationTest` uses the same `@SpringBootTest` pattern (H2 in-memory, `create-drop`, Flyway off, `ELIGIBLE_REAL` mode) with its own DB name to avoid context collision. Each of the 4 test methods seeds one employee lifecycle scenario and asserts exact amounts via `compareTo == 0`.

**Tech Stack:** Java 21, Spring Boot Test, H2 in-memory (PostgreSQL mode), JUnit 5, `@Transactional` (per-test rollback), `LaunchPayrollCalculationUseCase`, `JdbcTemplate`

---

## File Map

| Action | Path |
|--------|------|
| Create | `src/test/java/com/b4rrhh/payroll/scenario/PayrollScenarioFixtures.java` |
| Create | `src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java` |
| Modify | `src/test/java/com/b4rrhh/payroll/application/usecase/LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest.java` |

---

## Domain Reminders

- **Segment** = subdivision of a presence caused by a mid-period working-time change.
- **Presence** = a continuous employment stint. Multiple presences → multiple `Payroll` records.
- `D01 = min(daysInSegment, 30)`. Applied per segment.
- `SALARIO_BASE per segment = D01 × P02(47.50) × J01(workingTimePercentage / 100)`.
- PERCENTAGE concepts (700, 703, 800) operate on the period total of SALARIO_BASE (B01 aggregate, PERIOD-scoped).
- Technical concepts (D01, J01, P01, P02, B01, P_SS_CC, P_SS_DESEMPLEO, P_IRPF) are NOT persisted (`payslip_order_code = null`).
- Only 7 concepts are persisted per payroll: 101, 700, 703, 800, 970, 980, 990.
- `buildSegments()` in `CalculatePayrollUnitService` clips every working-time window to `[presenceStart, presenceEnd]`; a window where clipped start > clipped end is silently skipped.
- The working-time query (`findOverlappingByEmployeeIdAndPeriodOrdered`) uses the **payroll period** dates (Jan 1–31), not the presence dates — the presence-scoped clipping happens in `buildSegments()`.
- `end_date = null` on a working-time record means "open until period end" — **this bleeds into adjacent presences of the same employee**. Always set an explicit `end_date` on working-time records that must not bleed.

---

## Task 1: Create `PayrollScenarioFixtures`

**Files:**
- Create: `src/test/java/com/b4rrhh/payroll/scenario/PayrollScenarioFixtures.java`

- [ ] **Step 1: Create the file**

```java
package com.b4rrhh.payroll.scenario;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PayrollScenarioFixtures {

    static final String AGREEMENT_CODE     = "99002405011982";
    static final String CATEGORY_CODE      = "99002405-G2";
    static final String TABLE_CODE         = "P02_99002405011982";
    static final BigDecimal DAILY_RATE     = new BigDecimal("47.50");

    private final JdbcTemplate jdbc;

    public PayrollScenarioFixtures(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Seeds the full concept graph: rule_system, 15 concepts + 1 TABLE object,
     * operands, feed relations, concept assignments, activations, binding,
     * table row, and the agreement_category_profile.
     */
    public void seedConceptGraph(String ruleSystemCode) {
        jdbc.update(
                "insert into rulesystem.rule_system (code, name, country_code, active, created_at, updated_at)" +
                " values (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                ruleSystemCode, ruleSystemCode, ruleSystemCode, true);

        for (String code : new String[]{"101","D01","J01","P01","P02","B01",
                "P_SS_CC","P_SS_DESEMPLEO","P_IRPF","700","703","800","970","980","990"}) {
            jdbc.update(
                    "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at)" +
                    " values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    ruleSystemCode, "CONCEPT", code);
        }
        jdbc.update(
                "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at)" +
                " values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                ruleSystemCode, "TABLE", "P02_DAILY_AMOUNT_TABLE");

        Long id101         = objectId(ruleSystemCode, "CONCEPT", "101");
        Long idD01         = objectId(ruleSystemCode, "CONCEPT", "D01");
        Long idJ01         = objectId(ruleSystemCode, "CONCEPT", "J01");
        Long idP01         = objectId(ruleSystemCode, "CONCEPT", "P01");
        Long idP02         = objectId(ruleSystemCode, "CONCEPT", "P02");
        Long idB01         = objectId(ruleSystemCode, "CONCEPT", "B01");
        Long idPSSCC       = objectId(ruleSystemCode, "CONCEPT", "P_SS_CC");
        Long idPSSDESEMP   = objectId(ruleSystemCode, "CONCEPT", "P_SS_DESEMPLEO");
        Long idPIRPF       = objectId(ruleSystemCode, "CONCEPT", "P_IRPF");
        Long id700         = objectId(ruleSystemCode, "CONCEPT", "700");
        Long id703         = objectId(ruleSystemCode, "CONCEPT", "703");
        Long id800         = objectId(ruleSystemCode, "CONCEPT", "800");
        Long id970         = objectId(ruleSystemCode, "CONCEPT", "970");
        Long id980         = objectId(ruleSystemCode, "CONCEPT", "980");
        Long id990         = objectId(ruleSystemCode, "CONCEPT", "990");
        Long idP02Table    = objectId(ruleSystemCode, "TABLE",   "P02_DAILY_AMOUNT_TABLE");

        String cSql = "insert into payroll_engine.payroll_concept" +
                " (object_id, concept_mnemonic, calculation_type, functional_nature, result_composition_mode," +
                "  payslip_order_code, execution_scope, persist_to_concepts, created_at, updated_at)" +
                " values (?, ?, ?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbc.update(cSql, id101,       "SALARIO_BASE",            "RATE_BY_QUANTITY", "EARNING",         "REPLACE", "101",  "PERIOD");
        jdbc.update(cSql, idD01,       "DIAS_DEVENGO",            "ENGINE_PROVIDED",  "TECHNICAL",       "REPLACE",  null,  "PERIOD");
        jdbc.update(cSql, idJ01,       "COEFICIENTE_JORNADA",     "ENGINE_PROVIDED",  "TECHNICAL",       "REPLACE",  null,  "PERIOD");
        jdbc.update(cSql, idP01,       "PRECIO_DIA",              "RATE_BY_QUANTITY", "BASE",            "REPLACE",  null,  "PERIOD");
        jdbc.update(cSql, idP02,       "PRECIO_DIA_PLENO",        "DIRECT_AMOUNT",    "BASE",            "REPLACE",  null,  "PERIOD");
        jdbc.update(cSql, idB01,       "BASE_COTIZABLE",          "AGGREGATE",        "BASE",            "REPLACE",  null,  "PERIOD");
        jdbc.update(cSql, idPSSCC,     "TIPO_CC_TRABAJADOR",      "ENGINE_PROVIDED",  "TECHNICAL",       "REPLACE",  null,  "PERIOD");
        jdbc.update(cSql, idPSSDESEMP, "TIPO_DESEMPLEO_TRABAJADOR","ENGINE_PROVIDED", "TECHNICAL",       "REPLACE",  null,  "PERIOD");
        jdbc.update(cSql, idPIRPF,     "TIPO_IRPF",               "ENGINE_PROVIDED",  "TECHNICAL",       "REPLACE",  null,  "PERIOD");
        jdbc.update(cSql, id700,       "CC_TRABAJADOR",           "PERCENTAGE",       "DEDUCTION",       "REPLACE", "700",  "PERIOD");
        jdbc.update(cSql, id703,       "DESEMPLEO_TRABAJADOR",    "PERCENTAGE",       "DEDUCTION",       "REPLACE", "703",  "PERIOD");
        jdbc.update(cSql, id800,       "RETENCION_IRPF",          "PERCENTAGE",       "DEDUCTION",       "REPLACE", "800",  "PERIOD");
        jdbc.update(cSql, id970,       "TOTAL_DEVENGOS",          "AGGREGATE",        "TOTAL_EARNING",   "REPLACE", "970",  "PERIOD");
        jdbc.update(cSql, id980,       "TOTAL_DEDUCCIONES",       "AGGREGATE",        "TOTAL_DEDUCTION", "REPLACE", "980",  "PERIOD");
        jdbc.update(cSql, id990,       "LIQUIDO_A_PAGAR",         "AGGREGATE",        "NET_PAY",         "REPLACE", "990",  "PERIOD");

        String oSql = "insert into payroll_engine.payroll_concept_operand" +
                " (target_object_id, operand_role, source_object_id, created_at, updated_at)" +
                " values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbc.update(oSql, id101, "QUANTITY",   idD01);
        jdbc.update(oSql, id101, "RATE",       idP01);
        jdbc.update(oSql, idP01, "QUANTITY",   idJ01);
        jdbc.update(oSql, idP01, "RATE",       idP02);
        jdbc.update(oSql, id700, "BASE",       idB01);
        jdbc.update(oSql, id700, "PERCENTAGE", idPSSCC);
        jdbc.update(oSql, id703, "BASE",       idB01);
        jdbc.update(oSql, id703, "PERCENTAGE", idPSSDESEMP);
        jdbc.update(oSql, id800, "BASE",       idB01);
        jdbc.update(oSql, id800, "PERCENTAGE", idPIRPF);

        String fSql = "insert into payroll_engine.payroll_concept_feed_relation" +
                " (source_object_id, target_object_id, feed_mode, feed_value, invert_sign," +
                "  effective_from, effective_to, created_at, updated_at)" +
                " values (?, ?, ?, ?, false, DATE '2025-01-01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        String fInv = "insert into payroll_engine.payroll_concept_feed_relation" +
                " (source_object_id, target_object_id, feed_mode, feed_value, invert_sign," +
                "  effective_from, effective_to, created_at, updated_at)" +
                " values (?, ?, ?, ?, true, DATE '2025-01-01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbc.update(fSql,  idP02Table, idP02, "FEED_BY_SOURCE", null);
        jdbc.update(fSql,  id101,      idB01, "FEED_BY_SOURCE", null);
        jdbc.update(fSql,  id101,      id970, "FEED_BY_SOURCE", null);
        jdbc.update(fSql,  id700,      id980, "FEED_BY_SOURCE", null);
        jdbc.update(fSql,  id703,      id980, "FEED_BY_SOURCE", null);
        jdbc.update(fSql,  id800,      id980, "FEED_BY_SOURCE", null);
        jdbc.update(fSql,  id970,      id990, "FEED_BY_SOURCE", null);
        jdbc.update(fInv,  id980,      id990, "FEED_BY_SOURCE", null);

        String aSql = "insert into payroll_engine.concept_assignment" +
                " (rule_system_code, concept_code, company_code, agreement_code, employee_type_code," +
                "  valid_from, valid_to, priority, created_at, updated_at)" +
                " values (?, ?, ?, ?, ?, DATE '2025-01-01', null, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbc.update(aSql, ruleSystemCode, "101", null, AGREEMENT_CODE, null, 10);
        jdbc.update(aSql, ruleSystemCode, "700", null, AGREEMENT_CODE, null, 700);
        jdbc.update(aSql, ruleSystemCode, "703", null, AGREEMENT_CODE, null, 703);
        jdbc.update(aSql, ruleSystemCode, "800", null, AGREEMENT_CODE, null, 800);
        jdbc.update(aSql, ruleSystemCode, "970", null, AGREEMENT_CODE, null, 970);
        jdbc.update(aSql, ruleSystemCode, "980", null, AGREEMENT_CODE, null, 980);
        jdbc.update(aSql, ruleSystemCode, "990", null, AGREEMENT_CODE, null, 990);

        for (String cc : new String[]{"101","700","703","800"}) {
            jdbc.update(
                    "insert into payroll.payroll_object_activation" +
                    " (rule_system_code, owner_type_code, owner_code, target_object_type_code, target_object_code, active)" +
                    " values (?, ?, ?, ?, ?, ?)",
                    ruleSystemCode, "AGREEMENT", AGREEMENT_CODE, "PAYROLL_CONCEPT", cc, true);
        }
        jdbc.update(
                "insert into payroll.payroll_object_binding" +
                " (rule_system_code, owner_type_code, owner_code, binding_role_code, bound_object_type_code, bound_object_code, active)" +
                " values (?, ?, ?, ?, ?, ?, ?)",
                ruleSystemCode, "AGREEMENT", AGREEMENT_CODE, "P02_DAILY_AMOUNT_TABLE", "TABLE", TABLE_CODE, true);
        jdbc.update(
                "insert into payroll.payroll_table_row (rule_system_code, table_code, search_code, start_date, end_date, daily_value, active)" +
                " values (?, ?, ?, DATE '2025-01-01', null, ?, ?)",
                ruleSystemCode, TABLE_CODE, CATEGORY_CODE, DAILY_RATE, true);

        jdbc.update(
                "insert into rulesystem.rule_entity" +
                " (rule_system_code, rule_entity_type_code, code, name, active, start_date, created_at, updated_at)" +
                " values (?, ?, ?, ?, ?, DATE '2025-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                ruleSystemCode, "AGREEMENT_CATEGORY", CATEGORY_CODE, "G2", true);
        Long categoryId = jdbc.queryForObject(
                "select id from rulesystem.rule_entity where rule_system_code = ? and rule_entity_type_code = ? and code = ?",
                Long.class, ruleSystemCode, "AGREEMENT_CATEGORY", CATEGORY_CODE);
        jdbc.update(
                "insert into rulesystem.agreement_category_profile" +
                " (agreement_category_rule_entity_id, grupo_cotizacion_code, tipo_nomina, created_at, updated_at)" +
                " values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                categoryId, "05", "MENSUAL");
    }

    /** Inserts one employee row; returns the generated surrogate id. */
    public long insertEmployee(String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        jdbc.update(
                "insert into employee.employee" +
                " (rule_system_code, employee_type_code, employee_number, first_name, last_name_1, status, created_at, updated_at)" +
                " values (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                ruleSystemCode, employeeTypeCode, employeeNumber, "Test", "Employee", "ACTIVE");
        return jdbc.queryForObject(
                "select id from employee.employee where rule_system_code = ? and employee_type_code = ? and employee_number = ?",
                Long.class, ruleSystemCode, employeeTypeCode, employeeNumber);
    }

    /**
     * Inserts one presence row; endDate may be null for an open-ended presence.
     * Returns the generated surrogate id.
     */
    public long insertPresence(long employeeId, int presenceNumber, LocalDate startDate, LocalDate endDate) {
        jdbc.update(
                "insert into employee.presence" +
                " (employee_id, presence_number, company_code, entry_reason_code, start_date, end_date, created_at, updated_at)" +
                " values (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                employeeId, presenceNumber, "ES01", "HIRE", startDate, endDate);
        return jdbc.queryForObject(
                "select id from employee.presence where employee_id = ? and presence_number = ?",
                Long.class, employeeId, presenceNumber);
    }

    /** Inserts a labor classification row (open-ended). */
    public void insertLaborClassification(long employeeId, LocalDate from) {
        jdbc.update(
                "insert into employee.labor_classification" +
                " (employee_id, agreement_code, agreement_category_code, start_date, end_date, created_at, updated_at)" +
                " values (?, ?, ?, ?, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                employeeId, AGREEMENT_CODE, CATEGORY_CODE, from);
    }

    /**
     * Inserts a working-time row. The working_time_number is auto-assigned as
     * max(existing) + 1 for the employee. {@code to} may be null (open-ended).
     *
     * WARNING: an open-ended (to=null) working-time record extends to the payroll
     * period end and will be clipped by buildSegments() to any presence that overlaps
     * the period. If the employee has multiple presences, set an explicit end date to
     * prevent bleed-through into subsequent presences.
     */
    public void insertWorkingTime(long employeeId, BigDecimal percentage, LocalDate from, LocalDate to) {
        Integer max = jdbc.queryForObject(
                "select coalesce(max(working_time_number), 0) from employee.working_time where employee_id = ?",
                Integer.class, employeeId);
        int nextNum = (max == null ? 0 : max) + 1;
        jdbc.update(
                "insert into employee.working_time" +
                " (employee_id, working_time_number, start_date, end_date, working_time_percentage," +
                "  weekly_hours, daily_hours, monthly_hours, created_at, updated_at)" +
                " values (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                employeeId, nextNum, from, to, percentage,
                new BigDecimal("40.00"), new BigDecimal("8.00"), new BigDecimal("173.33"));
    }

    private Long objectId(String ruleSystemCode, String objectTypeCode, String objectCode) {
        return jdbc.queryForObject(
                "select id from payroll_engine.payroll_object" +
                " where rule_system_code = ? and object_type_code = ? and object_code = ?",
                Long.class, ruleSystemCode, objectTypeCode, objectCode);
    }
}
```

- [ ] **Step 2: Commit**

```
git add src/test/java/com/b4rrhh/payroll/scenario/PayrollScenarioFixtures.java
git commit -m "test(payroll): add PayrollScenarioFixtures shared seed helper"
```

---

## Task 2: Refactor existing E2E test to use `PayrollScenarioFixtures`

**Files:**
- Modify: `src/test/java/com/b4rrhh/payroll/application/usecase/LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest.java`

The goal is to replace the inline `seedMinimalEligibleRealGraph()` method and the inline JDBC inserts in `setUpData()` with calls to the new fixture. The test method itself (`launchPersistsEligibleConceptsWithAggregates`) stays unchanged.

- [ ] **Step 1: Rewrite `setUpData()` and remove `seedMinimalEligibleRealGraph()` and `objectId()`**

Replace the entire class body with the following. The test assertion body (lines 105–228) is unchanged — only the setup and seed methods are replaced:

```java
package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.scenario.PayrollScenarioFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
        "spring.datasource.url=jdbc:h2:mem:payroll_e2e_eligible_real;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "payroll.launch.execution.mode=ELIGIBLE_REAL"
})
@Transactional
class LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest {

    private String employeeNumber;
    private PayrollScenarioFixtures fixtures;

    @Autowired
    private LaunchPayrollCalculationUseCase launchPayrollCalculationUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpData() {
        fixtures = new PayrollScenarioFixtures(jdbcTemplate);
        fixtures.seedConceptGraph("ESP");

        employeeNumber = "ER" + (System.nanoTime() % 1_000_000_000L);
        long employeeId = fixtures.insertEmployee("ESP", "INTERNAL", employeeNumber);
        fixtures.insertPresence(employeeId, 1, LocalDate.of(2025, 1, 1), null);
        fixtures.insertLaborClassification(employeeId, LocalDate.of(2025, 1, 1));
        fixtures.insertWorkingTime(employeeId, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), null);
    }

    @Test
    void launchPersistsEligibleConceptsWithAggregates() {
        var run = launchPayrollCalculationUseCase.launch(new LaunchPayrollCalculationCommand(
                "ESP",
                "202501",
                "NORMAL",
                "ENGINE",
                "1.0",
                new PayrollLaunchTargetSelection(
                        PayrollLaunchTargetSelectionType.SINGLE_EMPLOYEE,
                        new PayrollLaunchEmployeeTarget("INTERNAL", employeeNumber),
                        null
                )
        ));

        assertEquals("COMPLETED", run.status());
        assertEquals(1, run.totalCandidates());
        assertEquals(1, run.totalCalculated());

        Long payrollId = jdbcTemplate.queryForObject(
                "select id from payroll.payroll where rule_system_code = ? and employee_type_code = ? and employee_number = ? and payroll_period_code = ? and payroll_type_code = ? and presence_number = ? and status = ?",
                Long.class,
                "ESP", "INTERNAL", employeeNumber, "202501", "NORMAL", 1, "CALCULATED");
        assertNotNull(payrollId);

        Integer conceptCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ?",
                Integer.class, payrollId);
        assertEquals(7, conceptCount);

        for (String unpersisted : new String[]{"D01","J01","P01","P02","B01","P_SS_CC","P_SS_DESEMPLEO","P_IRPF"}) {
            assertEquals(0, jdbcTemplate.queryForObject(
                    "select count(*) from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                    Integer.class, payrollId, unpersisted),
                    unpersisted + " must not be persisted");
        }

        BigDecimal amount101 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "101");
        BigDecimal quantity101 = jdbcTemplate.queryForObject(
                "select quantity from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "101");
        BigDecimal rate101 = jdbcTemplate.queryForObject(
                "select rate from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "101");
        assertEquals(0, new BigDecimal("1425.00").compareTo(amount101));
        assertEquals(0, new BigDecimal("30").compareTo(quantity101));
        assertEquals(0, new BigDecimal("47.50").compareTo(rate101));

        BigDecimal amount700 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "700");
        assertEquals(0, new BigDecimal("66.98").compareTo(amount700));

        BigDecimal amount703 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "703");
        assertEquals(0, new BigDecimal("22.09").compareTo(amount703));

        BigDecimal amount800 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "800");
        assertEquals(0, new BigDecimal("213.75").compareTo(amount800));

        BigDecimal amount970 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "970");
        BigDecimal amount980 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "980");
        BigDecimal amount990 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "990");
        assertEquals(0, new BigDecimal("1425.00").compareTo(amount970));
        assertEquals(0, new BigDecimal("302.82").compareTo(amount980));
        assertEquals(0, new BigDecimal("1122.18").compareTo(amount990));

        Integer warningCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_warning where payroll_id = ? and warning_code = ?",
                Integer.class, payrollId, "ELIGIBLE_REAL_EXECUTION");
        Integer snapshotCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_context_snapshot where payroll_id = ? and snapshot_type_code = ?",
                Integer.class, payrollId, "EMPLOYEE_PAYROLL_CONTEXT");
        Integer claimsAfterLaunch = jdbcTemplate.queryForObject(
                "select count(*) from payroll.calculation_claim where run_id = ?",
                Integer.class, run.id());
        Integer segmentCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_segment where payroll_id = ?",
                Integer.class, payrollId);
        LocalDate segmentStart = jdbcTemplate.queryForObject(
                "select segment_start from payroll.payroll_segment where payroll_id = ?",
                LocalDate.class, payrollId);
        assertEquals(1, warningCount);
        assertEquals(1, snapshotCount);
        assertEquals(0, claimsAfterLaunch);
        assertEquals(1, segmentCount);
        assertEquals(LocalDate.of(2025, 1, 1), segmentStart);
    }
}
```

- [ ] **Step 2: Run the existing test to confirm it still passes**

```
mvn test -Dtest=LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest -pl . 2>&1 | tail -20
```

Expected output contains: `Tests run: 1, Failures: 0, Errors: 0`

- [ ] **Step 3: Commit**

```
git add src/test/java/com/b4rrhh/payroll/application/usecase/LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest.java
git commit -m "test(payroll): delegate E2E seed to PayrollScenarioFixtures"
```

---

## Task 3: `partialMonthHire` — Create test class and first scenario

**Files:**
- Create: `src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java`

Scenario: Employee hired on Jan 15, presence open-ended.
- Effective days: Jan 15–31 = 17 days, D01 = 17, J01 = 1.00
- SALARIO_BASE = 17 × 47.50 = 807.50
- CC = 807.50 × 4.70% = 37.95 (37.9525 → HALF_UP)
- DESEMPLEO = 807.50 × 1.55% = 12.52 (12.51625 → HALF_UP)
- IRPF = 807.50 × 15.00% = 121.13 (121.125 → HALF_UP)
- DEDUCCIONES = 37.95 + 12.52 + 121.13 = 171.60
- LIQUIDO = 807.50 − 171.60 = 635.90

- [ ] **Step 1: Create the file**

```java
package com.b4rrhh.payroll.scenario;

import com.b4rrhh.payroll.application.usecase.LaunchPayrollCalculationCommand;
import com.b4rrhh.payroll.application.usecase.LaunchPayrollCalculationUseCase;
import com.b4rrhh.payroll.application.usecase.PayrollLaunchEmployeeTarget;
import com.b4rrhh.payroll.application.usecase.PayrollLaunchTargetSelection;
import com.b4rrhh.payroll.application.usecase.PayrollLaunchTargetSelectionType;
import com.b4rrhh.payroll.domain.model.CalculationRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
        "spring.datasource.url=jdbc:h2:mem:payroll_happy_path;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "payroll.launch.execution.mode=ELIGIBLE_REAL"
})
@Transactional
class PayrollHappyPathIntegrationTest {

    @Autowired
    private LaunchPayrollCalculationUseCase launch;

    @Autowired
    private JdbcTemplate jdbc;

    private PayrollScenarioFixtures fixtures;

    @BeforeEach
    void setUp() {
        fixtures = new PayrollScenarioFixtures(jdbc);
        fixtures.seedConceptGraph("ESP");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String uniqueEmployeeNumber() {
        return "HP" + (System.nanoTime() % 1_000_000_000L);
    }

    private CalculationRun launchSingleEmployee(String employeeNumber) {
        return launch.launch(new LaunchPayrollCalculationCommand(
                "ESP", "202501", "NORMAL", "ENGINE", "1.0",
                new PayrollLaunchTargetSelection(
                        PayrollLaunchTargetSelectionType.SINGLE_EMPLOYEE,
                        new PayrollLaunchEmployeeTarget("INTERNAL", employeeNumber),
                        null)));
    }

    private Long payrollId(String employeeNumber, int presenceNumber) {
        return jdbc.queryForObject(
                "select id from payroll.payroll" +
                " where rule_system_code = ? and employee_type_code = ? and employee_number = ?" +
                "   and payroll_period_code = ? and payroll_type_code = ? and presence_number = ? and status = ?",
                Long.class, "ESP", "INTERNAL", employeeNumber, "202501", "NORMAL", presenceNumber, "CALCULATED");
    }

    private BigDecimal amount(Long payrollId, String conceptCode) {
        return jdbc.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, conceptCode);
    }

    private void assertAmount(Long payrollId, String conceptCode, String expected) {
        assertEquals(0, new BigDecimal(expected).compareTo(amount(payrollId, conceptCode)),
                conceptCode + " expected " + expected);
    }

    // ── Scenario 1: partial-month hire ───────────────────────────────────────

    @Test
    void partialMonthHire() {
        // Jan 15 → open; 17 days; SALARIO_BASE = 17 × 47.50 = 807.50
        String emp = uniqueEmployeeNumber();
        long empId = fixtures.insertEmployee("ESP", "INTERNAL", emp);
        fixtures.insertPresence(empId, 1, LocalDate.of(2025, 1, 15), null);
        fixtures.insertLaborClassification(empId, LocalDate.of(2025, 1, 15));
        fixtures.insertWorkingTime(empId, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 15), null);

        var run = launchSingleEmployee(emp);

        assertEquals("COMPLETED", run.status());
        assertEquals(1, run.totalCalculated());

        Long pid = payrollId(emp, 1);
        assertNotNull(pid);

        assertEquals(7, jdbc.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ?",
                Integer.class, pid));
        assertEquals(1, jdbc.queryForObject(
                "select count(*) from payroll.payroll_segment where payroll_id = ?",
                Integer.class, pid));

        assertAmount(pid, "101", "807.50");
        assertAmount(pid, "700", "37.95");
        assertAmount(pid, "703", "12.52");
        assertAmount(pid, "800", "121.13");
        assertAmount(pid, "970", "807.50");
        assertAmount(pid, "980", "171.60");
        assertAmount(pid, "990", "635.90");
    }
}
```

- [ ] **Step 2: Run the test**

```
mvn test -Dtest=PayrollHappyPathIntegrationTest#partialMonthHire -pl . 2>&1 | tail -20
```

Expected: `Tests run: 1, Failures: 0, Errors: 0`

If amounts differ, **re-derive from the engine output**, not from the assertion — the spec amounts are the source of truth.

- [ ] **Step 3: Commit**

```
git add src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java
git commit -m "test(payroll): add partialMonthHire happy path scenario"
```

---

## Task 4: `midMonthTermination`

**Files:**
- Modify: `src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java`

Scenario: Employee hired Jan 1, terminated Jan 20.
- Effective days: Jan 1–20 = 20 days, D01 = 20, J01 = 1.00
- SALARIO_BASE = 20 × 47.50 = 950.00
- CC = 950.00 × 4.70% = 44.65
- DESEMPLEO = 950.00 × 1.55% = 14.73 (14.725 → HALF_UP)
- IRPF = 950.00 × 15.00% = 142.50
- DEDUCCIONES = 44.65 + 14.73 + 142.50 = 201.88
- LIQUIDO = 950.00 − 201.88 = 748.12

Note: `end_date = '2025-01-20'` on the presence is what bounds the effective days. The working time is open-ended (null) — that is fine here because there is only one presence.

- [ ] **Step 1: Add `midMonthTermination` test method to the class**

Add after the `partialMonthHire` method:

```java
    // ── Scenario 2: mid-month termination ────────────────────────────────────

    @Test
    void midMonthTermination() {
        // Jan 1 → Jan 20; 20 days; SALARIO_BASE = 20 × 47.50 = 950.00
        String emp = uniqueEmployeeNumber();
        long empId = fixtures.insertEmployee("ESP", "INTERNAL", emp);
        fixtures.insertPresence(empId, 1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 20));
        fixtures.insertLaborClassification(empId, LocalDate.of(2025, 1, 1));
        fixtures.insertWorkingTime(empId, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), null);

        var run = launchSingleEmployee(emp);

        assertEquals("COMPLETED", run.status());
        assertEquals(1, run.totalCalculated());

        Long pid = payrollId(emp, 1);
        assertNotNull(pid);

        assertEquals(7, jdbc.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ?",
                Integer.class, pid));
        assertEquals(1, jdbc.queryForObject(
                "select count(*) from payroll.payroll_segment where payroll_id = ?",
                Integer.class, pid));

        assertAmount(pid, "101", "950.00");
        assertAmount(pid, "700", "44.65");
        assertAmount(pid, "703", "14.73");
        assertAmount(pid, "800", "142.50");
        assertAmount(pid, "970", "950.00");
        assertAmount(pid, "980", "201.88");
        assertAmount(pid, "990", "748.12");
    }
```

- [ ] **Step 2: Run the test**

```
mvn test -Dtest=PayrollHappyPathIntegrationTest#midMonthTermination -pl . 2>&1 | tail -20
```

Expected: `Tests run: 1, Failures: 0, Errors: 0`

- [ ] **Step 3: Commit**

```
git add src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java
git commit -m "test(payroll): add midMonthTermination happy path scenario"
```

---

## Task 5: `twoPresencesSamePeriod`

**Files:**
- Modify: `src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java`

Scenario: Same employee, two clean presences in the same period.
- Presence 1: Jan 1–15 (15 days), Presence 2: Jan 16–30 (15 days)
- Single working-time record (Jan 1–null, 100%) — the engine clips it per presence
- Each payroll: D01=15, SALARIO_BASE = 15 × 47.50 = 712.50
- CC = 712.50 × 4.70% = 33.49 (33.4875 → HALF_UP)
- DESEMPLEO = 712.50 × 1.55% = 11.04 (11.04375 → HALF_UP)
- IRPF = 712.50 × 15.00% = 106.88 (106.875 → HALF_UP)
- DEDUCCIONES = 33.49 + 11.04 + 106.88 = 151.41
- LIQUIDO = 712.50 − 151.41 = 561.09
- The launch targets the single employee and must produce exactly 2 payrolls.

- [ ] **Step 1: Add `twoPresencesSamePeriod` test method**

```java
    // ── Scenario 3: two clean presences in the same period ───────────────────

    @Test
    void twoPresencesSamePeriod() {
        // Presence 1: Jan 1–15 (15 days); Presence 2: Jan 16–30 (15 days)
        // Both produce the same amounts: SALARIO_BASE = 712.50
        String emp = uniqueEmployeeNumber();
        long empId = fixtures.insertEmployee("ESP", "INTERNAL", emp);
        fixtures.insertPresence(empId, 1, LocalDate.of(2025, 1, 1),  LocalDate.of(2025, 1, 15));
        fixtures.insertPresence(empId, 2, LocalDate.of(2025, 1, 16), LocalDate.of(2025, 1, 30));
        fixtures.insertLaborClassification(empId, LocalDate.of(2025, 1, 1));
        // Single WT (open-ended) — engine clips it to [presenceStart, presenceEnd] per presence
        fixtures.insertWorkingTime(empId, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), null);

        var run = launchSingleEmployee(emp);

        assertEquals("COMPLETED", run.status());
        assertEquals(2, run.totalCalculated());

        for (int presenceNum = 1; presenceNum <= 2; presenceNum++) {
            Long pid = payrollId(emp, presenceNum);
            assertNotNull(pid, "payroll for presence " + presenceNum + " must exist");

            assertEquals(7, jdbc.queryForObject(
                    "select count(*) from payroll.payroll_concept where payroll_id = ?",
                    Integer.class, pid), "concept count for presence " + presenceNum);
            assertEquals(1, jdbc.queryForObject(
                    "select count(*) from payroll.payroll_segment where payroll_id = ?",
                    Integer.class, pid), "segment count for presence " + presenceNum);

            assertAmount(pid, "101", "712.50");
            assertAmount(pid, "700",  "33.49");
            assertAmount(pid, "703",  "11.04");
            assertAmount(pid, "800", "106.88");
            assertAmount(pid, "970", "712.50");
            assertAmount(pid, "980", "151.41");
            assertAmount(pid, "990", "561.09");
        }
    }
```

- [ ] **Step 2: Run the test**

```
mvn test -Dtest=PayrollHappyPathIntegrationTest#twoPresencesSamePeriod -pl . 2>&1 | tail -20
```

Expected: `Tests run: 1, Failures: 0, Errors: 0`

- [ ] **Step 3: Commit**

```
git add src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java
git commit -m "test(payroll): add twoPresencesSamePeriod happy path scenario"
```

---

## Task 6: `twoPresencesWithSegments`

**Files:**
- Modify: `src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java`

Most complex scenario: 2 presences × 2 working-time segments each.

**Presence 1 (Jan 1–15):**
- Segment 1a: Jan 1–9 (9 days, 100%) → D01=9, SALARIO=9×47.50×1.00=427.50
- Segment 1b: Jan 10–15 (6 days, 50%) → D01=6, SALARIO=6×47.50×0.50=142.50
- SALARIO_BASE = 427.50 + 142.50 = 570.00
- CC = 570.00 × 4.70% = 26.79
- DESEMPLEO = 570.00 × 1.55% = 8.84 (8.835 → HALF_UP)
- IRPF = 570.00 × 15.00% = 85.50
- DEDUCCIONES = 26.79 + 8.84 + 85.50 = 121.13
- LIQUIDO = 570.00 − 121.13 = 448.87

**Presence 2 (Jan 16–30):**
- Segment 2a: Jan 16–25 (10 days, 100%) → D01=10, SALARIO=10×47.50×1.00=475.00
- Segment 2b: Jan 26–30 (5 days, 50%) → D01=5, SALARIO=5×47.50×0.50=118.75
- SALARIO_BASE = 475.00 + 118.75 = 593.75
- CC = 593.75 × 4.70% = 27.91 (27.90625 → HALF_UP)
- DESEMPLEO = 593.75 × 1.55% = 9.20 (9.203125 → HALF_UP)
- IRPF = 593.75 × 15.00% = 89.06 (89.0625 → HALF_UP)
- DEDUCCIONES = 27.91 + 9.20 + 89.06 = 126.17
- LIQUIDO = 593.75 − 126.17 = 467.58

**Working-time design — critical:**
All 4 WT records belong to the same employee. The engine fetches them all and clips per presence. To prevent WT2 (Jan 10–50%) from bleeding into Presence 2's calculation, WT2 must have `end_date = '2025-01-15'` (not null).

| # | start      | end        | % |
|---|------------|------------|---|
| 1 | 2025-01-01 | 2025-01-09 | 100 |
| 2 | 2025-01-10 | **2025-01-15** | 50 |
| 3 | 2025-01-16 | 2025-01-25 | 100 |
| 4 | 2025-01-26 | null       | 50 |

- [ ] **Step 1: Add `twoPresencesWithSegments` test method**

```java
    // ── Scenario 4: two presences, each with a working-time change (segments) ─

    @Test
    void twoPresencesWithSegments() {
        String emp = uniqueEmployeeNumber();
        long empId = fixtures.insertEmployee("ESP", "INTERNAL", emp);

        // Presence 1: Jan 1–15
        fixtures.insertPresence(empId, 1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15));
        // Presence 2: Jan 16–30
        fixtures.insertPresence(empId, 2, LocalDate.of(2025, 1, 16), LocalDate.of(2025, 1, 30));

        fixtures.insertLaborClassification(empId, LocalDate.of(2025, 1, 1));

        // WT1: Jan 1–9 @ 100% (presence 1, seg 1a)
        fixtures.insertWorkingTime(empId, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 9));
        // WT2: Jan 10–15 @ 50% — explicit end_date prevents bleed into presence 2
        fixtures.insertWorkingTime(empId, new BigDecimal("50.00"),
                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 15));
        // WT3: Jan 16–25 @ 100% (presence 2, seg 2a)
        fixtures.insertWorkingTime(empId, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 16), LocalDate.of(2025, 1, 25));
        // WT4: Jan 26–null @ 50% (presence 2, seg 2b)
        fixtures.insertWorkingTime(empId, new BigDecimal("50.00"),
                LocalDate.of(2025, 1, 26), null);

        var run = launchSingleEmployee(emp);

        assertEquals("COMPLETED", run.status());
        assertEquals(2, run.totalCalculated());

        // ── Presence 1 ────────────────────────────────────────────────────────
        Long pid1 = payrollId(emp, 1);
        assertNotNull(pid1, "payroll for presence 1 must exist");

        assertEquals(7, jdbc.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ?",
                Integer.class, pid1), "presence 1 concept count");
        assertEquals(2, jdbc.queryForObject(
                "select count(*) from payroll.payroll_segment where payroll_id = ?",
                Integer.class, pid1), "presence 1 segment count");

        // SALARIO_BASE = (9×47.50×1.00) + (6×47.50×0.50) = 427.50 + 142.50 = 570.00
        assertAmount(pid1, "101", "570.00");
        assertAmount(pid1, "700",  "26.79");
        assertAmount(pid1, "703",   "8.84");
        assertAmount(pid1, "800",  "85.50");
        assertAmount(pid1, "970", "570.00");
        assertAmount(pid1, "980", "121.13");
        assertAmount(pid1, "990", "448.87");

        // ── Presence 2 ────────────────────────────────────────────────────────
        Long pid2 = payrollId(emp, 2);
        assertNotNull(pid2, "payroll for presence 2 must exist");

        assertEquals(7, jdbc.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ?",
                Integer.class, pid2), "presence 2 concept count");
        assertEquals(2, jdbc.queryForObject(
                "select count(*) from payroll.payroll_segment where payroll_id = ?",
                Integer.class, pid2), "presence 2 segment count");

        // SALARIO_BASE = (10×47.50×1.00) + (5×47.50×0.50) = 475.00 + 118.75 = 593.75
        assertAmount(pid2, "101", "593.75");
        assertAmount(pid2, "700",  "27.91");
        assertAmount(pid2, "703",   "9.20");
        assertAmount(pid2, "800",  "89.06");
        assertAmount(pid2, "970", "593.75");
        assertAmount(pid2, "980", "126.17");
        assertAmount(pid2, "990", "467.58");
    }
```

- [ ] **Step 2: Run all 4 scenario tests**

```
mvn test -Dtest=PayrollHappyPathIntegrationTest -pl . 2>&1 | tail -20
```

Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 3: Run the full test suite to confirm no regressions**

```
mvn test -pl . 2>&1 | tail -30
```

Expected: BUILD SUCCESS, 0 failures.

- [ ] **Step 4: Commit**

```
git add src/test/java/com/b4rrhh/payroll/scenario/PayrollHappyPathIntegrationTest.java
git commit -m "test(payroll): add twoPresencesWithSegments happy path scenario"
```

---

## Self-Review Checklist

**Spec coverage:**
- ✅ Scenario 1 (partial hire): Task 3
- ✅ Scenario 2 (mid-month termination): Task 4
- ✅ Scenario 3 (two presences, clean): Task 5
- ✅ Scenario 4 (two presences + working-time change per presence): Task 6
- ✅ Refactor existing E2E test to use fixtures: Task 2
- ✅ Fixtures class with parameterized seedConceptGraph: Task 1
- ✅ D01 = min(daysInSegment, 30) satisfied for all scenarios (max 20 days per segment)
- ✅ PERCENTAGE concepts applied to PERIOD total of B01 (not per-segment)
- ✅ Rounding: HALF_UP verified for every non-round amount

**Gotcha documented:**
- WT2 in scenario 4 must have `end_date = '2025-01-15'` — noted in domain reminders and in the WT table in Task 6.

**Type consistency:**
- `launchSingleEmployee()` returns the use case's return type via `var` — no type dependency on a specific record name.
- `payrollId()` helper matches the exact query pattern from the existing E2E test.
- All `BigDecimal` comparisons use `compareTo == 0` (no scale sensitivity).

**Placeholder scan:** No TBD / TODO / "similar to" references — all code is complete.
