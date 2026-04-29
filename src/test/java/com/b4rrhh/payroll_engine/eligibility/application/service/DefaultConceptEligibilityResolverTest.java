package com.b4rrhh.payroll_engine.eligibility.application.service;

import com.b4rrhh.payroll_engine.eligibility.domain.exception.DuplicateConceptAssignmentException;
import com.b4rrhh.payroll_engine.eligibility.domain.model.ConceptAssignment;
import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.eligibility.domain.model.ResolvedConceptAssignment;
import com.b4rrhh.payroll_engine.eligibility.domain.port.ConceptAssignmentRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DefaultConceptEligibilityResolver}.
 *
 * <p>The repository is replaced with an in-memory fake so no Spring context is needed.
 */
class DefaultConceptEligibilityResolverTest {

    private static final String RS = "ESP";
    private static final LocalDate REF = LocalDate.of(2025, 3, 1);

    private static final EmployeeAssignmentContext FULL_CONTEXT =
            new EmployeeAssignmentContext(RS, "EMP1", "METAL", "INDEFINIDO");

    // ── test: global assignment applies ────────────────────────────────────

    @Test
    void globalAssignment_appliesWhenContextMatches() {
        ConceptAssignment global = assignment(RS, "SALARIO_BASE", null, null, null, 0);
        DefaultConceptEligibilityResolver resolver = resolverWith(List.of(global));

        List<ResolvedConceptAssignment> result = resolver.resolve(FULL_CONTEXT, REF);

        assertEquals(1, result.size());
        assertEquals("SALARIO_BASE", result.get(0).getConceptCode());
        assertEquals(0, result.get(0).getWinningPriority());
        assertNull(result.get(0).getCompanyCode());
        assertNull(result.get(0).getAgreementCode());
        assertNull(result.get(0).getEmployeeTypeCode());
    }

    // ── test: more specific higher-priority assignment wins ────────────────

    @Test
    void higherPriorityAssignmentWins() {
        ConceptAssignment global = assignment(RS, "SALARIO_BASE", null, null, null, 0);
        ConceptAssignment specific = assignment(RS, "SALARIO_BASE", "EMP1", "METAL", "INDEFINIDO", 30);

        // repository returns both candidates (wildcard filtering already applied in repo)
        DefaultConceptEligibilityResolver resolver = resolverWith(List.of(global, specific));

        List<ResolvedConceptAssignment> result = resolver.resolve(FULL_CONTEXT, REF);

        assertEquals(1, result.size());
        ResolvedConceptAssignment winner = result.get(0);
        assertEquals("SALARIO_BASE", winner.getConceptCode());
        assertEquals(30, winner.getWinningPriority());
        assertEquals("EMP1", winner.getCompanyCode());
        assertEquals("METAL", winner.getAgreementCode());
        assertEquals("INDEFINIDO", winner.getEmployeeTypeCode());
    }

    // ── test: multiple concepts resolved correctly ─────────────────────────

    @Test
    void multipleConceptsResolved() {
        ConceptAssignment salario = assignment(RS, "SALARIO_BASE", null, null, null, 0);
        ConceptAssignment transporte = assignment(RS, "PLUS_TRANSPORTE", "EMP1", "METAL", null, 20);

        DefaultConceptEligibilityResolver resolver = resolverWith(List.of(salario, transporte));

        List<ResolvedConceptAssignment> result = resolver.resolve(FULL_CONTEXT, REF);

        assertEquals(2, result.size());
        // sorted: priority desc (20 first), then conceptCode asc
        assertEquals("PLUS_TRANSPORTE", result.get(0).getConceptCode());
        assertEquals(20, result.get(0).getWinningPriority());
        assertEquals("SALARIO_BASE", result.get(1).getConceptCode());
        assertEquals(0, result.get(1).getWinningPriority());
    }

    // ── test: deterministic ordering by conceptCode when same priority ─────

