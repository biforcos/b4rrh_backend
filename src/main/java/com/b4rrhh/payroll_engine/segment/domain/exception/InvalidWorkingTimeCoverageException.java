package com.b4rrhh.payroll_engine.segment.domain.exception;

/**
 * Thrown when the supplied working time windows do not fully and contiguously cover
 * the calculation period.
 *
 * <p>Payroll calculation requires complete coverage: no day in the period may be left
 * without an assigned working-time window. The builder does not fill gaps automatically;
 * callers must supply a coherent set of windows.
 */
public class InvalidWorkingTimeCoverageException extends RuntimeException {

    public InvalidWorkingTimeCoverageException(String message) {
        super(message);
    }
}
