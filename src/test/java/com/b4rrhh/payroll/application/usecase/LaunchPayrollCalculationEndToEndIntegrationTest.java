package com.b4rrhh.payroll.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
        "spring.datasource.url=jdbc:h2:mem:payroll_e2e;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@Transactional
class LaunchPayrollCalculationEndToEndIntegrationTest {

        private String employeeNumber;

    @Autowired
    private LaunchPayrollCalculationUseCase launchPayrollCalculationUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpData() {
                employeeNumber = "E2E" + (System.nanoTime() % 1_000_000_000L);

        jdbcTemplate.update(
                "insert into rulesystem.rule_system (code, name, country_code, active, created_at, updated_at) values (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "ESP",
                "Spain",
                "ESP",
                true
        );
        jdbcTemplate.update(
                "insert into employee.employee (rule_system_code, employee_type_code, employee_number, first_name, last_name_1, status, created_at, updated_at) values (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "ESP",
                "INTERNAL",
                employeeNumber,
                "Name",
                "Surname",
                "ACTIVE"
        );
        Long employeeId = jdbcTemplate.queryForObject(
                "select id from employee.employee where rule_system_code = ? and employee_type_code = ? and employee_number = ?",
                Long.class,
                "ESP",
                "INTERNAL",
                employeeNumber
        );
        assertNotNull(employeeId);

        jdbcTemplate.update(
                "insert into employee.presence (employee_id, presence_number, company_code, entry_reason_code, start_date, created_at, updated_at) values (?, ?, ?, ?, DATE '2025-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                employeeId,
                1,
                "ES01",
                "HIRE"
        );
    }

    @Test
    void launchPersistsPayrollWithDeterministicFakePayloadAndCleansClaims() {
        var run = launchPayrollCalculationUseCase.launch(new LaunchPayrollCalculationCommand(
                "ESP",
                "202501",
                "ORD",
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

        Integer payrollCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll where rule_system_code = ? and employee_type_code = ? and employee_number = ? and payroll_period_code = ? and payroll_type_code = ? and presence_number = ? and status = ?",
                Integer.class,
                "ESP",
                "INTERNAL",
                employeeNumber,
                "202501",
                "ORD",
                1,
                "CALCULATED"
        );
        assertEquals(1, payrollCount);

        Long payrollId = jdbcTemplate.queryForObject(
                "select id from payroll.payroll where rule_system_code = ? and employee_type_code = ? and employee_number = ? and payroll_period_code = ? and payroll_type_code = ? and presence_number = ?",
                Long.class,
                "ESP",
                "INTERNAL",
                employeeNumber,
                "202501",
                "ORD",
                1
        );
        assertNotNull(payrollId);

        Integer conceptCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ?",
                Integer.class,
                payrollId
        );
        assertEquals(10, conceptCount);

        Integer snapshotCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_context_snapshot where payroll_id = ?",
                Integer.class,
                payrollId
        );
        assertEquals(1, snapshotCount);

        Integer baseFakeCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                Integer.class,
                payrollId,
                "BASE_FAKE"
        );
        Integer netFakeCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                Integer.class,
                payrollId,
                "NET_FAKE"
        );
        Integer grossFakeCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                Integer.class,
                payrollId,
                "GROSS_FAKE"
        );
        Integer taxFakeCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                Integer.class,
                payrollId,
                "TAX_FAKE"
        );
        assertEquals(1, baseFakeCount);
        assertEquals(1, netFakeCount);
        assertEquals(1, grossFakeCount);
        assertEquals(1, taxFakeCount);

        Integer warningCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_warning where payroll_id = ?",
                Integer.class,
                payrollId
        );
        assertEquals(1, warningCount);

        Integer deterministicWarningCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_warning where payroll_id = ? and warning_code = ? and severity_code = ? and message = ?",
                Integer.class,
                payrollId,
                "DETERMINISTIC_FAKE_PAYROLL",
                "INFO",
                "Payroll generated by deterministic fake calculator"
        );
        assertEquals(1, deterministicWarningCount);

        Integer launchSnapshotCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_context_snapshot where payroll_id = ? and snapshot_type_code = ? and source_vertical_code = ?",
                Integer.class,
                payrollId,
                "EMPLOYEE_PAYROLL_CONTEXT",
                "PAYROLL_LAUNCH"
        );
        assertEquals(1, launchSnapshotCount);

        Integer claimsAfterLaunch = jdbcTemplate.queryForObject(
                "select count(*) from payroll.calculation_claim where run_id = ?",
                Integer.class,
                run.id()
        );
        assertEquals(0, claimsAfterLaunch);
    }
}