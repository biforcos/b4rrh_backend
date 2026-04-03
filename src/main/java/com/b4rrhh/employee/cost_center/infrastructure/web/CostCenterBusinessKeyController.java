package com.b4rrhh.employee.cost_center.infrastructure.web;

import com.b4rrhh.employee.cost_center.application.usecase.CloseCostCenterDistributionCommand;
import com.b4rrhh.employee.cost_center.application.usecase.CloseCostCenterDistributionUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.CostCenterDistributionItem;
import com.b4rrhh.employee.cost_center.application.usecase.CostCenterDistributionReadModel;
import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterDistributionCommand;
import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterDistributionUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.GetCurrentCostCenterDistributionQuery;
import com.b4rrhh.employee.cost_center.application.usecase.GetCurrentCostCenterDistributionUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.ListCostCenterDistributionHistoryQuery;
import com.b4rrhh.employee.cost_center.application.usecase.ListCostCenterDistributionHistoryUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.ReplaceCostCenterDistributionFromDateCommand;
import com.b4rrhh.employee.cost_center.application.usecase.ReplaceCostCenterDistributionFromDateUseCase;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CloseCostCenterDistributionRequest;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterCurrentDistributionResponse;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterDistributionHistoryResponse;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterDistributionItemResponse;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterDistributionWindowResponse;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterEmployeeKeyResponse;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CreateCostCenterDistributionRequest;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.ReplaceCostCenterDistributionFromDateRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/cost-centers")
public class CostCenterBusinessKeyController {

    private final CreateCostCenterDistributionUseCase createCostCenterDistributionUseCase;
    private final GetCurrentCostCenterDistributionUseCase getCurrentCostCenterDistributionUseCase;
    private final ListCostCenterDistributionHistoryUseCase listCostCenterDistributionHistoryUseCase;
    private final ReplaceCostCenterDistributionFromDateUseCase replaceCostCenterDistributionFromDateUseCase;
    private final CloseCostCenterDistributionUseCase closeCostCenterDistributionUseCase;

    public CostCenterBusinessKeyController(
            CreateCostCenterDistributionUseCase createCostCenterDistributionUseCase,
            GetCurrentCostCenterDistributionUseCase getCurrentCostCenterDistributionUseCase,
            ListCostCenterDistributionHistoryUseCase listCostCenterDistributionHistoryUseCase,
            ReplaceCostCenterDistributionFromDateUseCase replaceCostCenterDistributionFromDateUseCase,
            CloseCostCenterDistributionUseCase closeCostCenterDistributionUseCase
    ) {
        this.createCostCenterDistributionUseCase = createCostCenterDistributionUseCase;
        this.getCurrentCostCenterDistributionUseCase = getCurrentCostCenterDistributionUseCase;
        this.listCostCenterDistributionHistoryUseCase = listCostCenterDistributionHistoryUseCase;
        this.replaceCostCenterDistributionFromDateUseCase = replaceCostCenterDistributionFromDateUseCase;
        this.closeCostCenterDistributionUseCase = closeCostCenterDistributionUseCase;
    }

    @PostMapping("/distributions")
    public ResponseEntity<CostCenterDistributionWindowResponse> createDistribution(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateCostCenterDistributionRequest request
    ) {
        List<CostCenterDistributionItem> items = toCommandItems(request.items());

        CostCenterDistributionWindow created = createCostCenterDistributionUseCase.create(
                new CreateCostCenterDistributionCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.startDate(),
                        items
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toWindowResponse(created));
    }

    @GetMapping
    public ResponseEntity<CostCenterDistributionHistoryResponse> listHistory(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        CostCenterDistributionReadModel.History history = listCostCenterDistributionHistoryUseCase.listHistory(
                new ListCostCenterDistributionHistoryQuery(ruleSystemCode, employeeTypeCode, employeeNumber)
        );

        return ResponseEntity.ok(toHistoryResponse(history));
    }

