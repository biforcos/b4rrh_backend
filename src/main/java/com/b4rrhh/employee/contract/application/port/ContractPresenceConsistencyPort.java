package com.b4rrhh.employee.contract.application.port;

import java.time.LocalDate;
import java.util.List;

public interface ContractPresenceConsistencyPort {

    boolean existsPresenceContainingPeriod(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<PresencePeriod> findPresencePeriodsByEmployeeIdOrderByStartDate(Long employeeId);
}
