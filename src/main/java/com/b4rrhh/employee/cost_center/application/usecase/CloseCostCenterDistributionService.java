package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CloseCostCenterDistributionService implements CloseCostCenterDistributionUseCase {

    private final CostCenterRepository costCenterRepository;
    private final EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    private final CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;

    public CloseCostCenterDistributionService(
            CostCenterRepository costCenterRepository,
            EmployeeCostCenterLookupPort employeeCostCenterLookupPort,
            CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort
    ) {
        this.costCenterRepository = costCenterRepository;
        this.employeeCostCenterLookupPort = employeeCostCenterLookupPort;
        this.costCenterPresenceConsistencyPort = costCenterPresenceConsistencyPort;
    }

    @Override
    @Transactional
    public CostCenterDistributionWindow close(CloseCostCenterDistributionCommand command) {
        String ruleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String employeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String employeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        if (command.windowStartDate() == null) {
            throw new CostCenterDistributionInvalidException("windowStartDate is required");
        }
        if (command.endDate() == null) {
            throw new CostCenterDistributionInvalidException("endDate is required");
        }
        if (command.endDate().isBefore(command.windowStartDate())) {
            throw new CostCenterDistributionInvalidException(
                    "endDate must not be before windowStartDate"
            );
        }

        EmployeeCostCenterContext employee = employeeCostCenterLookupPort
                .findByBusinessKeyForUpdate(ruleSystemCode, employeeTypeCode, employeeNumber)
                .orElseThrow(() -> new CostCenterEmployeeNotFoundException(
                        ruleSystemCode, employeeTypeCode, employeeNumber
                ));

        List<CostCenterAllocation> windowItems = costCenterRepository.findByEmployeeIdAndStartDate(
                employee.employeeId(), command.windowStartDate()
        );

        if (windowItems.isEmpty()) {
            throw new CostCenterDistributionNotFoundException(
                    ruleSystemCode, employeeTypeCode, employeeNumber, command.windowStartDate()
            );
        }

        // Validate presence containment for the closed period
        if (!costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                employee.employeeId(), command.windowStartDate(), command.endDate()
        )) {
            throw new CostCenterOutsidePresencePeriodException(
                    ruleSystemCode, employeeTypeCode, employeeNumber,
                    command.windowStartDate(), command.endDate()
            );
        }

        costCenterRepository.closeAllForWindow(employee.employeeId(), command.windowStartDate(), command.endDate());

        // Build the closed window for the response
        List<CostCenterAllocation> closedItems = windowItems.stream()
                .map(item -> item.close(command.endDate()))
                .toList();

        return new CostCenterDistributionWindow(command.windowStartDate(), command.endDate(), closedItems);
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
