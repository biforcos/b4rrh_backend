package com.b4rrhh.employee.presence.application.port;

import java.util.Optional;

public interface EmployeePresenceLookupPort {

    Optional<EmployeePresenceContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeePresenceContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
