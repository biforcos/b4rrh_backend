package com.b4rrhh.payroll_engine.eligibility.domain.model;

import java.util.Objects;

/**
 * The result of resolving a single applicable concept for a given employee context.
 *
 * <p>Contains the concept code plus enough source information for debugging and auditing:
 * which assignment won the resolution, and at what priority.
 *
 * <p>This is a value object — immutable, identity by fields.
 */
public class ResolvedConceptAssignment {

    private final String conceptCode;
    private final int winningPriority;
    private final String ruleSystemCode;
    private final String companyCode;
    private final String agreementCode;
    private final String employeeTypeCode;

    public ResolvedConceptAssignment(
            String conceptCode,
            int winningPriority,
            String ruleSystemCode,
            String companyCode,
            String agreementCode,
            String employeeTypeCode
    ) {
        this.conceptCode = Objects.requireNonNull(conceptCode, "conceptCode is required");
        this.ruleSystemCode = Objects.requireNonNull(ruleSystemCode, "ruleSystemCode is required");
        this.winningPriority = winningPriority;
        this.companyCode = companyCode;
        this.agreementCode = agreementCode;
        this.employeeTypeCode = employeeTypeCode;
    }

    public String getConceptCode() { return conceptCode; }
    public int getWinningPriority() { return winningPriority; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getCompanyCode() { return companyCode; }
    public String getAgreementCode() { return agreementCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResolvedConceptAssignment other)) return false;
        return winningPriority == other.winningPriority
                && Objects.equals(conceptCode, other.conceptCode)
                && Objects.equals(ruleSystemCode, other.ruleSystemCode)
                && Objects.equals(companyCode, other.companyCode)
                && Objects.equals(agreementCode, other.agreementCode)
                && Objects.equals(employeeTypeCode, other.employeeTypeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conceptCode, winningPriority, ruleSystemCode, companyCode, agreementCode, employeeTypeCode);
    }

    @Override
    public String toString() {
        return "ResolvedConceptAssignment{concept=" + conceptCode
                + ", priority=" + winningPriority
                + ", ruleSystem=" + ruleSystemCode
                + ", company=" + companyCode
                + ", agreement=" + agreementCode
                + ", employeeType=" + employeeTypeCode + '}';
    }
}
