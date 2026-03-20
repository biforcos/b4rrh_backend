package com.b4rrhh.employee.journey.application.port;

import java.time.LocalDate;

public record JourneyContractRecord(
        String contractCode,
        String contractSubtypeCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
