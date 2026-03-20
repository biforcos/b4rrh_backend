package com.b4rrhh.employee.journey.application.usecase;

import java.util.List;

public record JourneyTrackView(
        String trackCode,
        String trackLabel,
        List<JourneyItemView> items
) {
}
