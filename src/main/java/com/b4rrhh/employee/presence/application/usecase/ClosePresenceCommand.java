package com.b4rrhh.employee.presence.application.usecase;

import java.time.LocalDate;

public record ClosePresenceCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        Integer presenceNumber,
        LocalDate endDate,
        String exitReasonCode
) {
}
