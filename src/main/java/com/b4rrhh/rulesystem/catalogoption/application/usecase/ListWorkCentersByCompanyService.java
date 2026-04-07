package com.b4rrhh.rulesystem.catalogoption.application.usecase;

import com.b4rrhh.rulesystem.catalogoption.application.query.ListWorkCentersByCompanyQuery;
import com.b4rrhh.rulesystem.catalogoption.domain.model.WorkCenterByCompanyOption;
import com.b4rrhh.rulesystem.catalogoption.domain.port.WorkCenterByCompanyCatalogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListWorkCentersByCompanyService implements ListWorkCentersByCompanyUseCase {

    private final WorkCenterByCompanyCatalogRepository workCenterByCompanyCatalogRepository;

    public ListWorkCentersByCompanyService(WorkCenterByCompanyCatalogRepository workCenterByCompanyCatalogRepository) {
        this.workCenterByCompanyCatalogRepository = workCenterByCompanyCatalogRepository;
    }

    @Override
    public WorkCentersByCompanyResult get(ListWorkCentersByCompanyQuery query) {
        String normalizedRuleSystemCode = normalizeRequired("ruleSystemCode", query.ruleSystemCode());
        String normalizedCompanyCode = normalizeRequired("companyCode", query.companyCode()).toUpperCase();
        String normalizedQLike = normalizeLike(query.q());
        LocalDate effectiveReferenceDate = query.referenceDate() != null ? query.referenceDate() : LocalDate.now();

        List<WorkCenterByCompanyOption> items = workCenterByCompanyCatalogRepository.findByCompany(
                normalizedRuleSystemCode,
                normalizedCompanyCode,
                effectiveReferenceDate,
                normalizedQLike
        );

        return new WorkCentersByCompanyResult(
                normalizedRuleSystemCode,
                normalizedCompanyCode,
                effectiveReferenceDate,
                items
        );
    }

    private String normalizeRequired(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim().toUpperCase();
    }

    private String normalizeLike(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return "%" + value.trim().toLowerCase() + "%";
    }
}