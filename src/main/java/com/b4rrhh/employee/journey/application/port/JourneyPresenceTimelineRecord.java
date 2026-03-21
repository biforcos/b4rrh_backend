package com.b4rrhh.employee.journey.application.port;

import java.time.LocalDate;

public record JourneyPresenceTimelineRecord(
        Integer presenceNumber,
        String companyCode,
        String entryReasonCode,
        String exitReasonCode,
        LocalDate startDate,
        LocalDate endDate
) {
}