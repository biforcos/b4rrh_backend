package com.b4rrhh.employee.cost_center.application.usecase;

import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Closes all active cost center lines for an employee at the termination date.
 * Identifies the active window by its startDate and closes all its lines together.
 * Does not fail if no active distribution exists — termination may precede any cost center setup.
 */
@Service
public class CloseActiveCostCenterDistributionAtTerminationService
        implements CloseActiveCostCenterDistributionAtTerminationUseCase {

    private final CostCenterRepository costCenterRepository;
    private final EmployeeCostCenterLookupPort employeeCostCenterLookupPort;

    public CloseActiveCostCenterDistributionAtTerminationService(
            CostCenterRepository costCenterRepository,
            EmployeeCostCenterLookupPort employeeCostCenterLookupPort
    ) {
        this.costCenterRepository = costCenterRepository;
        this.employeeCostCenterLookupPort = employeeCostCenterLookupPort;
    }

    @Override
    @Transactional
    public void closeIfPresent(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate terminationDate
    ) {
        Optional<EmployeeCostCenterContext> employeeOpt = employeeCostCenterLookupPort
                .findByBusinessKeyForUpdate(ruleSystemCode, employeeTypeCode, employeeNumber);

        if (employeeOpt.isEmpty()) {
            return;
        }

        Long employeeId = employeeOpt.get().employeeId();

        List<CostCenterAllocation> activeLines = costCenterRepository.findActiveAtDate(
                employeeId, terminationDate
        );

        if (activeLines.isEmpty()) {
            return;
        }

        // Group active lines by their startDate to identify the window
        LocalDate windowStartDate = activeLines.get(0).getStartDate();
        costCenterRepository.closeAllForWindow(employeeId, windowStartDate, terminationDate);
    }
}
