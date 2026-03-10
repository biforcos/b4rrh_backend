package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetPresenceByIdService implements GetPresenceByIdUseCase {

    private final PresenceRepository presenceRepository;
    private final EmployeePresenceLookupPort employeePresenceLookupPort;

    public GetPresenceByIdService(PresenceRepository presenceRepository, EmployeePresenceLookupPort employeePresenceLookupPort) {
        this.presenceRepository = presenceRepository;
        this.employeePresenceLookupPort = employeePresenceLookupPort;
    }

    @Override
    public Optional<Presence> getById(Long employeeId, Long presenceId) {
        employeePresenceLookupPort.findById(employeeId)
                .orElseThrow(() -> new PresenceEmployeeNotFoundException(employeeId));

        return presenceRepository.findByIdAndEmployeeId(presenceId, employeeId);
    }
}
