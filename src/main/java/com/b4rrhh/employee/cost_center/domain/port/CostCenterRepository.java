package com.b4rrhh.employee.cost_center.domain.port;

import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CostCenterRepository {

    Optional<CostCenterAllocation> findByEmployeeIdAndCostCenterCodeAndStartDate(
            Long employeeId,
            String costCenterCode,
            LocalDate startDate
    );

    List<CostCenterAllocation> findByEmployeeIdOrderByStartDate(Long employeeId);

    boolean existsOverlappingPeriodByCostCenterCode(
            Long employeeId,
            String costCenterCode,
            LocalDate startDate,
            LocalDate endDate
    );

    void save(CostCenterAllocation costCenterAllocation);

    void update(CostCenterAllocation costCenterAllocation);
}