    @GetMapping("/current")
    public ResponseEntity<CostCenterCurrentDistributionResponse> getCurrent(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        CostCenterDistributionReadModel.CurrentDistribution current = getCurrentCostCenterDistributionUseCase.getCurrent(
                new GetCurrentCostCenterDistributionQuery(ruleSystemCode, employeeTypeCode, employeeNumber)
        );

        return ResponseEntity.ok(toCurrentResponse(current));
    }

    @PostMapping("/replace-from-date")
    public ResponseEntity<CostCenterDistributionWindowResponse> replaceFromDate(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody ReplaceCostCenterDistributionFromDateRequest request
    ) {
        List<CostCenterDistributionItem> items = toCommandItems(request.items());

        CostCenterDistributionWindow replaced = replaceCostCenterDistributionFromDateUseCase.replaceFromDate(
                new ReplaceCostCenterDistributionFromDateCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.effectiveDate(),
                        items
                )
        );

        return ResponseEntity.ok(toWindowResponse(replaced));
    }

    @PostMapping("/distributions/{startDate}/close")
    public ResponseEntity<CostCenterDistributionWindowResponse> closeDistribution(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestBody CloseCostCenterDistributionRequest request
    ) {
        CostCenterDistributionWindow closed = closeCostCenterDistributionUseCase.close(
                new CloseCostCenterDistributionCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        startDate,
                        request.endDate()
                )
        );

        return ResponseEntity.ok(toWindowResponse(closed));
    }

    // ---- mapping helpers ----

    private List<CostCenterDistributionItem> toCommandItems(
            List<com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterDistributionItemRequest> requestItems
    ) {
        if (requestItems == null) {
            return List.of();
        }
        return requestItems.stream()
                .map(i -> new CostCenterDistributionItem(i.costCenterCode(), i.allocationPercentage()))
                .toList();
    }

    private CostCenterDistributionWindowResponse toWindowResponse(CostCenterDistributionWindow window) {
        List<CostCenterDistributionItemResponse> items = window.getItems().stream()
                .map(item -> new CostCenterDistributionItemResponse(
                        item.getCostCenterCode(),
                        null, // no enrichment at command response level
                        item.getAllocationPercentage()
                ))
                .toList();

        return new CostCenterDistributionWindowResponse(
                window.getStartDate(),
                window.getEndDate(),
                window.getTotalAllocationPercentage(),
                items
        );
    }

    private CostCenterCurrentDistributionResponse toCurrentResponse(
            CostCenterDistributionReadModel.CurrentDistribution model
    ) {
        CostCenterEmployeeKeyResponse employeeKey = new CostCenterEmployeeKeyResponse(
                model.ruleSystemCode(), model.employeeTypeCode(), model.employeeNumber()
        );

        CostCenterDistributionWindowResponse window = null;
        if (model.currentDistribution() != null) {
            window = toReadModelWindowResponse(model.currentDistribution());
        }

        return new CostCenterCurrentDistributionResponse(employeeKey, window);
    }

    private CostCenterDistributionHistoryResponse toHistoryResponse(
            CostCenterDistributionReadModel.History model
    ) {
        CostCenterEmployeeKeyResponse employeeKey = new CostCenterEmployeeKeyResponse(
                model.ruleSystemCode(), model.employeeTypeCode(), model.employeeNumber()
        );

        List<CostCenterDistributionWindowResponse> windows = model.windows().stream()
                .map(this::toReadModelWindowResponse)
                .toList();

        return new CostCenterDistributionHistoryResponse(employeeKey, windows);
    }

    private CostCenterDistributionWindowResponse toReadModelWindowResponse(
            CostCenterDistributionReadModel.Window window
    ) {
        List<CostCenterDistributionItemResponse> items = window.items().stream()
                .map(item -> new CostCenterDistributionItemResponse(
                        item.costCenterCode(),
                        item.costCenterName(),
                        item.allocationPercentage()
                ))
                .toList();

        return new CostCenterDistributionWindowResponse(
                window.startDate(), window.endDate(), window.totalAllocationPercentage(), items
        );
    }
}
