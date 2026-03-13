package com.b4rrhh.employee.presence.domain.port;

import com.b4rrhh.employee.presence.domain.model.Presence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PresenceRepository {
    Optional<Presence> findByEmployeeIdAndPresenceNumber(Long employeeId, Integer presenceNumber);

    List<Presence> findByEmployeeIdOrderByStartDate(Long employeeId);

    boolean existsOverlappingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);

    boolean existsActivePresence(Long employeeId);

    Optional<Integer> findMaxPresenceNumberByEmployeeId(Long employeeId);

    Presence save(Presence presence);
}
