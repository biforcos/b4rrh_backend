package com.b4rrhh.payroll.agreementplus.application.service;

import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import com.b4rrhh.payroll.basesalary.domain.EmployeeByBusinessKeyLookupPort;
import com.b4rrhh.payroll.basesalary.domain.EmployeeAgreementCategoryLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollObjectActivationLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollObjectBindingLookupPort;
import com.b4rrhh.payroll.basesalary.domain.PayrollTableRowLookupPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Application service for calculating the monthly agreement plus (PLUS_CONVENIO).
 *
 * Strictly parallel to CalculateBaseSalaryService.
 * Reuses the same lookup ports — no new ports were needed.
 *
 * Flow:
 *   Step 0: resolve employee by business key
 *   Step 1: resolve agreement context (ruleSystemCode + agreementCode)
 *   Step 2: resolve agreement category code at effective date
 *   Step 3: verify PLUS_CONVENIO is activated for the agreement
 *   Step 4: resolve which table is bound for AGREEMENT_PLUS_TABLE
 *   Step 5: resolve the applicable table row by category and date
 *   Step 6: return monthly_value
 */
@Service
public class CalculateAgreementPlusService {

    private final EmployeeByBusinessKeyLookupPort employeeByBusinessKeyLookup;
    private final EmployeeAgreementContextLookupPort employeeAgreementContextLookup;
    private final EmployeeAgreementCategoryLookupPort employeeAgreementCategoryLookup;
    private final PayrollObjectActivationLookupPort payrollObjectActivationLookup;
    private final PayrollObjectBindingLookupPort bindingLookup;
    private final PayrollTableRowLookupPort tableRowLookup;

    public CalculateAgreementPlusService(
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
     * Calculate the monthly agreement plus for an employee on a given effective date.
     *
     * @param ruleSystemCode   the rule system (e.g., "ESP")
     * @param employeeTypeCode the employee type (e.g., "INTERNAL")
     * @param employeeNumber   the employee business key
     * @param effectiveDate    the date for which to calculate
     * @return the monthly agreement plus amount
     * @throws IllegalArgumentException if required configuration is missing or inactive
     * @throws IllegalStateException    if no valid labor classification exists
     */
    public BigDecimal calculateAgreementPlus(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate effectiveDate
    ) {
        // Step 0: Resolve employee ID from business key
        Long employeeId = employeeByBusinessKeyLookup.resolveEmployeeId(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber
        ).orElseThrow(() -> new IllegalArgumentException(
                "Employee not found: " + employeeNumber
        ));

        // Step 1: Resolve agreement context (ruleSystemCode + agreementCode)
        var agreementContext = employeeAgreementContextLookup.resolveContext(
                employeeId,
                effectiveDate
        );

        String agreementCode = agreementContext.agreementCode();

        // Step 2: Resolve category from labor classification
        String categoryCode = employeeAgreementCategoryLookup.resolveAgreementCategoryCode(
                employeeId,
                effectiveDate
        ).orElseThrow(() -> new IllegalArgumentException(
                "No agreement category found for employee " + employeeNumber + " on " + effectiveDate
        ));

        // Step 3: Verify PLUS_CONVENIO concept activation
        boolean plusConvenioActivated = payrollObjectActivationLookup.isActive(
                ruleSystemCode,
                "AGREEMENT",
                agreementCode,
                "PAYROLL_CONCEPT",
                "PLUS_CONVENIO"
        );
        if (!plusConvenioActivated) {
            throw new IllegalArgumentException(
                    "PLUS_CONVENIO is not active for agreement " + agreementCode
            );
        }

        // Step 4: Resolve table binding (AGREEMENT_PLUS_TABLE -> TABLE code)
        String tableCode = bindingLookup.resolveBoundObjectCode(
                ruleSystemCode,
                "AGREEMENT",
                agreementCode,
                "AGREEMENT_PLUS_TABLE"
        ).orElseThrow(() -> new IllegalArgumentException(
                "No agreement plus table binding found for agreement " + agreementCode
        ));

        // Step 5: Resolve table row and return monthly value
        return tableRowLookup.resolveMonthlyValue(
                ruleSystemCode,
                tableCode,
                categoryCode,
                effectiveDate
        ).orElseThrow(() -> new IllegalArgumentException(
                "No agreement plus row found in table " + tableCode +
                        " for category " + categoryCode + " on " + effectiveDate
        ));
    }
}
