package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.ExecutionScope;
import com.b4rrhh.payroll_engine.concept.domain.model.FunctionalNature;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.ResultCompositionMode;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraphBuilder;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateConceptIdentityException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DefaultExecutionPlanBuilder}.
 *
 * <p>Reference scenario mirrors the PoC:
 * T_DIAS_PRESENCIA_SEGMENTO (DIRECT_AMOUNT) and T_PRECIO_DIA (DIRECT_AMOUNT) must
 * precede SALARIO_BASE (RATE_BY_QUANTITY) in topological order.
 */
class ExecutionPlanBuilderTest {

    private static final String RS = "ESP";

    private final DefaultExecutionPlanBuilder builder = new DefaultExecutionPlanBuilder();

    // ── helpers ───────────────────────────────────────────────────────────────

    private static PayrollConcept concept(String code, CalculationType type) {
        PayrollObject object = new PayrollObject(null, RS, PayrollObjectTypeCode.CONCEPT, code, null, null);
        return new PayrollConcept(
                object, code,
                type,
                FunctionalNature.INFORMATIONAL,
                ResultCompositionMode.REPLACE,
                null,
                ExecutionScope.SEGMENT,
                null, null
        );
    }

    private static ConceptNodeIdentity id(String code) {
        return new ConceptNodeIdentity(RS, code);
    }

    /**
     * Builds the PoC graph: SALARIO_BASE depends on T_DIAS_PRESENCIA_SEGMENTO and T_PRECIO_DIA.
     */
    private static ConceptDependencyGraph pocGraph(
            PayrollConcept diasPresencia,
            PayrollConcept precioDia,
            PayrollConcept salarioBase
    ) {
        return new ConceptDependencyGraphBuilder()
                .addNode(diasPresencia)
                .addNode(precioDia)
                .addOperandDependency(salarioBase, diasPresencia)
                .addOperandDependency(salarioBase, precioDia)
                .build();
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void planSizeMatchesGraphNodeCount() {
        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        List<ConceptExecutionPlanEntry> plan =
                builder.build(pocGraph(dias, precio, salario), List.of(dias, precio, salario));

        assertEquals(3, plan.size());
    }

    @Test
    void salarioBaseAppearLastInTopologicalOrder() {
        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        List<ConceptExecutionPlanEntry> plan =
                builder.build(pocGraph(dias, precio, salario), List.of(dias, precio, salario));

        assertEquals(id("SALARIO_BASE"), plan.get(plan.size() - 1).identity());
    }

    @Test
    void technicalConceptsAppearBeforeSalarioBase() {
        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        List<ConceptExecutionPlanEntry> plan =
                builder.build(pocGraph(dias, precio, salario), List.of(dias, precio, salario));

        int salarioIdx = indexOf(plan, "SALARIO_BASE");
        int diasIdx    = indexOf(plan, "T_DIAS_PRESENCIA_SEGMENTO");
        int precioIdx  = indexOf(plan, "T_PRECIO_DIA");

        // Both dependencies must appear before SALARIO_BASE.
        org.junit.jupiter.api.Assertions.assertTrue(diasIdx < salarioIdx,
                "T_DIAS_PRESENCIA_SEGMENTO must appear before SALARIO_BASE");
        org.junit.jupiter.api.Assertions.assertTrue(precioIdx < salarioIdx,
                "T_PRECIO_DIA must appear before SALARIO_BASE");
    }

    @Test
    void calculationTypeIsSourcedFromPayrollConcept() {
        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        List<ConceptExecutionPlanEntry> plan =
                builder.build(pocGraph(dias, precio, salario), List.of(dias, precio, salario));

        ConceptExecutionPlanEntry salarioEntry = plan.stream()
                .filter(e -> e.identity().equals(id("SALARIO_BASE")))
                .findFirst().orElseThrow();
        ConceptExecutionPlanEntry diasEntry = plan.stream()
                .filter(e -> e.identity().equals(id("T_DIAS_PRESENCIA_SEGMENTO")))
                .findFirst().orElseThrow();

        assertEquals(CalculationType.RATE_BY_QUANTITY, salarioEntry.calculationType());
        assertEquals(CalculationType.DIRECT_AMOUNT, diasEntry.calculationType());
    }

    @Test
    void singleNodeGraphWithNoDependenciesProducesOnePlanEntry() {
        PayrollConcept solo = concept("STANDALONE", CalculationType.DIRECT_AMOUNT);
        ConceptDependencyGraph graph = new ConceptDependencyGraphBuilder()
                .addNode(solo)
                .build();

        List<ConceptExecutionPlanEntry> plan = builder.build(graph, List.of(solo));

        assertEquals(1, plan.size());
        assertEquals(id("STANDALONE"), plan.get(0).identity());
        assertEquals(CalculationType.DIRECT_AMOUNT, plan.get(0).calculationType());
    }

    @Test
    void conceptsOutsideGraphAreNotIncludedInPlan() {
        // Only one concept is in the graph; the second is supplied in the list but not in the graph.
        PayrollConcept inGraph  = concept("IN_GRAPH",  CalculationType.DIRECT_AMOUNT);
        PayrollConcept outGraph = concept("OUT_GRAPH", CalculationType.DIRECT_AMOUNT);

        ConceptDependencyGraph graph = new ConceptDependencyGraphBuilder()
                .addNode(inGraph)
                .build();

        List<ConceptExecutionPlanEntry> plan = builder.build(graph, List.of(inGraph, outGraph));

        assertEquals(1, plan.size());
        assertEquals(id("IN_GRAPH"), plan.get(0).identity());
    }

    @Test
    void planOrderFollowsTopologicalOrderNotConceptListOrder() {
        // Concept list is supplied in reverse dependency order: SALARIO_BASE first, then its dependencies.
        // The execution plan must still produce dependencies before SALARIO_BASE.
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);
        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);

