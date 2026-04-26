package com.b4rrhh.payroll.application.port;

import java.time.LocalDate;
import java.util.Optional;

public interface EmployeePersonalDataLookupPort {
    Optional<EmployeePersonalDataContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate referenceDate
    );
}
