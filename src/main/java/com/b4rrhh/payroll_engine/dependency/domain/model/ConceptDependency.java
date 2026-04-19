package com.b4rrhh.payroll_engine.dependency.domain.model;

import java.util.Objects;

/**
 * A directed dependency edge in the concept dependency graph.
 * Semantics: {@code dependent} requires {@code dependency} to be calculated first.
 * In graph terms: edge from dependent → dependency (dependency must precede dependent).
 */
public final class ConceptDependency {

    private final ConceptNodeIdentity dependent;
    private final ConceptNodeIdentity dependency;
    private final DependencyType type;

    public ConceptDependency(
            ConceptNodeIdentity dependent,
            ConceptNodeIdentity dependency,
            DependencyType type
    ) {
        if (dependent == null) {
            throw new IllegalArgumentException("dependent is required");
        }
        if (dependency == null) {
            throw new IllegalArgumentException("dependency is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        if (dependent.equals(dependency)) {
            throw new IllegalArgumentException(
                    "A concept cannot depend on itself: " + dependent
            );
        }
        this.dependent = dependent;
        this.dependency = dependency;
        this.type = type;
    }

    public ConceptNodeIdentity getDependent() {
        return dependent;
    }

    public ConceptNodeIdentity getDependency() {
        return dependency;
    }

    public DependencyType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConceptDependency other)) return false;
        return Objects.equals(dependent, other.dependent)
                && Objects.equals(dependency, other.dependency)
                && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependent, dependency, type);
    }

    @Override
    public String toString() {
        return dependent + " --[" + type + "]--> " + dependency;
    }
}
