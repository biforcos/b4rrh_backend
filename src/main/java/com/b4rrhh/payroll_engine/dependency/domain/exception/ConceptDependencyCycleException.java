package com.b4rrhh.payroll_engine.dependency.domain.exception;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

import java.util.List;
import java.util.stream.Collectors;

public class ConceptDependencyCycleException extends RuntimeException {

    public ConceptDependencyCycleException(List<ConceptNodeIdentity> cycle) {
        super("Dependency cycle detected among PayrollConcepts: "
                + cycle.stream()
                        .map(ConceptNodeIdentity::toString)
                        .collect(Collectors.joining(" -> ")));
    }
}
