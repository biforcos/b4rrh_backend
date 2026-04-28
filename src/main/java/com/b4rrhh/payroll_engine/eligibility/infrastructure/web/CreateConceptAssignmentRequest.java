package com.b4rrhh.payroll_engine.eligibility.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request payload for creating a concept assignment rule. The optional dimensions
 * ({@code companyCode}, {@code agreementCode}, {@code employeeTypeCode}) act as wildcards
 * when null at execution time. {@code priority} is an explicit integer chosen by the
 * caller; higher values win in eligibility resolution.
 */
public record CreateConceptAssignmentRequest(
        @NotBlank String conceptCode,
        String companyCode,
        String agreementCode,
        String employeeTypeCode,
        @NotNull LocalDate validFrom,
        LocalDate validTo,
        @NotNull Integer priority
) {
}
