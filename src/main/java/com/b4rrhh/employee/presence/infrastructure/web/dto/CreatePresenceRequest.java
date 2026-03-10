package com.b4rrhh.employee.presence.infrastructure.web.dto;

import java.time.LocalDate;

public record CreatePresenceRequest(
        String companyCode,
        String entryReasonCode,
        String exitReasonCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
