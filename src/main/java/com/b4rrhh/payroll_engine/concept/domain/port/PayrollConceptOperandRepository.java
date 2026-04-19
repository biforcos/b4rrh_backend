package com.b4rrhh.payroll_engine.concept.domain.port;

import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;

import java.util.List;

public interface PayrollConceptOperandRepository {

    PayrollConceptOperand save(PayrollConceptOperand operand);

    /**
     * Returns all operand definitions where the target concept matches the given business key.
     */
    List<PayrollConceptOperand> findByTarget(String ruleSystemCode, String conceptCode);
}
