package com.b4rrhh.payroll.application.usecase;

final class CalculationRunStatuses {

    static final String REQUESTED = "REQUESTED";
    static final String RUNNING = "RUNNING";
    static final String COMPLETED = "COMPLETED";
    static final String COMPLETED_WITH_ERRORS = "COMPLETED_WITH_ERRORS";
    static final String FAILED = "FAILED";

    private CalculationRunStatuses() {
    }
}