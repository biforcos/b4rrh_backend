package com.b4rrhh.employee.journey.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.Map;

public record JourneyItemResponse(
        LocalDate startDate,
        LocalDate endDate,
        String label,
        Map<String, Object> details
) {
}
