package com.b4rrhh.payroll_engine.object.domain.port;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;

import java.util.Optional;

public interface PayrollObjectRepository {

    PayrollObject save(PayrollObject payrollObject);

    Optional<PayrollObject> findByBusinessKey(
            String ruleSystemCode,
            PayrollObjectTypeCode objectTypeCode,
            String objectCode
    );

    boolean existsByBusinessKey(
            String ruleSystemCode,
            PayrollObjectTypeCode objectTypeCode,
            String objectCode
    );
}
