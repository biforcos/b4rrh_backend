package com.b4rrhh.employee.identifier.application.port;

import java.util.Optional;

public interface EmployeeIdentifierLookupPort {

    Optional<EmployeeIdentifierContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeeIdentifierContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
