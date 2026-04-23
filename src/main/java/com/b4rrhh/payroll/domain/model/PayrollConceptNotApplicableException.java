package com.b4rrhh.payroll.domain.model;

/**
 * Exception thrown when a payroll concept is genuinely not applicable
 * to a payroll unit (e.g., not activated in the agreement).
 *
 * This exception MUST be used ONLY when:
 * - The concept activation lookup returns false/inactive
 * - The concept is not required for this payroll unit
 *
 * This exception MUST NOT be used for:
 * - Missing employee
 * - Missing configuration data
 * - Missing labor classification context
 * - Missing bindings or table rows for an ACTIVE concept
 * - Database/lookup errors
 *
 * Semantics: When this exception is thrown, the concept is silently skipped.
 * The orchestrator continues with the next concept without propagating the error.
 */
public class PayrollConceptNotApplicableException extends Exception {

    public PayrollConceptNotApplicableException(String message) {
        super(message);
    }

    public PayrollConceptNotApplicableException(String message, Throwable cause) {
        super(message, cause);
    }
}
