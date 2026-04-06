package com.b4rrhh.rulesystem.workcenter.domain.model;

import java.time.LocalDate;

public record WorkCenter(
        String ruleSystemCode,
        String workCenterCode,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
}