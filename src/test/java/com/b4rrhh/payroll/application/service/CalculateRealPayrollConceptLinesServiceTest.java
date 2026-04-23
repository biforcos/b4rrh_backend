package com.b4rrhh.payroll.application.service;

import com.b4rrhh.payroll.agreementplus.application.service.CalculateAgreementPlusService;
import com.b4rrhh.payroll.application.usecase.CalculatePayrollUnitCommand;
import com.b4rrhh.payroll.basesalary.application.service.CalculateBaseSalaryService;
import com.b4rrhh.payroll.domain.model.PayrollConcept;
import com.b4rrhh.payroll.domain.model.PayrollConceptNotApplicableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalculateRealPayrollConceptLinesService.
 *
 * Tests verify the semantic distinction between:
 * 1. Inactive concept (PayrollConceptNotApplicableException) -> SKIP silently
 * 2. Active concept with missing/broken config (IllegalStateException) -> FAIL the orchestration
 *
 * Also verifies that empty result is valid ONLY when both concepts are non-applicable,
 * not when configuration errors were silently swallowed.
 */
class CalculateRealPayrollConceptLinesServiceTest {

    private static final String RULE_SYSTEM = "ESP";
    private static final String EMPLOYEE_TYPE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2026, 1, 15);
    private static final String PAYROLL_PERIOD_CODE = "2026-01";
    
    private static final BigDecimal SALARY = new BigDecimal("1500.00");
    private static final BigDecimal PLUS = new BigDecimal("250.00");

    private CalculateRealPayrollConceptLinesService service;

    private FakeCalculateBaseSalaryService baseSalaryService;
    private FakeCalculateAgreementPlusService agreementPlusService;

    @BeforeEach
    void setUp() {
        baseSalaryService = new FakeCalculateBaseSalaryService();
        agreementPlusService = new FakeCalculateAgreementPlusService();
        service = new CalculateRealPayrollConceptLinesService(baseSalaryService, agreementPlusService);
    }

    @Test
    void calculateConceptLinesWhenBothConceptsActive() throws Exception {
        baseSalaryService.setSalary(SALARY);
        agreementPlusService.setPlus(PLUS);

        List<PayrollConcept> result = service.calculateConceptLines(buildCommand());

        assertEquals(2, result.size(), "Should contain 2 concepts when both active");
        assertEquals("BASE_SALARY", result.get(0).getConceptCode());
        assertEquals("PLUS_CONVENIO", result.get(1).getConceptCode());
        assertEquals(0, SALARY.compareTo(result.get(0).getAmount()));
        assertEquals(0, PLUS.compareTo(result.get(1).getAmount()));
    }

    @Test
    void calculateConceptLinesWhenOnlyBaseSalaryActive() throws Exception {
        baseSalaryService.setSalary(SALARY);
        agreementPlusService.setNotApplicable(true);

        List<PayrollConcept> result = service.calculateConceptLines(buildCommand());

        assertEquals(1, result.size(), "Should contain 1 concept when PLUS_CONVENIO inactive");
        assertEquals("BASE_SALARY", result.get(0).getConceptCode());
        assertEquals(0, SALARY.compareTo(result.get(0).getAmount()));
    }

    @Test
    void calculateConceptLinesWhenOnlyAgreementPlusActive() throws Exception {
        baseSalaryService.setNotApplicable(true);
        agreementPlusService.setPlus(PLUS);

        List<PayrollConcept> result = service.calculateConceptLines(buildCommand());

        assertEquals(1, result.size(), "Should contain 1 concept when BASE_SALARY inactive");
        assertEquals("PLUS_CONVENIO", result.get(0).getConceptCode());
        assertEquals(0, PLUS.compareTo(result.get(0).getAmount()));
    }

    @Test
    void calculateConceptLinesWhenBothInactive_ReturnsEmptyListValidly() throws Exception {
        baseSalaryService.setNotApplicable(true);
        agreementPlusService.setNotApplicable(true);

        List<PayrollConcept> result = service.calculateConceptLines(buildCommand());

        assertEquals(0, result.size(), "Should be empty when both concepts are non-applicable");
        assertTrue(result.isEmpty(), "Empty list is valid when both concepts are inactive");
    }

    @Test
    void calculateConceptLinesFailsWhenBaseSalaryActivatedButMisconfigured() {
        baseSalaryService.setConfigurationError(
                new IllegalStateException("Configuration error: BASE_SALARY table binding missing")
        );
        agreementPlusService.setPlus(PLUS);

        CalculatePayrollUnitCommand command = buildCommand();
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateConceptLines(command),
                "Should propagate configuration error from BASE_SALARY"
        );
        assertTrue(exception.getMessage().contains("Configuration error"));
    }

    @Test
    void calculateConceptLinesFailsWhenAgreementPlusActivatedButMisconfigured() {
        baseSalaryService.setSalary(SALARY);
        agreementPlusService.setConfigurationError(
                new IllegalStateException("Configuration error: PLUS_CONVENIO table row missing")
        );

        CalculatePayrollUnitCommand command = buildCommand();
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.calculateConceptLines(command),
                "Should propagate configuration error from PLUS_CONVENIO"
        );
        assertTrue(exception.getMessage().contains("Configuration error"));
    }

    @Test
    void calculateConceptLinesFailsWhenBothActivatedButBothMisconfigured() {
        baseSalaryService.setConfigurationError(
                new IllegalStateException("Configuration error: BASE_SALARY binding missing")
        );
        agreementPlusService.setConfigurationError(
                new IllegalStateException("Configuration error: PLUS_CONVENIO binding missing")
        );

        CalculatePayrollUnitCommand command = buildCommand();
        assertThrows(
                IllegalStateException.class,
                () -> service.calculateConceptLines(command),
                "Should fail on first misconfigured active concept"
        );
    }

    @Test
    void calculateConceptLinesFailsWhenOtherExceptionOccurs() {
        baseSalaryService.setRuntimeError(new RuntimeException("Database connection failed"));
        agreementPlusService.setPlus(PLUS);

        CalculatePayrollUnitCommand command = buildCommand();
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.calculateConceptLines(command),
                "Should propagate non-configuration runtime errors"
        );
        assertEquals("Database connection failed", exception.getMessage());
    }

    // ---- Test Doubles (Lightweight Fakes) ----

    /**
     * Simple fake implementation of CalculateBaseSalaryService for testing.
     * Avoids Mockito/Java 25 compatibility issues.
     */
    private static class FakeCalculateBaseSalaryService extends CalculateBaseSalaryService {
        private BigDecimal salaryToReturn;
        private boolean notApplicable = false;
        private Exception errorToThrow;

        FakeCalculateBaseSalaryService() {
            super(null, null, null, null, null, null); // Dependencies not used in test
        }

        void setSalary(BigDecimal salary) {
            this.salaryToReturn = salary;
            this.notApplicable = false;
            this.errorToThrow = null;
        }

        void setNotApplicable(boolean notApplicable) {
            this.notApplicable = notApplicable;
            this.salaryToReturn = null;
            this.errorToThrow = null;
        }

        void setConfigurationError(Exception error) {
            this.errorToThrow = error;
            this.salaryToReturn = null;
            this.notApplicable = false;
        }

        void setRuntimeError(Exception error) {
            this.errorToThrow = error;
            this.salaryToReturn = null;
            this.notApplicable = false;
        }

        @Override
        public BigDecimal calculateBaseSalary(
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber,
                LocalDate effectiveDate
        ) throws PayrollConceptNotApplicableException {
            if (errorToThrow != null) {
                if (errorToThrow instanceof PayrollConceptNotApplicableException) {
                    throw (PayrollConceptNotApplicableException) errorToThrow;
                } else if (errorToThrow instanceof IllegalStateException) {
                    throw (IllegalStateException) errorToThrow;
                } else {
                    throw (RuntimeException) errorToThrow;
                }
            }
            if (notApplicable) {
                throw new PayrollConceptNotApplicableException("BASE_SALARY is not activated");
            }
            return salaryToReturn;
        }
    }

    /**
     * Simple fake implementation of CalculateAgreementPlusService for testing.
     * Avoids Mockito/Java 25 compatibility issues.
     */
    private static class FakeCalculateAgreementPlusService extends CalculateAgreementPlusService {
        private BigDecimal plusToReturn;
        private boolean notApplicable = false;
        private Exception errorToThrow;

        FakeCalculateAgreementPlusService() {
            super(null, null, null, null, null, null); // Dependencies not used in test
        }

        void setPlus(BigDecimal plus) {
            this.plusToReturn = plus;
            this.notApplicable = false;
            this.errorToThrow = null;
        }

        void setNotApplicable(boolean notApplicable) {
            this.notApplicable = notApplicable;
            this.plusToReturn = null;
            this.errorToThrow = null;
        }

        void setConfigurationError(Exception error) {
            this.errorToThrow = error;
            this.plusToReturn = null;
            this.notApplicable = false;
        }

        void setRuntimeError(Exception error) {
            this.errorToThrow = error;
            this.plusToReturn = null;
            this.notApplicable = false;
        }

        @Override
        public BigDecimal calculateAgreementPlus(
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber,
                LocalDate effectiveDate
        ) throws PayrollConceptNotApplicableException {
            if (errorToThrow != null) {
                if (errorToThrow instanceof PayrollConceptNotApplicableException) {
                    throw (PayrollConceptNotApplicableException) errorToThrow;
                } else if (errorToThrow instanceof IllegalStateException) {
                    throw (IllegalStateException) errorToThrow;
                } else {
                    throw (RuntimeException) errorToThrow;
                }
            }
            if (notApplicable) {
                throw new PayrollConceptNotApplicableException("PLUS_CONVENIO is not activated");
            }
            return plusToReturn;
        }
    }

    private CalculatePayrollUnitCommand buildCommand() {
        return new CalculatePayrollUnitCommand(
                RULE_SYSTEM,           // ruleSystemCode
                EMPLOYEE_TYPE,         // employeeTypeCode
                EMPLOYEE_NUMBER,       // employeeNumber
                PAYROLL_PERIOD_CODE,   // payrollPeriodCode
                "REGULAR",             // payrollTypeCode
                1,                     // presenceNumber
                EFFECTIVE_DATE,        // periodStart
                EFFECTIVE_DATE.plusMonths(1), // periodEnd
                "MINIMAL_REAL",        // calculationEngineCode
                "1.0"                  // calculationEngineVersion
        );
    }
}
