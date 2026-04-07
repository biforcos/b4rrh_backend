package com.b4rrhh.employee.working_time.application.port;

import java.time.LocalDate;

public interface WorkingTimePresenceConsistencyPort {

    boolean existsPresenceContainingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);
}