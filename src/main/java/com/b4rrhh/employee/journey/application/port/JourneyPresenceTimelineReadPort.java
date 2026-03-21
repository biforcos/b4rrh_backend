package com.b4rrhh.employee.journey.application.port;

import java.util.List;

public interface JourneyPresenceTimelineReadPort {

    List<JourneyPresenceTimelineRecord> findByEmployeeIdOrderByStartDate(Long employeeId);
}