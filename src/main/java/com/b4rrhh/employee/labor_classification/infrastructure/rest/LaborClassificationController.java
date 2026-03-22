package com.b4rrhh.employee.labor_classification.infrastructure.rest;

import com.b4rrhh.employee.labor_classification.application.command.CloseLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.command.GetLaborClassificationByBusinessKeyCommand;
import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.command.ReplaceLaborClassificationFromDateCommand;
import com.b4rrhh.employee.labor_classification.application.command.UpdateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.port.LaborClassificationCatalogReadPort;
import com.b4rrhh.employee.labor_classification.application.usecase.CloseLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.CreateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.GetLaborClassificationByBusinessKeyUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.ListEmployeeLaborClassificationsUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.ReplaceLaborClassificationFromDateUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.UpdateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.CloseLaborClassificationRequest;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.CreateLaborClassificationRequest;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.LaborClassificationResponse;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.ReplaceLaborClassificationFromDateRequest;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.UpdateLaborClassificationRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/labor-classifications")
public class LaborClassificationController {

    private final CreateLaborClassificationUseCase createLaborClassificationUseCase;
    private final ListEmployeeLaborClassificationsUseCase listEmployeeLaborClassificationsUseCase;
    private final GetLaborClassificationByBusinessKeyUseCase getLaborClassificationByBusinessKeyUseCase;
    private final UpdateLaborClassificationUseCase updateLaborClassificationUseCase;
    private final CloseLaborClassificationUseCase closeLaborClassificationUseCase;
        private final ReplaceLaborClassificationFromDateUseCase replaceLaborClassificationFromDateUseCase;
        private final LaborClassificationCatalogReadPort laborClassificationCatalogReadPort;

    public LaborClassificationController(
            CreateLaborClassificationUseCase createLaborClassificationUseCase,
            ListEmployeeLaborClassificationsUseCase listEmployeeLaborClassificationsUseCase,
            GetLaborClassificationByBusinessKeyUseCase getLaborClassificationByBusinessKeyUseCase,
            UpdateLaborClassificationUseCase updateLaborClassificationUseCase,
                        CloseLaborClassificationUseCase closeLaborClassificationUseCase,
                        ReplaceLaborClassificationFromDateUseCase replaceLaborClassificationFromDateUseCase,
                        LaborClassificationCatalogReadPort laborClassificationCatalogReadPort
    ) {
        this.createLaborClassificationUseCase = createLaborClassificationUseCase;
        this.listEmployeeLaborClassificationsUseCase = listEmployeeLaborClassificationsUseCase;
        this.getLaborClassificationByBusinessKeyUseCase = getLaborClassificationByBusinessKeyUseCase;
        this.updateLaborClassificationUseCase = updateLaborClassificationUseCase;
        this.closeLaborClassificationUseCase = closeLaborClassificationUseCase;
                this.replaceLaborClassificationFromDateUseCase = replaceLaborClassificationFromDateUseCase;
                this.laborClassificationCatalogReadPort = laborClassificationCatalogReadPort;
    }

    @PostMapping
    public ResponseEntity<LaborClassificationResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateLaborClassificationRequest request
    ) {
        LaborClassification created = createLaborClassificationUseCase.create(
                new CreateLaborClassificationCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.agreementCode(),
                        request.agreementCategoryCode(),
                        request.startDate(),
                        request.endDate()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ruleSystemCode, created));
    }

    @GetMapping
    public ResponseEntity<List<LaborClassificationResponse>> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        List<LaborClassificationResponse> response = listEmployeeLaborClassificationsUseCase
                .listByEmployeeBusinessKey(new ListEmployeeLaborClassificationsCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                ))
                .stream()
                .map(laborClassification -> toResponse(ruleSystemCode, laborClassification))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{startDate}")
    public ResponseEntity<LaborClassificationResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        LaborClassification laborClassification = getLaborClassificationByBusinessKeyUseCase.getByBusinessKey(
                new GetLaborClassificationByBusinessKeyCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        startDate
                )
        );

        return ResponseEntity.ok(toResponse(ruleSystemCode, laborClassification));
    }

    @PutMapping("/{startDate}")
    public ResponseEntity<LaborClassificationResponse> update(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestBody UpdateLaborClassificationRequest request
    ) {
        LaborClassification updated = updateLaborClassificationUseCase.update(
                new UpdateLaborClassificationCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        startDate,
                        request.agreementCode(),
                        request.agreementCategoryCode()
                )
        );

        return ResponseEntity.ok(toResponse(ruleSystemCode, updated));
    }

    @PostMapping("/{startDate}/close")
    public ResponseEntity<LaborClassificationResponse> close(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestBody CloseLaborClassificationRequest request
    ) {
        LaborClassification closed = closeLaborClassificationUseCase.close(
                new CloseLaborClassificationCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        startDate,
                        request.endDate()
                )
        );

        return ResponseEntity.ok(toResponse(ruleSystemCode, closed));
    }

    @PostMapping("/replace-from-date")
    public ResponseEntity<LaborClassificationResponse> replaceFromDate(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody ReplaceLaborClassificationFromDateRequest request
    ) {
        LaborClassification replaced = replaceLaborClassificationFromDateUseCase.replaceFromDate(
                new ReplaceLaborClassificationFromDateCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.effectiveDate(),
                        request.agreementCode(),
                        request.agreementCategoryCode()
                )
        );

                return ResponseEntity.ok(toResponse(ruleSystemCode, replaced));
    }

    private LaborClassificationResponse toResponse(String ruleSystemCode, LaborClassification laborClassification) {
        String agreementName = laborClassificationCatalogReadPort
                .findAgreementName(ruleSystemCode, laborClassification.getAgreementCode())
                .orElse(null);
        String agreementCategoryName = laborClassificationCatalogReadPort
                .findAgreementCategoryName(ruleSystemCode, laborClassification.getAgreementCategoryCode())
                .orElse(null);

        return new LaborClassificationResponse(
                laborClassification.getAgreementCode(),
                agreementName,
                laborClassification.getAgreementCategoryCode(),
                agreementCategoryName,
                laborClassification.getStartDate(),
                laborClassification.getEndDate()
        );
    }
}
