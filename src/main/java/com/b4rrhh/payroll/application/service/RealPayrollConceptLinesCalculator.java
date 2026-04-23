package com.b4rrhh.payroll.application.service;

import com.b4rrhh.payroll.application.usecase.CalculatePayrollUnitCommand;
import com.b4rrhh.payroll.domain.model.PayrollConcept;

import java.util.List;

/**
 * Port for calculating real payroll concept lines (BASE_SALARY, PLUS_CONVENIO) for a payroll unit.
 *
 * Extracted as an interface to allow substitution in tests without requiring Mockito inline mocking,
 * which is incompatible with Java 25 for concrete classes.
 *
 * Architectural layer: application/service (internal application boundary)
 */
public interface RealPayrollConceptLinesCalculator {

    /**
     * Calculate real concept lines for a payroll unit.
     *
     * Inactive concepts are skipped silently (PayrollConceptNotApplicableException is caught internally).
     * Active concepts with missing or broken configuration will cause an IllegalStateException to propagate.
     *
     * @param command the payroll unit calculation request
     * @return list of PayrollConcept lines for activated concepts (may be empty if all inactive)
     * @throws IllegalStateException if configuration is broken for an active concept
     */
    List<PayrollConcept> calculateConceptLines(CalculatePayrollUnitCommand command);
}
