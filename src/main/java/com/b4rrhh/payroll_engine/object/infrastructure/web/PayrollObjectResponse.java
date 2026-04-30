package com.b4rrhh.payroll_engine.object.infrastructure.web;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;

public record PayrollObjectResponse(
        String ruleSystemCode,
        String objectCode,
        String objectTypeCode,
        Integer displayOrder,
        boolean active
) {
    static PayrollObjectResponse from(PayrollObject obj) {
        return new PayrollObjectResponse(
                obj.getRuleSystemCode(),
                obj.getObjectCode(),
                obj.getObjectTypeCode().name(),
                null,
                true
        );
    }
}
