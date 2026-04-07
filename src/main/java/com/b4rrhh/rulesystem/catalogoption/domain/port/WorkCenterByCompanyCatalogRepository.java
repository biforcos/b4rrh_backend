package com.b4rrhh.rulesystem.catalogoption.domain.port;

import com.b4rrhh.rulesystem.catalogoption.domain.model.WorkCenterByCompanyOption;

import java.time.LocalDate;
import java.util.List;

public interface WorkCenterByCompanyCatalogRepository {

    List<WorkCenterByCompanyOption> findByCompany(
            String ruleSystemCode,
            String companyCode,
            LocalDate referenceDate,
            String qLike
    );
}