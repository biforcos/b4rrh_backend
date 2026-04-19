package com.b4rrhh.payroll_engine.dependency.domain.model;

public enum DependencyType {
    /**
     * A concept directly uses another concept as an operand in its calculation.
     * Source concept depends on the dependency concept.
     */
    OPERAND_DEPENDENCY,

    /**
     * A concept receives a feed from another concept.
     * Derived from PayrollConceptFeedRelation with FEED_BY_SOURCE mode.
     */
    FEED_DEPENDENCY
}
