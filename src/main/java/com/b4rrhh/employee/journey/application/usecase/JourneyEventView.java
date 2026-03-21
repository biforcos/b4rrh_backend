package com.b4rrhh.employee.journey.application.usecase;

import java.time.LocalDate;
import java.util.Map;

public record JourneyEventView(
        LocalDate eventDate,
        JourneyEventType eventType,
        JourneyTrackCode trackCode,
        String title,
        String subtitle,
        JourneyEventStatus status,
        boolean isCurrent,
        Map<String, Object> details
) {
}