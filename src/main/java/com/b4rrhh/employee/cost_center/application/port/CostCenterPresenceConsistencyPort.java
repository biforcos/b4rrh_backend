package com.b4rrhh.employee.cost_center.application.port;

import java.time.LocalDate;

public interface CostCenterPresenceConsistencyPort {

    boolean existsPresenceContainingPeriod(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );
}
