package com.b4rrhh.employee.journey.application.port;

import java.util.List;

public interface JourneyLaborClassificationReadPort {

    List<JourneyLaborClassificationRecord> findByEmployeeIdOrderByStartDate(Long employeeId);
}
