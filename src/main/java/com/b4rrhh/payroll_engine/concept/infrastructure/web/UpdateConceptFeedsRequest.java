package com.b4rrhh.payroll_engine.concept.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Request body for the PUT feeds endpoint. Replaces the full set of feed relations whose
 * target is the given concept; an empty list removes every feed.
 */
public record UpdateConceptFeedsRequest(
        @NotNull
        @Valid
        List<FeedItem> feeds
) {

    public record FeedItem(
            @NotBlank String sourceObjectCode,
            @NotNull Boolean invertSign,
            @NotNull LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
    }
}
