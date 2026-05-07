package com.b4rrhh.employee.tax_information.application.port;

import java.util.Optional;

public interface EmployeeForTaxInfoLookupPort {
    Optional<Long> findEmployeeId(String ruleSystemCode, String employeeTypeCode, String employeeNumber);
}
