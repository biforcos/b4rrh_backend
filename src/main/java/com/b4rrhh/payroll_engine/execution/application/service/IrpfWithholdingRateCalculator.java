package com.b4rrhh.payroll_engine.execution.application.service;

import com.b4rrhh.payroll_engine.execution.domain.model.TechnicalConceptSegmentData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * {@code ENGINE_PROVIDED} calculator for concept P_IRPF — tipo de retención IRPF.
 *
 * <p>Returns a fixed withholding rate (15.00%). This is a placeholder value until
 * a proper IRPF calculation engine is available. In production, the rate would come
 * from employee tax data and be recalculated annually or on life-event changes.
 */
@Component
public class IrpfWithholdingRateCalculator implements TechnicalConceptCalculator {

    private static final BigDecimal IRPF_RATE = new BigDecimal("15.00");

    @Override
    public String conceptCode() {
        return "P_IRPF";
    }

    @Override
    public BigDecimal resolve(TechnicalConceptSegmentData context) {
        return IRPF_RATE;
    }
}
