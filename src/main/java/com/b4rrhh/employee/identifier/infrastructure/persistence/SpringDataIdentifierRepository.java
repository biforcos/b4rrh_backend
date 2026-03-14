package com.b4rrhh.employee.identifier.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataIdentifierRepository extends JpaRepository<IdentifierEntity, Long> {

    List<IdentifierEntity> findByEmployeeIdOrderByIdentifierTypeCodeAsc(Long employeeId);

    Optional<IdentifierEntity> findByEmployeeIdAndIdentifierTypeCode(Long employeeId, String identifierTypeCode);

    boolean existsByEmployeeIdAndIsPrimaryTrue(Long employeeId);

    boolean existsByEmployeeIdAndIsPrimaryTrueAndIdentifierTypeCodeNot(Long employeeId, String identifierTypeCode);

    void deleteByEmployeeIdAndIdentifierTypeCode(Long employeeId, String identifierTypeCode);
}
