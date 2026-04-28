package com.b4rrhh.payroll_engine.concept.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body for the PUT operands endpoint. Replaces the full set of operands attached
 * to the target concept; an empty list removes every operand.
 */
public record UpdateConceptOperandsRequest(
        @NotNull
        @Valid
        List<OperandItem> operands
) {

    public record OperandItem(
            @NotBlank String operandRole,
            @NotBlank String sourceObjectCode
    ) {
    }
}
