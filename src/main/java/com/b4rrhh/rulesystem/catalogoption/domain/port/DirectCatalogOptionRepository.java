package com.b4rrhh.rulesystem.catalogoption.domain.port;

import com.b4rrhh.rulesystem.catalogoption.domain.model.DirectCatalogOption;

import java.time.LocalDate;
import java.util.List;

public interface DirectCatalogOptionRepository {

    List<DirectCatalogOption> findDirectOptions(
            String ruleSystemCode,
            String ruleEntityTypeCode,
            LocalDate referenceDate,
            String qLike
    );
}
