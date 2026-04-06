package com.b4rrhh.rulesystem.workcenter.infrastructure.web;

import com.b4rrhh.rulesystem.workcenter.application.usecase.CreateWorkCenterUseCase;
import com.b4rrhh.rulesystem.workcenter.application.usecase.GetWorkCenterQuery;
import com.b4rrhh.rulesystem.workcenter.application.usecase.GetWorkCenterUseCase;
import com.b4rrhh.rulesystem.workcenter.application.usecase.ListWorkCentersQuery;
import com.b4rrhh.rulesystem.workcenter.application.usecase.ListWorkCentersUseCase;
import com.b4rrhh.rulesystem.workcenter.application.usecase.UpdateWorkCenterUseCase;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.assembler.WorkCenterResponseAssembler;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.CreateWorkCenterRequest;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.UpdateWorkCenterRequest;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.WorkCenterListItemResponse;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.WorkCenterResponse;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.mapper.WorkCenterCommandMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/work-centers")
public class WorkCenterController {

    private final CreateWorkCenterUseCase createWorkCenterUseCase;
    private final ListWorkCentersUseCase listWorkCentersUseCase;
    private final GetWorkCenterUseCase getWorkCenterUseCase;
    private final UpdateWorkCenterUseCase updateWorkCenterUseCase;
        private final WorkCenterCommandMapper workCenterCommandMapper;
    private final WorkCenterResponseAssembler workCenterResponseAssembler;

    public WorkCenterController(
            CreateWorkCenterUseCase createWorkCenterUseCase,
            ListWorkCentersUseCase listWorkCentersUseCase,
            GetWorkCenterUseCase getWorkCenterUseCase,
            UpdateWorkCenterUseCase updateWorkCenterUseCase,
            WorkCenterCommandMapper workCenterCommandMapper,
            WorkCenterResponseAssembler workCenterResponseAssembler
    ) {
        this.createWorkCenterUseCase = createWorkCenterUseCase;
        this.listWorkCentersUseCase = listWorkCentersUseCase;
        this.getWorkCenterUseCase = getWorkCenterUseCase;
        this.updateWorkCenterUseCase = updateWorkCenterUseCase;
        this.workCenterCommandMapper = workCenterCommandMapper;
        this.workCenterResponseAssembler = workCenterResponseAssembler;
    }

    @PostMapping
    public ResponseEntity<WorkCenterResponse> create(@RequestBody CreateWorkCenterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                workCenterResponseAssembler.toResponse(
                        createWorkCenterUseCase.create(workCenterCommandMapper.toCreateCommand(request))
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<WorkCenterListItemResponse>> list() {
        List<WorkCenterListItemResponse> response = listWorkCentersUseCase.list(new ListWorkCentersQuery(null))
                .stream()
                .map(workCenterResponseAssembler::toListItemResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ruleSystemCode}/{workCenterCode}")
    public ResponseEntity<WorkCenterResponse> get(
            @PathVariable String ruleSystemCode,
            @PathVariable String workCenterCode
    ) {
        return ResponseEntity.ok(workCenterResponseAssembler.toResponse(
                getWorkCenterUseCase.get(new GetWorkCenterQuery(ruleSystemCode, workCenterCode))
        ));
    }

    @PutMapping("/{ruleSystemCode}/{workCenterCode}")
    public ResponseEntity<WorkCenterResponse> update(
            @PathVariable String ruleSystemCode,
            @PathVariable String workCenterCode,
            @RequestBody UpdateWorkCenterRequest request
    ) {
        return ResponseEntity.ok(workCenterResponseAssembler.toResponse(
                updateWorkCenterUseCase.update(
                        workCenterCommandMapper.toUpdateCommand(ruleSystemCode, workCenterCode, request)
                )
        ));
    }
}