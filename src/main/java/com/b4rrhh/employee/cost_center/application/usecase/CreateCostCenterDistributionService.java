package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.application.service.CostCenterCatalogValidator;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionConflictException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import com.b4rrhh.employee.cost_center.domain.service.CostCenterDistributionTimelineValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateCostCenterDistributionService implements CreateCostCenterDistributionUseCase {

    private final CostCenterRepository costCenterRepository;
    private final EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    private final CostCenterCatalogValidator costCenterCatalogValidator;
    private final CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;
    private final CostCenterDistributionTimelineValidator timelineValidator;

    public CreateCostCenterDistributionService(
            CostCenterRepository costCenterRepository,
            EmployeeCostCenterLookupPort employeeCostCenterLookupPort,
            CostCenterCatalogValidator costCenterCatalogValidator,
            CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort,
            CostCenterDistributionTimelineValidator timelineValidator
    ) {
        this.costCenterRepository = costCenterRepository;
        this.employeeCostCenterLookupPort = employeeCostCenterLookupPort;
        this.costCenterCatalogValidator = costCenterCatalogValidator;
        this.costCenterPresenceConsistencyPort = costCenterPresenceConsistencyPort;
        this.timelineValidator = timelineValidator;
    }

    @Override
    @Transactional
    public CostCenterDistributionWindow create(CreateCostCenterDistributionCommand command) {
        String ruleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String employeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String employeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        if (command.startDate() == null) {
            throw new CostCenterDistributionInvalidException("startDate is required");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new CostCenterDistributionInvalidException("at least one distribution item is required");
        }

        EmployeeCostCenterContext employee = employeeCostCenterLookupPort
                .findByBusinessKeyForUpdate(ruleSystemCode, employeeTypeCode, employeeNumber)
                .orElseThrow(() -> new CostCenterEmployeeNotFoundException(
                        ruleSystemCode, employeeTypeCode, employeeNumber
                ));

        // Build allocations (catalog validation + row-level validation happens in constructor)
        List<CostCenterAllocation> allocations = new ArrayList<>();
        for (CostCenterDistributionItem item : command.items()) {
            String costCenterCode = costCenterCatalogValidator.normalizeRequiredCode("costCenterCode", item.costCenterCode());
            costCenterCatalogValidator.validateCostCenterCode(ruleSystemCode, costCenterCode, command.startDate());
            allocations.add(new CostCenterAllocation(
                    employee.employeeId(),
                    costCenterCode,
                    item.allocationPercentage(),
                    command.startDate(),
                    null
            ));
        }

        // Validate sum <= 100
        timelineValidator.validateWindow(allocations);

        // Reject if any active line already exists at startDate (would create concurrent windows)
        List<CostCenterAllocation> activeAtStartDate = costCenterRepository.findActiveAtDate(
                employee.employeeId(), command.startDate()
        );
        if (!activeAtStartDate.isEmpty()) {
            throw new CostCenterDistributionConflictException(
                    ruleSystemCode, employeeTypeCode, employeeNumber, command.startDate()
            );
        }

        // Validate presence containment for an open-ended period
        if (!costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                employee.employeeId(), command.startDate(), null
        )) {
            throw new CostCenterOutsidePresencePeriodException(
                    ruleSystemCode, employeeTypeCode, employeeNumber, command.startDate(), null
            );
        }

        costCenterRepository.saveAll(allocations);

        return new CostCenterDistributionWindow(command.startDate(), null, allocations);
    }

    private String normalizeRuleSystemCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }
        return value.trim().toUpperCase();
    }

    private String normalizeEmployeeTypeCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeTypeCode is required");
        }
        return value.trim().toUpperCase();
    }

    private String normalizeEmployeeNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeNumber is required");
        }
        return value.trim();
    }
}
