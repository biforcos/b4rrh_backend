package com.b4rrhh.payroll.application.usecase;

/**
 * Controls which execution path the payroll launcher uses per calculation unit.
 *
 * <ul>
 *   <li>{@link #FAKE}: deterministic fake concept materialization (current safe default).</li>
 *   <li>{@link #ELIGIBLE_REAL}: delegates to payroll_engine eligible execution flow.</li>
 *   <li>{@link #MINIMAL_REAL}: calculates BASE_SALARY and PLUS_CONVENIO concepts directly (minimal real plan).</li>
 * </ul>
 */
public enum PayrollExecutionMode {
    FAKE,
    ELIGIBLE_REAL,
    MINIMAL_REAL
}
