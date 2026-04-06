package com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto;

import java.time.LocalDate;

public record WorkCenterListItemResponse(
        String ruleSystemCode,
        String workCenterCode,
        String name,
        String companyCode,
        String city,
        String countryCode,
        boolean active,
        LocalDate startDate,
        LocalDate endDate
) {
}