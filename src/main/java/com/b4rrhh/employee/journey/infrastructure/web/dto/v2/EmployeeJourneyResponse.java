package com.b4rrhh.employee.journey.infrastructure.web.dto.v2;

import java.util.List;

public record EmployeeJourneyResponse(
        JourneyEmployeeHeader employee,
        List<JourneyEventResponse> events
) {
}