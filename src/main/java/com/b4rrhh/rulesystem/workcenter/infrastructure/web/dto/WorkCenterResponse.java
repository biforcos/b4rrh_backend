package com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto;

import java.time.LocalDate;

public record WorkCenterResponse(
        String ruleSystemCode,
        String workCenterCode,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String companyCode,
        WorkCenterAddress address
) {
}