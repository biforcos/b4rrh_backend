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

        public CalculationRun withStatus(String newStatus) {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                newStatus,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid,
                                totalErrors,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun withStartedAt(LocalDateTime newStartedAt) {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid,
                                totalErrors,
                                newStartedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun withFinishedExecution(String newStatus, LocalDateTime newFinishedAt, String newSummaryJson) {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                newStatus,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid,
                                totalErrors,
                                startedAt,
                                newFinishedAt,
                                newSummaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun withTotalCandidates(int newTotalCandidates) {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                newTotalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid,
                                totalErrors,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun incrementTotalEligible() {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible + 1,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid,
                                totalErrors,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun incrementTotalClaimed() {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed + 1,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid,
                                totalErrors,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun incrementTotalSkippedNotEligible() {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible + 1,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid,
                                totalErrors,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun incrementTotalSkippedAlreadyClaimed() {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed + 1,
                                totalCalculated,
                                totalNotValid,
                                totalErrors,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun incrementTotalCalculated() {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated + 1,
                                totalNotValid,
                                totalErrors,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun incrementTotalNotValid() {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid + 1,
                                totalErrors,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }

        public CalculationRun incrementTotalErrors() {
                return new CalculationRun(
                                id,
                                ruleSystemCode,
                                payrollPeriodCode,
                                payrollTypeCode,
                                calculationEngineCode,
                                calculationEngineVersion,
                                requestedAt,
                                requestedBy,
                                status,
                                targetSelectionJson,
                                totalCandidates,
                                totalEligible,
                                totalClaimed,
                                totalSkippedNotEligible,
                                totalSkippedAlreadyClaimed,
                                totalCalculated,
                                totalNotValid,
                                totalErrors + 1,
                                startedAt,
                                finishedAt,
                                summaryJson,
                                createdAt,
                                updatedAt
                );
        }
}