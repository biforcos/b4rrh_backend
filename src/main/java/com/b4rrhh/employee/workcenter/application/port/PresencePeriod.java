package com.b4rrhh.employee.workcenter.application.port;

import java.time.LocalDate;

public record PresencePeriod(
        LocalDate startDate,
        LocalDate endDate
) {
}