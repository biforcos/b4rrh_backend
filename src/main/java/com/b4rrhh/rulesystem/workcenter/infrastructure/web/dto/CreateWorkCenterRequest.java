package com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto;

import java.time.LocalDate;

public record CreateWorkCenterRequest(
        String ruleSystemCode,
        String workCenterCode,
        String name,
        String description,
        LocalDate startDate,
        String companyCode,
        WorkCenterAddress address
) {
}