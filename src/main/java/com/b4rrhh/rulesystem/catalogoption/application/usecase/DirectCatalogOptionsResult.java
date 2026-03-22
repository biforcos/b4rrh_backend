package com.b4rrhh.rulesystem.catalogoption.application.usecase;

import com.b4rrhh.rulesystem.catalogoption.domain.model.DirectCatalogOption;

import java.time.LocalDate;
import java.util.List;

public record DirectCatalogOptionsResult(
        String ruleSystemCode,
        String ruleEntityTypeCode,
        LocalDate referenceDate,
        List<DirectCatalogOption> items
) {
}
