package com.b4rrhh.payroll_engine.dependency.domain.model;

import java.util.Objects;

/**
 * Identifies a PayrollConcept node in the dependency graph by its business key.
 * Technical IDs are not used for graph identity.
 */
public final class ConceptNodeIdentity {

    private final String ruleSystemCode;
    private final String conceptCode;

    public ConceptNodeIdentity(String ruleSystemCode, String conceptCode) {
        if (ruleSystemCode == null || ruleSystemCode.isBlank()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }
        if (conceptCode == null || conceptCode.isBlank()) {
            throw new IllegalArgumentException("conceptCode is required");
        }
        this.ruleSystemCode = ruleSystemCode;
        this.conceptCode = conceptCode;
    }

    public String getRuleSystemCode() {
        return ruleSystemCode;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConceptNodeIdentity other)) return false;
        return Objects.equals(ruleSystemCode, other.ruleSystemCode)
                && Objects.equals(conceptCode, other.conceptCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleSystemCode, conceptCode);
    }

    @Override
    public String toString() {
        return ruleSystemCode + "/" + conceptCode;
    }
}
