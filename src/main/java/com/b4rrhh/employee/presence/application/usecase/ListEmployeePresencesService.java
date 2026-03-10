package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListEmployeePresencesService implements ListEmployeePresencesUseCase {

    private final PresenceRepository presenceRepository;
    private final EmployeePresenceLookupPort employeePresenceLookupPort;

    public ListEmployeePresencesService(PresenceRepository presenceRepository, EmployeePresenceLookupPort employeePresenceLookupPort) {
        this.presenceRepository = presenceRepository;
        this.employeePresenceLookupPort = employeePresenceLookupPort;
    }

    @Override
    public List<Presence> listByEmployeeId(Long employeeId) {
        employeePresenceLookupPort.findById(employeeId)
                .orElseThrow(() -> new PresenceEmployeeNotFoundException(employeeId));

        return presenceRepository.findByEmployeeIdOrderByStartDate(employeeId);
    }
}
