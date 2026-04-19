package com.b4rrhh.payroll_engine.execution.domain.exception;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;

/**
 * Thrown when the segment execution engine encounters a calculation type
 * that is not supported in this PoC iteration.
 */
public class UnsupportedCalculationTypeException extends RuntimeException {

    public UnsupportedCalculationTypeException(CalculationType type) {
        super("Calculation type not supported in PoC segment execution engine: " + type +
              ". Only DIRECT_AMOUNT and RATE_BY_QUANTITY are supported in this iteration.");
    }
}
