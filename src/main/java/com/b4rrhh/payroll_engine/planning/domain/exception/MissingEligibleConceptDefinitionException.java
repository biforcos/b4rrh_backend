package com.b4rrhh.payroll_engine.planning.domain.exception;

/**
 * Thrown when an eligibility-resolved concept code has no matching concept definition
 * in the concept repository.
 *
 * <p>This indicates a data integrity issue: eligibility assignments reference a concept
 * that has not been defined (or was deleted). Fail fast to avoid silent miscalculations.
 */
public class MissingEligibleConceptDefinitionException extends RuntimeException {

    private final String ruleSystemCode;
    private final String conceptCode;

    public MissingEligibleConceptDefinitionException(String ruleSystemCode, String conceptCode) {
        super("No concept definition found for eligible concept: " + ruleSystemCode + "/" + conceptCode);
        this.ruleSystemCode = ruleSystemCode;
        this.conceptCode = conceptCode;
    }

    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getConceptCode() { return conceptCode; }
}
