package com.b4rrhh.employee.address.application.port;

import java.util.Optional;

public interface EmployeeAddressLookupPort {

    Optional<EmployeeAddressContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeeAddressContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
