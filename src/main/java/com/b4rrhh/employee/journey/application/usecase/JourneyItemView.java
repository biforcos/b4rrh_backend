package com.b4rrhh.employee.journey.application.usecase;

import java.time.LocalDate;
import java.util.Map;

public record JourneyItemView(
        LocalDate startDate,
        LocalDate endDate,
        String label,
        Map<String, Object> details
) {
}
