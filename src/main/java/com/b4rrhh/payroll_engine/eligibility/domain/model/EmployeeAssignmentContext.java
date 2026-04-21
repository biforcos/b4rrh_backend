package com.b4rrhh.payroll_engine.eligibility.domain.model;

import java.util.Objects;

/**
 * Query value object carrying the employee-context dimensions used for eligibility resolution.
 *
 * <p>This is NOT a domain aggregate. It is a lightweight carrier of the context keys that
 * the resolver uses to match against {@link ConceptAssignment} scope dimensions.
 *
 * <h3>Null semantics for optional dimensions</h3>
 * <p>The optional fields ({@code companyCode}, {@code agreementCode}, {@code employeeTypeCode})
 * may be null to indicate that the dimension is <strong>unknown or unspecified</strong> for
 * this context. A null context dimension is NOT a wildcard that matches any assignment value.
 * It means the caller does not know that dimension, so:
 * <ul>
 *   <li>Assignments with a null dimension (wildcard) <strong>will</strong> match.</li>
 *   <li>Assignments with a specific non-null dimension <strong>will not</strong> match,
 *       because we cannot confirm the context satisfies that specific requirement.</li>
 * </ul>
 * <p>This asymmetry is intentional: only the assignment side carries wildcard semantics.
 * The context side is always concrete (known value) or absent (unknown value).
 *
 * <h3>employeeTypeCode note</h3>
 * <p>This field is a structural employee classification (e.g. indefinite, temporary, intern).
 * It is NOT the collective-agreement professional category ({@code labor_classification}).
 */
public class EmployeeAssignmentContext {

    private final String ruleSystemCode;
    private final String companyCode;
    private final String agreementCode;
    private final String employeeTypeCode;

    public EmployeeAssignmentContext(
            String ruleSystemCode,
            String companyCode,
            String agreementCode,
            String employeeTypeCode
    ) {
        if (ruleSystemCode == null || ruleSystemCode.isBlank()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }
        this.ruleSystemCode = ruleSystemCode;
        this.companyCode = companyCode;
        this.agreementCode = agreementCode;
        this.employeeTypeCode = employeeTypeCode;
    }

    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getCompanyCode() { return companyCode; }
    public String getAgreementCode() { return agreementCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeeAssignmentContext other)) return false;
        return Objects.equals(ruleSystemCode, other.ruleSystemCode)
                && Objects.equals(companyCode, other.companyCode)
                && Objects.equals(agreementCode, other.agreementCode)
                && Objects.equals(employeeTypeCode, other.employeeTypeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleSystemCode, companyCode, agreementCode, employeeTypeCode);
    }

    @Override
    public String toString() {
        return "EmployeeAssignmentContext{ruleSystem=" + ruleSystemCode
                + ", company=" + companyCode
                + ", agreement=" + agreementCode
                + ", employeeType=" + employeeTypeCode + '}';
    }
}
