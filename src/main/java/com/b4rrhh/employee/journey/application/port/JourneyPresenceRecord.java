package com.b4rrhh.employee.journey.application.port;

import java.time.LocalDate;

public record JourneyPresenceRecord(
        String companyCode,
        String entryReasonCode,
        String exitReasonCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
