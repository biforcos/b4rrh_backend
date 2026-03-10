package com.b4rrhh.employee.presence.application.usecase;

import java.time.LocalDate;

public record CreatePresenceCommand(
        Long employeeId,
        String companyCode,
        String entryReasonCode,
        String exitReasonCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
