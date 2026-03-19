package com.b4rrhh.employee.contract.application.port;

import java.time.LocalDate;

public record PresencePeriod(
        LocalDate startDate,
        LocalDate endDate
) {
}
