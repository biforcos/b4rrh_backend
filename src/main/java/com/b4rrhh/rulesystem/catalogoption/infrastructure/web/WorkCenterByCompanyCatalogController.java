package com.b4rrhh.rulesystem.catalogoption.infrastructure.web;

import com.b4rrhh.rulesystem.catalogoption.application.query.ListWorkCentersByCompanyQuery;
import com.b4rrhh.rulesystem.catalogoption.application.usecase.ListWorkCentersByCompanyUseCase;
import com.b4rrhh.rulesystem.catalogoption.application.usecase.WorkCentersByCompanyResult;
import com.b4rrhh.rulesystem.catalogoption.infrastructure.web.dto.WorkCenterByCompanyOptionResponse;
import com.b4rrhh.rulesystem.catalogoption.infrastructure.web.dto.WorkCentersByCompanyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/catalog-options")
public class WorkCenterByCompanyCatalogController {

    private final ListWorkCentersByCompanyUseCase listWorkCentersByCompanyUseCase;

    public WorkCenterByCompanyCatalogController(ListWorkCentersByCompanyUseCase listWorkCentersByCompanyUseCase) {
        this.listWorkCentersByCompanyUseCase = listWorkCentersByCompanyUseCase;
    }

    @GetMapping("/work-centers-by-company")
    public ResponseEntity<WorkCentersByCompanyResponse> getByCompany(
            @RequestParam String ruleSystemCode,
            @RequestParam String companyCode,
            @RequestParam(required = false) LocalDate referenceDate,
            @RequestParam(required = false) String q
    ) {
        WorkCentersByCompanyResult result = listWorkCentersByCompanyUseCase.get(
                new ListWorkCentersByCompanyQuery(ruleSystemCode, companyCode, referenceDate, q)
        );

        List<WorkCenterByCompanyOptionResponse> items = result.items().stream()
                .map(item -> new WorkCenterByCompanyOptionResponse(item.code(), item.name()))
                .toList();

        return ResponseEntity.ok(new WorkCentersByCompanyResponse(
                result.ruleSystemCode(),
                result.companyCode(),
                result.referenceDate(),
                items
        ));
    }
}