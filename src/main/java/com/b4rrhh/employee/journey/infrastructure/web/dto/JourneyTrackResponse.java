package com.b4rrhh.employee.journey.infrastructure.web.dto;

import java.util.List;

public record JourneyTrackResponse(
        String trackCode,
        String trackLabel,
        List<JourneyItemResponse> items
) {
}
