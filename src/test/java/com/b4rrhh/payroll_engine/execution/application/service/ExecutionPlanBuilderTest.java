package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.ExecutionScope;
import com.b4rrhh.payroll_engine.concept.domain.model.FunctionalNature;
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import com.b4rrhh.payroll_engine.concept.domain.model.ResultCompositionMode;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptOperandRepository;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraph;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptDependencyGraphBuilder;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateConceptIdentityException;
import com.b4rrhh.payroll_engine.execution.domain.exception.DuplicateOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingAggregateSourcesException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingConceptDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.MissingOperandDefinitionException;
import com.b4rrhh.payroll_engine.execution.domain.exception.OperandGraphMismatchException;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private final DefaultExecutionPlanBuilder builder =
            new DefaultExecutionPlanBuilder(pocOperandRepo(), new RateByQuantityConfigurationValidator());

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns a stub operand repository seeded with the PoC SALARIO_BASE operand definitions:
     * QUANTITY=T_DIAS_PRESENCIA_SEGMENTO, RATE=T_PRECIO_DIA.
     */
    private static PayrollConceptOperandRepository pocOperandRepo() {
        PayrollObject targetObj = new PayrollObject(3L, RS, PayrollObjectTypeCode.CONCEPT,
                "SALARIO_BASE", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject qObj = new PayrollObject(1L, RS, PayrollObjectTypeCode.CONCEPT,
                "T_DIAS_PRESENCIA_SEGMENTO", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject rObj = new PayrollObject(2L, RS, PayrollObjectTypeCode.CONCEPT,
                "T_PRECIO_DIA", LocalDateTime.now(), LocalDateTime.now());

        List<PayrollConceptOperand> salarioBaseOperands = List.of(
                new PayrollConceptOperand(null, targetObj, OperandRole.QUANTITY, qObj,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, targetObj, OperandRole.RATE, rObj,
                        LocalDateTime.now(), LocalDateTime.now())
        );

        return new PayrollConceptOperandRepository() {
            @Override
            public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
            @Override
            public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                return "SALARIO_BASE".equals(code) ? salarioBaseOperands : Collections.emptyList();
            }
        };
    }

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

    // ── operand enrichment ────────────────────────────────────────────────────

    @Test
    void salarioBasePlanEntryContainsQuantityOperand() {
        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        List<ConceptExecutionPlanEntry> plan =
                builder.build(pocGraph(dias, precio, salario), List.of(dias, precio, salario));

        ConceptExecutionPlanEntry salarioEntry = plan.stream()
                .filter(e -> e.identity().equals(id("SALARIO_BASE")))
                .findFirst().orElseThrow();

        assertEquals(id("T_DIAS_PRESENCIA_SEGMENTO"), salarioEntry.operands().get(OperandRole.QUANTITY));
    }

    @Test
    void salarioBasePlanEntryContainsRateOperand() {
        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        List<ConceptExecutionPlanEntry> plan =
                builder.build(pocGraph(dias, precio, salario), List.of(dias, precio, salario));

        ConceptExecutionPlanEntry salarioEntry = plan.stream()
                .filter(e -> e.identity().equals(id("SALARIO_BASE")))
                .findFirst().orElseThrow();

        assertEquals(id("T_PRECIO_DIA"), salarioEntry.operands().get(OperandRole.RATE));
    }

    @Test
    void missingOperandDefinitionDuringPlanBuildThrows() {
        // Repo returns only RATE — QUANTITY is missing → MissingOperandDefinitionException
        PayrollObject targetObj = new PayrollObject(3L, RS, PayrollObjectTypeCode.CONCEPT,
                "SALARIO_BASE", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject rObj = new PayrollObject(2L, RS, PayrollObjectTypeCode.CONCEPT,
                "T_PRECIO_DIA", LocalDateTime.now(), LocalDateTime.now());
        List<PayrollConceptOperand> rateOnly = List.of(
                new PayrollConceptOperand(null, targetObj, OperandRole.RATE, rObj,
                        LocalDateTime.now(), LocalDateTime.now()));

        DefaultExecutionPlanBuilder missingQuantityBuilder = new DefaultExecutionPlanBuilder(
                new PayrollConceptOperandRepository() {
                    @Override
                    public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
                    @Override
                    public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                        return "SALARIO_BASE".equals(code) ? rateOnly : Collections.emptyList();
                    }
                },
                new RateByQuantityConfigurationValidator());

        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        assertThrows(MissingOperandDefinitionException.class,
                () -> missingQuantityBuilder.build(pocGraph(dias, precio, salario), List.of(dias, precio, salario)));
    }

    @Test
    void duplicateOperandDefinitionDuringPlanBuildThrows() {
        // Repo returns two QUANTITY operands → DuplicateOperandDefinitionException
        PayrollObject targetObj = new PayrollObject(3L, RS, PayrollObjectTypeCode.CONCEPT,
                "SALARIO_BASE", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject q1 = new PayrollObject(1L, RS, PayrollObjectTypeCode.CONCEPT,
                "T_DIAS_PRESENCIA_SEGMENTO", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject q2 = new PayrollObject(4L, RS, PayrollObjectTypeCode.CONCEPT,
                "T_OTHER_QUANTITY", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject rObj = new PayrollObject(2L, RS, PayrollObjectTypeCode.CONCEPT,
                "T_PRECIO_DIA", LocalDateTime.now(), LocalDateTime.now());
        List<PayrollConceptOperand> duplicateQuantity = List.of(
                new PayrollConceptOperand(null, targetObj, OperandRole.QUANTITY, q1,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, targetObj, OperandRole.QUANTITY, q2,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, targetObj, OperandRole.RATE, rObj,
                        LocalDateTime.now(), LocalDateTime.now()));

        DefaultExecutionPlanBuilder duplicateBuilder = new DefaultExecutionPlanBuilder(
                new PayrollConceptOperandRepository() {
                    @Override
                    public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
                    @Override
                    public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                        return "SALARIO_BASE".equals(code) ? duplicateQuantity : Collections.emptyList();
                    }
                },
                new RateByQuantityConfigurationValidator());

        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        // Build a graph where ALL THREE sources are declared deps of SALARIO_BASE
        // so the validator does not complain about mismatch before findSingle checks for duplicates
        PayrollConcept otherQuantityConcept = concept("T_OTHER_QUANTITY", CalculationType.DIRECT_AMOUNT);
        ConceptDependencyGraph graphWithExtra = new ConceptDependencyGraphBuilder()
                .addOperandDependency(salario, dias)
                .addOperandDependency(salario, otherQuantityConcept)
                .addOperandDependency(salario, precio)
                .build();

        assertThrows(DuplicateOperandDefinitionException.class,
                () -> duplicateBuilder.build(graphWithExtra, List.of(dias, precio,
                        otherQuantityConcept, salario)));
    }

    @Test
    void graphMismatchDuringPlanBuildThrows() {
        // Repo returns a QUANTITY source (T_OTHER) that is NOT in the graph deps → OperandGraphMismatchException
        PayrollObject targetObj = new PayrollObject(3L, RS, PayrollObjectTypeCode.CONCEPT,
                "SALARIO_BASE", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject qObj = new PayrollObject(5L, RS, PayrollObjectTypeCode.CONCEPT,
                "T_OTHER", LocalDateTime.now(), LocalDateTime.now());
        PayrollObject rObj = new PayrollObject(2L, RS, PayrollObjectTypeCode.CONCEPT,
                "T_PRECIO_DIA", LocalDateTime.now(), LocalDateTime.now());
        List<PayrollConceptOperand> mismatchedOperands = List.of(
                new PayrollConceptOperand(null, targetObj, OperandRole.QUANTITY, qObj,
                        LocalDateTime.now(), LocalDateTime.now()),
                new PayrollConceptOperand(null, targetObj, OperandRole.RATE, rObj,
                        LocalDateTime.now(), LocalDateTime.now()));

        DefaultExecutionPlanBuilder mismatchBuilder = new DefaultExecutionPlanBuilder(
                new PayrollConceptOperandRepository() {
                    @Override
                    public PayrollConceptOperand save(PayrollConceptOperand o) { throw new UnsupportedOperationException(); }
                    @Override
                    public List<PayrollConceptOperand> findByTarget(String rs, String code) {
                        return "SALARIO_BASE".equals(code) ? mismatchedOperands : Collections.emptyList();
                    }
                },
                new RateByQuantityConfigurationValidator());

        PayrollConcept dias    = concept("T_DIAS_PRESENCIA_SEGMENTO", CalculationType.DIRECT_AMOUNT);
        PayrollConcept precio  = concept("T_PRECIO_DIA",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept salario = concept("SALARIO_BASE",              CalculationType.RATE_BY_QUANTITY);

        // Graph has dias+precio as deps — T_OTHER is NOT a declared dep
        assertThrows(OperandGraphMismatchException.class,
                () -> mismatchBuilder.build(pocGraph(dias, precio, salario), List.of(dias, precio, salario)));
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

    // ── aggregate enrichment ──────────────────────────────────────────────────

    /**
     * Builds a 3-node graph: TOTAL_DEVENGOS_SEGMENTO (AGGREGATE) depends on
     * SALARIO_BASE and PLUS_TRANSPORTE. Uses operand-dependency edges because
     * {@link ConceptDependencyGraphBuilder#addOperandDependency} and feed-dependency
     * edges produce the same structural result in {@code getDependenciesOf}.
     */
    private static ConceptDependencyGraph aggregateGraph(
            PayrollConcept salarioBase,
            PayrollConcept plusTransporte,
            PayrollConcept totalDevengos
    ) {
        return new ConceptDependencyGraphBuilder()
                .addNode(salarioBase)
                .addNode(plusTransporte)
                .addOperandDependency(totalDevengos, salarioBase)
                .addOperandDependency(totalDevengos, plusTransporte)
                .build();
    }

    @Test
    void aggregatePlanEntryContainsBothSources() {
        PayrollConcept salario  = concept("SALARIO_BASE",              CalculationType.DIRECT_AMOUNT);
        PayrollConcept plus     = concept("PLUS_TRANSPORTE",           CalculationType.DIRECT_AMOUNT);
        PayrollConcept total    = concept("TOTAL_DEVENGOS_SEGMENTO",   CalculationType.AGGREGATE);

        ConceptDependencyGraph graph = aggregateGraph(salario, plus, total);
        List<ConceptExecutionPlanEntry> plan = builder.build(graph, List.of(salario, plus, total));

        ConceptExecutionPlanEntry totalEntry = plan.stream()
                .filter(e -> e.identity().equals(id("TOTAL_DEVENGOS_SEGMENTO")))
                .findFirst().orElseThrow();

        assertEquals(CalculationType.AGGREGATE, totalEntry.calculationType());
        org.junit.jupiter.api.Assertions.assertTrue(
                totalEntry.aggregateSources().contains(id("SALARIO_BASE")),
                "aggregateSources must include SALARIO_BASE");
        org.junit.jupiter.api.Assertions.assertTrue(
                totalEntry.aggregateSources().contains(id("PLUS_TRANSPORTE")),
                "aggregateSources must include PLUS_TRANSPORTE");
        assertEquals(2, totalEntry.aggregateSources().size(),
                "aggregateSources must contain exactly the two declared sources");
    }

    @Test
    void aggregatePlanEntryAppearsAfterItsSources() {
        PayrollConcept salario  = concept("SALARIO_BASE",            CalculationType.DIRECT_AMOUNT);
        PayrollConcept plus     = concept("PLUS_TRANSPORTE",         CalculationType.DIRECT_AMOUNT);
        PayrollConcept total    = concept("TOTAL_DEVENGOS_SEGMENTO", CalculationType.AGGREGATE);

        ConceptDependencyGraph graph = aggregateGraph(salario, plus, total);
        List<ConceptExecutionPlanEntry> plan = builder.build(graph, List.of(total, plus, salario));

        int totalIdx   = indexOf(plan, "TOTAL_DEVENGOS_SEGMENTO");
        int salarioIdx = indexOf(plan, "SALARIO_BASE");
        int plusIdx    = indexOf(plan, "PLUS_TRANSPORTE");

        org.junit.jupiter.api.Assertions.assertTrue(salarioIdx < totalIdx,
                "SALARIO_BASE must appear before TOTAL_DEVENGOS_SEGMENTO");
        org.junit.jupiter.api.Assertions.assertTrue(plusIdx < totalIdx,
                "PLUS_TRANSPORTE must appear before TOTAL_DEVENGOS_SEGMENTO");
    }

    @Test
    void missingAggregateSourcesThrowsMissingAggregateSourcesException() {
        // TOTAL node is in the graph but has no declared dependencies.
        PayrollConcept total = concept("TOTAL_ISOLATED", CalculationType.AGGREGATE);
        ConceptDependencyGraph graph = new ConceptDependencyGraphBuilder()
                .addNode(total)
                .build();

        assertThrows(MissingAggregateSourcesException.class,
                () -> builder.build(graph, List.of(total)));
    }
}
