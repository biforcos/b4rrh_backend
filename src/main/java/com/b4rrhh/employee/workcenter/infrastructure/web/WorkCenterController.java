package com.b4rrhh.employee.workcenter.infrastructure.web;

import com.b4rrhh.employee.workcenter.application.port.WorkCenterCatalogReadPort;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.DeleteWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.DeleteWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.GetWorkCenterByBusinessKeyUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.ListEmployeeWorkCentersUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.UpdateWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.UpdateWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.infrastructure.web.dto.CloseWorkCenterRequest;
import com.b4rrhh.employee.workcenter.infrastructure.web.dto.CreateWorkCenterRequest;
import com.b4rrhh.employee.workcenter.infrastructure.web.dto.UpdateWorkCenterRequest;
import com.b4rrhh.employee.workcenter.infrastructure.web.dto.WorkCenterResponse;
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
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/work-centers")
public class WorkCenterController {

    private final CreateWorkCenterUseCase createWorkCenterUseCase;
    private final CloseWorkCenterUseCase closeWorkCenterUseCase;
        private final DeleteWorkCenterUseCase deleteWorkCenterUseCase;
    private final GetWorkCenterByBusinessKeyUseCase getWorkCenterByBusinessKeyUseCase;
    private final ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase;
        private final UpdateWorkCenterUseCase updateWorkCenterUseCase;
        private final WorkCenterCatalogReadPort workCenterCatalogReadPort;

    public WorkCenterController(
            CreateWorkCenterUseCase createWorkCenterUseCase,
            CloseWorkCenterUseCase closeWorkCenterUseCase,
            DeleteWorkCenterUseCase deleteWorkCenterUseCase,
            GetWorkCenterByBusinessKeyUseCase getWorkCenterByBusinessKeyUseCase,
            ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase,
            UpdateWorkCenterUseCase updateWorkCenterUseCase,
            WorkCenterCatalogReadPort workCenterCatalogReadPort
    ) {
        this.createWorkCenterUseCase = createWorkCenterUseCase;
        this.closeWorkCenterUseCase = closeWorkCenterUseCase;
        this.deleteWorkCenterUseCase = deleteWorkCenterUseCase;
        this.getWorkCenterByBusinessKeyUseCase = getWorkCenterByBusinessKeyUseCase;
        this.listEmployeeWorkCentersUseCase = listEmployeeWorkCentersUseCase;
        this.updateWorkCenterUseCase = updateWorkCenterUseCase;
        this.workCenterCatalogReadPort = workCenterCatalogReadPort;
    }

    @PostMapping
    public ResponseEntity<WorkCenterResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateWorkCenterRequest request
    ) {
        WorkCenter created = createWorkCenterUseCase.create(
                new CreateWorkCenterCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.workCenterCode(),
                        request.startDate(),
                        request.endDate()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ruleSystemCode, created));
    }

    @GetMapping
    public ResponseEntity<List<WorkCenterResponse>> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        List<WorkCenterResponse> response = listEmployeeWorkCentersUseCase
                .listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .stream()
                .map(workCenter -> toResponse(ruleSystemCode, workCenter))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{workCenterAssignmentNumber}")
    public ResponseEntity<WorkCenterResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable Integer workCenterAssignmentNumber
    ) {
        return getWorkCenterByBusinessKeyUseCase.getByBusinessKey(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        workCenterAssignmentNumber
                )
                .map(workCenter -> ResponseEntity.ok(toResponse(ruleSystemCode, workCenter)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{workCenterAssignmentNumber}")
    public ResponseEntity<WorkCenterResponse> update(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable Integer workCenterAssignmentNumber,
            @RequestBody UpdateWorkCenterRequest request
    ) {
        WorkCenter updated = updateWorkCenterUseCase.update(
                new UpdateWorkCenterCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        workCenterAssignmentNumber,
                        request.workCenterCode(),
                        request.startDate(),
                        request.endDate()
                )
        );

        return ResponseEntity.ok(toResponse(ruleSystemCode, updated));
    }

    @PostMapping("/{workCenterAssignmentNumber}/close")
    public ResponseEntity<WorkCenterResponse> close(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable Integer workCenterAssignmentNumber,
            @RequestBody CloseWorkCenterRequest request
    ) {
        WorkCenter closed = closeWorkCenterUseCase.close(
                new CloseWorkCenterCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        workCenterAssignmentNumber,
                        request.endDate()
                )
        );

        return ResponseEntity.ok(toResponse(ruleSystemCode, closed));
    }

    @DeleteMapping("/{workCenterAssignmentNumber}")
    public ResponseEntity<Void> delete(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable Integer workCenterAssignmentNumber
    ) {
        deleteWorkCenterUseCase.delete(
                new DeleteWorkCenterCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        workCenterAssignmentNumber
                )
        );

        return ResponseEntity.noContent().build();
    }

    private WorkCenterResponse toResponse(String ruleSystemCode, WorkCenter workCenter) {
        String workCenterName = workCenterCatalogReadPort
                .findWorkCenterName(ruleSystemCode, workCenter.getWorkCenterCode())
                .orElse(null);

        return new WorkCenterResponse(
                workCenter.getWorkCenterAssignmentNumber(),
                workCenter.getWorkCenterCode(),
                workCenterName,
                workCenter.getStartDate(),
                workCenter.getEndDate()
        );
    }
}