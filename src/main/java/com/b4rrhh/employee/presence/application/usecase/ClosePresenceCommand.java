package com.b4rrhh.employee.presence.application.usecase;

import java.time.LocalDate;

public record ClosePresenceCommand(
        Long employeeId,
        Long presenceId,
        LocalDate endDate,
        String exitReasonCode
) {
}
