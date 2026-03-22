package com.b4rrhh.employee.workcenter.domain.port;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkCenterAssignmentRepository {

    Optional<WorkCenterAssignment> findByEmployeeIdAndWorkCenterAssignmentNumber(Long employeeId, Integer workCenterAssignmentNumber);

    List<WorkCenterAssignment> findByEmployeeIdOrderByStartDate(Long employeeId);

    boolean existsOverlappingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);

    boolean existsOverlappingPeriodExcludingAssignment(
            Long employeeId,
            Integer excludedWorkCenterAssignmentNumber,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<Integer> findMaxWorkCenterAssignmentNumberByEmployeeId(Long employeeId);

    WorkCenterAssignment save(WorkCenterAssignment workCenterAssignment);
}
