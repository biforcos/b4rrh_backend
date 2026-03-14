package com.b4rrhh.employee.identifier.infrastructure.persistence;

import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.domain.port.IdentifierRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class IdentifierPersistenceAdapter implements IdentifierRepository {

    private final SpringDataIdentifierRepository springDataIdentifierRepository;

    public IdentifierPersistenceAdapter(SpringDataIdentifierRepository springDataIdentifierRepository) {
        this.springDataIdentifierRepository = springDataIdentifierRepository;
    }

    @Override
    public List<Identifier> findByEmployeeIdOrderByIdentifierTypeCode(Long employeeId) {
        return springDataIdentifierRepository.findByEmployeeIdOrderByIdentifierTypeCodeAsc(employeeId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Identifier> findByEmployeeIdAndIdentifierTypeCode(Long employeeId, String identifierTypeCode) {
        return springDataIdentifierRepository.findByEmployeeIdAndIdentifierTypeCode(employeeId, identifierTypeCode)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmployeeIdAndIsPrimaryTrue(Long employeeId) {
        return springDataIdentifierRepository.existsByEmployeeIdAndIsPrimaryTrue(employeeId);
    }

    @Override
    public boolean existsByEmployeeIdAndIsPrimaryTrueAndIdentifierTypeCodeNot(Long employeeId, String identifierTypeCode) {
        return springDataIdentifierRepository.existsByEmployeeIdAndIsPrimaryTrueAndIdentifierTypeCodeNot(
                employeeId,
                identifierTypeCode
        );
    }

    @Override
    public Identifier save(Identifier identifier) {
        IdentifierEntity entity = toEntity(identifier);
        IdentifierEntity saved = springDataIdentifierRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteByEmployeeIdAndIdentifierTypeCode(Long employeeId, String identifierTypeCode) {
        springDataIdentifierRepository.deleteByEmployeeIdAndIdentifierTypeCode(employeeId, identifierTypeCode);
    }

    private Identifier toDomain(IdentifierEntity entity) {
        return new Identifier(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getIdentifierTypeCode(),
                entity.getIdentifierValue(),
                entity.getIssuingCountryCode(),
                entity.getExpirationDate(),
                entity.isPrimary(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private IdentifierEntity toEntity(Identifier identifier) {
        IdentifierEntity entity = new IdentifierEntity();
        entity.setId(identifier.getId());
        entity.setEmployeeId(identifier.getEmployeeId());
        entity.setIdentifierTypeCode(identifier.getIdentifierTypeCode());
        entity.setIdentifierValue(identifier.getIdentifierValue());
        entity.setIssuingCountryCode(identifier.getIssuingCountryCode());
        entity.setExpirationDate(identifier.getExpirationDate());
        entity.setPrimary(identifier.isPrimary());
        entity.setCreatedAt(identifier.getCreatedAt());
        entity.setUpdatedAt(identifier.getUpdatedAt());
        return entity;
    }
}
