package com.b4rrhh.employee.journey.application.port;

import java.util.List;

public interface JourneyContractReadPort {

    List<JourneyContractRecord> findByEmployeeIdOrderByStartDate(Long employeeId);
}
