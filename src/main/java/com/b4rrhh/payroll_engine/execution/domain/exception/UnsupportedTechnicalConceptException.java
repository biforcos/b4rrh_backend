package com.b4rrhh.payroll_engine.execution.domain.exception;

/**
 * Thrown when no {@link com.b4rrhh.payroll_engine.execution.application.service.TechnicalConceptCalculator}
 * is registered for a given ENGINE_PROVIDED concept code.
 */
public class UnsupportedTechnicalConceptException extends RuntimeException {

    public UnsupportedTechnicalConceptException(String conceptCode) {
        super("No TechnicalConceptCalculator registered for concept: '" + conceptCode + "'.");
    }
}
