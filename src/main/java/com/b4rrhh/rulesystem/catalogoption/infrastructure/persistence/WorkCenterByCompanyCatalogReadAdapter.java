package com.b4rrhh.rulesystem.catalogoption.infrastructure.persistence;

import com.b4rrhh.rulesystem.catalogoption.domain.model.WorkCenterByCompanyOption;
import com.b4rrhh.rulesystem.catalogoption.domain.port.WorkCenterByCompanyCatalogRepository;
import com.b4rrhh.rulesystem.workcenter.infrastructure.persistence.SpringDataWorkCenterProfileRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class WorkCenterByCompanyCatalogReadAdapter implements WorkCenterByCompanyCatalogRepository {

    private final SpringDataWorkCenterProfileRepository springDataWorkCenterProfileRepository;

    public WorkCenterByCompanyCatalogReadAdapter(SpringDataWorkCenterProfileRepository springDataWorkCenterProfileRepository) {
        this.springDataWorkCenterProfileRepository = springDataWorkCenterProfileRepository;
    }

    @Override
    public List<WorkCenterByCompanyOption> findByCompany(
            String ruleSystemCode,
            String companyCode,
            LocalDate referenceDate,
            String qLike
    ) {
        return springDataWorkCenterProfileRepository
                .findWorkCentersByRuleSystemCodeAndCompanyCode(ruleSystemCode, companyCode, referenceDate, qLike)
                .stream()
                .map(row -> new WorkCenterByCompanyOption(row.getCode(), row.getName()))
                .toList();
    }
}