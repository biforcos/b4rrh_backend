package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.application.port.WorkCenterPresenceConsistencyPort;
import com.b4rrhh.employee.workcenter.domain.port.EmployeePresencePort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EmployeePresenceAdapter implements EmployeePresencePort {

    private final WorkCenterPresenceConsistencyPort workCenterPresenceConsistencyPort;

    public EmployeePresenceAdapter(WorkCenterPresenceConsistencyPort workCenterPresenceConsistencyPort) {
        this.workCenterPresenceConsistencyPort = workCenterPresenceConsistencyPort;
    }

    @Override
    public boolean existsPresenceContainingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return workCenterPresenceConsistencyPort.existsPresenceContainingPeriod(employeeId, startDate, endDate);
    }
}
