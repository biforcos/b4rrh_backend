package com.b4rrhh.employee.identifier.infrastructure.web;

import com.b4rrhh.employee.identifier.application.usecase.CreateIdentifierCommand;
import com.b4rrhh.employee.identifier.application.usecase.CreateIdentifierUseCase;
import com.b4rrhh.employee.identifier.application.usecase.DeleteIdentifierCommand;
import com.b4rrhh.employee.identifier.application.usecase.DeleteIdentifierUseCase;
import com.b4rrhh.employee.identifier.application.usecase.GetIdentifierByBusinessKeyUseCase;
import com.b4rrhh.employee.identifier.application.usecase.ListEmployeeIdentifiersUseCase;
import com.b4rrhh.employee.identifier.application.usecase.UpdateIdentifierCommand;
import com.b4rrhh.employee.identifier.application.usecase.UpdateIdentifierUseCase;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.infrastructure.web.dto.CreateIdentifierRequest;
import com.b4rrhh.employee.identifier.infrastructure.web.dto.IdentifierResponse;
import com.b4rrhh.employee.identifier.infrastructure.web.dto.UpdateIdentifierRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/identifiers")
public class IdentifierController {

    private final CreateIdentifierUseCase createIdentifierUseCase;
    private final UpdateIdentifierUseCase updateIdentifierUseCase;
    private final GetIdentifierByBusinessKeyUseCase getIdentifierByBusinessKeyUseCase;
    private final ListEmployeeIdentifiersUseCase listEmployeeIdentifiersUseCase;
    private final DeleteIdentifierUseCase deleteIdentifierUseCase;

    public IdentifierController(
            CreateIdentifierUseCase createIdentifierUseCase,
            UpdateIdentifierUseCase updateIdentifierUseCase,
            GetIdentifierByBusinessKeyUseCase getIdentifierByBusinessKeyUseCase,
            ListEmployeeIdentifiersUseCase listEmployeeIdentifiersUseCase,
            DeleteIdentifierUseCase deleteIdentifierUseCase
    ) {
        this.createIdentifierUseCase = createIdentifierUseCase;
        this.updateIdentifierUseCase = updateIdentifierUseCase;
        this.getIdentifierByBusinessKeyUseCase = getIdentifierByBusinessKeyUseCase;
        this.listEmployeeIdentifiersUseCase = listEmployeeIdentifiersUseCase;
        this.deleteIdentifierUseCase = deleteIdentifierUseCase;
    }

    @PostMapping
    public ResponseEntity<IdentifierResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateIdentifierRequest request
    ) {
        Identifier created = createIdentifierUseCase.create(
                new CreateIdentifierCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.identifierTypeCode(),
                        request.identifierValue(),
                        request.issuingCountryCode(),
                        request.expirationDate(),
                        request.isPrimary()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<IdentifierResponse>> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        List<IdentifierResponse> response = listEmployeeIdentifiersUseCase
                .listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{identifierTypeCode}")
    public ResponseEntity<IdentifierResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String identifierTypeCode
    ) {
        return getIdentifierByBusinessKeyUseCase.getByBusinessKey(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        identifierTypeCode
                )
                .map(identifier -> ResponseEntity.ok(toResponse(identifier)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{identifierTypeCode}")
    public ResponseEntity<IdentifierResponse> update(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String identifierTypeCode,
            @RequestBody UpdateIdentifierRequest request
    ) {
        Identifier updated = updateIdentifierUseCase.update(
                new UpdateIdentifierCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        identifierTypeCode,
                        request.getIdentifierValue(),
                        request.getIssuingCountryCode(),
                        request.getExpirationDate(),
                        request.getIsPrimary()
                )
        );

        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{identifierTypeCode}")
    public ResponseEntity<Void> delete(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String identifierTypeCode
    ) {
        deleteIdentifierUseCase.delete(
                new DeleteIdentifierCommand(ruleSystemCode, employeeTypeCode, employeeNumber, identifierTypeCode)
        );
        return ResponseEntity.noContent().build();
    }

    private IdentifierResponse toResponse(Identifier identifier) {
        return new IdentifierResponse(
                identifier.getIdentifierTypeCode(),
                identifier.getIdentifierValue(),
                identifier.getIssuingCountryCode(),
                identifier.getExpirationDate(),
                identifier.isPrimary()
        );
    }
}
