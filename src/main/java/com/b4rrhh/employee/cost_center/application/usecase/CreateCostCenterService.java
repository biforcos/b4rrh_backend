package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.CreateCostCenterCommand;
import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.application.service.CostCenterCatalogValidator;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOverlapException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCostCenterService implements CreateCostCenterUseCase {

    private final CostCenterRepository costCenterRepository;
    private final EmployeeCostCenterLookupPort employeeCostCenterLookupPort;
    private final CostCenterCatalogValidator costCenterCatalogValidator;
    private final CostCenterPresenceConsistencyPort costCenterPresenceConsistencyPort;

    public CreateCostCenterService(
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
    public CostCenterAllocation create(CreateCostCenterCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

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

        String costCenterCode = costCenterCatalogValidator.normalizeRequiredCode("costCenterCode", command.costCenterCode());
        costCenterCatalogValidator.validateCostCenterCode(normalizedRuleSystemCode, costCenterCode, command.startDate());

        CostCenterAllocation newAllocation = new CostCenterAllocation(
                employee.employeeId(),
                costCenterCode,
                command.allocationPercentage(),
                command.startDate(),
                command.endDate()
        );

        if (costCenterRepository.existsOverlappingPeriodByCostCenterCode(
                employee.employeeId(),
                costCenterCode,
                newAllocation.getStartDate(),
                newAllocation.getEndDate()
        )) {
            throw new CostCenterOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    costCenterCode,
                    newAllocation.getStartDate(),
                    newAllocation.getEndDate()
            );
        }

        if (!costCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                employee.employeeId(),
                newAllocation.getStartDate(),
                newAllocation.getEndDate()
        )) {
            throw new CostCenterOutsidePresencePeriodException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    newAllocation.getStartDate(),
                    newAllocation.getEndDate()
            );
        }

        costCenterRepository.save(newAllocation);
        return newAllocation;
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
}
