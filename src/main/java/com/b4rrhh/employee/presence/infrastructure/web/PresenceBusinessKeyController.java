package com.b4rrhh.employee.presence.infrastructure.web;

import com.b4rrhh.employee.presence.application.usecase.ClosePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.GetPresenceByBusinessKeyUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.infrastructure.web.dto.ClosePresenceRequest;
import com.b4rrhh.employee.presence.infrastructure.web.dto.CreatePresenceRequest;
import com.b4rrhh.employee.presence.infrastructure.web.dto.PresenceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/presences")
public class PresenceBusinessKeyController {

    private final CreatePresenceUseCase createPresenceUseCase;
    private final ClosePresenceUseCase closePresenceUseCase;
        private final GetPresenceByBusinessKeyUseCase getPresenceByBusinessKeyUseCase;
    private final ListEmployeePresencesUseCase listEmployeePresencesUseCase;

    public PresenceBusinessKeyController(
            CreatePresenceUseCase createPresenceUseCase,
            ClosePresenceUseCase closePresenceUseCase,
                        GetPresenceByBusinessKeyUseCase getPresenceByBusinessKeyUseCase,
            ListEmployeePresencesUseCase listEmployeePresencesUseCase
    ) {
        this.createPresenceUseCase = createPresenceUseCase;
        this.closePresenceUseCase = closePresenceUseCase;
                this.getPresenceByBusinessKeyUseCase = getPresenceByBusinessKeyUseCase;
        this.listEmployeePresencesUseCase = listEmployeePresencesUseCase;
    }

    @PostMapping
    public ResponseEntity<PresenceResponse> create(
            @PathVariable String ruleSystemCode,
                        @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreatePresenceRequest request
    ) {
        Presence created = createPresenceUseCase.create(
                new CreatePresenceCommand(
                                                ruleSystemCode,
                                                employeeTypeCode,
                                                employeeNumber,
                        request.companyCode(),
                        request.entryReasonCode(),
                        request.exitReasonCode(),
                        request.startDate(),
                        request.endDate()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<PresenceResponse>> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        List<PresenceResponse> response = listEmployeePresencesUseCase
                .listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{presenceNumber}")
    public ResponseEntity<PresenceResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable Integer presenceNumber
    ) {
        return getPresenceByBusinessKeyUseCase.getByBusinessKey(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        presenceNumber
                )
                .map(presence -> ResponseEntity.ok(toResponse(presence)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{presenceNumber}/close")
    public ResponseEntity<PresenceResponse> close(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable Integer presenceNumber,
            @RequestBody ClosePresenceRequest request
    ) {
        Presence closed = closePresenceUseCase.close(
                new ClosePresenceCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        presenceNumber,
                        request.endDate(),
                        request.exitReasonCode()
                )
        );

        return ResponseEntity.ok(toResponse(closed));
    }

    private PresenceResponse toResponse(Presence presence) {
        return new PresenceResponse(
                presence.getPresenceNumber(),
                presence.getCompanyCode(),
                presence.getEntryReasonCode(),
                presence.getExitReasonCode(),
                presence.getStartDate(),
                presence.getEndDate()
        );
    }
}
