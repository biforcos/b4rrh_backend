package com.b4rrhh.employee.workcenter.application.port;

import java.time.LocalDate;
import java.util.List;

public interface WorkCenterPresenceConsistencyPort {

    boolean existsPresenceContainingPeriod(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsPresenceStartingAt(Long employeeId, LocalDate startDate);

    List<PresencePeriod> findPresencePeriodsByEmployeeIdOrderByStartDate(Long employeeId);
}