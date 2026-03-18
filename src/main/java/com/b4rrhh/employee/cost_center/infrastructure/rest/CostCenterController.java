package com.b4rrhh.employee.cost_center.infrastructure.rest;

import com.b4rrhh.employee.cost_center.application.command.CloseCostCenterCommand;
import com.b4rrhh.employee.cost_center.application.command.CreateCostCenterCommand;
import com.b4rrhh.employee.cost_center.application.command.GetCostCenterByBusinessKeyCommand;
import com.b4rrhh.employee.cost_center.application.command.ListEmployeeCostCentersCommand;
import com.b4rrhh.employee.cost_center.application.command.UpdateCostCenterCommand;
import com.b4rrhh.employee.cost_center.application.usecase.CloseCostCenterUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.GetCostCenterByBusinessKeyUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.ListEmployeeCostCentersUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.UpdateCostCenterUseCase;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.infrastructure.rest.dto.CloseCostCenterRequest;
import com.b4rrhh.employee.cost_center.infrastructure.rest.dto.CostCenterResponse;
import com.b4rrhh.employee.cost_center.infrastructure.rest.dto.CreateCostCenterRequest;
import com.b4rrhh.employee.cost_center.infrastructure.rest.dto.UpdateCostCenterRequest;
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
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/cost-centers")
public class CostCenterController {

    private final CreateCostCenterUseCase createCostCenterUseCase;
    private final ListEmployeeCostCentersUseCase listEmployeeCostCentersUseCase;
    private final GetCostCenterByBusinessKeyUseCase getCostCenterByBusinessKeyUseCase;
    private final UpdateCostCenterUseCase updateCostCenterUseCase;
    private final CloseCostCenterUseCase closeCostCenterUseCase;

    public CostCenterController(
            CreateCostCenterUseCase createCostCenterUseCase,
            ListEmployeeCostCentersUseCase listEmployeeCostCentersUseCase,
            GetCostCenterByBusinessKeyUseCase getCostCenterByBusinessKeyUseCase,
            UpdateCostCenterUseCase updateCostCenterUseCase,
            CloseCostCenterUseCase closeCostCenterUseCase
    ) {
        this.createCostCenterUseCase = createCostCenterUseCase;
        this.listEmployeeCostCentersUseCase = listEmployeeCostCentersUseCase;
        this.getCostCenterByBusinessKeyUseCase = getCostCenterByBusinessKeyUseCase;
        this.updateCostCenterUseCase = updateCostCenterUseCase;
        this.closeCostCenterUseCase = closeCostCenterUseCase;
    }

    @PostMapping
    public ResponseEntity<CostCenterResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateCostCenterRequest request
    ) {
        CostCenterAllocation created = createCostCenterUseCase.create(
                new CreateCostCenterCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.costCenterCode(),
                        request.allocationPercentage(),
                        request.startDate(),
                        request.endDate()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<CostCenterResponse>> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        List<CostCenterResponse> response = listEmployeeCostCentersUseCase
                .listByEmployeeBusinessKey(new ListEmployeeCostCentersCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                ))
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{costCenterCode}/{startDate}")
    public ResponseEntity<CostCenterResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String costCenterCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        CostCenterAllocation allocation = getCostCenterByBusinessKeyUseCase.getByBusinessKey(
                new GetCostCenterByBusinessKeyCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        costCenterCode,
                        startDate
                )
        );

        return ResponseEntity.ok(toResponse(allocation));
    }

    @PutMapping("/{costCenterCode}/{startDate}")
    public ResponseEntity<CostCenterResponse> update(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String costCenterCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestBody UpdateCostCenterRequest request
    ) {
        CostCenterAllocation updated = updateCostCenterUseCase.update(
                new UpdateCostCenterCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        costCenterCode,
                        startDate,
                        request.allocationPercentage()
                )
        );

        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping("/{costCenterCode}/{startDate}/close")
    public ResponseEntity<CostCenterResponse> close(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String costCenterCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestBody CloseCostCenterRequest request
    ) {
        CostCenterAllocation closed = closeCostCenterUseCase.close(
                new CloseCostCenterCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        costCenterCode,
                        startDate,
                        request.endDate()
                )
        );

        return ResponseEntity.ok(toResponse(closed));
    }

    private CostCenterResponse toResponse(CostCenterAllocation costCenterAllocation) {
        return new CostCenterResponse(
                costCenterAllocation.getCostCenterCode(),
                costCenterAllocation.getAllocationPercentage(),
                costCenterAllocation.getStartDate(),
                costCenterAllocation.getEndDate()
        );
    }
}
