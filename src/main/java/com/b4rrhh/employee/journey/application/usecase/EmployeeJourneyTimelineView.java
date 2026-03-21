package com.b4rrhh.employee.journey.application.usecase;

import java.util.List;

public record EmployeeJourneyTimelineView(
        JourneyEmployeeHeaderView employee,
        List<JourneyEventView> events
) {
}