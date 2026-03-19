package com.b4rrhh.employee.labor_classification.application.port;

import java.time.LocalDate;
import java.util.List;

public interface LaborClassificationPresenceConsistencyPort {

    boolean existsPresenceContainingPeriod(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<PresencePeriod> findPresencePeriodsByEmployeeIdOrderByStartDate(Long employeeId);
}
