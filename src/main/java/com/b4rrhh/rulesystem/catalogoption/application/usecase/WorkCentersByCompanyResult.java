package com.b4rrhh.rulesystem.catalogoption.application.usecase;

import com.b4rrhh.rulesystem.catalogoption.domain.model.WorkCenterByCompanyOption;

import java.time.LocalDate;
import java.util.List;

public record WorkCentersByCompanyResult(
        String ruleSystemCode,
        String companyCode,
        LocalDate referenceDate,
        List<WorkCenterByCompanyOption> items
) {
}