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
        assertAmount(pid, "700",  "37.95");
        assertAmount(pid, "703",  "12.52");
        assertAmount(pid, "800", "121.13");
        assertAmount(pid, "970", "807.50");
        assertAmount(pid, "980", "171.60");
        assertAmount(pid, "990", "635.90");
    }
}
