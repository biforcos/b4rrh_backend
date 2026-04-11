package com.b4rrhh.payroll.domain.model;

import java.time.LocalDateTime;

public record CalculationRun(
        Long id,
        String ruleSystemCode,
        String payrollPeriodCode,
        String payrollTypeCode,
        String calculationEngineCode,
        String calculationEngineVersion,
        LocalDateTime requestedAt,
        String requestedBy,
        String status,
        String targetSelectionJson,
        Integer totalCandidates,
        Integer totalEligible,
        Integer totalClaimed,
        Integer totalSkippedNotEligible,
        Integer totalSkippedAlreadyClaimed,
        Integer totalCalculated,
        Integer totalNotValid,
        Integer totalErrors,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        String summaryJson,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}