package com.b4rrhh.employee.journey.application.port;

import java.util.List;

public interface JourneyWorkCenterReadPort {

    List<JourneyWorkCenterRecord> findByEmployeeIdOrderByStartDate(Long employeeId);
}
