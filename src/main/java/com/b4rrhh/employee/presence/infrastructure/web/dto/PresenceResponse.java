package com.b4rrhh.employee.presence.infrastructure.web.dto;

import java.time.LocalDate;

public record PresenceResponse(
        Long id,
        Long employeeId,
        Integer presenceNumber,
        String companyCode,
        String entryReasonCode,
        String exitReasonCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
