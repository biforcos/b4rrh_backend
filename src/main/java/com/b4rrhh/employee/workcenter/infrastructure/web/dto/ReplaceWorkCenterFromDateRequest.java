package com.b4rrhh.employee.workcenter.infrastructure.web.dto;

import java.time.LocalDate;

public record ReplaceWorkCenterFromDateRequest(
        LocalDate effectiveDate,
        String workCenterCode
) {
}