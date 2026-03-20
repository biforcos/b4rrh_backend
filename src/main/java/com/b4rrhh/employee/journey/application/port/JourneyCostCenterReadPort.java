package com.b4rrhh.employee.journey.application.port;

import java.util.List;

public interface JourneyCostCenterReadPort {

    List<JourneyCostCenterRecord> findByEmployeeIdOrderByStartDate(Long employeeId);
}
