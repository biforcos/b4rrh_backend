package com.b4rrhh.employee.labor_classification.application.port;

import java.util.Optional;

public interface EmployeeLaborClassificationLookupPort {

    Optional<EmployeeLaborClassificationContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeeLaborClassificationContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
