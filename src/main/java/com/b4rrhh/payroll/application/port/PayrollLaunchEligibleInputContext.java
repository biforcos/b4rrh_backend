package com.b4rrhh.payroll.application.port;

import java.util.List;

public record PayrollLaunchEligibleInputContext(
        String companyCode,
        String agreementCode,
        String agreementCategoryCode,
        List<PayrollLaunchWorkingTimeWindowContext> workingTimeWindows
) {
}
