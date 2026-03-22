package com.b4rrhh.employee.labor_classification.infrastructure.rest;

import com.b4rrhh.employee.labor_classification.application.command.ListAgreementCategoryCatalogCommand;
import com.b4rrhh.employee.labor_classification.application.model.AgreementCategoryCatalogItem;
import com.b4rrhh.employee.labor_classification.application.usecase.ListAgreementCategoryCatalogUseCase;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.AgreementCategoryCatalogItemResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/labor-classification-catalog")
public class LaborClassificationCatalogController {

    private final ListAgreementCategoryCatalogUseCase listAgreementCategoryCatalogUseCase;

    public LaborClassificationCatalogController(ListAgreementCategoryCatalogUseCase listAgreementCategoryCatalogUseCase) {
        this.listAgreementCategoryCatalogUseCase = listAgreementCategoryCatalogUseCase;
    }

    @GetMapping("/agreement-categories")
    public ResponseEntity<List<AgreementCategoryCatalogItemResponse>> listAgreementCategories(
            @RequestParam String ruleSystemCode,
            @RequestParam String agreementCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate
    ) {
        List<AgreementCategoryCatalogItemResponse> response = listAgreementCategoryCatalogUseCase
                .list(new ListAgreementCategoryCatalogCommand(ruleSystemCode, agreementCode, referenceDate))
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private AgreementCategoryCatalogItemResponse toResponse(AgreementCategoryCatalogItem source) {
        return new AgreementCategoryCatalogItemResponse(
                source.code(),
                source.name(),
                source.startDate(),
                source.endDate()
        );
    }
}
