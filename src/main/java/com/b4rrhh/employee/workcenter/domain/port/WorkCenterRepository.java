package com.b4rrhh.employee.workcenter.domain.port;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkCenterRepository {

    Optional<WorkCenter> findByEmployeeIdAndWorkCenterAssignmentNumber(Long employeeId, Integer workCenterAssignmentNumber);

    List<WorkCenter> findByEmployeeIdOrderByStartDate(Long employeeId);

    boolean existsOverlappingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);

    boolean existsOverlappingPeriodExcludingAssignment(
            Long employeeId,
            Integer excludedWorkCenterAssignmentNumber,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<Integer> findMaxWorkCenterAssignmentNumberByEmployeeId(Long employeeId);

    WorkCenter save(WorkCenter workCenter);

    void delete(WorkCenter workCenter);
}