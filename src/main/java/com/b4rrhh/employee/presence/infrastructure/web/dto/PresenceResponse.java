package com.b4rrhh.employee.presence.infrastructure.web.dto;

import java.time.LocalDate;

public record PresenceResponse(
        Integer presenceNumber,
        String companyCode,
        String companyName,
        String entryReasonCode,
        String entryReasonName,
        String exitReasonCode,
        String exitReasonName,
        LocalDate startDate,
        LocalDate endDate
) {
}
