package com.b4rrhh.employee.journey.infrastructure.web.dto;

import java.util.List;

public record EmployeeJourneyResponse(
        JourneyEmployeeHeaderResponse employee,
        List<JourneyTrackResponse> tracks
) {
}
