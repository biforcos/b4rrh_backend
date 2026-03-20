package com.b4rrhh.employee.journey.application.usecase;

import java.util.List;

public record EmployeeJourneyView(
        JourneyEmployeeHeaderView employee,
        List<JourneyTrackView> tracks
) {
}