    @Test
    void samePriorityDifferentConcepts_sortedByConceptCodeAscending() {
        ConceptAssignment a = assignment(RS, "CONCEPT_Z", null, null, null, 10);
        ConceptAssignment b = assignment(RS, "CONCEPT_A", null, null, null, 10);
        ConceptAssignment c = assignment(RS, "CONCEPT_M", null, null, null, 10);

        DefaultConceptEligibilityResolver resolver = resolverWith(List.of(a, b, c));

        List<ResolvedConceptAssignment> result = resolver.resolve(FULL_CONTEXT, REF);

        assertEquals(3, result.size());
        assertEquals("CONCEPT_A", result.get(0).getConceptCode());
        assertEquals("CONCEPT_M", result.get(1).getConceptCode());
        assertEquals("CONCEPT_Z", result.get(2).getConceptCode());
    }

    // ── test: empty context returns empty list ─────────────────────────────

    @Test
    void noAssignmentsFound_returnsEmptyList() {
        DefaultConceptEligibilityResolver resolver = resolverWith(List.of());

        List<ResolvedConceptAssignment> result = resolver.resolve(FULL_CONTEXT, REF);

        assertEquals(0, result.size());
    }

    // ── test: duplicate winning priority for same concept throws ──────────

    @Test
    void duplicatePriorityForSameConcept_throwsDuplicateConceptAssignmentException() {
        ConceptAssignment a = assignment(RS, "SALARIO_BASE", "EMP1", null, null, 20);
        ConceptAssignment b = assignment(RS, "SALARIO_BASE", null, "METAL", null, 20);

        DefaultConceptEligibilityResolver resolver = resolverWith(List.of(a, b));

        assertThrows(DuplicateConceptAssignmentException.class,
                () -> resolver.resolve(FULL_CONTEXT, REF));
    }

    // ── test: lower priority loses even when more specific ─────────────────

    @Test
    void lowerPriorityAssignmentLosesRegardlessOfSpecificity() {
        ConceptAssignment lowSpecific = assignment(RS, "SALARIO_BASE", "EMP1", "METAL", "INDEFINIDO", 5);
        ConceptAssignment highGlobal = assignment(RS, "SALARIO_BASE", null, null, null, 15);

        DefaultConceptEligibilityResolver resolver = resolverWith(List.of(lowSpecific, highGlobal));

        List<ResolvedConceptAssignment> result = resolver.resolve(FULL_CONTEXT, REF);

        assertEquals(1, result.size());
        assertEquals(15, result.get(0).getWinningPriority());
        assertNull(result.get(0).getCompanyCode());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static ConceptAssignment assignment(
            String rs, String concept, String company, String agreement, String employeeType, int priority
    ) {
        return new ConceptAssignment(
                null, rs, concept, company, agreement, employeeType,
                LocalDate.of(2025, 1, 1), null, priority,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    /**
     * Creates a resolver backed by a simple in-memory fake repository
     * that always returns the given list regardless of context/date.
     */
    private static DefaultConceptEligibilityResolver resolverWith(List<ConceptAssignment> candidates) {
        ConceptAssignmentRepository fakeRepo = new ConceptAssignmentRepository() {
            @Override
            public ConceptAssignment save(ConceptAssignment a) { throw new UnsupportedOperationException(); }

            @Override
            public List<ConceptAssignment> findApplicableAssignments(EmployeeAssignmentContext ctx, LocalDate ref) {
                return candidates;
            }
            @Override
            public List<ConceptAssignment> findAllByRuleSystemCode(String ruleSystemCode) {
                return candidates;
            }
            @Override
            public List<ConceptAssignment> findAllByRuleSystemCodeAndConceptCode(String ruleSystemCode, String conceptCode) {
                return candidates.stream()
                        .filter(a -> conceptCode.equals(a.getConceptCode()))
                        .toList();
            }
            @Override
            public void deleteById(Long id) {
                // no-op for test fake
            }
            @Override
            public boolean existsByIdAndRuleSystemCode(Long id, String ruleSystemCode) {
                return false;
            }
            @Override
            public Optional<ConceptAssignment> findByIdAndRuleSystemCode(Long id, String ruleSystemCode) {
                return Optional.empty();
            }
        };
        return new DefaultConceptEligibilityResolver(fakeRepo);
    }
}
