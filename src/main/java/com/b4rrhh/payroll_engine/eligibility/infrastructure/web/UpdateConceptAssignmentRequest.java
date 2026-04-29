package com.b4rrhh.payroll_engine.eligibility.infrastructure.web;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateConceptAssignmentRequest(
        String companyCode,
        String agreementCode,
        String employeeTypeCode,
        @NotNull LocalDate validFrom,
        LocalDate validTo,
        @NotNull Integer priority
) {
}
