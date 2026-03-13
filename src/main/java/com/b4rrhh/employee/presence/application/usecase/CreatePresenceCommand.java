package com.b4rrhh.employee.presence.application.usecase;

import java.time.LocalDate;

public record CreatePresenceCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String companyCode,
        String entryReasonCode,
        String exitReasonCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
