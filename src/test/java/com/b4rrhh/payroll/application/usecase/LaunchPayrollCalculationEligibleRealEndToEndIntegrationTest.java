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

        seedMinimalEligibleRealGraph();
    }

    @Test
    void launchPersistsOnlyConcept101ForEligibleRealMinimalExecution() {
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
        assertEquals(1, conceptCount);

        Integer concept101Count = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                Integer.class,
                payrollId,
                "101"
        );
        Integer conceptD01Count = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                Integer.class,
                payrollId,
                "D01"
        );
        Integer conceptP01Count = jdbcTemplate.queryForObject(
                "select count(*) from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                Integer.class,
                payrollId,
                "P01"
        );
        assertEquals(1, concept101Count);
        assertEquals(0, conceptD01Count);
        assertEquals(0, conceptP01Count);

        BigDecimal amount = jdbcTemplate.queryForObject(
                "select amount from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class,
                payrollId,
                "101"
        );
        BigDecimal quantity = jdbcTemplate.queryForObject(
                "select quantity from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class,
                payrollId,
                "101"
        );
        BigDecimal rate = jdbcTemplate.queryForObject(
                "select rate from payroll.payroll_concept where payroll_id = ? and concept_code = ?",
                BigDecimal.class,
                payrollId,
                "101"
        );
        assertEquals(0, new BigDecimal("1425.00").compareTo(amount));
        assertEquals(0, new BigDecimal("30").compareTo(quantity));
        assertEquals(0, new BigDecimal("47.50").compareTo(rate));

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
        assertEquals(1, warningCount);
        assertEquals(1, snapshotCount);
        assertEquals(0, claimsAfterLaunch);
    }

    private void seedMinimalEligibleRealGraph() {
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "ESP",
                "CONCEPT",
                "101"
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "ESP",
                "CONCEPT",
                "D01"
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "ESP",
                "CONCEPT",
                "P01"
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "ESP",
                "CONSTANT",
                "D01_FIXED_30"
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "ESP",
                "TABLE",
                "P01_DAILY_AMOUNT_TABLE"
        );

        Long concept101ObjectId = objectId("CONCEPT", "101");
        Long conceptD01ObjectId = objectId("CONCEPT", "D01");
        Long conceptP01ObjectId = objectId("CONCEPT", "P01");
        Long d01ConstantObjectId = objectId("CONSTANT", "D01_FIXED_30");
        Long p01TableRoleObjectId = objectId("TABLE", "P01_DAILY_AMOUNT_TABLE");

        jdbcTemplate.update(
                "insert into payroll_engine.payroll_concept (object_id, concept_mnemonic, calculation_type, functional_nature, result_composition_mode, payslip_order_code, execution_scope, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                concept101ObjectId,
                "SALARIO_BASE",
                "RATE_BY_QUANTITY",
                "EARNING",
                "REPLACE",
                "101",
                "PERIOD"
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_concept (object_id, concept_mnemonic, calculation_type, functional_nature, result_composition_mode, payslip_order_code, execution_scope, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                conceptD01ObjectId,
                "DIAS_MES",
                "DIRECT_AMOUNT",
                "INFORMATIONAL",
                "REPLACE",
                null,
                "PERIOD"
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_concept (object_id, concept_mnemonic, calculation_type, functional_nature, result_composition_mode, payslip_order_code, execution_scope, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                conceptP01ObjectId,
                "PRECIO_DIA",
                "DIRECT_AMOUNT",
                "BASE",
                "REPLACE",
                null,
                "PERIOD"
        );

        jdbcTemplate.update(
                "insert into payroll_engine.payroll_concept_operand (target_object_id, operand_role, source_object_id, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                concept101ObjectId,
                "QUANTITY",
                conceptD01ObjectId
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_concept_operand (target_object_id, operand_role, source_object_id, created_at, updated_at) values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                concept101ObjectId,
                "RATE",
                conceptP01ObjectId
        );

        jdbcTemplate.update(
                "insert into payroll_engine.payroll_concept_feed_relation (source_object_id, target_object_id, feed_mode, feed_value, effective_from, effective_to, created_at, updated_at) values (?, ?, ?, ?, DATE '2025-01-01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                d01ConstantObjectId,
                conceptD01ObjectId,
                "FEED_BY_SOURCE",
                new BigDecimal("30")
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_concept_feed_relation (source_object_id, target_object_id, feed_mode, feed_value, effective_from, effective_to, created_at, updated_at) values (?, ?, ?, ?, DATE '2025-01-01', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                p01TableRoleObjectId,
                conceptP01ObjectId,
                "FEED_BY_SOURCE",
                null
        );

        jdbcTemplate.update(
                "insert into payroll.payroll_object_activation (rule_system_code, owner_type_code, owner_code, target_object_type_code, target_object_code, active) values (?, ?, ?, ?, ?, ?)",
                "ESP",
                "AGREEMENT",
                "99002405011982",
                "PAYROLL_CONCEPT",
                "101",
                true
        );
        jdbcTemplate.update(
                "insert into payroll.payroll_object_binding (rule_system_code, owner_type_code, owner_code, binding_role_code, bound_object_type_code, bound_object_code, active) values (?, ?, ?, ?, ?, ?, ?)",
                "ESP",
                "AGREEMENT",
                "99002405011982",
                "P01_DAILY_AMOUNT_TABLE",
                "TABLE",
                "P01_99002405011982",
                true
        );
        jdbcTemplate.update(
                "insert into payroll.payroll_table_row (rule_system_code, table_code, search_code, start_date, end_date, daily_value, active) values (?, ?, ?, DATE '2025-01-01', null, ?, ?)",
                "ESP",
                "P01_99002405011982",
                "99002405-G2",
                new BigDecimal("47.50"),
                true
        );
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
