package com.b4rrhh.payroll.basesalary.application.service;

import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import com.b4rrhh.payroll.basesalary.domain.EmployeeByBusinessKeyLookupPort;
import com.b4rrhh.payroll.basesalary.domain.EmployeeAgreementCategoryLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollObjectActivationLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollObjectBindingLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollTableRowLookupPort;
import com.b4rrhh.payroll.domain.model.PayrollConceptNotApplicableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateBaseSalaryServiceTest {

    private static final String RULE_SYSTEM = "ESP";
    private static final String EMPLOYEE_TYPE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";
    private static final Long EMPLOYEE_ID = 1L;
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2026, 1, 15);
    private static final String AGREEMENT_CODE = "99002405011982";
    private static final String CATEGORY_CODE = "99002405-G2";
    private static final String TABLE_CODE = "SB_99002405011982";
    private static final BigDecimal EXPECTED_SALARY = new BigDecimal("1425.00");

        @Mock
        private EmployeeByBusinessKeyLookupPort employeeByBusinessKeyLookup;

    @Mock
    private EmployeeAgreementContextLookupPort agreementContextLookup;

    @Mock
    private EmployeeAgreementCategoryLookupPort agreementCategoryLookup;

    @Mock
    private PayrollObjectActivationLookupPort activationLookup;

    @Mock
    private PayrollObjectBindingLookupPort bindingLookup;

    @Mock
    private PayrollTableRowLookupPort tableRowLookup;

    private CalculateBaseSalaryService service;

    @BeforeEach
    void setUp() {
        service = new CalculateBaseSalaryService(
                                employeeByBusinessKeyLookup,
                agreementContextLookup,
                agreementCategoryLookup,
                activationLookup,
                bindingLookup,
                tableRowLookup
        );
    }

    @Test
    void calculateBaseSalarySucceedsWhenAllLookupsResolveCorrectly() throws PayrollConceptNotApplicableException {
        when(employeeByBusinessKeyLookup.resolveEmployeeId(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(EMPLOYEE_ID));
        when(agreementContextLookup.resolveContext(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(new EmployeeAgreementContext(RULE_SYSTEM, AGREEMENT_CODE));
        when(agreementCategoryLookup.resolveAgreementCategoryCode(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(Optional.of(CATEGORY_CODE));
        when(activationLookup.isActive(RULE_SYSTEM, "AGREEMENT", AGREEMENT_CODE, "PAYROLL_CONCEPT", "BASE_SALARY"))
                .thenReturn(true);
        when(bindingLookup.resolveBoundObjectCode(RULE_SYSTEM, "AGREEMENT", AGREEMENT_CODE, "BASE_SALARY_TABLE"))
                .thenReturn(Optional.of(TABLE_CODE));
        when(tableRowLookup.resolveMonthlyValue(RULE_SYSTEM, TABLE_CODE, CATEGORY_CODE, EFFECTIVE_DATE))
                .thenReturn(Optional.of(EXPECTED_SALARY));

        BigDecimal result = service.calculateBaseSalary(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER, EFFECTIVE_DATE);

        assertEquals(0, EXPECTED_SALARY.compareTo(result));
    }

    @Test
    void calculateBaseSalaryFailsWhenEmployeeNotFound() {
        when(employeeByBusinessKeyLookup.resolveEmployeeId(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.calculateBaseSalary(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER, EFFECTIVE_DATE)
        );
    }

    @Test
    void calculateBaseSalaryFailsWhenAgreementContextMissing() {
        when(employeeByBusinessKeyLookup.resolveEmployeeId(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(EMPLOYEE_ID));
        when(agreementContextLookup.resolveContext(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenThrow(new IllegalStateException("No valid labor classification found"));

        assertThrows(
                IllegalStateException.class,
                () -> service.calculateBaseSalary(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER, EFFECTIVE_DATE)
        );
    }

    @Test
    void calculateBaseSalaryFailsWhenCategoryMissing() {
        when(employeeByBusinessKeyLookup.resolveEmployeeId(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(EMPLOYEE_ID));
        when(agreementContextLookup.resolveContext(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(new EmployeeAgreementContext(RULE_SYSTEM, AGREEMENT_CODE));
        when(agreementCategoryLookup.resolveAgreementCategoryCode(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.calculateBaseSalary(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER, EFFECTIVE_DATE)
        );
    }

    @Test
    void calculateBaseSalaryFailsWhenActivationMissing() {
        when(employeeByBusinessKeyLookup.resolveEmployeeId(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(EMPLOYEE_ID));
        when(agreementContextLookup.resolveContext(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(new EmployeeAgreementContext(RULE_SYSTEM, AGREEMENT_CODE));
        when(agreementCategoryLookup.resolveAgreementCategoryCode(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(Optional.of(CATEGORY_CODE));
        when(activationLookup.isActive(RULE_SYSTEM, "AGREEMENT", AGREEMENT_CODE, "PAYROLL_CONCEPT", "BASE_SALARY"))
                .thenReturn(false);

        assertThrows(
                PayrollConceptNotApplicableException.class,
                () -> service.calculateBaseSalary(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER, EFFECTIVE_DATE)
        );
    }

    @Test
    void calculateBaseSalaryFailsWhenBindingMissing() {
        when(employeeByBusinessKeyLookup.resolveEmployeeId(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(EMPLOYEE_ID));
        when(agreementContextLookup.resolveContext(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(new EmployeeAgreementContext(RULE_SYSTEM, AGREEMENT_CODE));
        when(agreementCategoryLookup.resolveAgreementCategoryCode(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(Optional.of(CATEGORY_CODE));
        when(activationLookup.isActive(RULE_SYSTEM, "AGREEMENT", AGREEMENT_CODE, "PAYROLL_CONCEPT", "BASE_SALARY"))
                .thenReturn(true);
        when(bindingLookup.resolveBoundObjectCode(RULE_SYSTEM, "AGREEMENT", AGREEMENT_CODE, "BASE_SALARY_TABLE"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.calculateBaseSalary(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER, EFFECTIVE_DATE)
        );
    }

    @Test
    void calculateBaseSalaryFailsWhenTableRowMissing() {
        when(employeeByBusinessKeyLookup.resolveEmployeeId(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(EMPLOYEE_ID));
        when(agreementContextLookup.resolveContext(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(new EmployeeAgreementContext(RULE_SYSTEM, AGREEMENT_CODE));
        when(agreementCategoryLookup.resolveAgreementCategoryCode(EMPLOYEE_ID, EFFECTIVE_DATE))
                .thenReturn(Optional.of(CATEGORY_CODE));
        when(activationLookup.isActive(RULE_SYSTEM, "AGREEMENT", AGREEMENT_CODE, "PAYROLL_CONCEPT", "BASE_SALARY"))
                .thenReturn(true);
        when(bindingLookup.resolveBoundObjectCode(RULE_SYSTEM, "AGREEMENT", AGREEMENT_CODE, "BASE_SALARY_TABLE"))
                .thenReturn(Optional.of(TABLE_CODE));
        when(tableRowLookup.resolveMonthlyValue(RULE_SYSTEM, TABLE_CODE, CATEGORY_CODE, EFFECTIVE_DATE))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.calculateBaseSalary(RULE_SYSTEM, EMPLOYEE_TYPE, EMPLOYEE_NUMBER, EFFECTIVE_DATE)
        );
    }

}
