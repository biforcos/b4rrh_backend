package com.b4rrhh.payroll.application.port;

import java.time.LocalDate;
import java.util.Optional;

public interface PayrollLaunchEligibleInputLookupPort {

    Optional<PayrollLaunchEligibleInputContext> findByUnitAndPeriod(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}
