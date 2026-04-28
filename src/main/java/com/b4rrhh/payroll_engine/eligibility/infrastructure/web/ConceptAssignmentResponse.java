package com.b4rrhh.payroll_engine.eligibility.infrastructure.web;

import java.time.LocalDate;

/**
 * Wire representation of a persisted concept assignment.
 *
 * <p>The {@code assignmentCode} is the opaque identifier exposed in the OpenAPI contract.
 * In this iteration it is the surrogate {@code id} rendered as a string; this lets the
 * delete endpoint locate the row without leaking the JPA-level Long type to clients.
 */
public record ConceptAssignmentResponse(
        String assignmentCode,
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
