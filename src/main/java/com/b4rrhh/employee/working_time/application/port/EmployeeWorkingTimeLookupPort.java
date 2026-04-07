package com.b4rrhh.employee.working_time.application.port;

import java.util.Optional;

public interface EmployeeWorkingTimeLookupPort {

    Optional<EmployeeWorkingTimeContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeeWorkingTimeContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}