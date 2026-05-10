package com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SpringDataEmployeeNumberingConfigRepository
        extends JpaRepository<EmployeeNumberingConfigEntity, Long> {

    Optional<EmployeeNumberingConfigEntity> findByRuleSystemCode(String ruleSystemCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmployeeNumberingConfigEntity e WHERE e.ruleSystemCode = :code")
    Optional<EmployeeNumberingConfigEntity> findByRuleSystemCodeForUpdate(@Param("code") String code);
}
