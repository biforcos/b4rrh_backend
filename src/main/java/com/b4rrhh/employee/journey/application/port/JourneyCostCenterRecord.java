package com.b4rrhh.employee.journey.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JourneyCostCenterRecord(
        String costCenterCode,
        BigDecimal allocationPercentage,
        LocalDate startDate,
        LocalDate endDate
) {
}
