package com.b4rrhh.payroll_engine.dependency.domain.model;

import com.b4rrhh.payroll_engine.dependency.domain.exception.ConceptDependencyCycleException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable directed dependency graph of PayrollConcepts.
 *
 * <p><strong>Edge direction:</strong> an edge {@code A → B} means:
 * <em>A depends on B</em>, therefore <em>B must be calculated before A</em>.
 * In the topological order returned by {@link #topologicalOrder()}, B will appear
 * at a lower index than A.
 *
 * <p>Nodes are identified by {@link ConceptNodeIdentity} (ruleSystemCode + conceptCode).
 * The graph is structural only — it carries no runtime execution logic.
 *
 * <p>Use {@link ConceptDependencyGraphBuilder} to construct instances.
 * Cycle detection is performed eagerly during construction.
 */
public final class ConceptDependencyGraph {

    private final Set<ConceptNodeIdentity> nodes;
    // adjacency: node → set of its direct dependencies (nodes that must be calculated before it)
    // i.e. adjacency.get(A) contains B means there is an edge A → B
    private final Map<ConceptNodeIdentity, Set<ConceptNodeIdentity>> adjacency;
    private final List<ConceptDependency> edges;

    ConceptDependencyGraph(
            Set<ConceptNodeIdentity> nodes,
            List<ConceptDependency> edges
    ) {
        this.nodes = Collections.unmodifiableSet(new LinkedHashSet<>(nodes));
        this.edges = Collections.unmodifiableList(new ArrayList<>(edges));

        Map<ConceptNodeIdentity, Set<ConceptNodeIdentity>> adj = new HashMap<>();
        for (ConceptNodeIdentity node : nodes) {
            adj.put(node, new LinkedHashSet<>());
        }
        for (ConceptDependency edge : edges) {
            adj.computeIfAbsent(edge.getDependent(), k -> new LinkedHashSet<>())
               .add(edge.getDependency());
            // ensure dependency node is also in the map
            adj.computeIfAbsent(edge.getDependency(), k -> new LinkedHashSet<>());
        }
        this.adjacency = Collections.unmodifiableMap(adj);
    }

    public Set<ConceptNodeIdentity> getNodes() {
        return nodes;
    }

    public List<ConceptDependency> getEdges() {
        return edges;
    }

    public Set<ConceptNodeIdentity> getDependenciesOf(ConceptNodeIdentity node) {
        return Collections.unmodifiableSet(
                adjacency.getOrDefault(node, Collections.emptySet())
        );
    }

    /**
     * Returns a topological ordering of all nodes such that each dependency appears
     * before all concepts that depend on it.
     *
     * @throws ConceptDependencyCycleException if the graph contains a cycle
     */
    public List<ConceptNodeIdentity> topologicalOrder() {
        Set<ConceptNodeIdentity> allNodes = new LinkedHashSet<>(nodes);
        adjacency.keySet().forEach(allNodes::add);

        Map<ConceptNodeIdentity, Mark> marks = new HashMap<>();
        Deque<ConceptNodeIdentity> result = new ArrayDeque<>();

        for (ConceptNodeIdentity node : allNodes) {
            if (!marks.containsKey(node)) {
                visit(node, marks, result, new ArrayDeque<>());
            }
        }

        return new ArrayList<>(result);
    }

    private void visit(
            ConceptNodeIdentity node,
            Map<ConceptNodeIdentity, Mark> marks,
            Deque<ConceptNodeIdentity> result,
            Deque<ConceptNodeIdentity> stack
    ) {
        Mark mark = marks.get(node);

        if (mark == Mark.PERMANENT) {
            return;
        }

        if (mark == Mark.TEMPORARY) {
            // cycle detected: reconstruct cycle path
            List<ConceptNodeIdentity> cycle = new ArrayList<>();
            boolean found = false;
            for (ConceptNodeIdentity n : stack) {
                if (n.equals(node)) {
                    found = true;
                }
                if (found) {
                    cycle.add(n);
                }
            }
            cycle.add(node);
            Collections.reverse(cycle);
            throw new ConceptDependencyCycleException(cycle);
        }

        marks.put(node, Mark.TEMPORARY);
        stack.push(node);

        Set<ConceptNodeIdentity> dependencies = adjacency.getOrDefault(node, Collections.emptySet());
        for (ConceptNodeIdentity dep : dependencies) {
            visit(dep, marks, result, stack);
        }

        stack.pop();
        marks.put(node, Mark.PERMANENT);
        result.addLast(node);
    }

    private enum Mark {
        TEMPORARY,
        PERMANENT
    }
}
