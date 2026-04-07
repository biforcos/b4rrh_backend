package com.b4rrhh.employee.workcenter.domain.port;

import java.time.LocalDate;
import java.util.Optional;

public interface EmployeeActiveCompanyLookupPort {

    Optional<String> findActiveCompanyCode(Long employeeId, LocalDate referenceDate);
}