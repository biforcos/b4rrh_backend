package com.b4rrhh.employee.labor_classification.application.port;

import java.time.LocalDate;

public record PresencePeriod(
        LocalDate startDate,
        LocalDate endDate
) {
}
