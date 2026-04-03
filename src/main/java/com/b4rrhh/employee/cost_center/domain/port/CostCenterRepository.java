package com.b4rrhh.employee.cost_center.domain.port;

import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;

import java.time.LocalDate;
import java.util.List;

public interface CostCenterRepository {

    List<CostCenterAllocation> findByEmployeeIdOrderByStartDate(Long employeeId);

    /** Returns all allocations for the employee whose period includes the given date. */
    List<CostCenterAllocation> findActiveAtDate(Long employeeId, LocalDate date);

    /** Returns all allocations belonging to the window identified by (employeeId, startDate). */
    List<CostCenterAllocation> findByEmployeeIdAndStartDate(Long employeeId, LocalDate startDate);

    void saveAll(List<CostCenterAllocation> allocations);

    /** Closes all open allocations in a window (identified by employeeId + startDate) with the given endDate. */
    void closeAllForWindow(Long employeeId, LocalDate windowStartDate, LocalDate closeDate);
}
