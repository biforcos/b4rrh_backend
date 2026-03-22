package com.b4rrhh.employee.workcenter.domain.port;

import java.time.LocalDate;

public interface EmployeePresencePort {

    boolean existsPresenceContainingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);
}
