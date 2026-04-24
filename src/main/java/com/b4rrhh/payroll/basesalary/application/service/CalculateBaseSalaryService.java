package com.b4rrhh.payroll.basesalary.application.service;

import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import com.b4rrhh.payroll.basesalary.domain.EmployeeByBusinessKeyLookupPort;
import com.b4rrhh.payroll.basesalary.domain.EmployeeAgreementCategoryLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollObjectActivationLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollObjectBindingLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollTableRowLookupPort;
import com.b4rrhh.payroll.domain.model.PayrollConceptNotApplicableException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Legacy spike service kept for backward compatibility/tests.
 * Not part of the canonical payroll launch flow.
 * Canonical flow uses PayrollConceptGraphCalculator.calculateConceptResult("101", ...).
 * Will be removed in a future cleanup.
 *
 * Application service for calculating base salary.
 * Orchestrates the resolution of agreement, category, binding, and table row
 * to return the monthly base salary.
 */
@Service
@Deprecated
public class CalculateBaseSalaryService {

        private final EmployeeByBusinessKeyLookupPort employeeByBusinessKeyLookup;
    private final EmployeeAgreementContextLookupPort employeeAgreementContextLookup;
        private final EmployeeAgreementCategoryLookupPort employeeAgreementCategoryLookup;
        private final PayrollObjectActivationLookupPort payrollObjectActivationLookup;
    private final PayrollObjectBindingLookupPort bindingLookup;
    private final PayrollTableRowLookupPort tableRowLookup;

    public CalculateBaseSalaryService(
                        EmployeeByBusinessKeyLookupPort employeeByBusinessKeyLookup,
            EmployeeAgreementContextLookupPort employeeAgreementContextLookup,
                        EmployeeAgreementCategoryLookupPort employeeAgreementCategoryLookup,
                        PayrollObjectActivationLookupPort payrollObjectActivationLookup,
            PayrollObjectBindingLookupPort bindingLookup,
                        PayrollTableRowLookupPort tableRowLookup
    ) {
                this.employeeByBusinessKeyLookup = employeeByBusinessKeyLookup;
        this.employeeAgreementContextLookup = employeeAgreementContextLookup;
                this.employeeAgreementCategoryLookup = employeeAgreementCategoryLookup;
                this.payrollObjectActivationLookup = payrollObjectActivationLookup;
        this.bindingLookup = bindingLookup;
        this.tableRowLookup = tableRowLookup;
    }

    /**
     * Calculate base salary for an employee on a given effective date.
     *
     * @param ruleSystemCode the rule system (e.g., "ESP")
     * @param employeeTypeCode the employee type (e.g., "INTERNAL")
     * @param employeeNumber the employee business key
     * @param effectiveDate the date for which to calculate
     * @return the monthly base salary
     * @throws PayrollConceptNotApplicableException if BASE_SALARY is not activated for the agreement
     * @throws IllegalStateException if required configuration or lookup fails
     */
    public BigDecimal calculateBaseSalary(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate effectiveDate
    ) throws PayrollConceptNotApplicableException {
        // Step 0: Resolve employee ID from business key
        Long employeeId = employeeByBusinessKeyLookup.resolveEmployeeId(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber
        ).orElseThrow(() -> new IllegalStateException(
                "Configuration error: Employee not found: " + employeeNumber
        ));

        // Step 1: Resolve agreement code from employee + date
        var agreementContext = employeeAgreementContextLookup.resolveContext(
                employeeId,
                effectiveDate
        );

        String agreementCode = agreementContext.agreementCode();

        // Step 2: Resolve category from labor classification context
        String categoryCode = employeeAgreementCategoryLookup.resolveAgreementCategoryCode(
                employeeId,
                effectiveDate
        ).orElseThrow(() -> new IllegalStateException(
                "Configuration error: No agreement category found for employee " + employeeNumber + " on " + effectiveDate
        ));

        // Step 3: Verify concept activation
        boolean baseSalaryActivated = payrollObjectActivationLookup.isActive(
                ruleSystemCode,
                "AGREEMENT",
                agreementCode,
                "PAYROLL_CONCEPT",
                "BASE_SALARY"
        );
        if (!baseSalaryActivated) {
            throw new PayrollConceptNotApplicableException(
                    "BASE_SALARY is not active for agreement " + agreementCode
            );
        }

        // Step 4: Resolve table binding
        String tableCode = bindingLookup.resolveBoundObjectCode(
                ruleSystemCode,
                "AGREEMENT",
                agreementCode,
                "BASE_SALARY_TABLE"
        ).orElseThrow(() -> new IllegalStateException(
                "Configuration error: No base salary table binding found for agreement " + agreementCode
        ));

        // Step 5: Resolve table row and get monthly value
        return tableRowLookup.resolveMonthlyValue(
                ruleSystemCode,
                tableCode,
                categoryCode,
                effectiveDate
        ).orElseThrow(() -> new IllegalStateException(
                "Configuration error: No base salary row found in table " + tableCode +
                        " for category " + categoryCode + " on " + effectiveDate
        ));
    }
}
