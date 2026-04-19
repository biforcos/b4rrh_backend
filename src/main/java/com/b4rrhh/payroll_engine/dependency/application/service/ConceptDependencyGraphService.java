package com.b4rrhh.payroll_engine.dependency.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;

import java.time.LocalDate;
import java.util.List;

/**
 * Application service that builds a {@link ConceptDependencyGraph} from a list of
 * {@link PayrollConcept}s and their persisted feed relations.
 *
 * <p>The reference date is required because feed relations are time-bounded
 * (they have effective-from / effective-to). Only relations active on the
 * reference date are included in the graph.
 *
 * <p>Only concepts present in the input list become nodes. Relations whose
 * source concept is not in the input list are silently ignored — the execution
 * plan builder will fail explicitly if a required dependency is absent.
 */
public interface ConceptDependencyGraphService {

    /**
     * Builds a dependency graph from the given concepts and their active feed relations.
     *
     * @param concepts      the full set of concepts to include as graph nodes
     * @param referenceDate the date used to filter active feed relations
     * @return a validated, cycle-free {@link ConceptDependencyGraph}
     */
    ConceptDependencyGraph build(List<PayrollConcept> concepts, LocalDate referenceDate);
}
