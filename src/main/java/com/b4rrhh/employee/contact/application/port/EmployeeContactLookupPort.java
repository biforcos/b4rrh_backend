package com.b4rrhh.employee.contact.application.port;

import java.util.Optional;

public interface EmployeeContactLookupPort {

    Optional<EmployeeContactContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeeContactContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
