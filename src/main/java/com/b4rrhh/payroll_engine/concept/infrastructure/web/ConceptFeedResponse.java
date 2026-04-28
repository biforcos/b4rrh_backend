package com.b4rrhh.payroll_engine.concept.infrastructure.web;

import java.time.LocalDate;

/**
 * Wire representation of a single feed relation pointing into the target concept.
 *
 * <p>Each item maps to a row in {@code payroll_concept_feed_relation}: the
 * {@code sourceObjectCode} identifies the upstream object (CONCEPT, TABLE or CONSTANT)
 * whose value is fed into the target. The boolean {@code invertSign} flips the
 * contribution at execution time, while {@code effectiveFrom} / {@code effectiveTo}
 * define the temporal validity window.
 */
public record ConceptFeedResponse(
        String sourceObjectCode,
        boolean invertSign,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
