package com.b4rrhh.rulesystem.catalogoption.application.query;

import java.time.LocalDate;

public record ListWorkCentersByCompanyQuery(
        String ruleSystemCode,
        String companyCode,
        LocalDate referenceDate,
        String q
) {
}