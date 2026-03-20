package com.b4rrhh.employee.journey.application.port;

import java.util.List;

public interface JourneyPresenceReadPort {

    List<JourneyPresenceRecord> findByEmployeeIdOrderByStartDate(Long employeeId);
}
