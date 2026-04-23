package com.b4rrhh.payroll.basesalary.domain;

import java.util.Optional;

/**
 * Output port to resolve an employee id from business key.
 */
public interface EmployeeByBusinessKeyLookupPort {

    Optional<Long> resolveEmployeeId(String ruleSystemCode, String employeeTypeCode, String employeeNumber);
}
