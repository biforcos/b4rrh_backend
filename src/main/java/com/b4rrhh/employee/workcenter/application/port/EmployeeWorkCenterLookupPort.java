package com.b4rrhh.employee.workcenter.application.port;

import java.util.Optional;

public interface EmployeeWorkCenterLookupPort {

    Optional<EmployeeWorkCenterContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeeWorkCenterContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}