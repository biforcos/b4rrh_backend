package com.b4rrhh.payroll.application.port;

import java.time.LocalDate;
import java.util.List;

public interface PayrollLaunchPresenceLookupPort {

    List<PayrollLaunchEmployeeContext> findEmployeesWithPresenceInPeriod(
            String ruleSystemCode,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    List<PayrollLaunchPresenceContext> findRelevantPresences(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}