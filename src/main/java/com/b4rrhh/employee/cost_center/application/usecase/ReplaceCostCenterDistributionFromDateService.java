package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.application.service.CostCenterCatalogValidator;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import com.b4rrhh.employee.cost_center.domain.service.CostCenterDistributionTimelineValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReplaceCostCenterDistributionFromDateService implements ReplaceCostCenterDistributionFromDateUseCase {

    private final CostCenterRepository costCenterRepository;
    private final EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    private final CostCenterCatalogValidator costCenterCatalogValidator;
    private final CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;
    private final CostCenterDistributionTimelineValidator timelineValidator;

    public ReplaceCostCenterDistributionFromDateService(
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
    public CostCenterDistributionWindow replaceFromDate(ReplaceCostCenterDistributionFromDateCommand command) {
        String ruleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String employeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String employeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        if (command.effectiveDate() == null) {
            throw new CostCenterDistributionInvalidException("effectiveDate is required");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new CostCenterDistributionInvalidException("at least one distribution item is required");
        }

        EmployeeCostCenterContext employee = employeeCostCenterLookupPort
                .findByBusinessKeyForUpdate(ruleSystemCode, employeeTypeCode, employeeNumber)
                .orElseThrow(() -> new CostCenterEmployeeNotFoundException(
                        ruleSystemCode, employeeTypeCode, employeeNumber
                ));

        // Find the active window at effectiveDate - 1 day (the window being replaced)
        LocalDate dayBeforeEffective = command.effectiveDate().minusDays(1);
        List<CostCenterAllocation> activeBeforeEffective = costCenterRepository.findActiveAtDate(
                employee.employeeId(), dayBeforeEffective
        );

        if (activeBeforeEffective.isEmpty()) {
            // No active window before effectiveDate — cannot replace what doesn't exist
            throw new CostCenterDistributionNotFoundException(
                    ruleSystemCode, employeeTypeCode, employeeNumber,
                    "no active distribution found before effectiveDate=" + command.effectiveDate()
            );
        }

        // The window being replaced is identified by the startDate common to the active lines
        LocalDate windowStartDate = activeBeforeEffective.get(0).getStartDate();

        // Build new allocations (catalog validation + row-level validation in constructor)
        List<CostCenterAllocation> newAllocations = new ArrayList<>();
        for (CostCenterDistributionItem item : command.items()) {
            String costCenterCode = costCenterCatalogValidator.normalizeRequiredCode("costCenterCode", item.costCenterCode());
            costCenterCatalogValidator.validateCostCenterCode(ruleSystemCode, costCenterCode, command.effectiveDate());
            newAllocations.add(new CostCenterAllocation(
                    employee.employeeId(),
                    costCenterCode,
                    item.allocationPercentage(),
                    command.effectiveDate(),
                    null
            ));
        }

        // Validate sum <= 100 for new window
        timelineValidator.validateWindow(newAllocations);

        // Validate presence containment for the new open-ended window
        if (!costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                employee.employeeId(), command.effectiveDate(), null
        )) {
            throw new CostCenterOutsidePresencePeriodException(
                    ruleSystemCode, employeeTypeCode, employeeNumber, command.effectiveDate(), null
            );
        }

        // Close the active window at effectiveDate - 1
        costCenterRepository.closeAllForWindow(employee.employeeId(), windowStartDate, dayBeforeEffective);

        // Save the new window
        costCenterRepository.saveAll(newAllocations);

        return new CostCenterDistributionWindow(command.effectiveDate(), null, newAllocations);
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
