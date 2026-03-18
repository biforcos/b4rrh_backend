package com.b4rrhh.employee.cost_center.application.port;

import java.util.Optional;

public interface EmployeeCostCenterLookupPort {

    Optional<EmployeeCostCenterContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeeCostCenterContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
