package com.b4rrhh.payroll.application.port;

import java.time.LocalDate;

public interface EmployeeTaxInfoPayrollLookupPort {
    EmployeeTaxInfoContext findLatestOnOrBefore(
        String ruleSystemCode, String employeeTypeCode, String employeeNumber, LocalDate referenceDate);
}
