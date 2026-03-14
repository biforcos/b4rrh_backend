package com.b4rrhh.employee.identifier.domain.port;

import com.b4rrhh.employee.identifier.domain.model.Identifier;

import java.util.List;
import java.util.Optional;

public interface IdentifierRepository {

    List<Identifier> findByEmployeeIdOrderByIdentifierTypeCode(Long employeeId);

    Optional<Identifier> findByEmployeeIdAndIdentifierTypeCode(Long employeeId, String identifierTypeCode);

    boolean existsByEmployeeIdAndIsPrimaryTrue(Long employeeId);

    boolean existsByEmployeeIdAndIsPrimaryTrueAndIdentifierTypeCodeNot(Long employeeId, String identifierTypeCode);

    Identifier save(Identifier identifier);

    void deleteByEmployeeIdAndIdentifierTypeCode(Long employeeId, String identifierTypeCode);
}
