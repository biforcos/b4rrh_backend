package com.b4rrhh.payroll.application.service;

import com.b4rrhh.payroll.application.usecase.CalculatePayrollUnitCommand;
import com.b4rrhh.payroll.agreementplus.application.service.CalculateAgreementPlusService;
import com.b4rrhh.payroll.basesalary.application.service.CalculateBaseSalaryService;
import com.b4rrhh.payroll.domain.model.PayrollConcept;
import com.b4rrhh.payroll.domain.model.PayrollConceptNotApplicableException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal real payroll concept line calculation orchestrator.
 *
 * Coordinates BASE_SALARY and PLUS_CONVENIO calculations for a payroll unit,
 * following agreement activation patterns.
 *
 * Semantics:
 * - Inactive concept (PayrollConceptNotApplicableException) -> skip silently
 * - Active concept with missing configuration (IllegalStateException) -> fail payroll unit
 * - All other errors (employee not found, database failure, etc.) -> fail payroll unit
 *
 * Result can be empty ONLY if both concepts are genuinely not applicable.
 * If an active concept has missing config, the payroll unit fails explicitly.
 *
 * Architectural layer: application/service (orchestrates domain concept services)
 * Coordinates: CalculateBaseSalaryService, CalculateAgreementPlusService
 * Input: payroll unit command (employee, period, execution context)
 * Output: list of real payroll concept lines (may be empty if all concepts are inactive)
 *
 * Design constraint: Minimal scope — BASE_SALARY and PLUS_CONVENIO only.
 * No generic concept registry, no forward-looking abstraction.
 * Each new concept requires explicit code (prevents feature creep).
 */
@Service
public class CalculateRealPayrollConceptLinesService implements RealPayrollConceptLinesCalculator {

    private final CalculateBaseSalaryService calculateBaseSalaryService;
    private final CalculateAgreementPlusService calculateAgreementPlusService;

    public CalculateRealPayrollConceptLinesService(
            CalculateBaseSalaryService calculateBaseSalaryService,
            CalculateAgreementPlusService calculateAgreementPlusService
    ) {
        this.calculateBaseSalaryService = calculateBaseSalaryService;
        this.calculateAgreementPlusService = calculateAgreementPlusService;
    }

    /**
     * Calculate real concept lines (BASE_SALARY, PLUS_CONVENIO) for a payroll unit.
     *
     * Inactive concepts are skipped silently.
     * Active concepts with missing or broken configuration will cause an exception to propagate.
     * Configuration/lookup errors are NOT silently skipped — they must be fixed before payroll calculates.
     *
     * @param command the payroll unit calculation request
     * @return list of PayrollConcept lines for activated concepts (may be empty if all inactive)
     * @throws PayrollConceptNotApplicableException (caught internally, concept skipped)
     * @throws IllegalStateException (propagated) if configuration is broken for an active concept
     * @throws RuntimeException (propagated) for other errors (employee not found, database failure, etc.)
     */
    public List<PayrollConcept> calculateConceptLines(CalculatePayrollUnitCommand command) {
        List<PayrollConcept> concepts = new ArrayList<>();

        // Calculate BASE_SALARY concept if activated
        try {
            BigDecimal baseSalary = calculateBaseSalaryService.calculateBaseSalary(
                    command.ruleSystemCode(),
                    command.employeeTypeCode(),
                    command.employeeNumber(),
                    command.periodStart()
            );
            concepts.add(new PayrollConcept(
                    1,
                    "BASE_SALARY",
                    "Base salary",
                    baseSalary,
                    BigDecimal.ONE,
                    baseSalary,
                    "EARNING",
                    command.payrollPeriodCode(),
                    1
            ));
        } catch (PayrollConceptNotApplicableException e) {
            // BASE_SALARY is not activated for this agreement.
            // This is normal — skip without error.
        }
        // NOTE: All other exceptions (IllegalStateException, etc.) are propagated.
        // If BASE_SALARY is activated but misconfigured, the payroll unit fails.

        // Calculate PLUS_CONVENIO concept if activated
        try {
            BigDecimal plusConvenio = calculateAgreementPlusService.calculateAgreementPlus(
                    command.ruleSystemCode(),
                    command.employeeTypeCode(),
                    command.employeeNumber(),
                    command.periodStart()
            );
            concepts.add(new PayrollConcept(
                    2,
                    "PLUS_CONVENIO",
                    "Agreement bonus",
                    plusConvenio,
                    BigDecimal.ONE,
                    plusConvenio,
                    "EARNING",
                    command.payrollPeriodCode(),
                    2
            ));
        } catch (PayrollConceptNotApplicableException e) {
            // PLUS_CONVENIO is not activated for this agreement.
            // This is normal — skip without error.
        }
        // NOTE: All other exceptions (IllegalStateException, etc.) are propagated.
        // If PLUS_CONVENIO is activated but misconfigured, the payroll unit fails.

        // Empty concept list is valid ONLY if both concepts are genuinely not applicable.
        return concepts;
    }
}
