package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.command.ListEmployeeCostCentersCommand;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterEmployeeNotFoundException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListEmployeeCostCentersService implements ListEmployeeCostCentersUseCase {

    private final CostCenterRepository costCenterRepository;
    private final EmployeeCostCenterLookupPort employeeCostCenterLookupPort;

    public ListEmployeeCostCentersService(
            CostCenterRepository costCenterRepository,
            EmployeeCostCenterLookupPort employeeCostCenterLookupPort
    ) {
        this.costCenterRepository = costCenterRepository;
        this.employeeCostCenterLookupPort = employeeCostCenterLookupPort;
    }

    @Override
    public List<CostCenterAllocation> listByEmployeeBusinessKey(ListEmployeeCostCentersCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        EmployeeCostCenterContext employee = employeeCostCenterLookupPort
                .findByBusinessKey(normalizedRuleSystemCode, normalizedEmployeeTypeCode, normalizedEmployeeNumber)
                .orElseThrow(() -> new CostCenterEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        return costCenterRepository.findByEmployeeIdOrderByStartDate(employee.employeeId());
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
