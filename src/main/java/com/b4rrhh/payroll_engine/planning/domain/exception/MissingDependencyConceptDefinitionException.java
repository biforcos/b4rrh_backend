package com.b4rrhh.payroll_engine.planning.domain.exception;

/**
 * Thrown when a concept required as a structural dependency (discovered through feed
 * relations during graph expansion) has no matching concept definition in the repository.
 *
 * <p>This is distinct from {@link MissingEligibleConceptDefinitionException}: the missing
 * concept was not directly assigned via eligibility but is a technical or upstream dependency
 * of one that was. Fail fast to expose the broken dependency chain before execution.
 */
public class MissingDependencyConceptDefinitionException extends RuntimeException {

    private final String ruleSystemCode;
    private final String conceptCode;

    public MissingDependencyConceptDefinitionException(String ruleSystemCode, String conceptCode) {
        super("No concept definition found for required dependency: " + ruleSystemCode + "/" + conceptCode);
        this.ruleSystemCode = ruleSystemCode;
        this.conceptCode = conceptCode;
    }

    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getConceptCode() { return conceptCode; }
}
