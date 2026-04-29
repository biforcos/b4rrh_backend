package com.b4rrhh.payroll_engine.eligibility.application.usecase;

import java.time.LocalDate;

public record UpdateConceptAssignmentCommand(
        String ruleSystemCode,
        String assignmentCode,
        String companyCode,
        String agreementCode,
        String employeeTypeCode,
        LocalDate validFrom,
        LocalDate validTo,
        int priority
) {
}
