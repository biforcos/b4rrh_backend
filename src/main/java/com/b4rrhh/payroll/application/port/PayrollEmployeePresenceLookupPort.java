package com.b4rrhh.payroll.application.port;

import java.util.Optional;

public interface PayrollEmployeePresenceLookupPort {

    Optional<PayrollEmployeePresenceContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber
    );
}