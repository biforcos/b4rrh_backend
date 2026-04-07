package com.b4rrhh.rulesystem.catalogoption.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.List;

public record WorkCentersByCompanyResponse(
        String ruleSystemCode,
        String companyCode,
        LocalDate referenceDate,
        List<WorkCenterByCompanyOptionResponse> items
) {
}