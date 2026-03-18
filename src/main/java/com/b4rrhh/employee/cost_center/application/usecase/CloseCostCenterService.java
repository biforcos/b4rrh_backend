package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.CloseCostCenterCommand;
import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.application.service.CostCenterCatalogValidator;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterAllocationNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterAlreadyClosedException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CloseCostCenterService implements CloseCostCenterUseCase {

    private final CostCenterRepository costCenterRepository;
    private final EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    private final CostCenterCatalogValidator costCenterCatalogValidator;
    private final CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;

    public CloseCostCenterService(
            CostCenterRepository costCenterRepository,
            EmployeeCostCenterLookupPort employeeCostCenterLookupPort,
            CostCenterCatalogValidator costCenterCatalogValidator,
            CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort
    ) {
        this.costCenterRepository = costCenterRepository;
        this.employeeCostCenterLookupPort = employeeCostCenterLookupPort;
        this.costCenterCatalogValidator = costCenterCatalogValidator;
        this.costCenterPresenceConsistencyPort = costCenterPresenceConsistencyPort;
    }

    @Override
    @Transactional
    public CostCenterAllocation close(CloseCostCenterCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        String normalizedCostCenterCode = costCenterCatalogValidator.normalizeRequiredCode("costCenterCode", command.costCenterCode());
        LocalDate normalizedStartDate = normalizeStartDate(command.startDate());

        EmployeeCostCenterContext employee = employeeCostCenterLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new CostCenterEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        CostCenterAllocation existing = costCenterRepository
                .findByEmployeeIdAndCostCenterCodeAndStartDate(
                        employee.employeeId(),
                        normalizedCostCenterCode,
                        normalizedStartDate
                )
                .orElseThrow(() -> new CostCenterAllocationNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedCostCenterCode,
                        normalizedStartDate
                ));

        if (!existing.isActive()) {
            throw new CostCenterAlreadyClosedException(existing.getCostCenterCode(), existing.getStartDate());
        }

        CostCenterAllocation closed = existing.close(command.endDate());

        if (!costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                employee.employeeId(),
                closed.getStartDate(),
                closed.getEndDate()
        )) {
            throw new CostCenterOutsidePresencePeriodException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    closed.getStartDate(),
                    closed.getEndDate()
            );
        }

        costCenterRepository.update(closed);
        return closed;
    }

    private String normalizeRuleSystemCode(String ruleSystemCode) {
        if (ruleSystemCode == null || ruleSystemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }

        return ruleSystemCode.trim().toUpperCase();
    }

    private String normalizeEmployeeTypeCode(String employeeTypeCode) {
        if (employeeTypeCode == null || employeeTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeTypeCode is required");
        }

        return employeeTypeCode.trim().toUpperCase();
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeNumber is required");
        }

        return employeeNumber.trim();
    }

    private LocalDate normalizeStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }

        return startDate;
    }
}
