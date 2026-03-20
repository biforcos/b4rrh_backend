package com.b4rrhh.employee.journey.application.port;

import java.util.Optional;

public interface EmployeeJourneyLookupPort {

    Optional<JourneyEmployeeContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
