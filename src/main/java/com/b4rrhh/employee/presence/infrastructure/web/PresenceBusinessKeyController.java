package com.b4rrhh.employee.presence.infrastructure.web;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.GetPresenceByIdUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.presence.application.usecase.ResolveEmployeePresenceByBusinessKeyUseCase;
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
@RequestMapping("/rule-systems/{ruleSystemCode}/employees/{employeeNumber}/presences")
public class PresenceBusinessKeyController {

    private final ResolveEmployeePresenceByBusinessKeyUseCase resolveEmployeePresenceByBusinessKeyUseCase;
    private final CreatePresenceUseCase createPresenceUseCase;
    private final ClosePresenceUseCase closePresenceUseCase;
    private final GetPresenceByIdUseCase getPresenceByIdUseCase;
    private final ListEmployeePresencesUseCase listEmployeePresencesUseCase;

    public PresenceBusinessKeyController(
            ResolveEmployeePresenceByBusinessKeyUseCase resolveEmployeePresenceByBusinessKeyUseCase,
            CreatePresenceUseCase createPresenceUseCase,
            ClosePresenceUseCase closePresenceUseCase,
            GetPresenceByIdUseCase getPresenceByIdUseCase,
            ListEmployeePresencesUseCase listEmployeePresencesUseCase
    ) {
        this.resolveEmployeePresenceByBusinessKeyUseCase = resolveEmployeePresenceByBusinessKeyUseCase;
        this.createPresenceUseCase = createPresenceUseCase;
        this.closePresenceUseCase = closePresenceUseCase;
        this.getPresenceByIdUseCase = getPresenceByIdUseCase;
        this.listEmployeePresencesUseCase = listEmployeePresencesUseCase;
    }

    @PostMapping
    public ResponseEntity<PresenceResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeNumber,
            @RequestBody CreatePresenceRequest request
    ) {
        EmployeePresenceContext employee = resolveEmployeePresenceByBusinessKeyUseCase.resolve(ruleSystemCode, employeeNumber);

        Presence created = createPresenceUseCase.create(
                new CreatePresenceCommand(
                        employee.employeeId(),
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
            @PathVariable String employeeNumber
    ) {
        EmployeePresenceContext employee = resolveEmployeePresenceByBusinessKeyUseCase.resolve(ruleSystemCode, employeeNumber);

        List<PresenceResponse> response = listEmployeePresencesUseCase.listByEmployeeId(employee.employeeId())
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{presenceId}")
    public ResponseEntity<PresenceResponse> getById(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeNumber,
            @PathVariable Long presenceId
    ) {
        EmployeePresenceContext employee = resolveEmployeePresenceByBusinessKeyUseCase.resolve(ruleSystemCode, employeeNumber);

        return getPresenceByIdUseCase.getById(employee.employeeId(), presenceId)
                .map(presence -> ResponseEntity.ok(toResponse(presence)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{presenceId}/close")
    public ResponseEntity<PresenceResponse> close(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeNumber,
            @PathVariable Long presenceId,
            @RequestBody ClosePresenceRequest request
    ) {
        EmployeePresenceContext employee = resolveEmployeePresenceByBusinessKeyUseCase.resolve(ruleSystemCode, employeeNumber);

        Presence closed = closePresenceUseCase.close(
                new ClosePresenceCommand(
                        employee.employeeId(),
                        presenceId,
                        request.endDate(),
                        request.exitReasonCode()
                )
        );

        return ResponseEntity.ok(toResponse(closed));
    }

    private PresenceResponse toResponse(Presence presence) {
        return new PresenceResponse(
                presence.getId(),
                presence.getEmployeeId(),
                presence.getPresenceNumber(),
                presence.getCompanyCode(),
                presence.getEntryReasonCode(),
                presence.getExitReasonCode(),
                presence.getStartDate(),
                presence.getEndDate()
        );
    }
}
