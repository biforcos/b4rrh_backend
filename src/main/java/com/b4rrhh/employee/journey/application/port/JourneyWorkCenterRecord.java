package com.b4rrhh.employee.journey.application.port;

import java.time.LocalDate;

public record JourneyWorkCenterRecord(
        String workCenterCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
