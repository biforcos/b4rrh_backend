package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;

import java.math.BigDecimal;

/**
 * Strategy interface for {@code ENGINE_PROVIDED} payroll concepts.
 *
 * <p>Implementations are discovered by Spring and registered in
 * {@link DefaultSegmentExecutionEngine} and {@link com.b4rrhh.payroll.application.usecase.CalculatePayrollUnitService}
 * by concept code. Each implementation handles exactly one concept code.
 *
 * <p>Implementations must be idempotent and stateless.
 */
public interface TechnicalConceptCalculator {

    /** The concept code this calculator handles (e.g. {@code "D01"}). */
    String conceptCode();

    /** Resolves the technical value for this concept given the segment context. */
    BigDecimal resolve(TechnicalConceptSegmentData context);
}
