package com.b4rrhh.payroll.application.port;

import java.math.BigDecimal;
import java.util.Map;

public interface EmployeePayrollInputLookupPort {

    Map<String, BigDecimal> findInputsByPeriod(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            int period
    );
}
