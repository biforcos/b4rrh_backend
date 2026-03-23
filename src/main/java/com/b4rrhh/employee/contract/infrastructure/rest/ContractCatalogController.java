package com.b4rrhh.employee.contract.infrastructure.rest;

import com.b4rrhh.employee.contract.application.command.ListContractSubtypeCatalogCommand;
import com.b4rrhh.employee.contract.application.model.ContractSubtypeCatalogItem;
import com.b4rrhh.employee.contract.application.usecase.ListContractSubtypeCatalogUseCase;
import com.b4rrhh.employee.contract.infrastructure.rest.dto.ContractSubtypeCatalogItemResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/contract-catalog")
public class ContractCatalogController {

    private final ListContractSubtypeCatalogUseCase listContractSubtypeCatalogUseCase;

    public ContractCatalogController(ListContractSubtypeCatalogUseCase listContractSubtypeCatalogUseCase) {
        this.listContractSubtypeCatalogUseCase = listContractSubtypeCatalogUseCase;
    }

    @GetMapping("/contract-subtypes")
    public ResponseEntity<List<ContractSubtypeCatalogItemResponse>> listContractSubtypes(
            @RequestParam String ruleSystemCode,
            @RequestParam String contractTypeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate
    ) {
        List<ContractSubtypeCatalogItemResponse> response = listContractSubtypeCatalogUseCase
                .list(new ListContractSubtypeCatalogCommand(ruleSystemCode, contractTypeCode, referenceDate))
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private ContractSubtypeCatalogItemResponse toResponse(ContractSubtypeCatalogItem source) {
        return new ContractSubtypeCatalogItemResponse(
                source.code(),
                source.name(),
                source.startDate(),
                source.endDate()
        );
    }
}