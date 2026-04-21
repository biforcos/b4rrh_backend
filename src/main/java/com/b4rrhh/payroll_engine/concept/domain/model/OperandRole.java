package com.b4rrhh.payroll_engine.concept.domain.model;

/**
 * Defines the semantic role of an operand within a multi-operand execution.
 *
 * <ul>
 *   <li>{@code QUANTITY} and {@code RATE} — used by {@code RATE_BY_QUANTITY} concepts.</li>
 *   <li>{@code BASE} and {@code PERCENTAGE} — used by {@code PERCENTAGE} concepts.</li>
 * </ul>
 */
public enum OperandRole {
    QUANTITY,
    RATE,
    BASE,
    PERCENTAGE
}
