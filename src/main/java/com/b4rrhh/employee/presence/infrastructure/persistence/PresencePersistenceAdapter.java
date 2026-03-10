package com.b4rrhh.employee.presence.infrastructure.persistence;

import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class PresencePersistenceAdapter implements PresenceRepository {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final SpringDataPresenceRepository springDataPresenceRepository;

    public PresencePersistenceAdapter(SpringDataPresenceRepository springDataPresenceRepository) {
        this.springDataPresenceRepository = springDataPresenceRepository;
    }

    @Override
    public Optional<Presence> findByIdAndEmployeeId(Long presenceId, Long employeeId) {
        return springDataPresenceRepository.findByIdAndEmployeeId(presenceId, employeeId)
                .map(this::toDomain);
    }

    @Override
    public List<Presence> findByEmployeeIdOrderByStartDate(Long employeeId) {
        return springDataPresenceRepository.findByEmployeeIdOrderByStartDateAsc(employeeId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsOverlappingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveEndDate = endDate == null ? MAX_DATE : endDate;
        return springDataPresenceRepository.existsOverlappingPeriod(employeeId, startDate, effectiveEndDate, MAX_DATE);
    }

    @Override
    public boolean existsActivePresence(Long employeeId) {
        return springDataPresenceRepository.existsByEmployeeIdAndEndDateIsNull(employeeId);
    }

    @Override
    public Optional<Integer> findMaxPresenceNumberByEmployeeId(Long employeeId) {
        return Optional.ofNullable(springDataPresenceRepository.findMaxPresenceNumberByEmployeeId(employeeId));
    }

    @Override
    public Presence save(Presence presence) {
        PresenceEntity entity = toEntity(presence);
        PresenceEntity saved = springDataPresenceRepository.save(entity);
        return toDomain(saved);
    }

    private Presence toDomain(PresenceEntity entity) {
        return new Presence(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getPresenceNumber(),
                entity.getCompanyCode(),
                entity.getEntryReasonCode(),
                entity.getExitReasonCode(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PresenceEntity toEntity(Presence presence) {
        PresenceEntity entity = new PresenceEntity();
        entity.setId(presence.getId());
        entity.setEmployeeId(presence.getEmployeeId());
        entity.setPresenceNumber(presence.getPresenceNumber());
        entity.setCompanyCode(presence.getCompanyCode());
        entity.setEntryReasonCode(presence.getEntryReasonCode());
        entity.setExitReasonCode(presence.getExitReasonCode());
        entity.setStartDate(presence.getStartDate());
        entity.setEndDate(presence.getEndDate());
        return entity;
    }
}
