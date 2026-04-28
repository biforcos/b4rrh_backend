package com.b4rrhh.payroll_engine.eligibility.application.usecase;

import java.time.LocalDate;

/**
 * Command to create a new concept assignment under the supplied rule system.
 *
 * <p>The optional dimensions {@code companyCode}, {@code agreementCode} and
 * {@code employeeTypeCode} act as wildcards when null at execution time. Their
 * blank-or-null normalisation is performed by the {@code ConceptAssignment} domain
 * constructor.
 */
public record CreateConceptAssignmentCommand(
        String ruleSystemCode,
        String conceptCode,
        String companyCode,
        String agreementCode,
        String employeeTypeCode,
        LocalDate validFrom,
        LocalDate validTo,
        int priority
) {
}
