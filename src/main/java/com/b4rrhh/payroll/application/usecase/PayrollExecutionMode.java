package com.b4rrhh.payroll.application.usecase;

/**
 * Controls which execution path the payroll launcher uses per calculation unit.
 *
 * <ul>
 *   <li>{@link #FAKE}: deterministic fake concept materialization (current safe default).</li>
 *   <li>{@link #ELIGIBLE_REAL}: canonical pilot path based on concept-graph resolution.</li>
 *   <li>{@link #MINIMAL_REAL}: legacy mode. Not supported. Will be removed.</li>
 * </ul>
 */
public enum PayrollExecutionMode {
    FAKE,
    ELIGIBLE_REAL,
    /**
     * Legacy mode. Not supported. Will be removed.
     */
    @Deprecated
    MINIMAL_REAL
}
