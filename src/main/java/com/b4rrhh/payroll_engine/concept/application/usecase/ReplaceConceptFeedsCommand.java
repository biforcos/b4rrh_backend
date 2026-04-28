package com.b4rrhh.payroll_engine.concept.application.usecase;

import java.time.LocalDate;
import java.util.List;

/**
 * Replaces the entire feed-relation list pointing into a concept identified by
 * ({@code ruleSystemCode}, {@code conceptCode}). An empty {@code items} list clears
 * every feed.
 *
 * <p>Each item declares the upstream object (a CONCEPT, TABLE or CONSTANT identified by
 * its object code in the same rule system), whether the contribution must be sign-inverted
 * at execution time, and the temporal validity window.
 */
public record ReplaceConceptFeedsCommand(
        String ruleSystemCode,
        String conceptCode,
        List<Item> items
) {

    public record Item(
            String sourceObjectCode,
            boolean invertSign,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
    }
}