        // Graph encodes: SALARIO_BASE depends on the two technical concepts.
        ConceptDependencyGraph graph = pocGraph(dias, precio, salario);

        // Concept list order is intentionally reversed from the expected execution order.
        List<ConceptExecutionPlanEntry> plan = builder.build(graph, List.of(salario, precio, dias));

        int salarioIdx = indexOf(plan, "SALARIO_BASE");
        int diasIdx    = indexOf(plan, "T_DIAS_PRESENCIA_SEGMENTO");
        int precioIdx  = indexOf(plan, "T_PRECIO_DIA");

        org.junit.jupiter.api.Assertions.assertTrue(diasIdx < salarioIdx,
                "T_DIAS_PRESENCIA_SEGMENTO must appear before SALARIO_BASE regardless of concept list order");
        org.junit.jupiter.api.Assertions.assertTrue(precioIdx < salarioIdx,
                "T_PRECIO_DIA must appear before SALARIO_BASE regardless of concept list order");
    }

    // ── error cases ───────────────────────────────────────────────────────────

    @Test
    void nullGraphIsRejected() {
        PayrollConcept dias = concept("T_DIAS", CalculationType.DIRECT_AMOUNT);
        assertThrows(IllegalArgumentException.class, () -> builder.build(null, List.of(dias)));
    }

    @Test
    void nullConceptListIsRejected() {
        PayrollConcept dias = concept("T_DIAS", CalculationType.DIRECT_AMOUNT);
        ConceptDependencyGraph graph = new ConceptDependencyGraphBuilder().addNode(dias).build();
        assertThrows(IllegalArgumentException.class, () -> builder.build(graph, null));
    }

    @Test
    void missingConceptDefinitionForGraphNodeThrows() {
        // Graph has SALARIO_BASE but concept list does not include it.
        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        ConceptDependencyGraph graph = pocGraph(dias, precio, salario);

        // Supply only 2 of the 3 concepts — SALARIO_BASE is missing from the list.
        assertThrows(MissingConceptDefinitionException.class,
                () -> builder.build(graph, List.of(dias, precio)));
    }

    @Test
    void duplicateConceptIdentityInListThrows() {
        PayrollConcept dias     = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept diasDupe = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario  = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        ConceptDependencyGraph graph = new ConceptDependencyGraphBuilder()
                .addNode(dias)
                .addNode(salario)
                .build();

        // Same identity appears twice in the list.
        assertThrows(DuplicateConceptIdentityException.class,
                () -> builder.build(graph, List.of(dias, diasDupe, salario)));
    }

    // ── utility ───────────────────────────────────────────────────────────────

    private int indexOf(List<ConceptExecutionPlanEntry> plan, String conceptCode) {
        for (int i = 0; i < plan.size(); i++) {
            if (plan.get(i).identity().getConceptCode().equals(conceptCode)) {
                return i;
            }
        }
        throw new AssertionError("Concept not found in plan: " + conceptCode);
    }
}
