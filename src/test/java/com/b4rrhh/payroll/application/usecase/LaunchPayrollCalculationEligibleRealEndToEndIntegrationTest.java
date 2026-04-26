package com.b4rrhh.payroll.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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

    @Autowired
    private LaunchPayrollCalculationUseCase launchPayrollCalculationUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpData() {
        employeeNumber = "ER" + (System.nanoTime() % 1_000_000_000L);

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
                "Eligible",
                "Real",
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
        jdbcTemplate.update(
                "insert into employee.labor_classification (employee_id, agreement_code, agreement_category_code, start_date, end_date, created_at, updated_at) values (?, ?, ?, DATE '2025-01-01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                employeeId,
                "99002405011982",
                "99002405-G2"
        );
        jdbcTemplate.update(
                "insert into employee.working_time (employee_id, working_time_number, start_date, end_date, working_time_percentage, weekly_hours, daily_hours, monthly_hours, created_at, updated_at) values (?, ?, DATE '2025-01-01', null, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                employeeId, 1,
                new BigDecimal("100.00"),
                new BigDecimal("40.00"),
                new BigDecimal("8.00"),
                new BigDecimal("173.33")
        );

        seedMinimalEligibleRealGraph();
    }

    @Test
    void launchPersistsEligibleConceptsWithAggregates() {
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

        Long payrollId = jdbcTemplate.queryForObject(
                "select id from payroll.payroll where rule_system_code = ? and employee_type_code = ? and employee_number = ? and payroll_period_code = ? and payroll_type_code = ? and presence_number = ? and status = ?",
                Long.class,
                "ESP",
                "INTERNAL",
                employeeNumber,
                "202501",
                "ORD",
                1,
                "CALCULATED"
        );
        assertNotNull(payrollId);

        Integer conceptCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ?",
                Integer.class,
                payrollId
        );
        assertEquals(3, conceptCount);

        // technical/base concepts must NOT be persisted (payslip_order_code is null)
        for (String unpersisted : new String[]{"D01", "J01", "P01", "P02"}) {
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

        BigDecimal amount970 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "970");
        BigDecimal amount990 = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class, payrollId, "990");
        assertEquals(0, new BigDecimal("1425.00").compareTo(amount970));
        assertEquals(0, new BigDecimal("1425.00").compareTo(amount990));

        Integer warningCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_warning where payroll_id = ? and warning_code = ?",
                Integer.class,
                payrollId,
                "ELIGIBLE_REAL_EXECUTION"
        );
        Integer snapshotCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_context_snapshot where payroll_id = ? and snapshot_type_code = ?",
                Integer.class,
                payrollId,
                "EMPLOYEE_PAYROLL_CONTEXT"
        );
        Integer claimsAfterLaunch = jdbcTemplate.queryForObject(
                "select count(*) from payroll.calculation_claim where run_id = ?",
                Integer.class,
                run.id()
        );
        Integer segmentCount = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_segment where payroll_id = ?",
                Integer.class,
                payrollId
        );
        java.time.LocalDate segmentStart = jdbcTemplate.queryForObject(
                "select segment_start from payroll.payroll_segment where payroll_id = ?",
                java.time.LocalDate.class,
                payrollId
        );
        assertEquals(1, warningCount);
        assertEquals(1, snapshotCount);
        assertEquals(0, claimsAfterLaunch);
        assertEquals(1, segmentCount);
        assertEquals(java.time.LocalDate.of(2025, 1, 1), segmentStart);
    }

    private void seedMinimalEligibleRealGraph() {
        // ── Payroll objects ──────────────────────────────────────────
        for (String code : new String[]{"101", "D01", "J01", "P01", "P02", "970", "990"}) {
            jdbcTemplate.update(
                    "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    "ESP", "CONCEPT", code);
        }
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "ESP", "TABLE", "P02_DAILY_AMOUNT_TABLE");

        Long id101   = objectId("CONCEPT", "101");
        Long idD01   = objectId("CONCEPT", "D01");
        Long idJ01   = objectId("CONCEPT", "J01");
        Long idP01   = objectId("CONCEPT", "P01");
        Long idP02   = objectId("CONCEPT", "P02");
        Long id970   = objectId("CONCEPT", "970");
        Long id990   = objectId("CONCEPT", "990");
        Long idP02Table = objectId("TABLE", "P02_DAILY_AMOUNT_TABLE");

        // ── Concepts ─────────────────────────────────────────────────
        String conceptSql = "insert into payroll_engine.payroll_concept (object_id, concept_mnemonic, calculation_type, functional_nature, result_composition_mode, payslip_order_code, execution_scope, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(conceptSql, id101, "SALARIO_BASE",       "RATE_BY_QUANTITY", "EARNING",       "REPLACE", "101", "PERIOD");
        jdbcTemplate.update(conceptSql, idD01, "DIAS_DEVENGO",       "JAVA_PROVIDED",    "TECHNICAL",     "REPLACE", null,  "PERIOD");
        jdbcTemplate.update(conceptSql, idJ01, "COEFICIENTE_JORNADA","JAVA_PROVIDED",    "TECHNICAL",     "REPLACE", null,  "PERIOD");
        jdbcTemplate.update(conceptSql, idP01, "PRECIO_DIA",         "RATE_BY_QUANTITY", "BASE",          "REPLACE", null,  "PERIOD");
        jdbcTemplate.update(conceptSql, idP02, "PRECIO_DIA_PLENO",   "DIRECT_AMOUNT",    "BASE",          "REPLACE", null,  "PERIOD");
        jdbcTemplate.update(conceptSql, id970, "TOTAL_DEVENGOS",     "AGGREGATE",        "TOTAL_EARNING", "REPLACE", "970", "PERIOD");
        jdbcTemplate.update(conceptSql, id990, "LIQUIDO_A_PAGAR",    "AGGREGATE",        "NET_PAY",       "REPLACE", "990", "PERIOD");

        // ── Operands ─────────────────────────────────────────────────
        //   101  = D01 (QUANTITY) × P01 (RATE)
        //   P01  = J01 (QUANTITY) × P02 (RATE)
        String operandSql = "insert into payroll_engine.payroll_concept_operand (target_object_id, operand_role, source_object_id, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(operandSql, id101, "QUANTITY", idD01);
        jdbcTemplate.update(operandSql, id101, "RATE",     idP01);
        jdbcTemplate.update(operandSql, idP01, "QUANTITY", idJ01);
        jdbcTemplate.update(operandSql, idP01, "RATE",     idP02);

        // ── Feed relations ────────────────────────────────────────────
        //   P02_DAILY_AMOUNT_TABLE → P02   (table lookup feeds full daily rate)
        //   101 → 970, 101 → 990           (earnings feed aggregates)
        String feedSql = "insert into payroll_engine.payroll_concept_feed_relation (source_object_id, target_object_id, feed_mode, feed_value, invert_sign, effective_from, effective_to, created_at, updated_at) values (?, ?, ?, ?, false, DATE '2025-01-01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(feedSql, idP02Table, idP02, "FEED_BY_SOURCE", null);
        jdbcTemplate.update(feedSql, id101, id970, "FEED_BY_SOURCE", null);
        jdbcTemplate.update(feedSql, id101, id990, "FEED_BY_SOURCE", null);

        // ── Concept assignments (primary concepts only) ───────────────
        String assignSql = "insert into payroll_engine.concept_assignment (rule_system_code, concept_code, company_code, agreement_code, employee_type_code, valid_from, valid_to, priority, created_at, updated_at) values (?, ?, ?, ?, ?, DATE '2025-01-01', null, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(assignSql, "ESP", "101", null, "99002405011982", null, 10);
        jdbcTemplate.update(assignSql, "ESP", "970", null, "99002405011982", null, 970);
        jdbcTemplate.update(assignSql, "ESP", "990", null, "99002405011982", null, 990);

        // ── Activation + binding + table data ────────────────────────
        jdbcTemplate.update(
                "insert into payroll.payroll_object_activation (rule_system_code, owner_type_code, owner_code, target_object_type_code, target_object_code, active) values (?, ?, ?, ?, ?, ?)",
                "ESP", "AGREEMENT", "99002405011982", "PAYROLL_CONCEPT", "101", true);
        jdbcTemplate.update(
                "insert into payroll.payroll_object_binding (rule_system_code, owner_type_code, owner_code, binding_role_code, bound_object_type_code, bound_object_code, active) values (?, ?, ?, ?, ?, ?, ?)",
                "ESP", "AGREEMENT", "99002405011982", "P02_DAILY_AMOUNT_TABLE", "TABLE", "P02_99002405011982", true);
        jdbcTemplate.update(
                "insert into payroll.payroll_table_row (rule_system_code, table_code, search_code, start_date, end_date, daily_value, active) values (?, ?, ?, DATE '2025-01-01', null, ?, ?)",
                "ESP", "P02_99002405011982", "99002405-G2", new BigDecimal("47.50"), true);
    }

        private Long objectId(String objectTypeCode, String objectCode) {
        return jdbcTemplate.queryForObject(
                                "select id from payroll_engine.payroll_object where rule_system_code = ? and object_type_code = ? and object_code = ?",
                Long.class,
                "ESP",
                                objectTypeCode,
                objectCode
        );
    }
}
