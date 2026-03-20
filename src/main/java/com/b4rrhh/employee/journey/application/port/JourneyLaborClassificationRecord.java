package com.b4rrhh.employee.journey.application.port;

import java.time.LocalDate;

public record JourneyLaborClassificationRecord(
        String agreementCode,
        String agreementCategoryCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
